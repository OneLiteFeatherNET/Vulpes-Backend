package net.onelitefeather.vulpes.backend.domain.error;

import io.micronaut.http.HttpStatus;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Locale;

/**
 * Stable, machine-readable identifiers for every error this API can return.
 *
 * <p>The enum constant name is what goes over the wire as {@link ProblemDetail#code()}. Clients are
 * expected to branch and localize on it, so a constant must never be renamed without a major version
 * bump &mdash; the generated Dart client turns these into an enum.
 *
 * <p>Each constant carries the HTTP status it maps to and a short, occurrence-independent title. Both
 * are part of the response, and RFC 9457 requires the {@code status} member to match the actual HTTP
 * status of the response.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 3.0.0
 */
@Schema(
        name = "ErrorCode",
        description = "Stable, machine-readable error code. Clients branch and localize on this value."
)
@Serdeable
public enum ErrorCode {

    /**
     * The request body or its parameters failed bean validation. The offending fields are listed in
     * {@link ProblemDetail#errors()}.
     *
     * <p>Maps to 400 rather than 422 on purpose: the distinction between "malformed" and "well-formed
     * but invalid" is carried by this code, and a single status keeps generic HTTP clients simple.
     */
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),

    /**
     * The request was syntactically usable but semantically wrong in a way validation cannot express,
     * for example an update without an identifier. This signals a client bug, not a missing resource.
     */
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),

    /**
     * The addressed resource does not exist, or exists but is not owned by the project in the request
     * path. Both cases deliberately produce the identical response so that resource existence does not
     * leak across project boundaries.
     */
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),

    /**
     * The project referenced by the request path does not exist.
     */
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Project not found"),

    /**
     * The request conflicts with the current state of the server, typically a uniqueness constraint.
     */
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "Resource conflict"),

    /**
     * The HTTP method is not supported for the addressed path.
     */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),

    /**
     * The request body was sent in a media type this endpoint does not accept.
     */
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),

    /**
     * Something failed that the client cannot act on. The response never carries details about it; the
     * cause is written to the server log under {@link ProblemDetail#traceId()}.
     */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private static final String TYPE_PREFIX = "https://vulpes.onelitefeather.net/errors/";

    private final HttpStatus status;
    private final String title;

    ErrorCode(HttpStatus status, String title) {
        this.status = status;
        this.title = title;
    }

    /**
     * Returns the HTTP status this error maps to.
     *
     * @return the http status
     */
    public HttpStatus status() {
        return status;
    }

    /**
     * Returns the short, human-readable summary of this error kind. It describes the type, not the
     * single occurrence, and is therefore safe to cache or match on.
     *
     * @return the title
     */
    public String title() {
        return title;
    }

    /**
     * Returns the URI reference identifying this problem type, as required by RFC 9457. It doubles as
     * the documentation link for the error.
     *
     * @return the problem type URI
     */
    public String type() {
        return TYPE_PREFIX + name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Picks the code that best describes a response the framework produced on its own, where no
     * {@code ApiException} named one.
     *
     * <p>Anything unmapped falls back to {@link #INVALID_REQUEST} for client errors and
     * {@link #INTERNAL_ERROR} for everything else, so an unexpected status still yields a well-formed
     * problem detail rather than a second response shape.
     *
     * @param status the status the framework chose
     * @return the matching error code
     */
    public static ErrorCode fromStatus(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> RESOURCE_NOT_FOUND;
            case CONFLICT -> RESOURCE_CONFLICT;
            case METHOD_NOT_ALLOWED -> METHOD_NOT_ALLOWED;
            case UNSUPPORTED_MEDIA_TYPE -> UNSUPPORTED_MEDIA_TYPE;
            default -> status.getCode() < 500 ? INVALID_REQUEST : INTERNAL_ERROR;
        };
    }
}
