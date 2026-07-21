package com.careeranchor.server.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductionConfigurationValidator implements InitializingBean {
    private static final List<RequiredProperty> REQUIRED = List.of(
            new RequiredProperty("DB_URL", "spring.datasource.url"),
            new RequiredProperty("DB_USERNAME", "spring.datasource.username"),
            new RequiredProperty("DB_PASSWORD", "spring.datasource.password"),
            new RequiredProperty("JWT_SECRET", "app.jwt.secret"),
            new RequiredProperty("WECHAT_APP_ID", "app.wechat.app-id"),
            new RequiredProperty("WECHAT_APP_SECRET", "app.wechat.app-secret")
    );

    private final Environment environment;

    public ProductionConfigurationValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        if (!environment.matchesProfiles("prod")) return;

        List<String> missing = new ArrayList<>();
        for (RequiredProperty required : REQUIRED) {
            String value = environment.getProperty(required.property());
            if (value == null || value.isBlank() || value.startsWith("${")) {
                missing.add(required.environmentVariable());
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required production environment variables: "
                    + String.join(", ", missing));
        }

        String jwtSecret = environment.getRequiredProperty("app.jwt.secret");
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        }
    }

    private record RequiredProperty(String environmentVariable, String property) {}
}
