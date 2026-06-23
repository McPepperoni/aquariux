package com.aquariux.trading.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.aquariux.trading.domain.TradingPair;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;

class HttpExchangePriceClientTest {

    @Test
    void parsesOnlySupportedPairsFromBinanceAndHuobiPayloads() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        HttpExchangePriceClient client = new HttpExchangePriceClient(
                restTemplate,
                "https://binance.test/api/v3/ticker/bookTicker",
                "https://huobi.test/market/tickers");

        server.expect(requestTo("https://binance.test/api/v3/ticker/bookTicker"))
                .andRespond(withSuccess(
                        """
                        [
                          {"symbol":"BTCUSDT","bidPrice":"30000.10","askPrice":"30002.10"},
                          {"symbol":"ETHUSDT","bidPrice":"2000.40","askPrice":"2001.40"},
                          {"symbol":"XRPUSDT","bidPrice":"0.50","askPrice":"0.51"}
                        ]
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://huobi.test/market/tickers"))
                .andRespond(withSuccess(
                        """
                        {
                          "status":"ok",
                          "data":[
                            {"symbol":"btcusdt","bid":30001.20,"ask":30003.20},
                            {"symbol":"ethusdt","bid":1999.90,"ask":2000.90},
                            {"symbol":"xrpusdt","bid":0.49,"ask":0.52}
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        List<ExchangePrice> prices = client.fetchPrices();

        server.verify();
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
