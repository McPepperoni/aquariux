package com.aquariux.trading.pricing;

import com.aquariux.trading.domain.AggregatedPrice;
import com.aquariux.trading.domain.TradingPair;
import com.aquariux.trading.repository.AggregatedPriceRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAggregationService {

    private static final List<TradingPair> SUPPORTED_PAIRS = List.of(TradingPair.BTCUSDT, TradingPair.ETHUSDT);

    private final ExchangePriceClient exchangePriceClient;
    private final AggregatedPriceRepository aggregatedPriceRepository;

    @Transactional
    public void aggregatePrices() {
        List<ExchangePrice> prices = exchangePriceClient.fetchPrices();
        Instant fetchedAt = Instant.now();

        for (TradingPair pair : SUPPORTED_PAIRS) {
            List<ExchangePrice> pairPrices = prices.stream()
                    .filter(price -> price.pair() == pair)
                    .toList();
            if (pairPrices.isEmpty()) {
                log.warn("No exchange prices available for pair={}", pair);
                continue;
            }

            ExchangePrice bestBid = pairPrices.stream()
                    .max(Comparator.comparing(ExchangePrice::bidPrice))
                    .orElseThrow();
            ExchangePrice bestAsk = pairPrices.stream()
                    .min(Comparator.comparing(ExchangePrice::askPrice))
                    .orElseThrow();

            aggregatedPriceRepository.save(AggregatedPrice.builder()
                    .pair(pair)
                    .bidPrice(bestBid.bidPrice())
                    .bidSource(bestBid.source())
                    .askPrice(bestAsk.askPrice())
                    .askSource(bestAsk.source())
                    .fetchedAt(fetchedAt)
                    .build());
            log.info(
                    "Aggregated price persisted pair={} bidPrice={} bidSource={} askPrice={} askSource={} fetchedAt={}",
                    pair,
                    bestBid.bidPrice(),
                    bestBid.source(),
                    bestAsk.askPrice(),
                    bestAsk.source(),
                    fetchedAt);
        }
    }
}
