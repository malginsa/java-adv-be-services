package com.example.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory brute force protector: blocks an email after N consecutive failures. */
@Service
public class LoginAttemptService {

    public record BlockedUser(String email, Instant blockedUntil, long remainingSeconds) {
    }

    private final int maxAttempts;
    private final Duration blockDuration;
    private final Map<String, Integer> failures = new ConcurrentHashMap<>();
    private final Map<String, Instant> blocked = new ConcurrentHashMap<>();

    public LoginAttemptService(@Value("${app.security.max-failed-attempts:3}") int maxAttempts,
                               @Value("${app.security.block-duration-minutes:5}") long blockMinutes) {
        this.maxAttempts = maxAttempts;
        this.blockDuration = Duration.ofMinutes(blockMinutes);
    }

    public void loginFailed(String email) {
        String key = normalize(email);
        if (isBlocked(key)) {
            return;
        }
        int count = failures.merge(key, 1, Integer::sum);
        if (count >= maxAttempts) {
            failures.remove(key);
            blocked.put(key, Instant.now().plus(blockDuration));
        }
    }

    public void loginSucceeded(String email) {
        failures.remove(normalize(email));
    }

    public boolean isBlocked(String email) {
        String key = normalize(email);
        Instant until = blocked.get(key);
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            blocked.remove(key, until);
            return false;
        }
        return true;
    }

    public List<BlockedUser> getBlockedUsers() {
        Instant now = Instant.now();
        blocked.entrySet().removeIf(e -> now.isAfter(e.getValue()));
        return blocked.entrySet().stream()
                .map(e -> new BlockedUser(e.getKey(), e.getValue(),
                        Duration.between(now, e.getValue()).toSeconds()))
                .sorted(Comparator.comparing(BlockedUser::email))
                .toList();
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
