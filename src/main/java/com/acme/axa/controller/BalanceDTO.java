package com.acme.axa.controller;

import lombok.Builder;

/**
 * Object für neuanlegen und ändern.
 *
 * @param points points
 * @param credits credits
 */
@Builder
public record BalanceDTO(
    int points,

    int credits
) {
}
