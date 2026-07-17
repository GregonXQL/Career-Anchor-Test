package com.careeranchor.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Wechat wechat, Assessment assessment) {
    public record Jwt(String secret, Duration userTtl, Duration adminTtl) {}
    public record Wechat(String appId, String appSecret, boolean mockEnabled) {}
    public record Assessment(int scaleMax, List<String> scaleLabels, int boostCount, int boostValue) {}
}
