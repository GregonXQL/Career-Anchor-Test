package com.careeranchor.server.service;

import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.exception.BizException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SubmissionRateLimiter {
    static final Duration COOLDOWN = Duration.ofSeconds(60);

    private final Clock clock;
    private final ConcurrentHashMap<Long, Instant> submissions = new ConcurrentHashMap<>();

    public SubmissionRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public Instant acquire(long userId) {
        Instant now = clock.instant();
        AtomicBoolean denied = new AtomicBoolean(false);
        submissions.compute(userId, (ignored, previous) -> {
            if (previous != null && Duration.between(previous, now).compareTo(COOLDOWN) < 0) {
                denied.set(true);
                return previous;
            }
            return now;
        });
        if (denied.get()) {
            throw new BizException(ErrorCode.SUBMIT_TOO_FREQUENTLY);
        }
        return now;
    }

    public void release(long userId, Instant marker) {
        submissions.remove(userId, marker);
    }
}
