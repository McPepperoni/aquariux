package com.aquariux.trading.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;

class PricingConfigurationTest {

    @Test
    void feignClientsAreInterfacesWithYamlConfiguredBaseUrlsAndExpectedPaths() throws NoSuchMethodException {
        assertFeignClient(BinancePriceClient.class, "binancePriceClient", "getBookTickers",
                "/api/v3/ticker/bookTicker", "https://api.binance.com");
        assertFeignClient(HuobiPriceClient.class, "huobiPriceClient", "getTickers",
                "/market/tickers", "https://api.huobi.pro");
    }

    @Test
    void schedulerRunsEveryTenSecondsAndCanBeDisabledInTests() throws NoSuchMethodException {
        Method method = PriceAggregationScheduler.class.getDeclaredMethod("refreshPrices");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        assertThat(scheduled.fixedRate()).isEqualTo(10_000);
        assertThat(scheduled.initialDelay()).isEqualTo(10_000);

        ConditionalOnProperty condition = PriceAggregationScheduler.class.getAnnotation(ConditionalOnProperty.class);
        assertThat(condition.prefix()).isEqualTo("app.pricing");
        assertThat(condition.name()).containsExactly("scheduling-enabled");
        assertThat(condition.havingValue()).isEqualTo("true");
        assertThat(condition.matchIfMissing()).isTrue();
    }

    private static void assertFeignClient(
            Class<?> clientType,
            String clientName,
            String methodName,
            String expectedPath,
            String expectedBaseUrl) throws NoSuchMethodException {
        assertThat(clientType.isInterface()).isTrue();
        FeignClient feignClient = clientType.getAnnotation(FeignClient.class);
        assertThat(feignClient.name()).isEqualTo(clientName);
        assertThat(feignClient.url()).isBlank();

        GetMapping mapping = clientType.getDeclaredMethod(methodName).getAnnotation(GetMapping.class);
        assertThat(mapping.value()).containsExactly(expectedPath);

        assertThat(applicationYaml().getProperty("spring.cloud.openfeign.client.config."
                + clientName + ".url")).isEqualTo(expectedBaseUrl);
    }

    private static Properties applicationYaml() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yaml"));
        return yaml.getObject();
    }
}
