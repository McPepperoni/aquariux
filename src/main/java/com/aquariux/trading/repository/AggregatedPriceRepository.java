package com.aquariux.trading.repository;

import com.aquariux.trading.domain.AggregatedPrice;
import com.aquariux.trading.domain.TradingPair;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AggregatedPriceRepository extends JpaRepository<AggregatedPrice, Long> {

    Optional<AggregatedPrice> findTopByPairOrderByFetchedAtDesc(TradingPair pair);

    List<AggregatedPrice> findByPairOrderByFetchedAtDesc(TradingPair pair);
}
