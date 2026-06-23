package com.aquariux.trading.repository;

import com.aquariux.trading.domain.Currency;
import com.aquariux.trading.domain.UserAccount;
import com.aquariux.trading.domain.WalletBalance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletBalanceRepository extends JpaRepository<WalletBalance, Long> {

    Optional<WalletBalance> findByUserAndCurrency(UserAccount user, Currency currency);

    List<WalletBalance> findByUserUsernameOrderByCurrencyAsc(String username);
}
