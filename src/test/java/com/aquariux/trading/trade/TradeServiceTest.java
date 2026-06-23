package com.aquariux.trading.trade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.pricing.scheduling-enabled=false")
@Transactional
class TradeServiceTest {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private WalletBalanceRepository walletBalanceRepository;

    @Autowired
    private AggregatedPriceRepository aggregatedPriceRepository;

    @Autowired
    private TradeTransactionRepository tradeTransactionRepository;

    @Test
    void buyDebitsUsdtAtAskPriceAndCreditsBaseCurrency() {
        savePrice(TradingPair.BTCUSDT, "29999.00", "30000.00");

        TradeResponse response = tradeService.execute(request("BTCUSDT", TradeSide.BUY, "0.5"));

        assertThat(response.getPair()).isEqualTo(TradingPair.BTCUSDT);
        assertThat(response.getSide()).isEqualTo(TradeSide.BUY);
        assertThat(response.getQuantity()).isEqualByComparingTo("0.5");
        assertThat(response.getPrice()).isEqualByComparingTo("30000.00");
        assertThat(response.getQuoteAmount()).isEqualByComparingTo("15000.000");
        assertWallet(Currency.USDT, "35000.00000000");
        assertWallet(Currency.BTC, "0.5");

        List<TradeTransaction> trades = tradeTransactionRepository.findAll();
        assertThat(trades).hasSize(1);
        TradeTransaction trade = trades.get(0);
        assertThat(trade.getSide()).isEqualTo(TradeSide.BUY);
        assertThat(trade.getPrice()).isEqualByComparingTo("30000.00");
        assertThat(trade.getQuoteAmount()).isEqualByComparingTo("15000.000");
    }

    @Test
    void sellDebitsBaseCurrencyAtBidPriceAndCreditsUsdt() {
        savePrice(TradingPair.BTCUSDT, "31000.00", "31002.00");
        setWallet(Currency.BTC, "1.25");

        TradeResponse response = tradeService.execute(request("BTCUSDT", TradeSide.SELL, "0.4"));

        assertThat(response.getPair()).isEqualTo(TradingPair.BTCUSDT);
        assertThat(response.getSide()).isEqualTo(TradeSide.SELL);
        assertThat(response.getQuantity()).isEqualByComparingTo("0.4");
        assertThat(response.getPrice()).isEqualByComparingTo("31000.00");
        assertThat(response.getQuoteAmount()).isEqualByComparingTo("12400.000");
        assertWallet(Currency.BTC, "0.85");
        assertWallet(Currency.USDT, "62400.00000000");

        List<TradeTransaction> trades = tradeTransactionRepository.findAll();
        assertThat(trades).hasSize(1);
        TradeTransaction trade = trades.get(0);
        assertThat(trade.getSide()).isEqualTo(TradeSide.SELL);
        assertThat(trade.getPrice()).isEqualByComparingTo("31000.00");
        assertThat(trade.getQuoteAmount()).isEqualByComparingTo("12400.000");
    }

    @Test
    void rejectsBuyWhenUsdtBalanceIsInsufficient() {
        savePrice(TradingPair.BTCUSDT, "29999.00", "30000.00");

        assertThatThrownBy(() -> tradeService.execute(request("BTCUSDT", TradeSide.BUY, "2")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient USDT");

        assertWallet(Currency.USDT, "50000.00000000");
        assertWallet(Currency.BTC, "0");
        assertThat(tradeTransactionRepository.findAll()).isEmpty();
    }

    @Test
    void rejectsSellWhenBaseCurrencyBalanceIsInsufficient() {
        savePrice(TradingPair.BTCUSDT, "31000.00", "31002.00");

        assertThatThrownBy(() -> tradeService.execute(request("BTCUSDT", TradeSide.SELL, "0.1")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient BTC");

        assertWallet(Currency.USDT, "50000.00000000");
        assertWallet(Currency.BTC, "0");
        assertThat(tradeTransactionRepository.findAll()).isEmpty();
    }

    @Test
    void rejectsTradeWhenLatestPriceIsMissing() {
        assertThatThrownBy(() -> tradeService.execute(request("BTCUSDT", TradeSide.BUY, "0.1")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Latest price not found");

        assertThat(tradeTransactionRepository.findAll()).isEmpty();
    }

    @Test
    void rejectsUnsupportedPair() {
        assertThatThrownBy(() -> tradeService.execute(request("DOGEUSDT", TradeSide.BUY, "10")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unsupported pair");
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThatThrownBy(() -> tradeService.execute(request("BTCUSDT", TradeSide.BUY, "0")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    @Test
    void walletRowsAreLockedBeforeBalanceChanges() throws NoSuchMethodException {
        Method method = WalletBalanceRepository.class.getMethod(
                "findByUserAndCurrencyForUpdate", UserAccount.class, Currency.class);

        assertThat(method.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    private TradeRequest request(String pair, TradeSide side, String quantity) {
        return TradeRequest.builder()
                .pair(pair)
                .side(side)
                .quantity(new BigDecimal(quantity))
                .build();
    }

    private void savePrice(TradingPair pair, String bid, String ask) {
        aggregatedPriceRepository.save(AggregatedPrice.builder()
                .pair(pair)
                .bidPrice(new BigDecimal(bid))
                .bidSource("BINANCE")
                .askPrice(new BigDecimal(ask))
                .askSource("HUOBI")
                .fetchedAt(Instant.now())
                .build());
    }

    private void setWallet(Currency currency, String balance) {
        wallet(currency).setBalance(new BigDecimal(balance));
    }

    private void assertWallet(Currency currency, String expectedBalance) {
        assertThat(wallet(currency).getBalance()).isEqualByComparingTo(expectedBalance);
    }

    private WalletBalance wallet(Currency currency) {
        UserAccount demo = userAccountRepository.findByUsername("demo").orElseThrow();
        return walletBalanceRepository.findByUserAndCurrency(demo, currency).orElseThrow();
    }
}
