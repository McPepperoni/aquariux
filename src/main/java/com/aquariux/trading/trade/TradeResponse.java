package com.aquariux.trading.trade;

import com.aquariux.trading.domain.Currency;
import com.aquariux.trading.domain.TradeSide;
import com.aquariux.trading.domain.TradingPair;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TradeResponse {

    Long id;
    TradingPair pair;
    TradeSide side;
    BigDecimal quantity;
    BigDecimal price;
    BigDecimal quoteAmount;
    Currency baseCurrency;
    Currency quoteCurrency;
    Instant createdAt;
}
