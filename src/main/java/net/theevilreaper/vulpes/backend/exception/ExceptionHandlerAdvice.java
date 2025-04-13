package net.theevilreaper.vulpes.backend.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import net.theevilreaper.vulpes.backend.domain.error.ErrorResponse;

/**
 * Handles each incoming exception and returns a response which contains a better overview of the exception.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@Produces
@Singleton
public class ExceptionHandlerAdvice implements ExceptionHandler<Throwable, HttpResponse<ErrorResponse>> {

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, Throwable exception) {
        return HttpResponse.notFound().body(new ErrorResponse.ErrorResponseDTO(exception.getMessage()));
    }
}