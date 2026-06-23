package com.aquariux.trading;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.pricing.scheduling-enabled=false")
class CryptoTradingApplicationTests {

    @Test
    void contextLoads() {
    }
}
