package com.aquariux.trading.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class FlywaySeedDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywaySeedsDemoUserWithInitialWalletBalances() {
        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "select count(*) from \"flyway_schema_history\" where \"success\" = true",
                Integer.class);
        assertThat(appliedMigrations).isNotNull().isGreaterThan(0);

        Map<String, BigDecimal> balances = jdbcTemplate.query(
                """
                select wb.currency, wb.balance
                from wallet_balances wb
                join app_users au on au.id = wb.user_id
                where au.username = ?
                """,
                (rs, rowNum) -> Map.entry(rs.getString("currency"), rs.getBigDecimal("balance")),
                "demo")
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(balances).hasSize(3);
        assertThat(balances.get("USDT")).isEqualByComparingTo("50000.00000000");
        assertThat(balances.get("BTC")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(balances.get("ETH")).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void walletBalancesAreUniquePerUserAndCurrency() {
        Long demoUserId = jdbcTemplate.queryForObject(
                "select id from app_users where username = ?",
                Long.class,
                "demo");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jdbcTemplate.update(
                        "insert into wallet_balances (user_id, currency, balance) values (?, ?, ?)",
                        demoUserId,
                        "USDT",
                        BigDecimal.ONE))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
