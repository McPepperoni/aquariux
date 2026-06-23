package com.aquariux.trading.trade;

import com.aquariux.trading.api.BadRequestException;
import com.aquariux.trading.domain.AggregatedPrice;
import com.aquariux.trading.domain.Currency;
import com.aquariux.trading.domain.TradeSide;
import com.aquariux.trading.domain.TradeTransaction;
import com.aquariux.trading.domain.TradingPair;
import com.aquariux.trading.domain.UserAccount;
import com.aquariux.trading.domain.WalletBalance;
import com.aquariux.trading.repository.AggregatedPriceRepository;
import com.aquariux.trading.repository.TradeTransactionRepository;
import com.aquariux.trading.repository.UserAccountRepository;
import com.aquariux.trading.repository.WalletBalanceRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService {

    private static final String DEMO_USERNAME = "demo";

    private final UserAccountRepository userAccountRepository;
    private final WalletBalanceRepository walletBalanceRepository;
    private final AggregatedPriceRepository aggregatedPriceRepository;
    private final TradeTransactionRepository tradeTransactionRepository;

    @Transactional
    public TradeResponse execute(TradeRequest request) {
        if (request == null) {
            throw new BadRequestException("Trade request is required");
        }

        TradingPair pair = parsePair(request.getPair());
        TradeSide side = parseSide(request.getSide());
        BigDecimal quantity = parseQuantity(request.getQuantity());

        AggregatedPrice latestPrice = aggregatedPriceRepository.findTopByPairOrderByFetchedAtDesc(pair)
                .orElseThrow(() -> new BadRequestException("Latest price not found for " + pair));
        UserAccount user = userAccountRepository.findByUsername(DEMO_USERNAME)
                .orElseThrow(() -> new BadRequestException("Demo user not found"));

        Currency baseCurrency = pair.getBaseCurrency();
        Currency quoteCurrency = pair.getQuoteCurrency();
        WalletBalance baseWallet = wallet(user, baseCurrency);
        WalletBalance quoteWallet = wallet(user, quoteCurrency);

        BigDecimal price = side == TradeSide.BUY ? latestPrice.getAskPrice() : latestPrice.getBidPrice();
        BigDecimal quoteAmount = quantity.multiply(price);

        if (side == TradeSide.BUY) {
            requireBalance(quoteWallet, quoteAmount, quoteCurrency);
            quoteWallet.setBalance(quoteWallet.getBalance().subtract(quoteAmount));
            baseWallet.setBalance(baseWallet.getBalance().add(quantity));
        } else {
            requireBalance(baseWallet, quantity, baseCurrency);
            baseWallet.setBalance(baseWallet.getBalance().subtract(quantity));
            quoteWallet.setBalance(quoteWallet.getBalance().add(quoteAmount));
        }

        Instant createdAt = Instant.now();
        TradeTransaction trade = tradeTransactionRepository.save(TradeTransaction.builder()
                .user(user)
                .pair(pair)
                .side(side)
                .quantity(quantity)
                .price(price)
                .quoteAmount(quoteAmount)
                .baseCurrency(baseCurrency)
                .quoteCurrency(quoteCurrency)
                .createdAt(createdAt)
                .build());

        return TradeResponse.builder()
                .id(trade.getId())
                .pair(trade.getPair())
                .side(trade.getSide())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .quoteAmount(trade.getQuoteAmount())
                .baseCurrency(trade.getBaseCurrency())
                .quoteCurrency(trade.getQuoteCurrency())
                .createdAt(trade.getCreatedAt())
                .build();
    }

    private TradingPair parsePair(String pair) {
        if (pair == null || pair.isBlank()) {
            throw new BadRequestException("Unsupported pair");
        }
        try {
            return TradingPair.valueOf(pair.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported pair: " + pair);
        }
    }

    private TradeSide parseSide(TradeSide side) {
        if (side == null) {
            throw new BadRequestException("Side must be BUY or SELL");
        }
        return side;
    }

    private BigDecimal parseQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }
        return quantity;
    }

    private WalletBalance wallet(UserAccount user, Currency currency) {
        return walletBalanceRepository.findByUserAndCurrencyForUpdate(user, currency)
                .orElseThrow(() -> new BadRequestException("Wallet balance not found for " + currency));
    }

    private void requireBalance(WalletBalance wallet, BigDecimal amount, Currency currency) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient " + currency);
        }
    }
}
