package com.acme.axa.controller;


import com.acme.axa.service.EmailExistException;
import com.acme.axa.service.PlayerWriteService;
import com.acme.axa.service.UsernameExistException;
import com.acme.axa.service.VersionInvalidException;
import com.acme.axa.service.VersionOutdatedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import static com.acme.axa.controller.PlayerGetController.ID_PATTERN;
import static com.acme.axa.controller.PlayerGetController.REST_PATH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

/**
 * Controller Klasse mit Rest-Schnittstelle.
 * <img src="..\..\..\..\..\..\..\extras\doc\PlayerWriteController.png" alt="PlayerWriteController" width=400>
 */
@Controller
@RequestMapping(REST_PATH)
@Validated
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"ClassFanOutComplexity", "MethodCoutnt", "java:S1075"})
public class PlayerWriteController {

    /**
     * Basispfad für "type" innerhalb von ProblemDetail.
     */
    @SuppressWarnings("TrailingComment")
    public static final String PROBLEM_PATH = "/problem/";
    private static final String VERSIONNUMBER_MISSING = "Versionnumber missing";

    private final PlayerWriteService service;
    private final PlayerMapper mapper;
    private final UriHelper uriHelper;

    /**
     * Neuen Spieler mit Datenansatz anlegen.
     *
     * @param playerDTO mit Objekt mit den Daten aus dem RequestBody eingegangen
     * @param request Request-Objekt, um Location im Response-Header zu erstellen
     * @return Response mit Statuscode 201 einschließlich Location-Header oder Statuscode
     *         422 falls Constraints verletzt sind oder die Emailadresse bereits existiert
     *         oder Statuscode 400 falls syntaktische Fehler im Request-Body vorliegen.
     * @throws URISyntaxException  alls die URI im Request-Objekt nicht korrekt wäre
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Neuen Player anlegen", tags = "Neuanlgen")
    @ApiResponse(responseCode = "201", description = "Player neu angelegt")
    @ApiResponse(responseCode = "400", description = "Synkatatktische Fehler")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte")
    ResponseEntity<Void> post(
        @RequestBody @Validated({Default.class, PlayerDTO.OnCreate.class}) final PlayerDTO playerDTO,
        final HttpServletRequest request
    ) throws URISyntaxException {
        log.debug("post: playerDTO={}", playerDTO);

        if (playerDTO.username() == null || playerDTO.password() == null) {
            return badRequest().build();
        }

        final var playerInput = mapper.toPlayer(playerDTO);
        final var playerdb = service.create(playerInput);
        final var baseUri = uriHelper.getBaseUri(request);
        final var location = new URI(baseUri.toString() + '/' + playerdb.getId());
        return created(location).build();
    }

    /**
     * Vorhandenen Player-Datensatz überschreiben.
     *
     * @param id Id der aktualisierten Spieler.
     * @param playerDTO das Objekt mit den Daten aus dem RequestBody eingegangen
     * @param version Versionsnummer für If-Match.
     * @param request Das Request-Objekt, um ggf. die URL für ProblemDetail zu ermitteln
     * @return Response mit Statuscode 204 oder Statuscode 400, falls der JSON-Datensatz syntaktisch nicht korrekt ist
     *      *      oder 422 falls Constraints verletzt sind oder die Emailadresse bereits existiert
     *      *      oder 412 falls die Versionsnummer nicht ok ist oder 428 falls die Versionsnummer fehlt.
     */
    @PutMapping(path = "{id:" + ID_PATTERN + "}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Player aktualisieren", tags = "Aktualisieren")
    @ApiResponse(responseCode = "204", description = "Aktualisiert")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler")
    @ApiResponse(responseCode = "412", description = "Versionsnummer falsch")
    @ApiResponse(responseCode = "404", description = "Player nicht vorhanden")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte")
    @ApiResponse(responseCode = "428", description = VERSIONNUMBER_MISSING)
    ResponseEntity<Void> put(
        @PathVariable final UUID id,
        @RequestBody @Valid final PlayerDTO playerDTO,
        @RequestHeader("If-Match") final Optional<String> version,
        final HttpServletRequest request
    ) {
        log.debug("put: id={}, {}", id, playerDTO);
        final int versionInt = getVersion(version, request);
        final var playerInput = mapper.toPlayer(playerDTO);
        final var player = service.update(playerInput, id, versionInt);
        log.debug("put: {}", player);
        return noContent().eTag("\"" + player.getVersion() + '"').build();
    }

    @SuppressWarnings({"MagicNumber", "RedundantSuppression"})
    private int getVersion(final Optional<String> versionOpt, final HttpServletRequest request) {
        log.trace("getVersion: {}", versionOpt);
        final var versionStr = versionOpt.orElseThrow(() -> new VersionInvalidException(
            PRECONDITION_FAILED,
            VERSIONNUMBER_MISSING,
            URI.create(request.getRequestURL().toString()))
        );
        if (versionStr.length() < 3 ||
            versionStr.charAt(0) != '"' ||
            versionStr.charAt(versionStr.length() - 1) != '"') {
            throw new VersionInvalidException(
                PRECONDITION_FAILED,
                "Ungültiger ETag" + versionStr,
                URI.create(request.getRequestURL().toString())
            );
        }
        final int version;
        try {
            version = Integer.parseInt(versionStr.substring(1, versionStr.length() - 1));
        } catch (final NumberFormatException ex) {
            throw new VersionInvalidException(
                PRECONDITION_FAILED,
                "Ungültiger ETag" + versionStr,
                URI.create(request.getRequestURL().toString()),
                ex
            );
        }

        log.trace("getVersion: version={}", version);
        return version;
    }

    @ExceptionHandler
    ProblemDetail onConstraintViolation(
        final MethodArgumentNotValidException ex,
        final HttpServletRequest request
    ) {
        log.debug("onConstraintViolation: {}", ex.getMessage());

        final var detailMesssages = ex.getDetailMessageArguments();
        final var detail = detailMesssages == null
            ? "Constrain Violations"
            : ((String) detailMesssages[1]).replace(", and", ", ");
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, detail);
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.CONSTRAINS.getValue()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onEmailExist(final EmailExistException ex, final HttpServletRequest request) {
        log.debug("onEmailExist: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.CONSTRAINS.getValue()));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onUsernameExist(final UsernameExistException ex, final HttpServletRequest request) {
        log.debug("onUsernameExist: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.CONSTRAINS.getValue()));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onVersionOutdated(
        final VersionOutdatedException ex,
        final HttpServletRequest request
    ) {
        log.debug("onVersionOutdated: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(PRECONDITION_FAILED, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.PRECONDITION.getValue()));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onMessageNotReadable(
        final HttpMessageNotReadableException ex,
        final HttpServletRequest request
    ) {
        log.debug("onMessageNotReadable: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.BAD_REQUEST.getValue()));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }


}
