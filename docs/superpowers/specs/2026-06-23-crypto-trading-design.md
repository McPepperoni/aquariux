# Crypto Trading System Design

## Context

This repository started as a take-home skeleton with only `README.md` and an existing `initial commit`. The app will be built directly in the current repo with Maven, Java 17, Spring Boot 3.5.x, Spring Web, Spring Data JPA, Bean Validation, and in-memory H2.

Spring Boot documentation was checked through Context7. The design uses standard `@RestController`, Spring Data JPA repositories, H2 datasource properties, and `@EnableScheduling` with `@Scheduled`.

## Approach Options

1. Recommended: Spring MVC + Spring Data JPA + H2. This is the smallest idiomatic Spring Boot implementation for REST APIs, transactions, and simple tables.
2. Plain JDBC repositories. Fewer abstractions, but more mapping code and less useful for a Spring Boot take-home.
3. Ledger-style wallet accounting. More finance-grade, but overbuilt for a single-user assignment with simple balance APIs.

The implementation will use option 1 and keep wallet balances as current balance rows, with trade rows as the audit trail.

## Assumptions

- Authentication and authorization are out of scope. All APIs operate on one seeded demo user.
- The demo user starts with `50000.00000000` USDT, `0` BTC, and `0` ETH.
- Supported pairs are exactly `BTCUSDT` and `ETHUSDT`.
- Trade quantity is in the base asset: BTC for `BTCUSDT`, ETH for `ETHUSDT`.
- BUY uses the latest aggregated ask price. SELL uses the latest aggregated bid price.
- Exchange calls are only for price aggregation. Trade execution does not call any third party.

## Data Model

`app_users`
- `id` primary key
- `username` unique

`wallet_balances`
- `id` primary key
- `user_id` foreign key to `app_users`
- `currency` one of `USDT`, `BTC`, `ETH`
- `balance decimal(38,18)`
- unique `(user_id, currency)`

`aggregated_prices`
- `id` primary key
- `pair` one of `BTCUSDT`, `ETHUSDT`
- `bid_price decimal(38,18)`
- `bid_source` exchange name for the best bid
- `ask_price decimal(38,18)`
- `ask_source` exchange name for the best ask
- `fetched_at` timestamp from the aggregation run

`trade_transactions`
- `id` primary key
- `user_id` foreign key to `app_users`
- `pair`
- `side` as `BUY` or `SELL`
- `quantity decimal(38,18)`
- `price decimal(38,18)`
- `quote_amount decimal(38,18)`
- `base_currency`
- `quote_currency`
- `created_at`

## Price Aggregation

A scheduled job runs every 10 seconds. It fetches Binance `bookTicker` and Huobi `market/tickers`, extracts the two supported pairs, and stores one row per pair with:

- best bid: highest available bid, used for SELL orders
- best ask: lowest available ask, used for BUY orders

If one source fails or lacks a pair, the other source can still produce a price. If neither source has a pair, the scheduler skips that pair for that run.

## APIs

`GET /api/prices/latest`
- Returns the newest aggregated price for each supported pair.

`POST /api/trades`
- Request: `{"pair":"BTCUSDT","side":"BUY","quantity":0.1}`
- Validates supported pair, positive quantity, and available latest price.
- BUY debits USDT by `quantity * askPrice`, credits base currency, and records a trade at the ask price.
- SELL debits base currency, credits USDT by `quantity * bidPrice`, and records a trade at the bid price.
- Fails with `400` for unsupported pairs, missing prices, invalid quantity, or insufficient balance.

`GET /api/wallets`
- Returns demo user balances for USDT, BTC, and ETH.

`GET /api/trades`
- Returns demo user trade history newest first.

## Testing

Tests will cover:

- best-price selection across Binance and Huobi style payloads
- BUY and SELL balance movements
- insufficient balance rejection
- REST endpoints for latest prices, wallet balances, trade execution, and trade history
- application context startup with H2

Network calls are mocked in tests. The local build must not depend on live exchange availability.

## Out of Scope

- Multi-user authentication
- Order books or partial fills
- External trade execution
- Deposits and withdrawals
- Pagination, filtering, and admin APIs
