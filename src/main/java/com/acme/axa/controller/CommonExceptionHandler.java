package com.acme.axa.controller;

import com.acme.axa.service.AccesForbiddenException;
import com.acme.axa.service.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Handler für allgemeine Expetions.
 */
@ControllerAdvice
@Slf4j
public class CommonExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
     void notFound(final NotFoundException response) {
        log.debug("onNotFound: {}", response.getMessage());
    }


    @ExceptionHandler
    @ResponseStatus(FORBIDDEN)
    void onAccessForbidden(final AccesForbiddenException ex) {
        log.debug("onAccessForbidden: {}", ex.getMessage());
    }
}
