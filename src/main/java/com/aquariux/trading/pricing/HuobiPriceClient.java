package com.aquariux.trading.pricing;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "huobiPriceClient", url = "${app.exchanges.huobi-url:https://api.huobi.pro}")
interface HuobiPriceClient {

    @GetMapping("/market/tickers")
    Response getTickers();

    record Response(List<Ticker> data) {}

    record Ticker(String symbol, BigDecimal bid, BigDecimal ask) {}
}
