package com.acme.axa.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;
/**
 * Scope gemäß <a href="https://www.rfc-editor.org/rfc/rfc6749.html">OAuth 2.0</a> (hier nur: "email profile").
 */
public enum ScopeType {
    /**
     * Scope "email profile" für OAuth 2.0.
     */
    EMAIL_PROFILE("email profile");

    private final String value;

    ScopeType(final String value) {
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
    public static ScopeType of(final String value) {
        return Stream.of(values())
            .filter(token -> token.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}
