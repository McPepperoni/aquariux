# Crypto Trading System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Spring Boot H2 crypto trading take-home assignment with price aggregation, trading, wallet, and history APIs.

**Architecture:** A small Spring Boot MVC app owns persistence through Spring Data JPA, with Flyway SQL migrations as the database source of truth. A scheduled aggregation service fetches two exchanges and stores latest best bid/ask rows. A transactional trade service updates seeded wallet balances and records trade history.

**Tech Stack:** Java 17, Maven, Spring Boot 3.5.x, Spring Web, Spring Data JPA, Flyway, Lombok, H2, Bean Validation, JUnit 5, MockMvc.

---

## Files

- Create `pom.xml`: Maven project and dependencies.
- Create `.gitignore`: build outputs and local IDE files.
- Create `src/main/java/com/aquariux/trading/CryptoTradingApplication.java`: Spring Boot entry point and scheduling.
- Create `src/main/java/com/aquariux/trading/domain/*.java`: JPA entities and enums.
- Create `src/main/java/com/aquariux/trading/repository/*.java`: Spring Data repositories.
- Create `src/main/resources/db/migration/*.sql`: Flyway schema and seed migrations.
- Create `src/main/java/com/aquariux/trading/pricing/*.java`: exchange client, scheduler, and aggregation logic.
- Create `src/main/java/com/aquariux/trading/trade/*.java`: trade service and DTOs.
- Create `src/main/java/com/aquariux/trading/api/*.java`: REST controllers and exception handler.
- Create `src/main/resources/application.yaml`: H2, Flyway, and JPA validation config.
- Create `src/test/java/com/aquariux/trading/**/*.java`: focused unit and integration tests.
- Update `README.md`: setup, endpoints, and test instructions.

## Task 1: Scaffold Project

- [ ] Write failing context test in `src/test/java/com/aquariux/trading/CryptoTradingApplicationTests.java`:

```java
package com.aquariux.trading;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CryptoTradingApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

- [ ] Run `mvn test`; expected failure before `pom.xml` and app classes exist.
- [ ] Add Maven Spring Boot project files, Lombok, Flyway, YAML config, and application entry point.
- [ ] Run `mvn test`; expected pass for context startup.
- [ ] Commit: `chore: scaffold spring boot project`.

## Task 2: Persistence And Seed Data

- [ ] Write failing data test proving demo user has USDT/BTC/ETH balances after startup.
- [ ] Add entities: `UserAccount`, `WalletBalance`, `AggregatedPrice`, `TradeTransaction`.
- [ ] Add repositories for users, wallets, prices, and trades.
- [ ] Add Flyway migrations to create schema and seed user `demo` with `50000` USDT and zero base assets.
- [ ] Use Lombok annotations for simple getters/setters/constructors while preserving JPA no-arg constructors.
- [ ] Run `mvn test`; expected data test pass.
- [ ] Commit: `feat: add trading persistence model`.

## Task 3: Price Aggregation

- [ ] Write failing unit tests for best bid and ask selection from Binance and Huobi payloads.
- [ ] Implement supported pairs, exchange price DTOs, `ExchangePriceClient`, and `PriceAggregationService`.
- [ ] Add scheduler method with a fixed 10 second interval.
- [ ] Run `mvn test`; expected aggregation tests pass.
- [ ] Commit: `feat: aggregate crypto prices`.

## Task 4: Trading Logic

- [ ] Write failing service tests:
  - BUY BTC debits USDT using ask price and credits BTC.
  - SELL BTC debits BTC using bid price and credits USDT.
  - insufficient USDT or base asset throws `BadRequestException`.
- [ ] Implement `TradeService`, request/response DTOs, and balance updates inside one transaction.
- [ ] Run `mvn test`; expected trade service tests pass.
- [ ] Commit: `feat: execute trades against latest price`.

## Task 5: REST APIs

- [ ] Write failing MockMvc tests for:
  - `GET /api/prices/latest`
  - `POST /api/trades`
  - `GET /api/wallets`
  - `GET /api/trades`
- [ ] Implement controllers and a JSON error handler.
- [ ] Run `mvn test`; expected API tests pass.
- [ ] Commit: `feat: expose trading apis`.

## Task 6: Documentation And Final Checks

- [ ] Update `README.md` with run command, H2 console info, and curl examples.
- [ ] Run `mvn test`.
- [ ] Run `mvn spring-boot:run` briefly or `mvn package` to verify packaging.
- [ ] Request review, fix Critical and Important findings.
- [ ] Commit: `docs: document crypto trading api`.
- [ ] Create final commit: `chore: end commit`.
- [ ] Push commits to `origin main`.

## Review Checklist

- `initial commit` exists before implementation.
- Scheduler interval defaults to 10 seconds.
- Binance and Huobi are the only price sources.
- Latest price endpoint reads stored aggregated prices.
- Trade endpoint never calls exchanges.
- BUY uses ask and SELL uses bid.
- Wallet endpoint exposes USDT, BTC, and ETH balances.
- Trade history endpoint returns persisted trades.
- H2 is in-memory.
- Flyway owns schema and seed data; Hibernate validates mappings.
- Config file is `application.yaml`, not `application.properties`.
- Lombok reduces boilerplate on entities and DTOs.
- Tests mock network access.
