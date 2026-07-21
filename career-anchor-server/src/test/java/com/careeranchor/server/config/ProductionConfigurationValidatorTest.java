package com.careeranchor.server.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionConfigurationValidatorTest {
    @Test
    void reportsEveryMissingProductionVariableTogether() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        ProductionConfigurationValidator validator = new ProductionConfigurationValidator(environment);

        assertThatThrownBy(validator::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContainingAll("DB_URL", "DB_USERNAME", "DB_PASSWORD", "JWT_SECRET",
                        "WECHAT_APP_ID", "WECHAT_APP_SECRET");
    }

    @Test
    void acceptsCompleteConfigurationAndRejectsWeakJwtSecret() {
        MockEnvironment environment = completeEnvironment().withProperty("app.jwt.secret", "too-short");
        ProductionConfigurationValidator weak = new ProductionConfigurationValidator(environment);
        assertThatThrownBy(weak::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");

        MockEnvironment secureEnvironment = completeEnvironment()
                .withProperty("app.jwt.secret", "0123456789abcdef0123456789abcdef");
        ProductionConfigurationValidator secure = new ProductionConfigurationValidator(secureEnvironment);
        assertThatCode(secure::afterPropertiesSet).doesNotThrowAnyException();
    }

    private MockEnvironment completeEnvironment() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:mysql://localhost/career_anchor")
                .withProperty("spring.datasource.username", "career_anchor")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("app.wechat.app-id", "wx-test-app")
                .withProperty("app.wechat.app-secret", "wechat-secret");
        environment.setActiveProfiles("prod");
        return environment;
    }
}
