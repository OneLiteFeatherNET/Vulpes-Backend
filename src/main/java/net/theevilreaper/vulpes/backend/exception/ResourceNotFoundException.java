package net.theevilreaper.vulpes.backend.exception;

import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.validation.constraints.NotNull;

/**
 * Thrown if a resource was not found in the database.
 * @author theEvilReaper
 * @since 1.0.0
 * @version 1.0.0
 */
public class ResourceNotFoundException extends HttpStatusException {

    private final HttpMethod httpMethod;

    /**
     * @param httpMethod the HTTP method which was used to access the resource
     * @param resourceId the id of the resource which was not found
     */
    public ResourceNotFoundException(@NotNull HttpMethod httpMethod, @NotNull String resourceId) {
        super(HttpStatus.NOT_FOUND, resourceId);
        this.httpMethod = httpMethod;
    }

    public @NotNull HttpMethod getHttpMethod() {
        return httpMethod;
    }
}