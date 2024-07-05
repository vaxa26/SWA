package com.acme.axa.service;

import java.net.URI;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import static com.acme.axa.controller.PlayerWriteController.PROBLEM_PATH;
import static com.acme.axa.controller.ProblemType.PRECONDITION;

/**
 * Exception falls Etag fehlt oder andere Probleme mit ETag.
 */
public class VersionInvalidException extends ErrorResponseException {
    /**
     * Umfasst Grundinformationen wenn Exception geworfen wird.
     *
     * @param statusCode statuscode
     * @param message Grund
     * @param uri uri
     */
    public VersionInvalidException(final HttpStatusCode statusCode, final String message, final URI uri) {
        this(statusCode, message, uri, null);
    }

    /**
     * Umfasst speziellere Informationen wenn Exception geworfen wird.
     *
     * @param statusCode statuscode
     * @param message nachricht
     * @param uri URI
     * @param cause Grund
     */
    public VersionInvalidException(
        final HttpStatusCode statusCode, final String message, final URI uri, final Throwable cause
    ) {
        super(statusCode, asProblemDetail(statusCode, message, uri), cause);
    }

    private static ProblemDetail asProblemDetail(final HttpStatusCode statusCode, final String detail, final URI uri) {
        final var problemDetail = ProblemDetail.forStatusAndDetail(statusCode, detail);
        problemDetail.setType(URI.create(PROBLEM_PATH + PRECONDITION.getValue()));
        problemDetail.setInstance(uri);
        return problemDetail;
    }
}
