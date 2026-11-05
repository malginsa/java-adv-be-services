package com.epam.task1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class HealthIndicatorTests {

    @Autowired
    private HealthEndpoint healthEndpoint;


    @Test
    public void testHealthEndpoint() {
        HealthComponent health = healthEndpoint.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void testUserDetailsServiceHealthIndicator() {
        HealthComponent health = healthEndpoint.healthForPath("userDetailsService");
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void testDatabaseHealthIndicator() {
        HealthComponent health = healthEndpoint.healthForPath("db");
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }
}
