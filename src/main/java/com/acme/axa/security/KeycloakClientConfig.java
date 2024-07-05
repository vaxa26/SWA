package com.acme.axa.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Konfiguration für einen Spring-HTTP-Client für Keycloak.
 */
public interface KeycloakClientConfig {
    /**
     * Logger-Objekt.
     */
    Logger LOGGER = LoggerFactory.getLogger(KeycloakClientConfig.class);

    /**
     * Bean-Methode, um ein Objekt zum Interface KeycloakRepository zu erstellen.
     *
     * @param clientBuilder Injiziertes Objekt vom Typ RestClient.Builder
     * @param keycloak Spring-Properties für Keycloak
     * @return Objekt zum Interface KeycloakRepository
     */
    @Bean
    default KeycloakRepository keycloakRepository(final RestClient.Builder clientBuilder, KeycloakProps keycloak) {
        final var baseUri = UriComponentsBuilder.newInstance()
            .scheme(keycloak.schema())
            .host(keycloak.host())
            .port(keycloak.port())
            .build();
        LOGGER.debug("keycloakRepository: baseUri={}", baseUri);

        final var restClient = clientBuilder.baseUrl(baseUri.toUriString()).build();
        final var clientAdapter = RestClientAdapter.create(restClient);
        final var proxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build();
        return proxyFactory.createClient(KeycloakRepository.class);
    }
}
