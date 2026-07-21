package com.careeranchor.server.util;

import com.careeranchor.server.config.AppProperties;
import com.careeranchor.server.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@DependsOn("productionConfigurationValidator")
public class JwtUtil {
    private final SecretKey key;
    private final AppProperties.Jwt properties;

    public JwtUtil(AppProperties properties) {
        this.properties = properties.jwt();
        this.key = Keys.hmacShaKeyFor(this.properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String issue(long userId, Role role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(role == Role.ADMIN ? properties.adminTtl() : properties.userTtl());
        return Jwts.builder()
                .subject(Long.toString(userId))
                .claim("uid", userId)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public TokenClaims parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new TokenClaims(claims.get("uid", Long.class), Role.valueOf(claims.get("role", String.class)));
    }

    public record TokenClaims(long userId, Role role) {}
}
