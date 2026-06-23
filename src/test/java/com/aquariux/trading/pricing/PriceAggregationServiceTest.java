package com.aquariux.trading.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aquariux.trading.domain.AggregatedPrice;
import com.aquariux.trading.domain.TradingPair;
import com.aquariux.trading.repository.AggregatedPriceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriceAggregationServiceTest {

    @Mock
    private ExchangePriceClient exchangePriceClient;

    @Mock
    private AggregatedPriceRepository aggregatedPriceRepository;

    @Captor
    private ArgumentCaptor<AggregatedPrice> priceCaptor;

    @Test
    void storesHighestBidAndLowestAskForEachSupportedPair() {
        when(exchangePriceClient.fetchPrices()).thenReturn(List.of(
                price("BINANCE", TradingPair.BTCUSDT, "30000.10", "30002.10"),
                price("HUOBI", TradingPair.BTCUSDT, "30001.20", "30003.20"),
                price("BINANCE", TradingPair.ETHUSDT, "2000.40", "2001.40"),
                price("HUOBI", TradingPair.ETHUSDT, "1999.90", "2000.90")));

        PriceAggregationService service = new PriceAggregationService(exchangePriceClient, aggregatedPriceRepository);

        service.aggregatePrices();

        verify(aggregatedPriceRepository, times(2)).save(priceCaptor.capture());
        Map<TradingPair, AggregatedPrice> saved = priceCaptor.getAllValues().stream()
                .collect(Collectors.toMap(AggregatedPrice::getPair, price -> price));

        assertThat(saved).containsOnlyKeys(TradingPair.BTCUSDT, TradingPair.ETHUSDT);
        assertAggregated(saved.get(TradingPair.BTCUSDT), "30001.20", "HUOBI", "30002.10", "BINANCE");
        assertAggregated(saved.get(TradingPair.ETHUSDT), "2000.40", "BINANCE", "2000.90", "HUOBI");
    }

    private static ExchangePrice price(String source, TradingPair pair, String bid, String ask) {
        return new ExchangePrice(source, pair, new BigDecimal(bid), new BigDecimal(ask));
    }

    private static void assertAggregated(
            AggregatedPrice price,
            String expectedBid,
            String expectedBidSource,
            String expectedAsk,
            String expectedAskSource) {
        assertThat(price.getBidPrice()).isEqualByComparingTo(new BigDecimal(expectedBid));
        assertThat(price.getBidSource()).isEqualTo(expectedBidSource);
        assertThat(price.getAskPrice()).isEqualByComparingTo(new BigDecimal(expectedAsk));
        assertThat(price.getAskSource()).isEqualTo(expectedAskSource);
        assertThat(price.getFetchedAt()).isNotNull();
    }
}
