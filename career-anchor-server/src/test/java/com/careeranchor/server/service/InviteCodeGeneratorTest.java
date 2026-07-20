package com.careeranchor.server.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InviteCodeGeneratorTest {
    @Test
    void generatesEightCharacterCodesWithoutAmbiguousCharacters() {
        InviteCodeGenerator generator = new InviteCodeGenerator();
        Set<String> generated = new HashSet<>();

        for (int index = 0; index < 500; index += 1) {
            String code = generator.next();
            assertThat(code).hasSize(8).matches("[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{8}");
            generated.add(code);
        }
        assertThat(generated).hasSize(500);
    }
}
