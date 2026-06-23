package com.aquariux.trading.api;

import com.aquariux.trading.domain.Currency;
import com.aquariux.trading.domain.WalletBalance;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class WalletBalanceResponse {

    Currency currency;
    BigDecimal balance;

    static WalletBalanceResponse from(WalletBalance wallet) {
        return WalletBalanceResponse.builder()
                .currency(wallet.getCurrency())
                .balance(wallet.getBalance())
                .build();
    }
}
