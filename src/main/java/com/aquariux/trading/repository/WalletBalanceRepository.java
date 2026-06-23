package com.aquariux.trading.repository;

import com.aquariux.trading.domain.Currency;
import com.aquariux.trading.domain.UserAccount;
import com.aquariux.trading.domain.WalletBalance;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletBalanceRepository extends JpaRepository<WalletBalance, Long> {

    Optional<WalletBalance> findByUserAndCurrency(UserAccount user, Currency currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wallet from WalletBalance wallet where wallet.user = :user and wallet.currency = :currency")
    Optional<WalletBalance> findByUserAndCurrencyForUpdate(
            @Param("user") UserAccount user,
            @Param("currency") Currency currency);

    List<WalletBalance> findByUserUsernameOrderByCurrencyAsc(String username);
}
