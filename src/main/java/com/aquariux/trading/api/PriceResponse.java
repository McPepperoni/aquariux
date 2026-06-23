package com.aquariux.trading.api;

import com.aquariux.trading.domain.AggregatedPrice;
import com.aquariux.trading.domain.TradingPair;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class PriceResponse {

    TradingPair pair;
    BigDecimal bidPrice;
    String bidSource;
    BigDecimal askPrice;
    String askSource;
    Instant fetchedAt;

    static PriceResponse from(AggregatedPrice price) {
        return PriceResponse.builder()
                .pair(price.getPair())
                .bidPrice(price.getBidPrice())
                .bidSource(price.getBidSource())
                .askPrice(price.getAskPrice())
                .askSource(price.getAskSource())
                .fetchedAt(price.getFetchedAt())
                .build();
    }
}
