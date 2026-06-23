package com.aquariux.trading.pricing;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PriceAggregationScheduler {

    private final PriceAggregationService priceAggregationService;

    @Scheduled(
            fixedRateString = "${app.price-refresh-ms:10000}",
            initialDelayString = "${app.price-refresh-ms:10000}")
    void refreshPrices() {
        priceAggregationService.aggregatePrices();
    }
}
