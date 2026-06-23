package com.aquariux.trading.repository;

import com.aquariux.trading.domain.TradeTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

    List<TradeTransaction> findByUserUsernameOrderByCreatedAtDesc(String username);
}
