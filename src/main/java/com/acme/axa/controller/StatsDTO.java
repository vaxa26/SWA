package com.acme.axa.controller;

import lombok.Builder;

/**
 * ValueObject zum neuankgen oder ändern.
 *
 * @param wins Gewonnene Spiele
 * @param loss Verlorene Spiele
 * @param winrate Gewinnrate
 * @param lossrate Verlierrate
 * @param hotstreak Siegesserie
 * @param loosestreak Verloreneserie
 */
@Builder
public record StatsDTO(

    int wins,

    int loss,

    double winrate,

    double lossrate,

    boolean hotstreak,

    boolean loosestreak
) {
}
