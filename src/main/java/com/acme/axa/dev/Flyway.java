package com.acme.axa.dev;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Migrationsstrategie für Flyway im Profile "dev": Tabellen, Indexe etc. löschen und dann neu aufbauen.
 *
 */
public interface Flyway {
    /**
     * Bean-Definition, um eine Migrationsstrategie für Flyway im Profile "dev" bereitzustellen, so dass zuerst alle
     * Tabellen, Indexe etc. gelöscht und dann neu aufgebaut werden.
     *
     * @return FlywayMigrationStrategy
     */
    @Bean
    @Profile("dev")
    default FlywayMigrationStrategy cleanMigrateStrategy() {
        // https://www.javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/Flyway.html
        return flyway -> {
            // Loeschen aller DB-Objekte im Schema: Tabellen, Indexe, Stored Procedures, Trigger, Views, ...
            // insbesondere die Tabelle flyway_schema_history
            flyway.clean();
            // Start der DB-Migration
            flyway.migrate();
        };
    }
}
