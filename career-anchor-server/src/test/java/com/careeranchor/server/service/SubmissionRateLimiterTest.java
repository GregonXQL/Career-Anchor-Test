package com.careeranchor.server.service;

import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.exception.BizException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmissionRateLimiterTest {
    private static final Instant START = Instant.parse("2026-07-20T02:00:00Z");

    @Test
    void deniesASecondSubmissionInsideSixtySeconds() {
        MutableClock clock = new MutableClock(START);
        SubmissionRateLimiter limiter = new SubmissionRateLimiter(clock);

        limiter.acquire(42L);
        clock.instant = START.plusSeconds(59);

        assertThatThrownBy(() -> limiter.acquire(42L))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.errorCode())
                                .isEqualTo(ErrorCode.SUBMIT_TOO_FREQUENTLY));

        clock.instant = START.plusSeconds(60);
        assertThat(limiter.acquire(42L)).isEqualTo(clock.instant);
    }

    @Test
    void releaseOnlyRemovesTheMatchingAcquisition() {
        MutableClock clock = new MutableClock(START);
        SubmissionRateLimiter limiter = new SubmissionRateLimiter(clock);
        Instant marker = limiter.acquire(42L);

        limiter.release(42L, marker.plusSeconds(1));
        assertThatThrownBy(() -> limiter.acquire(42L)).isInstanceOf(BizException.class);

        limiter.release(42L, marker);
        assertThat(limiter.acquire(42L)).isEqualTo(START);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
