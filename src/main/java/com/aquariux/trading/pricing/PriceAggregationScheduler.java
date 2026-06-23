package com.aquariux.trading.pricing;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PriceAggregationScheduler {

    private final PriceAggregationService priceAggregationService;

    @Scheduled(fixedRate = 10_000, initialDelay = 10_000)
    void refreshPrices() {
        priceAggregationService.aggregatePrices();
    }
}
