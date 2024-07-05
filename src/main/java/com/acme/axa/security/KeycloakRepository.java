package com.acme.axa.security;

import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * Konfiguration für einen Spring-HTTP-Client für Keycloak.
 */
@HttpExchange
public interface KeycloakRepository {
    /**
     * GET-Request, um von Keycloak die Konfigurationsdaten abzufragen.
     *
     * @return Die Konfigurationsdaten als Map
     */
    @GetExchange("/realms/spring/.well-known/openid-configuration")
    Map<String, Object> openidConfiguration();

    /**
     * POST-Request, um von Keycloak einen JSON-Datensatz mit Access-Token, Refresh-Token und Ablaufdauer zu erhalten.
     *
     * @param loginData als String für den Request-Body zum Content-Type application/x-www-form-urlencoded
     * @param authorization String mit Base64-Codierung für "BASIC Authentication"
     * @param contentType Content-Type "application/x-www-form-urlencoded"
     * @return JWT nach erfolgreichem Einloggen
     */
    @PostExchange("/realms/spring/protocol/openid-connect/token")
    @SuppressWarnings("JavadocReference")
    TokenDTO login(
        @RequestBody String loginData,
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(CONTENT_TYPE) String contentType
    );
}
