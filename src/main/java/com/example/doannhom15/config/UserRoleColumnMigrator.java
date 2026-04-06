package com.example.doannhom15.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * MySQL có thể đã tạo cột {@code users.role} kiểu ENUM('ADMIN','USER') — không chứa STAFF.
 * Đổi sang VARCHAR(32) để lưu mọi giá trị {@link com.example.doannhom15.model.User.Role}.
 * Lưu ý: nếu đã có dữ liệu user trong DB, giá trị role cũ vẫn giữ nguyên sau khi đổi kiểu.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class UserRoleColumnMigrator implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Override
    public void run(String... args) {
        if (datasourceUrl == null || !datasourceUrl.toLowerCase().contains("mysql")) {
            return;
        }
        try {
            // Lấy kiểu hiện tại của cột role
            String currentType = null;
            try {
                var rs = jdbcTemplate.queryForRowSet(
                    "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'role'");
                if (rs.next()) {
                    currentType = rs.getString("COLUMN_TYPE");
                    log.info("Detected users.role column type: {}", currentType);
                }
            } catch (Exception ex) {
                log.debug("Could not detect column type: {}", ex.getMessage());
            }

            if (currentType != null && currentType.startsWith("enum")) {
                log.info("ENUM role detected — converting to VARCHAR to support STAFF role...");
            }

            // Thử trực tiếp ALTER (đủ rộng để chứa VARCHAR 32)
            try {
                jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN role VARCHAR(32) NOT NULL");
                log.info("users.role → VARCHAR(32) [direct MODIFY OK, STAFF role now supported]");
                return;
            } catch (Exception alterErr) {
                // ALTER MODIFY thất bại → ENUM không chứa STAFF → cần tạo cột mới
                String em = alterErr.getMessage() != null ? alterErr.getMessage().toLowerCase() : "";

                if (em.contains("data truncated") || em.contains("enum")) {
                    log.warn("ENUM does not contain STAFF value. "
                            + "Creating temporary VARCHAR column to safely migrate data...");

                    // Thêm cột tạm
                    jdbcTemplate.execute(
                        "ALTER TABLE users ADD COLUMN role_tmp VARCHAR(32) NULL AFTER google_id");

                    // Copy dữ liệu cũ sang cột tạm
                    jdbcTemplate.execute(
                        "UPDATE users SET role_tmp = role");

                    // Drop cột ENUM cũ
                    jdbcTemplate.execute(
                        "ALTER TABLE users DROP COLUMN role");

                    // Rename cột tạm → role
                    jdbcTemplate.execute(
                        "ALTER TABLE users CHANGE COLUMN role_tmp role VARCHAR(32) NOT NULL");

                    log.info("users.role migrated from ENUM → VARCHAR(32) successfully!");
                } else {
                    log.error("ALTER users.role failed with unexpected error: {}", alterErr.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("CRITICAL: Could not fix users.role column! "
                    + "Please run manually in MySQL:\n"
                    + "  ALTER TABLE users MODIFY COLUMN role VARCHAR(32) NOT NULL;\n"
                    + "Error: {}", e.getMessage());
        }
    }
}
