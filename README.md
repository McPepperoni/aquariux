# Aquariux Crypto Trading Take Home

Spring Boot crypto trading API with in-memory H2, Flyway migrations, Lombok, Spring Data JPA, and interface-based OpenFeign clients for Binance and Huobi price aggregation.

## Requirements

- Java 17
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

H2 console:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:crypto_trading`
- User: `sa`
- Password: empty

## Test

```bash
mvn test
```

Tests disable the scheduler so they do not call live exchanges.

## Data

Flyway creates:

- `app_users`
- `wallet_balances`
- `aggregated_prices`
- `trade_transactions`

It seeds one demo user with:

- `50000` USDT
- `0` BTC
- `0` ETH

## Pricing

The scheduler runs every 10 seconds and stores the best aggregated prices for:

- `BTCUSDT`
- `ETHUSDT`

SELL uses the highest bid. BUY uses the lowest ask.

## APIs

Get latest prices:

```bash
curl http://localhost:8080/api/prices/latest
```

Execute a trade:

```bash
curl -X POST http://localhost:8080/api/trades \
  -H "Content-Type: application/json" \
  -d "{\"pair\":\"BTCUSDT\",\"side\":\"BUY\",\"quantity\":0.1}"
```

Get wallet balances:

```bash
curl http://localhost:8080/api/wallets
```

Get trade history:

```bash
curl http://localhost:8080/api/trades
```

OpenAPI JSON:

```bash
curl http://localhost:8080/v3/api-docs
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Notes

- Authentication is assumed and not implemented.
- Trades use stored aggregated prices only; they do not call third-party trading systems.
- Exchange base URLs are configured in `src/main/resources/application.yaml`.
