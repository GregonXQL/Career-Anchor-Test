package com.careeranchor.server.config;

import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.exception.BizException;
import com.careeranchor.server.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        try {
            JwtUtil.TokenClaims claims = jwtUtil.parse(authorization.substring(7));
            request.setAttribute(AuthContext.ATTRIBUTE, new AuthContext.Principal(claims.userId(), claims.role()));
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
    }
}
