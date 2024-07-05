package com.acme.axa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * entity für stats eines Players.
 */
@Entity
@Table(name = "stats")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class Stats {

    @Id
    @GeneratedValue
    private UUID id;
    /**
     * Anzahl der gewonnene Spiele.
     */
    private int wins;

    /**
     * Anzahl der verlorene Spiele.
     */
    private int loss;

    /**
     * Wahrscheinlichkeit für Siege.
     */

    private double winrate;

    /**
     * Wahrscheinlichkeit für Niederlagen.
     */
    private double lossrate;


    /**
     * hat der Spieler 3mal  hintereinander gewonnen?.
     */
    private boolean hotstreak;

    /**
     * hat der Spieler 3mal hintereinander verloren?.
     */
    private boolean loosestreak;

}

