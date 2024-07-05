package com.acme.axa.dev;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.acme.axa.dev.DevConfig.DEV;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
/**
 * Eine Controller-Klasse, um beim Enwickeln, die (Test-) DB neu zu laden.
 *
 */
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Slf4j
@Profile(DEV)
public class DbPopulateController {
    private final Flyway flyway;

    /**
     * Test- DB wird bei Post-Request Neu-geladen.
     *
     * @return wenn keine Exception Statuscode 200.
     */
    @PostMapping(value = "db_populate", produces = TEXT_PLAIN_VALUE)
    public String dbPopulate() {
        log.warn("Db reloading");
        flyway.clean();
        flyway.migrate();
        log.warn("DB wurde neu geladen");
        return "ok";
    }
}
