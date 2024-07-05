package com.acme.axa.controller;

/**
 * enum mit Problemdetails.
 */
public enum ProblemType {
    /**
     * Constraints als Fehlerursache.
     */
    CONSTRAINS("constraints"),
    /**
     * Fehler, wenn z.B. Emailadresse bereits existiert.
     */
    UNPROCESSABLE("unprocessable"),
    /**
     * Fehler beim Header If-Match.
     */
    PRECONDITION("precondition"),
    /**
     * Fehler bei z.B. einer Patch-Operation.
     */
    BAD_REQUEST("badRequest");
    private final String value;
    ProblemType(final String value) {
        this.value = value;
    }

    /**
     * Gibt den Fehler zurück.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }
}
