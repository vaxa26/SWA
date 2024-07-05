package com.acme.axa.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;
/**
 * Token-Typ für OAuth 2.0 (hier nur: Bearer).
 */
public enum TokenType {
    /**
     * Bearer als Token-Typ.
     */
    BEARER("Bearer");

    private final String value;

    TokenType(final String value) {
        this.value = value;
    }

    /**
     * Einen enum-Wert als String mit dem internen Wert ausgeben.
     * Dieser Wert wird durch Jackson in einem JSON-Datensatz verwendet.
     *
     * @return Interner Wert
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Konvertierung eines Strings in einen Enum-Wert.
     *
     * @param value Der String, zu dem ein passender Enum-Wert ermittelt werden soll.
     * @return Passender Enum-Wert oder null.
     */
    @JsonCreator
    public static TokenType of(final String value) {
        return Stream.of(values())
            .filter(token -> token.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}
