package com.careeranchor.server.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionConfigurationTest {
    @Test
    void productionRequiresEverySecretAndExternalServiceCredential() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/application-prod.yml")) {
            assertThat(stream).isNotNull();
            String yaml = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(yaml)
                    .contains("${DB_URL:}", "${DB_USERNAME:}", "${DB_PASSWORD:}")
                    .contains("${JWT_SECRET:}", "${WECHAT_APP_ID:}", "${WECHAT_APP_SECRET:}")
                    .contains("lazy-initialization: true")
                    .doesNotContain("career_anchor_dev", "dev-only-change-this-secret");
        }
    }
}
