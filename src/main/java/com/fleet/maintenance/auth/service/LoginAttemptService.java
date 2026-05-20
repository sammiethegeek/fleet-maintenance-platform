package com.fleet.maintenance.auth.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final int maxFailedAttempts;
    private final Duration lockoutDuration;
    private final Clock clock;

    public LoginAttemptService(
            @Value("${security.login.max-failed-attempts:5}") int maxFailedAttempts,
            @Value("${security.login.lockout-minutes:15}") long lockoutMinutes
    ) {
        this(maxFailedAttempts, Duration.ofMinutes(lockoutMinutes), Clock.systemUTC());
    }

    LoginAttemptService(int maxFailedAttempts, Duration lockoutDuration, Clock clock) {
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockoutDuration = lockoutDuration;
        this.clock = clock;
    }

    public boolean isLocked(String username) {
        AttemptState state = attempts.get(key(username));
        if (state == null || state.lockedUntil == null) {
            return false;
        }
        if (Instant.now(clock).isBefore(state.lockedUntil)) {
            return true;
        }
        attempts.remove(key(username));
        return false;
    }

    public void recordSuccess(String username) {
        attempts.remove(key(username));
    }

    public void recordFailure(String username) {
        attempts.compute(key(username), (ignored, existing) -> {
            int failures = existing == null ? 1 : existing.failures + 1;
            Instant lockedUntil = failures >= maxFailedAttempts ? Instant.now(clock).plus(lockoutDuration) : null;
            return new AttemptState(failures, lockedUntil);
        });
    }

    private String key(String username) {
        return username == null ? "" : username.toLowerCase(Locale.ROOT);
    }

    private record AttemptState(int failures, Instant lockedUntil) {
    }
}
