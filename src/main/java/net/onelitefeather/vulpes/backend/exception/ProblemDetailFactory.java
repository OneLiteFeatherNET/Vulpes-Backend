package net.onelitefeather.vulpes.backend.exception;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;

import java.util.List;
import java.util.UUID;

/**
 * Builds {@link ProblemDetail} bodies and supplies the correlation id that ties a response to the
 * server log.
 *
 * <p>Kept as a bean rather than a static helper so both error paths &mdash; the {@link ApiException}
 * handler and the global {@link ProblemErrorResponseProcessor} &mdash; produce byte-identical bodies,
 * and so tests can substitute a deterministic correlation id.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 3.0.0
 */
@Singleton
public class ProblemDetailFactory {

    /**
     * Builds a problem detail without field-level violations.
     *
     * @param code    the error code determining status, title and problem type
     * @param detail  the client-facing explanation, which must not expose internals
     * @param request the request being answered, may be {@code null}
     * @param traceId the correlation id to embed
     * @return the problem detail
     */
    public ProblemDetail create(
            ErrorCode code,
            String detail,
            @Nullable HttpRequest<?> request,
            String traceId
    ) {
        return create(code, detail, request, traceId, List.of());
    }

    /**
     * Builds a problem detail including field-level violations.
     *
     * @param code    the error code determining status, title and problem type
     * @param detail  the client-facing explanation, which must not expose internals
     * @param request the request being answered, may be {@code null}
     * @param traceId the correlation id to embed
     * @param errors  the field-level violations
     * @return the problem detail
     */
    public ProblemDetail create(
            ErrorCode code,
            String detail,
            @Nullable HttpRequest<?> request,
            String traceId,
            List<ProblemDetail.Violation> errors
    ) {
        return ProblemDetail.of(code, detail, request == null ? null : request.getPath(), traceId, errors);
    }

    /**
     * Returns the id under which this request is findable in the log.
     *
     * <p>When tracing is active this is the OpenTelemetry trace id, so the value in the response body
     * is the same one that appears in the JSON log lines and in Tempo. With tracing disabled there is
     * no trace to join, so a random id of the same shape is minted; it still correlates the response
     * with the log line the handler writes for it.
     *
     * @return a 32 character hexadecimal correlation id
     */
    public String currentTraceId() {
        try {
            SpanContext spanContext = Span.current().getSpanContext();
            if (spanContext.isValid()) {
                return spanContext.getTraceId();
            }
        } catch (RuntimeException | LinkageError ignored) {
            // No usable tracing on this classpath. This method sits on the path of every error
            // response, so it must degrade to the fallback id rather than replace the error the
            // caller was about to be told about with one of its own.
        }
        return randomTraceId();
    }

    /**
     * Mints an id shaped like an OpenTelemetry trace id, for requests that have no trace to join.
     *
     * @return a 32 character hexadecimal id
     */
    private String randomTraceId() {
        UUID fallback = UUID.randomUUID();
        return "%016x%016x".formatted(fallback.getMostSignificantBits(), fallback.getLeastSignificantBits());
    }
}
