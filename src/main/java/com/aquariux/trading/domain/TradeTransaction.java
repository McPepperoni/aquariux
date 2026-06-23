package com.aquariux.trading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "trade_transactions")
public class TradeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private TradingPair pair;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Setter
    private TradeSide side;

    @Column(nullable = false, precision = 38, scale = 18)
    @Setter
    private BigDecimal quantity;

    @Column(nullable = false, precision = 38, scale = 18)
    @Setter
    private BigDecimal price;

    @Column(name = "quote_amount", nullable = false, precision = 38, scale = 18)
    @Setter
    private BigDecimal quoteAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_currency", nullable = false, length = 10)
    @Setter
    private Currency baseCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "quote_currency", nullable = false, length = 10)
    @Setter
    private Currency quoteCurrency;

    @Column(name = "created_at", nullable = false)
    @Setter
    private Instant createdAt;

    @Builder
    public TradeTransaction(
            UserAccount user,
            TradingPair pair,
            TradeSide side,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal quoteAmount,
            Currency baseCurrency,
            Currency quoteCurrency,
            Instant createdAt) {
        this.user = user;
        this.pair = pair;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.quoteAmount = quoteAmount;
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.createdAt = createdAt;
    }

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
