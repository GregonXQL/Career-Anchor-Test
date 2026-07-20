package com.careeranchor.server.util;

import com.careeranchor.server.config.AppProperties;
import com.careeranchor.server.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {
    @Test
    void issuesAndParsesUserToken() {
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("test-secret-must-be-at-least-thirty-two-bytes", Duration.ofDays(7), Duration.ofHours(2)),
                new AppProperties.Wechat("", "", true, "https://api.weixin.qq.com", "develop", false),
                new AppProperties.Admin(""),
                new AppProperties.Assessment(6, List.of(), 3, 4));
        JwtUtil jwtUtil = new JwtUtil(properties);

        JwtUtil.TokenClaims claims = jwtUtil.parse(jwtUtil.issue(123L, Role.USER));

        assertThat(claims.userId()).isEqualTo(123L);
        assertThat(claims.role()).isEqualTo(Role.USER);
    }
}
