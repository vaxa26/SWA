package com.acme.axa.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import static com.acme.axa.controller.PlayerGetController.REST_PATH;

/**
 * Hilfsklasse für URIS in ProblemDetaul zu ermitteln, falls API-Gateway verwendet wird.
 *
 */
@Component
@Slf4j
class UriHelper {
    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String X_FORWARDED_HOST = "X-Forwarded-Host";
    private static final String X_FORWARDED_PREFIX = "X-Forwarded-Prefix";
    private static final String PLAYER_PREFIX = "/Player";

    URI getBaseUri(final HttpServletRequest request) {
        final var forwarded = request.getHeader(X_FORWARDED_HOST);
        if (forwarded != null) {
            return getBaseUriForwarded(request, forwarded);
        }

        final var uriComponents = ServletUriComponentsBuilder.fromRequest(request).build();
        final var baseUri =
            uriComponents.getScheme() + "://" + uriComponents.getHost() + ':' + uriComponents.getPort() + REST_PATH;
        log.debug("getBaseUri: (ohne forwarding) baseUri={}", baseUri);
        return URI.create(baseUri);

    }

    private URI getBaseUriForwarded(final HttpServletRequest request, final String forwardedhost) {
        final var forwardedProtocol = request.getHeader(X_FORWARDED_PROTO);
        if (forwardedProtocol == null) {
            throw new IllegalArgumentException("Kein '" + X_FORWARDED_PROTO + "' im Header");
        }

        var forwardedPrefix = request.getHeader(X_FORWARDED_PREFIX);
        if (forwardedPrefix == null) {
            log.trace("getBaseUriForwarded: Kein '{}' im Header", X_FORWARDED_PREFIX);
            forwardedPrefix = PLAYER_PREFIX;
        }
        final var baseUri = forwardedProtocol + "://" + forwardedhost + forwardedPrefix + REST_PATH;
        log.debug("getBaseUri: (mit forwarding) baseUri={}", baseUri);
        return URI.create(baseUri);
    }
}
