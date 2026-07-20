package com.careeranchor.server.config;

import com.careeranchor.server.enums.Role;

public final class AuthContext {
    public static final String ATTRIBUTE = "authPrincipal";

    private AuthContext() {}

    public record Principal(long userId, Role role) {}
}
