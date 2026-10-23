package com.rememberfit.backend.global.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// 기본값은 H2(MySQL 모드)이고, CI에서는 환경 변수로 실제 MySQL을 가리킨다.
// 운영은 MySQL이므로 H2 MySQL 모드만으로는 검증되지 않는 DDL 차이가 남는다.
@SpringBootTest(properties = {
        "spring.datasource.url=${MIGRATION_TEST_DB_URL:jdbc:h2:mem:rememberfit_migration;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE}",
        "spring.datasource.driver-class-name=${MIGRATION_TEST_DB_DRIVER:org.h2.Driver}",
        "spring.datasource.username=${MIGRATION_TEST_DB_USERNAME:sa}",
        "spring.datasource.password=${MIGRATION_TEST_DB_PASSWORD:}",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=false"
})
class DatabaseMigrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void appliesEveryMigrationAndPassesJpaValidation() {
        // 적용되지 않은 마이그레이션이 남아 있으면 안 된다.
        assertThat(flyway.info().pending()).isEmpty();
        // 최신 버전을 명시해 둔다. 새 마이그레이션을 추가하면 이 값을 함께 올린다.
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
    }
}
