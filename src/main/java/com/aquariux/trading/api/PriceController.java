package com.aquariux.trading.api;

import com.aquariux.trading.domain.TradingPair;
import com.aquariux.trading.repository.AggregatedPriceRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
class PriceController {

    private final AggregatedPriceRepository aggregatedPriceRepository;

    @GetMapping("/latest")
    List<PriceResponse> latestPrices() {
        return Arrays.stream(TradingPair.values())
                .map(aggregatedPriceRepository::findTopByPairOrderByFetchedAtDesc)
                .flatMap(optionalPrice -> optionalPrice.map(PriceResponse::from).stream())
                .toList();
    }
}
