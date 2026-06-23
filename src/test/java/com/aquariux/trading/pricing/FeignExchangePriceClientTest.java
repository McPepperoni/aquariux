package com.aquariux.trading.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import com.aquariux.trading.domain.TradingPair;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class FeignExchangePriceClientTest {

    @Test
    void parsesOnlySupportedPairsFromBinanceAndHuobiPayloads() {
        BinancePriceClient binanceClient = () -> List.of(
                new BinancePriceClient.Ticker("BTCUSDT", new BigDecimal("30000.10"), new BigDecimal("30002.10")),
                new BinancePriceClient.Ticker("ETHUSDT", new BigDecimal("2000.40"), new BigDecimal("2001.40")),
                new BinancePriceClient.Ticker("XRPUSDT", new BigDecimal("0.50"), new BigDecimal("0.51")));
        HuobiPriceClient huobiClient = () -> new HuobiPriceClient.Response(List.of(
                new HuobiPriceClient.Ticker("btcusdt", new BigDecimal("30001.20"), new BigDecimal("30003.20")),
                new HuobiPriceClient.Ticker("ethusdt", new BigDecimal("1999.90"), new BigDecimal("2000.90")),
                new HuobiPriceClient.Ticker("xrpusdt", new BigDecimal("0.49"), new BigDecimal("0.52"))));
        FeignExchangePriceClient client = new FeignExchangePriceClient(binanceClient, huobiClient);

        List<ExchangePrice> prices = client.fetchPrices();

        assertThat(prices).hasSize(4);
        assertPrice(prices, "BINANCE", TradingPair.BTCUSDT, "30000.10", "30002.10");
        assertPrice(prices, "BINANCE", TradingPair.ETHUSDT, "2000.40", "2001.40");
        assertPrice(prices, "HUOBI", TradingPair.BTCUSDT, "30001.20", "30003.20");
        assertPrice(prices, "HUOBI", TradingPair.ETHUSDT, "1999.90", "2000.90");
    }

    private static void assertPrice(
            List<ExchangePrice> prices,
            String source,
            TradingPair pair,
            String expectedBid,
            String expectedAsk) {
        ExchangePrice price = prices.stream()
                .filter(candidate -> candidate.source().equals(source) && candidate.pair() == pair)
                .findFirst()
                .orElseThrow();

        assertThat(price.bidPrice()).isEqualByComparingTo(new BigDecimal(expectedBid));
        assertThat(price.askPrice()).isEqualByComparingTo(new BigDecimal(expectedAsk));
    }
}
