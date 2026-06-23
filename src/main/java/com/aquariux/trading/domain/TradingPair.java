package com.aquariux.trading.domain;

public enum TradingPair {
    BTCUSDT(Currency.BTC, Currency.USDT),
    ETHUSDT(Currency.ETH, Currency.USDT);

    private final Currency baseCurrency;
    private final Currency quoteCurrency;

    TradingPair(Currency baseCurrency, Currency quoteCurrency) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Currency getQuoteCurrency() {
        return quoteCurrency;
    }
}
