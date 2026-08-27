package net.onelitefeather.vulpes.backend.domain.error;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The single error body this API returns, shaped after RFC 9457 (Problem Details for HTTP APIs).
 *
 * <p>{@code type}, {@code title}, {@code status}, {@code detail} and {@code instance} are the members
 * defined by the RFC. {@code code}, {@code traceId} and {@code errors} are extension members, which
 * the RFC explicitly allows.
 *
 * <p>Nothing derived from an exception, a SQL statement or the persistence layer may ever reach
 * {@code detail}. It is written by the code that raises the error, from data the caller already sent
 * us. Diagnostics belong in the server log, keyed by {@code traceId}.
 *
 * @param type     URI reference identifying the problem type
 * @param title    short, occurrence-independent summary of the problem type
 * @param status   the HTTP status code, mirrored from the response
 * @param detail   human-readable explanation of this specific occurrence
 * @param instance the request path this problem occurred on
 * @param code     stable, machine-readable error code for clients to branch and localize on
 * @param traceId  identifier correlating this response with the server log
 * @param errors   field-level violations, populated only for validation failures
 * @author theEvilReaper
 * @version 1.0.0
 * @since 3.0.0
 */
@Schema(
        name = "ProblemDetail",
        description = "RFC 9457 problem details. The single error body returned by every endpoint."
)
@Serdeable
public record ProblemDetail(
        @Schema(
                description = "URI reference identifying the problem type. Doubles as the documentation link.",
                example = "https://vulpes.onelitefeather.net/errors/resource-not-found",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String type,

        @Schema(
                description = "Short summary of the problem type. Stable across occurrences.",
                example = "Resource not found",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String title,

        @Schema(
                description = "HTTP status code. Always equal to the status of the response itself.",
                example = "404",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int status,

        @Schema(
                description = "Explanation of this occurrence. Safe to display, but not localized - "
                        + "prefer rendering a message chosen by 'code'.",
                example = "Attribute not found.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String detail,

        @Schema(
                description = "The request path this problem occurred on.",
                example = "/project/6f1c.../attribute/update",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Nullable String instance,

        @Schema(
                description = "Machine-readable error code. Branch and localize on this, not on 'detail'.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        ErrorCode code,

        @Schema(
                description = "Identifier correlating this response with the server log. Show it in "
                        + "support dialogs. Equals the OpenTelemetry trace id when tracing is enabled.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Nullable String traceId,

        @Schema(
                description = "Field-level violations. Empty unless 'code' is VALIDATION_FAILED.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<Violation> errors
) {

    /**
     * Builds a problem detail without field-level violations.
     *
     * @param code     the error code, which supplies type, title and status
     * @param detail   a caller-authored explanation that must not expose internals
     * @param instance the request path, may be {@code null}
     * @param traceId  the log correlation id, may be {@code null}
     * @return the problem detail
     */
    public static ProblemDetail of(
            ErrorCode code,
            String detail,
            @Nullable String instance,
            @Nullable String traceId
    ) {
        return of(code, detail, instance, traceId, List.of());
    }

    /**
     * Builds a problem detail including field-level violations.
     *
     * @param code     the error code, which supplies type, title and status
     * @param detail   a caller-authored explanation that must not expose internals
     * @param instance the request path, may be {@code null}
     * @param traceId  the log correlation id, may be {@code null}
     * @param errors   the field-level violations
     * @return the problem detail
     */
    public static ProblemDetail of(
            ErrorCode code,
            String detail,
            @Nullable String instance,
            @Nullable String traceId,
            List<Violation> errors
    ) {
        return new ProblemDetail(
                code.type(),
                code.title(),
                code.status().getCode(),
                detail,
                instance,
                code,
                traceId,
                List.copyOf(errors)
        );
    }

    /**
     * A single field-level validation failure.
     *
     * <p>{@code field} is the client-facing property path of the rejected value, so the frontend can
     * mark the matching input. Framework-internal prefixes such as the controller method and argument
     * name are stripped before it gets here.
     *
     * @param field   the property path of the rejected value
     * @param code    the constraint that was violated, for example {@code NotBlank}
     * @param message the human-readable violation message
     */
    @Schema(
            name = "ProblemViolation",
            description = "A single field-level validation failure."
    )
    @Serdeable
    public record Violation(
            @Schema(
                    description = "Property path of the rejected value.",
                    example = "displayName",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String field,

            @Schema(
                    description = "The violated constraint.",
                    example = "NotBlank",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String code,

            @Schema(
                    description = "Human-readable violation message.",
                    example = "must not be blank",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String message
    ) {
    }
}
