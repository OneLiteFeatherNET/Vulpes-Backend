package net.theevilreaper.vulpes.backend.exception;

import io.micronaut.http.HttpStatus;

/**
 * Represents an error object which would be sent to each client if an error occurs.
 * The object contains the status code from the {@link HttpStatus} and a message which describes the error.
 * The message can be null but the case is not recommended otherwise the client could run into the problem that he doesn't know what's wrong.
 *
 * @param httpMethod the http method which was used to trigger the error
 * @param statusCode the status code from the {@link HttpStatus}
 * @param message    the message which describes the error
 * @author theEvilReaper
 * @since 1.0.0
 */
public record ErrorResponse(String httpMethod, int statusCode, String message) {

    public ErrorResponse {
        if (httpMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("The HTTP method must not be empty");
        }
    }
}
