package com.acme.axa.dev;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Informationen für Entwickler im Hinblick auf Security werden protokolliert, nur logSecurity wird verwendet.
 */
public interface LogSignatureAlgorithms {
    /**
     * Bean-Definition, um einen Listener bereitzustellen, damit die im JDK vorhandenen Signature-Algorithmen
     * aufgelistet werden.
     *
     * @return Listener für die Ausgabe der Signature-Algorithmen
     */
    @SuppressWarnings("LambdaBodyLength")
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @Profile("logSecurity")
    default ApplicationListener<ApplicationReadyEvent> logSignatureAlgorithms() {
        final var log = LoggerFactory.getLogger(LogSignatureAlgorithms.class);
        return _ -> Arrays
            .stream(Security.getProviders())
            .forEach(provider -> logSignatureAlgorithms(provider, log));
    }

    private void logSignatureAlgorithms(final Provider provider, final Logger log) {
        provider
            .getServices()
            .forEach(service -> {
                if ("Signature".contentEquals(service.getType())) {
                    log.debug("{}", service.getAlgorithm());
                }
            });
    }
}
