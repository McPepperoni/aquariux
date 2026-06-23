package com.aquariux.trading.trade;

import com.aquariux.trading.domain.TradeSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TradeRequest {

    @NotBlank
    String pair;

    @NotNull
    TradeSide side;

    @NotNull
    @Positive
    BigDecimal quantity;
}
