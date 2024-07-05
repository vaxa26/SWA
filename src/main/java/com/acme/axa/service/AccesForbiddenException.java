package com.acme.axa.service;

import com.acme.axa.security.Rolle;
import java.util.Collection;
import lombok.Getter;

/**
 * Exception, falls der Zugriff wegen fehlender Rollen nicht erlaubt ist.
 */
@Getter
public class AccesForbiddenException extends RuntimeException {
    /**
     * Vorhandene Rollen.
     */
    private final Collection<Rolle> rollen;

    @SuppressWarnings("ParameterHidesMemberVariable")
    AccesForbiddenException(final Collection<Rolle> rollen) {

        super(STR."Unzureichende Rolle: \{rollen}");
        this.rollen = rollen;
    }
}
