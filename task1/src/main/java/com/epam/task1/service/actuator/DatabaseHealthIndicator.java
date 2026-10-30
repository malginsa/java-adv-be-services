package com.epam.task1.service.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Health health() {
        try {
            if (!dataSource.getConnection().isValid(1000)) {
                return Health.down().withDetail("error", "Database connection invalid").build();
            }
            return Health.up()
                    .withDetail("responseTime", getResponseTime() + "ms")
                    .withDetail("database", dataSource.getConnection().getMetaData().getDatabaseProductName())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .withDetail("error", "Database health check failed")
                    .build();
        }
    }

    private long getResponseTime() {
        Instant start = Instant.now();
        jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return Instant.now().until(start, ChronoUnit.SECONDS);
    }
}
