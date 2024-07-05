package com.acme.axa.service;

import lombok.Getter;

/**
 * Excetpion, wenn Version zu alt.
 */
@Getter
public class VersionOutdatedException extends RuntimeException {
    private final int version;

    VersionOutdatedException(final int version) {
        super(STR."Version \{version} outdated.");
        this.version = version;
    }
}
