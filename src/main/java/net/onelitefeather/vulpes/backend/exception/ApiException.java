package net.onelitefeather.vulpes.backend.exception;

import io.micronaut.core.annotation.Nullable;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;

import java.util.List;

/**
 * The exception every layer below the controller raises to fail a request with a defined HTTP status
 * and a defined client-facing message.
 *
 * <p>The message passed to this exception ends up verbatim in the response body, so it must be
 * authored at the throw site from data the caller already holds &mdash; an entity name, an id the
 * client itself sent. Never pass a message obtained from a lower layer: a JDBC or Hibernate message
 * carries table names, column names and SQL fragments, which is exactly the leak this type exists to
 * prevent.
 *
 * <p>When the reason for failing differs from what the client may learn, put the honest reason into
 * {@code internalDetail}. It is written to the server log and never serialized. That is how a
 * cross-project access can be answered with a plain "not found" while still being recorded as what it
 * was.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 3.0.0
 */
public class ApiException extends RuntimeException {

    private final transient ErrorCode code;
    private final transient String internalDetail;
    private final transient List<ProblemDetail.Violation> violations;
    private final transient String currentEtag;

    /**
     * Constructs a new exception carrying only the client-facing detail.
     *
     * @param code   the error code determining status, title and problem type
     * @param detail the client-facing explanation, safe to expose
     */
    public ApiException(ErrorCode code, String detail) {
        this(code, detail, null);
    }

    /**
     * Constructs a new exception with a separate, log-only explanation.
     *
     * @param code           the error code determining status, title and problem type
     * @param detail         the client-facing explanation, safe to expose
     * @param internalDetail the explanation for the server log, never sent to the client
     */
    public ApiException(ErrorCode code, String detail, @Nullable String internalDetail) {
        this(code, detail, internalDetail, List.of());
    }

    /**
     * Constructs a new exception carrying field-level violations for the response body.
     *
     * @param code           the error code determining status, title and problem type
     * @param detail         the client-facing explanation, safe to expose
     * @param internalDetail the explanation for the server log, never sent to the client
     * @param violations     the field-level violations, empty unless the code is {@code VALIDATION_FAILED}
     */
    public ApiException(
            ErrorCode code,
            String detail,
            @Nullable String internalDetail,
            List<ProblemDetail.Violation> violations
    ) {
        this(code, detail, internalDetail, violations, null);
    }

    /**
     * Constructs a new exception that also tells the client the current ETag of the addressed asset.
     *
     * @param code           the error code determining status, title and problem type
     * @param detail         the client-facing explanation, safe to expose
     * @param internalDetail the explanation for the server log, never sent to the client
     * @param violations     the field-level violations, empty unless the code is {@code VALIDATION_FAILED}
     * @param currentEtag    the current quoted ETag, sent as the ETag response header, or {@code null}
     */
    public ApiException(
            ErrorCode code,
            String detail,
            @Nullable String internalDetail,
            List<ProblemDetail.Violation> violations,
            @Nullable String currentEtag
    ) {
        super(detail);
        this.code = code;
        this.internalDetail = internalDetail;
        this.violations = List.copyOf(violations);
        this.currentEtag = currentEtag;
    }

    /**
     * Returns the error code determining the HTTP status, title and problem type of the response.
     *
     * @return the error code
     */
    public ErrorCode code() {
        return code;
    }

    /**
     * Returns the client-facing explanation.
     *
     * @return the detail
     */
    public String detail() {
        return getMessage();
    }

    /**
     * Returns the log-only explanation, or {@code null} when the client-facing detail is the whole
     * story.
     *
     * @return the internal detail or {@code null}
     */
    public @Nullable String internalDetail() {
        return internalDetail;
    }

    /**
     * Returns the field-level violations to list in the response body.
     *
     * @return the violations, empty when the failure is not a validation failure
     */
    public List<ProblemDetail.Violation> violations() {
        return violations;
    }

    /**
     * Returns the current ETag of the addressed asset, to be sent as the ETag response header.
     *
     * @return the quoted ETag or {@code null} when the failure does not concern an asset version
     */
    public @Nullable String currentEtag() {
        return currentEtag;
    }

    /**
     * The addressed resource does not exist.
     *
     * @param entityName the human-readable entity name, for example {@code Attribute}
     * @return the exception to throw
     */
    public static ApiException notFound(String entityName) {
        return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, entityName + " not found.");
    }

    /**
     * The addressed resource exists but belongs to a different project.
     *
     * <p>Answers exactly like {@link #notFound(String)} so that the response cannot be used to probe
     * for resources in projects the caller does not address, while the real reason still reaches the
     * log.
     *
     * @param entityName the human-readable entity name, for example {@code Attribute}
     * @param entityId   the identifier the client asked for
     * @param projectId  the project the client addressed
     * @return the exception to throw
     */
    public static ApiException notOwnedByProject(String entityName, Object entityId, Object projectId) {
        return notOwnedBy(entityName, entityId, "project", projectId);
    }

    /**
     * The addressed resource exists but hangs off a different parent than the one in the request path.
     *
     * <p>Answers exactly like {@link #notFound(String)} so that the response cannot be used to probe
     * for resource ids under parents the caller does not address, while the real reason still reaches
     * the log.
     *
     * @param entityName the human-readable entity name, for example {@code Lore entry}
     * @param entityId   the identifier the client asked for
     * @param ownerName  the human-readable parent name, for example {@code item}
     * @param ownerId    the parent the client addressed
     * @return the exception to throw
     */
    public static ApiException notOwnedBy(String entityName, Object entityId, String ownerName, Object ownerId) {
        return new ApiException(
                ErrorCode.RESOURCE_NOT_FOUND,
                entityName + " not found.",
                "%s %s exists but is not owned by %s %s".formatted(entityName, entityId, ownerName, ownerId)
        );
    }

    /**
     * The project addressed by the request path does not exist.
     *
     * @return the exception to throw
     */
    public static ApiException projectNotFound() {
        return new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Project not found.");
    }

    /**
     * The request is semantically unusable in a way bean validation does not cover.
     *
     * @param detail the client-facing explanation, safe to expose
     * @return the exception to throw
     */
    public static ApiException invalidRequest(String detail) {
        return new ApiException(ErrorCode.INVALID_REQUEST, detail);
    }

    /**
     * The request body or its values failed a check, and the offending fields are named.
     *
     * @param violations the field-level violations to list in the response body
     * @return the exception to throw
     */
    public static ApiException validationFailed(List<ProblemDetail.Violation> violations) {
        return new ApiException(
                ErrorCode.VALIDATION_FAILED,
                "The request contains invalid values. See 'errors' for the affected fields.",
                null,
                violations
        );
    }

    /**
     * The request conflicts with the current state of the server.
     *
     * @param detail the client-facing explanation, safe to expose
     * @return the exception to throw
     */
    public static ApiException conflict(String detail) {
        return new ApiException(ErrorCode.RESOURCE_CONFLICT, detail);
    }

    /**
     * The If-Match precondition of an asset replacement does not match the stored content.
     *
     * @param currentEtag the quoted ETag of the stored content, so the client can decide how to continue
     * @return the exception to throw
     */
    public static ApiException assetPreconditionFailed(String currentEtag) {
        return new ApiException(
                ErrorCode.ASSET_PRECONDITION_FAILED,
                "The asset was changed since the If-Match ETag was taken.",
                null,
                List.of(),
                currentEtag
        );
    }
}
