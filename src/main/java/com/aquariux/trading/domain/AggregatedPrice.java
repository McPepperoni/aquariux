package com.aquariux.trading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "aggregated_prices")
public class AggregatedPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private TradingPair pair;

    @Column(name = "bid_price", nullable = false, precision = 38, scale = 18)
    @Setter
    private BigDecimal bidPrice;

    @Column(name = "bid_source", nullable = false, length = 64)
    @Setter
    private String bidSource;

    @Column(name = "ask_price", nullable = false, precision = 38, scale = 18)
    @Setter
    private BigDecimal askPrice;

    @Column(name = "ask_source", nullable = false, length = 64)
    @Setter
    private String askSource;

    @Column(name = "fetched_at", nullable = false)
    @Setter
    private Instant fetchedAt;

    @Builder
    public AggregatedPrice(
            TradingPair pair,
            BigDecimal bidPrice,
            String bidSource,
            BigDecimal askPrice,
            String askSource,
            Instant fetchedAt) {
        this.pair = pair;
        this.bidPrice = bidPrice;
        this.bidSource = bidSource;
        this.askPrice = askPrice;
        this.askSource = askSource;
        this.fetchedAt = fetchedAt;
    }

    @PrePersist
    void setFetchedAtIfMissing() {
        if (fetchedAt == null) {
            fetchedAt = Instant.now();
        }
    }
}
