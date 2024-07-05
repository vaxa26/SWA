package com.acme.axa.controller;

import com.acme.axa.entity.Player;
import com.acme.axa.security.JWTService;
import com.acme.axa.service.PlayerReadService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.acme.axa.controller.PlayerGetController.REST_PATH;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

/**
 * Eine Controller-Klasse bildet die Rest-Schnittstelle.
 * <img src="..\..\..\..\..\..\..\extras\doc\PlayerGetController.png" alt="PlayerGetController" width=400>
 */
@RestController
@RequestMapping(REST_PATH)
@OpenAPIDefinition(info = @Info(title = "Player API", version = "v2"))
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"ClassFanOutComplexity", "java:S1075"})
public class PlayerGetController {

    /**
     * Basispfad für die Rest-Schnittstelle.
     * */
    public static final String REST_PATH = "/rest";

    /**
     * Pfad Playername abzufragen.
     */
    public static final String PLAYERNAME_PATH = "/playername";

    /**
     * Muster für eine UUID.
     */
    public static final String ID_PATTERN =
        "[\\da-f]{8}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{12}";

    /**
     * Pfad um nach id zu suchen.
     */
    private final PlayerReadService service;
    private final JWTService jwtService;
    private final UriHelper uriHelper;

    /**
     * Suche alle Player-ID als Pfadparameter.
     *
     * @param id ID des gesuchten Players
     * @param request RequestObjekt, um Links für HATEOS zu erstellen.
     * @param jwt für sevurity
     * @param version Versionsnummer
     * @return gefundener Player
     */
    @GetMapping(path = "{id:" + ID_PATTERN + "}", produces = HAL_JSON_VALUE)
    @Observed(name = "get-by-id")
    @Operation(summary = "Searching Player", tags = "Seraching")
    @ApiResponse(responseCode = "200", description = "Player Found")
    @ApiResponse(responseCode = "404", description = "No Player")
    @SuppressWarnings("ReturnCount")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    ResponseEntity<PlayerModel> getPlayerById(
        @PathVariable final UUID id, @RequestHeader("If-None-Match")  final Optional<String> version,
        final HttpServletRequest request,
        @AuthenticationPrincipal final Jwt jwt
    ) {
        final var username = jwtService.getUsername(jwt);
        log.debug("getById: id={}, version={}, username={}", id, version, username);
        if (username == null) {
            log.error("Trotz Spring Security wurde getPlayerById ohne Usernmae aufgerufen");
            return status(UNAUTHORIZED).build();
        }
        final var rollen = jwtService.getRollen(jwt);
        log.trace("getById: rollen={}", rollen);

        final var player = service.findById(id, username, rollen, false);
        log.trace("getById: {}", player);

        final var currentVersion = "\"" + player.getVersion() + '"';
        if  (Objects.equals(version.orElse(null), currentVersion)) {
            return status(NOT_MODIFIED).build();
        }

        final var model = playerToModel(player, request);
        log.debug("getById: model={}", model);
        return ok().eTag(currentVersion).body(model);
    }

    private PlayerModel playerToModel(final Player player, final HttpServletRequest request) {
        final var model = new PlayerModel(player);

        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var idUri = baseUri + '/' + player.getId();
        final var selfLink = Link.of(idUri);
        final var listLink = Link.of(baseUri, LinkRelation.of("list"));
        final var addLink = Link.of(baseUri, LinkRelation.of("add"));
        final var updateLink = Link.of(idUri, LinkRelation.of("update"));
        final var removeLink = Link.of(idUri, LinkRelation.of("remove"));
        model.add(selfLink, listLink, addLink, updateLink, removeLink);
        return model;

    }



    /**
     * Suche anhand Suchkriterien.
     *
     * @param suchkriterien suchkriterien
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @return gefundener player
     */
    @GetMapping(produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit Kriterien", tags = "Search")
    @ApiResponse(responseCode = "200", description = "")
    @ApiResponse(responseCode = "404", description = "No Player found")
    CollectionModel<PlayerModel> getPlayer(
        @RequestParam @NonNull final MultiValueMap<String, String> suchkriterien,
        final HttpServletRequest request
    ) {
        log.debug("get: suchkriterien {}", suchkriterien);

        final var baseUri = uriHelper.getBaseUri(request).toString();

        final var models = service.find(suchkriterien)
            .stream()
            .map(player -> {
                final var model = new PlayerModel(player);
                model.add(Link.of(baseUri + '/' + player.getId()));
                return model;
            })
            .toList();
        log.debug("get:  {}", models);
        return CollectionModel.of(models);
    }

}
