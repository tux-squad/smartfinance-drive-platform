package com.smartfinance.smartfinancedriveplatform.profiles.infrastructure.tokens.reniec;

import com.smartfinance.smartfinancedriveplatform.profiles.application.outboundservices.ReniecDniVerifierService;
import com.smartfinance.smartfinancedriveplatform.profiles.domain.model.valueobjects.ReniecDniInfo;
import com.smartfinance.smartfinancedriveplatform.shared.infrastructure.cache.CacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("ReniecDniCache Spring Context & Caching Resolution Tests")
class ReniecDniCacheContextTest {

    @Configuration
    static class TestConfig {
        @Bean
        public RestClient restClient() {
            return mock(RestClient.class);
        }

        @Bean
        public ReniecDniVerifierService reniecDniVerifierService(RestClient restClient) {
            return new FactilizaDniVerifierServiceImpl(restClient);
        }
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CacheConfig.class, TestConfig.class);

    @Test
    @DisplayName("Should resolve reniecDniCache in Spring context without IllegalArgumentException")
    void shouldResolveReniecDniCacheCleanly() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            CacheManager cacheManager = context.getBean(CacheManager.class);
            assertThat(cacheManager).isNotNull();

            Cache reniecCache = cacheManager.getCache(CacheConfig.RENIEC_DNI_CACHE);
            assertThat(reniecCache).isNotNull();

            Cache sunatCache = cacheManager.getCache(CacheConfig.SUNAT_RUC_CACHE);
            assertThat(sunatCache).isNotNull();

            // Calling through Spring proxy to guarantee cache resolution succeeds without throwing IllegalArgumentException
            ReniecDniVerifierService service = context.getBean(ReniecDniVerifierService.class);
            Optional<ReniecDniInfo> result = service.verifyDni("invalid");
            assertThat(result).isEmpty();
        });
    }
}
