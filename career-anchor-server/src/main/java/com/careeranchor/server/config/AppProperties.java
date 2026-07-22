package com.careeranchor.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Wechat wechat, Admin admin, Assessment assessment) {
    public record Jwt(String secret, Duration userTtl, Duration adminTtl) {}
    public record Wechat(String appId, String appSecret, boolean mockEnabled, boolean cloudCallEnabled,
                         String apiBaseUrl, String acodeEnvVersion, boolean acodeCheckPath) {}
    public record Admin(String bootstrapPassword) {}
    public record Assessment(int scaleMax, List<String> scaleLabels, int boostCount, int boostValue) {}
}
