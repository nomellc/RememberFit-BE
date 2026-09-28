package com.rememberfit.backend.global.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:rememberfit_migration;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=false"
})
class DatabaseMigrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void appliesInitialSchemaAndPassesJpaValidation() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
    }
}
