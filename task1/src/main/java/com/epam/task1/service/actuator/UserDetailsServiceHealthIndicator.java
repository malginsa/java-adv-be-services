package com.epam.task1.service.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceHealthIndicator implements HealthIndicator {

    private final UserDetailsService userDetailsService;

    public UserDetailsServiceHealthIndicator(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Health health() {
        try {
            userDetailsService.loadUserByUsername("admin");
            return Health.up()
                    .withDetail("admin user status", "is present")
                    .build();
        } catch (UsernameNotFoundException e) {
            return Health.up()
                    .withDetail("admin user status", "is absent")
                    .build();
        }
    }
}
