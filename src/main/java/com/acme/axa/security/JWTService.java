package com.acme.axa.security;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Service-Klasse, um Benutzernamen und Rollen aus einem JWT von Keycloak zu extrahieren.
 */
@Service
@Slf4j
@SuppressWarnings("java:S5852")
public class JWTService {


    /**
     * Zu einem gegebenen JWT wird der zugehörige Username gesucht.
     *
     * @param jwt JWT für Security
     * @return Der gesuchte Username oder null
     */
    public String getUsername(final Jwt jwt)  {
        log.debug("getUsername");
        if (jwt == null) {
            throw new UsernameNotFoundException("JWT == null");
        }
        final var username = (String) jwt.getClaims().get("preferred_username");
        log.debug("getUsername: username={}", username);
        return username;
    }

    /**
     * Zu einem gegebenen JWT werden die zugehörigen Rollen gesucht.
     *
     * @param jwt JWT für Security
     * @return Die gesuchten Rollen oder die leere Liste
     */
    public List<Rolle> getRollen(final Jwt jwt) {
        @SuppressWarnings("unchecked")
        final var realmAccess = (Map<String, List<String>>) jwt.getClaims().get("realm_access");
        final var rollenStr = realmAccess.get("roles");
        log.trace("getRollen: rollenStr={}", rollenStr);
        return rollenStr
            .stream()
            .map(Rolle::of)
            .filter(Objects::nonNull)
            .toList();
    }
}
