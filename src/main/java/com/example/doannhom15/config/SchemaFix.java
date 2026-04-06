package com.example.doannhom15.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Sửa các lỗi schema database
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaFix {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixBookingStatusColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE bookings MODIFY COLUMN status VARCHAR(20) NOT NULL");
            log.info("Fixed bookings.status column");
        } catch (Exception e) {
            log.debug("Schema fix skipped or column already correct: {}", e.getMessage());
        }
    }
    
    @PostConstruct
    public void fixPasswordColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL");
            log.info("Fixed users.password column to allow null");
        } catch (Exception e) {
            log.debug("Password column fix skipped or already correct: {}", e.getMessage());
        }
    }
}
