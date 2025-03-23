package net.theevilreaper.vulpes.backend.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

/**
 * Handles each incoming exception and returns a response which contains a better overview of the exception.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@Produces
@Singleton
@Requires(classes = {ResourceNotFoundException.class})
public class ExceptionHandlerAdvice implements ExceptionHandler<ResourceNotFoundException, HttpResponse<ErrorResponse>> {

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, ResourceNotFoundException exception) {
        String message = "The resource with the id " + exception.getMessage() + " was not found";
        ErrorResponse error = new ErrorResponse(
                exception.getHttpMethod().toString(),
                HttpStatus.NOT_FOUND.getCode(),
                message
        );
        return HttpResponse.notFound().body(error);
    }
}