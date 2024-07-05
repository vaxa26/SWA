package com.acme.axa.service;

import lombok.Getter;

/**
 * Exception ob Username schon vorhanden.
 */
@Getter
public class UsernameExistException extends RuntimeException {

    /**
     * schon vorhandener Username.
     */
    private final String username;

    UsernameExistException(@SuppressWarnings("ParameterHidesMemberVariable") final String username) {
        super(STR."Username: \{username} already exist!");
        this.username = username;
    }
}
