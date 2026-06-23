package com.aquariux.trading.pricing;

import com.aquariux.trading.domain.TradingPair;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
class HttpExchangePriceClient implements ExchangePriceClient {

    private static final String BINANCE = "BINANCE";
    private static final String HUOBI = "HUOBI";

    private final RestTemplate restTemplate;
    private final String binanceUrl;
    private final String huobiUrl;

    @Autowired
    HttpExchangePriceClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${app.exchanges.binance-url:https://api.binance.com/api/v3/ticker/bookTicker}") String binanceUrl,
            @Value("${app.exchanges.huobi-url:https://api.huobi.pro/market/tickers}") String huobiUrl) {
        this(restTemplateBuilder.build(), binanceUrl, huobiUrl);
    }

    HttpExchangePriceClient(RestTemplate restTemplate, String binanceUrl, String huobiUrl) {
        this.restTemplate = restTemplate;
        this.binanceUrl = binanceUrl;
        this.huobiUrl = huobiUrl;
    }

    @Override
    public List<ExchangePrice> fetchPrices() {
        List<ExchangePrice> prices = new ArrayList<>();
        prices.addAll(fetchBinance());
        prices.addAll(fetchHuobi());
        return prices;
    }

    private List<ExchangePrice> fetchBinance() {
        BinanceTicker[] tickers = restTemplate.getForObject(binanceUrl, BinanceTicker[].class);
        if (tickers == null) {
            return List.of();
        }

        List<ExchangePrice> prices = new ArrayList<>();
        for (BinanceTicker ticker : tickers) {
            supportedPair(ticker.symbol()).ifPresent(pair -> prices.add(
                    new ExchangePrice(BINANCE, pair, ticker.bidPrice(), ticker.askPrice())));
        }
        return prices;
    }

    private List<ExchangePrice> fetchHuobi() {
        HuobiResponse response = restTemplate.getForObject(huobiUrl, HuobiResponse.class);
        if (response == null || response.data() == null) {
            return List.of();
        }

        List<ExchangePrice> prices = new ArrayList<>();
        for (HuobiTicker ticker : response.data()) {
            supportedPair(ticker.symbol()).ifPresent(pair -> prices.add(
                    new ExchangePrice(HUOBI, pair, ticker.bid(), ticker.ask())));
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

    private record BinanceTicker(String symbol, BigDecimal bidPrice, BigDecimal askPrice) {}

    private record HuobiResponse(List<HuobiTicker> data) {}

    private record HuobiTicker(String symbol, BigDecimal bid, BigDecimal ask) {}
}
