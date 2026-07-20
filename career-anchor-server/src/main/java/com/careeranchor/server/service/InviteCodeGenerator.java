package com.careeranchor.server.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class InviteCodeGenerator {
    static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    static final int CODE_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    public String next() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index += 1) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
