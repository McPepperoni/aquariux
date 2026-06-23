package com.aquariux.trading.pricing;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "binancePriceClient")
interface BinancePriceClient {

    @GetMapping("/api/v3/ticker/bookTicker")
    List<Ticker> getBookTickers();

    record Ticker(String symbol, BigDecimal bidPrice, BigDecimal askPrice) {}
}
