package com.careeranchor.server.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class AdminLoginGuardTest {
    @Test
    void locksOnTheFifthFailureAndUnlocksAfterTenMinutes() {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-20T00:00:00Z"));
        AdminLoginGuard guard = new AdminLoginGuard(clock);

        for (int index = 0; index < 4; index += 1) {
            assertThat(guard.recordFailure(7L)).isFalse();
        }
        assertThat(guard.recordFailure(7L)).isTrue();
        assertThat(guard.isLocked(7L)).isTrue();

        clock.advance(Duration.ofMinutes(10));
        assertThat(guard.isLocked(7L)).isFalse();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) { this.instant = instant; }
        void advance(Duration duration) { instant = instant.plus(duration); }
        @Override public ZoneId getZone() { return ZoneId.of("Asia/Shanghai"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
