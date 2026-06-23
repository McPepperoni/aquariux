package com.aquariux.trading.pricing;

import com.aquariux.trading.domain.TradingPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
class FeignExchangePriceClient implements ExchangePriceClient {

    private static final String BINANCE = "BINANCE";
    private static final String HUOBI = "HUOBI";

    private final BinancePriceClient binancePriceClient;
    private final HuobiPriceClient huobiPriceClient;

    @Override
    public List<ExchangePrice> fetchPrices() {
        List<ExchangePrice> prices = new ArrayList<>();
        prices.addAll(fetchBinance());
        prices.addAll(fetchHuobi());
        return prices;
    }

    private List<ExchangePrice> fetchBinance() {
        List<BinancePriceClient.Ticker> tickers;
        try {
            tickers = binancePriceClient.getBookTickers();
        } catch (RuntimeException exception) {
            log.warn("Failed to fetch {} prices: {}", BINANCE, exception.toString());
            return List.of();
        }
        if (tickers == null) {
            log.warn("Failed to fetch {} prices: response was null", BINANCE);
            return List.of();
        }

        List<ExchangePrice> prices = new ArrayList<>();
        for (BinancePriceClient.Ticker ticker : tickers) {
            if (!hasPrice(ticker.bidPrice(), ticker.askPrice())) {
                continue;
            }
            supportedPair(ticker.symbol()).ifPresent(pair -> prices.add(ExchangePrice.builder()
                    .source(BINANCE)
                    .pair(pair)
                    .bidPrice(ticker.bidPrice())
                    .askPrice(ticker.askPrice())
                    .build()));
        }
        log.debug("Fetched {} supported {} prices", prices.size(), BINANCE);
        return prices;
    }

    private List<ExchangePrice> fetchHuobi() {
        HuobiPriceClient.Response response;
        try {
            response = huobiPriceClient.getTickers();
        } catch (RuntimeException exception) {
            log.warn("Failed to fetch {} prices: {}", HUOBI, exception.toString());
            return List.of();
        }
        if (response == null || response.data() == null) {
            log.warn("Failed to fetch {} prices: response was null", HUOBI);
            return List.of();
        }

        List<ExchangePrice> prices = new ArrayList<>();
        for (HuobiPriceClient.Ticker ticker : response.data()) {
            if (!hasPrice(ticker.bid(), ticker.ask())) {
                continue;
            }
            supportedPair(ticker.symbol()).ifPresent(pair -> prices.add(ExchangePrice.builder()
                    .source(HUOBI)
                    .pair(pair)
                    .bidPrice(ticker.bid())
                    .askPrice(ticker.ask())
                    .build()));
        }
        log.debug("Fetched {} supported {} prices", prices.size(), HUOBI);
        return prices;
    }

    private static Optional<TradingPair> supportedPair(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }

        return switch (symbol.toUpperCase(Locale.ROOT)) {
            case "BTCUSDT" -> Optional.of(TradingPair.BTCUSDT);
            case "ETHUSDT" -> Optional.of(TradingPair.ETHUSDT);
            default -> Optional.empty();
        };
    }

    private static boolean hasPrice(Object bid, Object ask) {
        return bid != null && ask != null;
    }
}
