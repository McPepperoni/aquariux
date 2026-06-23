package com.aquariux.trading.pricing;

import com.aquariux.trading.domain.TradingPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
        List<BinancePriceClient.Ticker> tickers = binancePriceClient.getBookTickers();
        if (tickers == null) {
            return List.of();
        }

        List<ExchangePrice> prices = new ArrayList<>();
        for (BinancePriceClient.Ticker ticker : tickers) {
            supportedPair(ticker.symbol()).ifPresent(pair -> prices.add(ExchangePrice.builder()
                    .source(BINANCE)
                    .pair(pair)
                    .bidPrice(ticker.bidPrice())
                    .askPrice(ticker.askPrice())
                    .build()));
        }
        return prices;
    }

    private List<ExchangePrice> fetchHuobi() {
        HuobiPriceClient.Response response = huobiPriceClient.getTickers();
        if (response == null || response.data() == null) {
            return List.of();
        }

        List<ExchangePrice> prices = new ArrayList<>();
        for (HuobiPriceClient.Ticker ticker : response.data()) {
            supportedPair(ticker.symbol()).ifPresent(pair -> prices.add(ExchangePrice.builder()
                    .source(HUOBI)
                    .pair(pair)
                    .bidPrice(ticker.bid())
                    .askPrice(ticker.ask())
                    .build()));
        }
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
}
