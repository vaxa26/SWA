package com.acme.axa.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;


/**
* enum aller Rankings mit JSon inspieriert von kunde projekt.
* */

public enum RankedType {
    /**
     * Rang: kein rang.
     */
    UNRANKED("Unranked"),

    /**
     * Rang Bronze.
     */
    BRONZE("Bronze"),
    /**
     * Rang Silber.
     */
    SILVER("Silver"),
    /**
     * Rang Gold.
     */
    GOLD("Gold"),
    /**
     * Rang Platinum.
     */
    PLATINUM("Platinum"),
    /**
     * Rang Emerald.
     */
    EMERALD("Emerald"),
    /**
     * Rand Dia.
     */
    DIAMOND("Diamond"),
    /**
     * Rang: Master.
     */
    MASTER("Master"),
    /**
     * Rang: grandmaster.
     */
    GRANDMASTER("Grandmaster"),
    /**
     * Rang Challenger.
     */
    CHALLENGER("Challenger");
    /**
    * Mapping in einem JSON-Datensatz.
    */
    private final String value;
    RankedType(final String value) {
        this.value = value;
    }
    /**
    * Enum Wert wird als String mit einem Wert ausgegeben.
    *
    * @return value  Value wird ausgegeben
    * */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
    * return die Rankings.
    *
    * @param value Wert wird übergeben
    * @return ranked Ranked wird ausgegeben
    * */
    @JsonCreator
    public static RankedType of(final String value) {
        return Stream.of(values())
        .filter(ranked -> ranked.getValue().equals(value))
        .findFirst()
        .orElse(null);
    }
}
