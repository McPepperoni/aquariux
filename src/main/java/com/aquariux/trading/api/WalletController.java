package com.aquariux.trading.api;

import com.aquariux.trading.domain.Currency;
import com.aquariux.trading.domain.WalletBalance;
import com.aquariux.trading.repository.WalletBalanceRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
class WalletController {

    private static final String DEMO_USERNAME = "demo";

    private final WalletBalanceRepository walletBalanceRepository;

    @GetMapping
    List<WalletBalanceResponse> balances() {
        Map<Currency, WalletBalance> walletsByCurrency = walletBalanceRepository
                .findByUserUsernameOrderByCurrencyAsc(DEMO_USERNAME)
                .stream()
                .collect(Collectors.toMap(WalletBalance::getCurrency, Function.identity()));

        return Arrays.stream(Currency.values())
                .map(walletsByCurrency::get)
                .filter(wallet -> wallet != null)
                .map(WalletBalanceResponse::from)
                .toList();
    }
}
