package com.aquariux.trading.pricing;

import com.aquariux.trading.domain.TradingPair;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ExchangePrice(String source, TradingPair pair, BigDecimal bidPrice, BigDecimal askPrice) {}
