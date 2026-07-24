package com.careeranchor.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
public class AppConfig {
    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Asia/Shanghai"));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
