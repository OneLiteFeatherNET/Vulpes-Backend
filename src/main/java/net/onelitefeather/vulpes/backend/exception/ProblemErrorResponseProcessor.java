package net.onelitefeather.vulpes.backend.exception;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders every error response the framework itself produces as an RFC 9457 problem detail.
 *
 * <p>Micronaut funnels the bodies of all its built-in handlers through an
 * {@link ErrorResponseProcessor} &mdash; bean validation failures, unbindable path variables,
 * malformed JSON, 404 on an unrouted path, 405, 415 and anything left unhandled. The stock processor
 * is annotated {@code @Requires(missingBeans = ErrorResponseProcessor.class)}, so declaring this bean
 * replaces it and leaves the API with exactly one error shape.
 *
 * <p>Two rules keep it from leaking. Details for 5xx are a fixed constant, never the exception
 * message, because that is where JDBC and Hibernate text with table names, column names and SQL
 * fragments would surface. Details for 4xx are passed through, because at that level the message is
 * written either by the framework or out of the caller's own input.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 3.0.0
 */
@Singleton
public class ProblemErrorResponseProcessor implements ErrorResponseProcessor<ProblemDetail> {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemErrorResponseProcessor.class);

    /**
     * The only thing a client learns about a server-side failure.
     */
    private static final String INTERNAL_DETAIL = "An unexpected error occurred. "
            + "Quote the traceId when reporting this.";

    /**
     * SQLSTATE class 23 is "integrity constraint violation" and is defined by the SQL standard, so
     * matching on it works across MariaDB and PostgreSQL without reading a vendor error message.
     */
    private static final String SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION = "23";

    /**
     * Guards against a cause chain that links back to itself.
     */
    private static final int MAX_CAUSE_DEPTH = 16;

    private final ProblemDetailFactory problemDetailFactory;

    /**
     * Constructs a new processor.
     *
     * @param problemDetailFactory the factory building the response body
     */
    public ProblemErrorResponseProcessor(ProblemDetailFactory problemDetailFactory) {
        this.problemDetailFactory = problemDetailFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MutableHttpResponse<ProblemDetail> processResponse(
            ErrorContext errorContext,
            MutableHttpResponse<?> response
    ) {
        Throwable cause = errorContext.getRootCause().orElse(null);
        String traceId = problemDetailFactory.currentTraceId();
        HttpStatus status = response.status();

        ProblemDetail problem = build(errorContext, cause, status, traceId);
        return response.status(problem.code().status())
                .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
                .body(problem);
    }

    /**
     * Chooses the error code, detail and violations for this failure.
     *
     * @param errorContext the framework's view of the failure
     * @param cause        the throwable behind it, may be {@code null}
     * @param status       the status the framework chose
     * @param traceId      the correlation id embedded in the response
     * @return the response body
     */
    private ProblemDetail build(
            ErrorContext errorContext,
            @Nullable Throwable cause,
            HttpStatus status,
            String traceId
    ) {
        if (cause instanceof ApiException apiException) {
            return problemDetailFactory.create(
                    apiException.code(), apiException.detail(), errorContext.getRequest(), traceId);
        }

        if (cause instanceof ConstraintViolationException violationException) {
            return problemDetailFactory.create(
                    ErrorCode.VALIDATION_FAILED,
                    "The request contains invalid values. See 'errors' for the affected fields.",
                    errorContext.getRequest(),
                    traceId,
                    toViolations(violationException)
            );
        }

        if (isIntegrityConstraintViolation(cause)) {
            LOG.warn(
                    "{} {} violated a database constraint traceId={}",
                    errorContext.getRequest().getMethod(), errorContext.getRequest().getPath(), traceId, cause
            );
            return problemDetailFactory.create(
                    ErrorCode.RESOURCE_CONFLICT,
                    "The request conflicts with data that already exists.",
                    errorContext.getRequest(),
                    traceId
            );
        }

        if (status.getCode() >= 500) {
            if (cause != null) {
                LOG.error(
                        "{} {} failed traceId={}",
                        errorContext.getRequest().getMethod(), errorContext.getRequest().getPath(), traceId, cause
                );
            }
            return problemDetailFactory.create(
                    ErrorCode.INTERNAL_ERROR, INTERNAL_DETAIL, errorContext.getRequest(), traceId);
        }

        ErrorCode code = ErrorCode.fromStatus(status);
        return problemDetailFactory.create(code, clientDetail(errorContext, code), errorContext.getRequest(), traceId);
    }

    /**
     * Picks the detail for a client error, preferring the framework's own message because it names the
     * argument or media type the caller got wrong.
     *
     * @param errorContext the framework's view of the failure
     * @param code         the error code chosen for the response
     * @return the detail to send
     */
    private String clientDetail(ErrorContext errorContext, ErrorCode code) {
        return errorContext.getErrors().stream()
                .map(io.micronaut.http.server.exceptions.response.Error::getMessage)
                .filter(message -> message != null && !message.isBlank())
                .findFirst()
                .orElseGet(code::title);
    }

    /**
     * Flattens the violations of a failed validation into the response representation.
     *
     * @param exception the validation failure
     * @return the violations, in encounter order
     */
    private List<ProblemDetail.Violation> toViolations(ConstraintViolationException exception) {
        List<ProblemDetail.Violation> violations = new ArrayList<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            violations.add(new ProblemDetail.Violation(
                    fieldOf(violation.getPropertyPath()),
                    violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                    violation.getMessage()
            ));
        }
        return violations;
    }

    /**
     * Reduces a validation property path to the field name the client sent.
     *
     * <p>Validating a controller argument yields a path like {@code add.model.displayName}, where the
     * first two nodes are the handler method and its parameter. Those are server-side names and mean
     * nothing to a frontend trying to mark an input, so they are dropped. A constraint that sits
     * directly on a parameter leaves nothing behind, in which case the parameter name is the field.
     *
     * @param path the property path of the violation
     * @return the client-facing field name
     */
    private String fieldOf(Path path) {
        StringBuilder field = new StringBuilder();
        String lastNodeName = "";
        for (Path.Node node : path) {
            lastNodeName = node.getName() == null ? lastNodeName : node.getName();
            if (node.getKind() == ElementKind.METHOD
                    || node.getKind() == ElementKind.CONSTRUCTOR
                    || node.getKind() == ElementKind.PARAMETER
                    || node.getKind() == ElementKind.CROSS_PARAMETER
                    || node.getKind() == ElementKind.RETURN_VALUE) {
                continue;
            }
            if (!field.isEmpty()) {
                field.append('.');
            }
            field.append(node.getName());
        }
        return field.isEmpty() ? lastNodeName : field.toString();
    }

    /**
     * Walks the cause chain looking for a uniqueness or foreign key violation reported by the driver.
     *
     * @param cause the throwable to inspect, may be {@code null}
     * @return {@code true} when the failure is a database integrity constraint violation
     */
    private boolean isIntegrityConstraintViolation(@Nullable Throwable cause) {
        Throwable current = cause;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof SQLException sqlException) {
                String sqlState = sqlException.getSQLState();
                if (sqlState != null && sqlState.startsWith(SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION)) {
                    return true;
                }
            }
            if (current == current.getCause()) {
                break;
            }
            current = current.getCause();
        }
        return false;
    }
}
