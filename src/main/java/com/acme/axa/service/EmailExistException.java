package com.acme.axa.service;

import lombok.Getter;

/**
 * Exception ob EMail schon vorhanden ist.
 *
 */
@Getter
public class EmailExistException extends RuntimeException {
    /**
     * schon vorhandene Email.
     */
    private final String email;

    EmailExistException(@SuppressWarnings("ParameterHidesMemberVariable") final String email) {
        super(STR."Emailadress \{email} already exist!");
        this.email = email;
    }
}
