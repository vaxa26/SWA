package com.acme.axa.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

/**
 * Runtime Exception, falls kein Player gefunden wurde.
 */
@Getter
public final class NotFoundException extends RuntimeException {
    /**
     * Nicht-vorhandene ID.
     */
    private final UUID id;
    private final Map<String, List<String>> suchkriterien;

    NotFoundException(final UUID id) {
        super(STR."kein Player gefunden mit ID \{id}");
        this.id = id;
        suchkriterien = null;
    }

    NotFoundException(final Map<String, List<String>> suchkriterien) {
        super("No Player Found.");
        id = null;
        this.suchkriterien = suchkriterien;
    }
}
