package com.coldchain.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 确保批次表具备赋码扩展字段
 */
@Slf4j
@Component
@Order(15)
@RequiredArgsConstructor
public class BatchSchemaRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            ensureColumn("shelf_life", "INT DEFAULT 30 COMMENT '保质期(天)'");
            ensureColumn("quantity", "INT DEFAULT 0 COMMENT '批次数量'");
            ensureColumn("unit", "VARCHAR(20) DEFAULT '箱' COMMENT '单位'");
        } catch (Exception e) {
            log.warn("批次表结构检查跳过: {}", e.getMessage());
        }
    }

    private void ensureColumn(String column, String definition) {
        Integer cnt = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_batch' AND COLUMN_NAME = ?
                """,
                Integer.class, column);
        if (cnt != null && cnt > 0) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE product_batch ADD COLUMN " + column + " " + definition);
        log.info("已扩展 product_batch.{}", column);
    }
}
