package com.example.security.controller;

import com.example.security.service.LoginAttemptService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class ApiController {

    private final LoginAttemptService attempts;

    public ApiController(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    /** Requires VIEW_INFO. */
    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "application", "MVC application",
                "randomStat", ThreadLocalRandom.current().nextInt(1, 1000),
                "uptimeSeconds", ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
    }

    /** Public. */
    @GetMapping("/about")
    public Map<String, String> about() {
        return Map.of("name", "Spring Security demo", "version", "1.0.0");
    }

    /** Requires VIEW_ADMIN. */
    @GetMapping("/admin")
    public Map<String, String> admin() {
        return Map.of("message", "Welcome to the admin area");
    }

    /** Requires VIEW_ADMIN. */
    @GetMapping("/blocked-users")
    public List<LoginAttemptService.BlockedUser> blockedUsers() {
        return attempts.getBlockedUsers();
    }
}
