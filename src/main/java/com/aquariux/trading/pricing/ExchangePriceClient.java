package com.aquariux.trading.pricing;

import java.util.List;

public interface ExchangePriceClient {

    List<ExchangePrice> fetchPrices();
}
