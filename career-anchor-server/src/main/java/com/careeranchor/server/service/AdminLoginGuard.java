package com.careeranchor.server.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdminLoginGuard {
    static final int MAX_FAILURES = 5;
    static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final Clock clock;
    private final ConcurrentHashMap<Long, Attempt> attempts = new ConcurrentHashMap<>();

    public AdminLoginGuard(Clock clock) {
        this.clock = clock;
    }

    public boolean isLocked(long userId) {
        Attempt attempt = attempts.get(userId);
        if (attempt == null || attempt.lockedUntil() == null) return false;
        if (!Instant.now(clock).isBefore(attempt.lockedUntil())) {
            attempts.remove(userId, attempt);
            return false;
        }
        return true;
    }

    public boolean recordFailure(long userId) {
        Instant now = Instant.now(clock);
        Attempt updated = attempts.compute(userId, (ignored, previous) -> {
            int failures = previous == null ? 1 : previous.failures() + 1;
            return failures >= MAX_FAILURES
                    ? new Attempt(failures, now.plus(LOCK_DURATION))
                    : new Attempt(failures, null);
        });
        return updated.lockedUntil() != null;
    }

    public void clear(long userId) {
        attempts.remove(userId);
    }

    record Attempt(int failures, Instant lockedUntil) {}
}
