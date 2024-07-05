/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.axa.security;

import com.c4_soft.springaddons.security.oidc.starter.synchronised.resourceserver.ResourceServerExpressionInterceptUrlRegistryPostProcessor;
import java.util.List;
import java.util.Map;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import static com.acme.axa.controller.PlayerGetController.PLAYERNAME_PATH;
import static com.acme.axa.controller.PlayerGetController.REST_PATH;
import static com.acme.axa.security.AuthController.AUTH_PATH;
import static com.acme.axa.security.Rolle.ADMIN;
import static com.acme.axa.security.Rolle.USER;
/**
 * Security-Konfiguration.
 *
 */
public interface SecurityConfig {
    /**
     * Bean-Methode zur Integration von Spring Security mit Keycloak.
     *
     * @return Post-Prozessor für Spring Security zur Integration mit Keycloak
     */
    @Bean
    default ResourceServerExpressionInterceptUrlRegistryPostProcessor authenticationPostProcessor() {
        return registry -> registry
            .requestMatchers(HttpMethod.OPTIONS, "/rest/**").permitAll()
            .requestMatchers("/rest/**").authenticated()
            .anyRequest().authenticated();
    }

    /**
     * Bean-Definition, um den Zugriffsschutz an der REST-Schnittstelle zu konfigurieren.
     *
     * @param http Injiziertes Objekt von HttpSecurity als Ausgangspunkt für die Konfiguration.
     * @param authenticationConverter Iniziertes Objekt von Converter für Anpassung an KC
     * @return Objekt von SecurityFilterChain
     * @throws Exception Wegen HttpSecurity.authorizeHttpRequests()
     */
    // https://github.com/spring-projects/spring-security-samples/blob/main/servlet/java-configuration/...
    // ...authentication/preauth/src/main/java/example/SecurityConfiguration.java
    @Bean
    @SuppressWarnings("LambdaBodyLength")
    default DefaultSecurityFilterChain securityFilterChain(
        final HttpSecurity http,
        final Converter<Jwt, AbstractAuthenticationToken> authenticationConverter
    ) throws Exception {
        return http
            .authorizeHttpRequests(authorize -> {
                final var restPathPlayerId = REST_PATH + "/*";
                authorize
                    .requestMatchers(HttpMethod.OPTIONS, REST_PATH + "/**").permitAll()
                    .requestMatchers(HttpMethod.GET, AUTH_PATH + "/me").hasAnyRole(ADMIN.name(), USER.name())
                    .requestMatchers(HttpMethod.POST, AUTH_PATH + "/login").permitAll()
                    .requestMatchers(HttpMethod.GET, REST_PATH).hasRole(ADMIN.name())
                    .requestMatchers(
                        HttpMethod.GET,
                        REST_PATH + PLAYERNAME_PATH + "/*",
                        "/swagger-ui.html"
                    ).hasRole(ADMIN.name())
                    .requestMatchers(HttpMethod.GET, restPathPlayerId).hasAnyRole(ADMIN.name(), USER.name())

                    .requestMatchers(HttpMethod.PUT, restPathPlayerId).hasRole(ADMIN.name())
                    .requestMatchers(HttpMethod.POST, "/dev/db_populate").hasRole(ADMIN.name())
                    .requestMatchers(HttpMethod.POST, REST_PATH, "/graphql", AUTH_PATH + "/login").permitAll()
                    .requestMatchers(
                        EndpointRequest.to(HealthEndpoint.class),
                        EndpointRequest.to(PrometheusScrapeEndpoint.class)
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/v3/api-docs.yaml", "/v3/api-docs", "/graphiql").permitAll()
                    .requestMatchers("/error", "/error/**").permitAll()

                    .anyRequest().authenticated();
            })

            .oauth2ResourceServer(resourceServer -> resourceServer
                .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(authenticationConverter))
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .build();
    }

    /**
     * Bean-Methode für die Überprüfung, ob ein Passwort ein bekanntes ("gehacktes") Passwort ist.
     *
     * @return "Checker-Objekt" für die Überprüfung, ob ein Passwort ein bekanntes ("gehacktes") Passwort ist
     */
    @Bean
    default CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    /**
     * Bean-Definition, um den Verschlüsselungsalgorithmus für Passwörter bereitzustellen. Es wird der
     * Default-Algorithmus von Spring Security verwendet: bcrypt.
     *
     * @return Objekt für die Verschlüsselung von Passwörtern.
     */
    @Bean
    default PasswordEncoder passwordEncoder() {
        final var saltLength = 32;
        final var hashLength = 16;
        final var parallelism = 1;
        final var numberOfBits = 14;
        final var memoryConsumptionKbytes = 1 << numberOfBits;
        final var iterations = 3;

        final var idForEncode = "argon2id";
        final Map<String, PasswordEncoder> encoders = Map.of(
            idForEncode,
            new Argon2PasswordEncoder(
                saltLength,
                hashLength,
                parallelism,
                memoryConsumptionKbytes,
                iterations
            )
        );
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

    /**
     * Bean, um Test-User anzulegen. Dazu gehören jeweils ein Benutzername, ein Passwort und diverse Rollen.
     * Das wird in Beispiel 2 verbessert werden.
     *
     * @param passwordEncoder Injiziertes Objekt zur Passwort-Verschlüsselung
     * @return Ein Objekt, mit dem diese (Test-) User verwaltet werden, z.B. für die künftige Suche.
     */
    @Bean
    @SuppressWarnings("java:S6437")
    default UserDetailsService userDetailsService(final PasswordEncoder passwordEncoder) {
        final var users = List.of(
            User.withUsername("admin")
                .password(passwordEncoder.encode("p"))
                .roles(ADMIN.name(), USER.name())
                .build()
        );

        return new InMemoryUserDetailsManager(users);
    }
}
