package com.acme.axa.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

/**
 * Enum für art von Skins.
 */
public enum SkinType {

    /**
     * Common Skins.
     */
    COMMON("C"),
    /**
     * Rare Skins.
     */
    RARE("R"),
    /**
     * Epische Skins.
     */
    EPIC("E"),
    /**
     * Legendäre Skins.
     */
    LEGENDARY("L");

    private final String value;

    SkinType(final String value) {
        this.value = value;
    }

    /**
     * Enum Wert wird als String mit einem Wert ausgegeben.
     *
     * @return value  Value wird ausgegeben.
     * */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * return die Rankings.
     *
     * @param value Wert wird übergeben.
     * @return skintype wird ausgegeben.
     * */
    @JsonCreator
    public static SkinType of(final String value) {
        return Stream.of(values())
            .filter(skin -> skin.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
