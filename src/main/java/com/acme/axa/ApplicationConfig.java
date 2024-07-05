package com.acme.axa;

import com.acme.axa.security.KeycloakClientConfig;
import com.acme.axa.security.SecurityConfig;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;


/**
 * Konfigurationsklasse für die Anwendung bzw. den Microservice.
 *
 */
final class ApplicationConfig implements SecurityConfig, KeycloakClientConfig {
    ApplicationConfig() {
    }

    // https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#native-image.advanced.custom-hints
    // https://stackoverflow.com/questions/76287163/...
    // ...how-to-specify-the-location-of-a-keystore-file-with-spring-aot-processing
    /**
     * Keystores f&uuml;r TLS und SQL-Skripte f&uuml;r GraalVM registrieren.
     */
    static class CertificateResourcesRegistrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
            hints.resources()
                .registerPattern("*.p12")
                // https://github.com/spring-projects/spring-boot/issues/31999
                // https://github.com/flyway/flyway/issues/2927
                .registerPattern("*.sql");
        }
    }
}
