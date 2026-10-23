package com.rememberfit.backend.global.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * V2 는 decks 와 study_logs 에 NOT NULL 인 user_id 를 추가한다.
 * 이미 데이터가 있는 DB 에서도 기존 행을 잃지 않고 올라가야 한다.
 * 빈 DB 만 검증하면 이 경로는 한 번도 실행되지 않는다.
 */
class MigrationBackfillTest {

    private static final String LOCATIONS = "classpath:db/migration";

    @Test
    void keepsExistingRowsByAssigningThemToAPlaceholderUser() throws Exception {
        String url = "jdbc:h2:mem:backfill_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";

        migrateTo(url, "1");
        seedLegacyRows(url);
        migrateTo(url, null);

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            // 기존 행이 그대로 남아 있어야 한다.
            assertThat(scalar(connection, "SELECT COUNT(*) FROM decks")).isEqualTo(1L);
            assertThat(scalar(connection, "SELECT COUNT(*) FROM study_logs")).isEqualTo(1L);

            // 자리표시 사용자 한 명이 만들어지고 기존 행이 그 사용자에게 붙어야 한다.
            assertThat(scalar(connection, "SELECT COUNT(*) FROM users")).isEqualTo(1L);
            assertThat(scalar(connection, "SELECT COUNT(*) FROM decks WHERE user_id IS NULL")).isZero();
            assertThat(scalar(connection, "SELECT COUNT(*) FROM study_logs WHERE user_id IS NULL")).isZero();
            assertThat(scalar(connection,
                    "SELECT COUNT(*) FROM decks d JOIN users u ON d.user_id = u.id")).isEqualTo(1L);
        }
    }

    @Test
    void createsNoPlaceholderUserWhenThereIsNothingToKeep() throws Exception {
        String url = "jdbc:h2:mem:empty_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";

        migrateTo(url, null);

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            // 빈 DB 에 쓸모없는 계정을 남기지 않는다.
            assertThat(scalar(connection, "SELECT COUNT(*) FROM users")).isZero();
        }
    }

    private void migrateTo(String url, String targetVersion) {
        var configuration = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations(LOCATIONS)
                .baselineOnMigrate(false);
        if (targetVersion != null) {
            configuration = configuration.target(MigrationVersion.fromVersion(targetVersion));
        }
        configuration.load().migrate();
    }

    private void seedLegacyRows(String url) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "INSERT INTO decks (title, created_at) VALUES ('기존 암기장', CURRENT_TIMESTAMP)");
            statement.executeUpdate("""
                    INSERT INTO study_logs
                        (card_id, deck_id, quality, repetition_after, interval_days_after,
                         ease_factor_after, next_review_date, study_date, studied_at)
                    VALUES (1, 1, 5, 1, 1, 2.5, DATE '2026-10-24', DATE '2026-10-23', CURRENT_TIMESTAMP)
                    """);
        }
    }

    private long scalar(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }
}
