package com.aquariux.trading.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aquariux.trading.domain.AggregatedPrice;
import com.aquariux.trading.domain.TradingPair;
import com.aquariux.trading.pricing.ExchangePriceClient;
import com.aquariux.trading.repository.AggregatedPriceRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
@AutoConfigureMockMvc
@Transactional
class TradingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AggregatedPriceRepository aggregatedPriceRepository;

    @MockitoBean
    private ExchangePriceClient exchangePriceClient;

    @AfterEach
    void exchangeClientWasNotUsed() {
        verifyNoInteractions(exchangePriceClient);
    }

    @Test
    void latestPricesReturnsNewestStoredPriceForEachSupportedPair() throws Exception {
        savePrice(TradingPair.BTCUSDT, "29900.00", "29910.00", Instant.parse("2026-06-23T09:00:00Z"));
        savePrice(TradingPair.BTCUSDT, "30000.00", "30010.00", Instant.parse("2026-06-23T10:00:00Z"));
        savePrice(TradingPair.ETHUSDT, "2000.00", "2001.00", Instant.parse("2026-06-23T10:01:00Z"));

        mockMvc.perform(get("/api/prices/latest"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].pair").value("BTCUSDT"))
                .andExpect(jsonPath("$[0].bidPrice").value(30000.00))
                .andExpect(jsonPath("$[0].bidSource").value("BINANCE"))
                .andExpect(jsonPath("$[0].askPrice").value(30010.00))
                .andExpect(jsonPath("$[0].askSource").value("HUOBI"))
                .andExpect(jsonPath("$[0].fetchedAt").value("2026-06-23T10:00:00Z"))
                .andExpect(jsonPath("$[1].pair").value("ETHUSDT"))
                .andExpect(jsonPath("$[1].bidPrice").value(2000.00))
                .andExpect(jsonPath("$[1].askPrice").value(2001.00));
    }

    @Test
    void tradeEndpointExecutesTradeAndReturnsTradeResponse() throws Exception {
        savePrice(TradingPair.BTCUSDT, "29999.00", "30000.00", Instant.parse("2026-06-23T10:00:00Z"));

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pair": "BTCUSDT",
                                  "side": "BUY",
                                  "quantity": 0.5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.pair").value("BTCUSDT"))
                .andExpect(jsonPath("$.side").value("BUY"))
                .andExpect(jsonPath("$.quantity").value(0.5))
                .andExpect(jsonPath("$.price").value(30000.00))
                .andExpect(jsonPath("$.quoteAmount").value(15000.00))
                .andExpect(jsonPath("$.baseCurrency").value("BTC"))
                .andExpect(jsonPath("$.quoteCurrency").value("USDT"))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void walletsReturnsDemoBalancesForSupportedCurrencies() throws Exception {
        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].currency", contains("USDT", "BTC", "ETH")))
                .andExpect(jsonPath("$[0].balance").value(50000.00))
                .andExpect(jsonPath("$[1].balance").value(0.00))
                .andExpect(jsonPath("$[2].balance").value(0.00));
    }

    @Test
    void tradesReturnsDemoTradeHistoryNewestFirst() throws Exception {
        savePrice(TradingPair.BTCUSDT, "29999.00", "30000.00", Instant.parse("2026-06-23T10:00:00Z"));
        executeTrade("BTCUSDT", "BUY", "0.2");
        executeTrade("BTCUSDT", "BUY", "0.3");

        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].pair").value("BTCUSDT"))
                .andExpect(jsonPath("$[0].quantity").value(0.3))
                .andExpect(jsonPath("$[1].pair").value("BTCUSDT"))
                .andExpect(jsonPath("$[1].quantity").value(0.2));
    }

    @Test
    void validationErrorsReturnUsefulBadRequestJson() throws Exception {
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pair": "",
                                  "side": null,
                                  "quantity": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.path").value("/api/trades"));
    }

    private void executeTrade(String pair, String side, String quantity) throws Exception {
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pair": "%s",
                                  "side": "%s",
                                  "quantity": %s
                                }
                                """.formatted(pair, side, quantity)))
                .andExpect(status().isOk());
    }

    private void savePrice(TradingPair pair, String bid, String ask, Instant fetchedAt) {
        aggregatedPriceRepository.save(AggregatedPrice.builder()
                .pair(pair)
                .bidPrice(new BigDecimal(bid))
                .bidSource("BINANCE")
                .askPrice(new BigDecimal(ask))
                .askSource("HUOBI")
                .fetchedAt(fetchedAt)
                .build());
    }
}
