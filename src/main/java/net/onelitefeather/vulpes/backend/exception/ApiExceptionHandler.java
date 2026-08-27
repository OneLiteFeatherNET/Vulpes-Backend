package net.onelitefeather.vulpes.backend.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turns an {@link ApiException} into the HTTP status its {@link ErrorCode} declares, with an
 * RFC 9457 body.
 *
 * <p>This is the only place that decides how much of a failure the caller gets to see. The response
 * carries the detail authored at the throw site; the honest reason, the stack trace and the
 * correlation id go to the log.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 3.0.0
 */
// Both types are declared so that a client asking for plain application/json still gets the
// error handled here; the response itself is always labelled application/problem+json.
@Produces({MediaType.APPLICATION_JSON_PROBLEM, MediaType.APPLICATION_JSON})
@Singleton
public class ApiExceptionHandler implements ExceptionHandler<ApiException, HttpResponse<ProblemDetail>> {

    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final ProblemDetailFactory problemDetailFactory;

    /**
     * Constructs a new handler.
     *
     * @param problemDetailFactory the factory building the response body
     */
    public ApiExceptionHandler(ProblemDetailFactory problemDetailFactory) {
        this.problemDetailFactory = problemDetailFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse<ProblemDetail> handle(HttpRequest request, ApiException exception) {
        ErrorCode code = exception.code();
        String traceId = problemDetailFactory.currentTraceId();
        log(request, exception, code, traceId);
        return HttpResponse.<ProblemDetail>status(code.status())
                .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
                .body(problemDetailFactory.create(code, exception.detail(), request, traceId));
    }

    /**
     * Records the failure at a level matching what it means for the server.
     *
     * <p>A 5xx is the server's fault and gets the stack trace. An exception carrying an internal
     * detail is a case where the caller was told less than the truth, which is worth seeing in the
     * log. Everything else is an ordinary client error and stays at debug so a scanner probing for
     * ids cannot flood the log.
     *
     * @param request   the request being answered
     * @param exception the failure
     * @param code      the error code of the failure
     * @param traceId   the correlation id embedded in the response
     */
    private void log(HttpRequest<?> request, ApiException exception, ErrorCode code, String traceId) {
        if (code.status().getCode() >= 500) {
            LOG.error("{} {} failed [{}] traceId={}", request.getMethod(), request.getPath(), code, traceId, exception);
        } else if (exception.internalDetail() != null) {
            LOG.warn(
                    "{} {} rejected [{}] traceId={}: {}",
                    request.getMethod(), request.getPath(), code, traceId, exception.internalDetail()
            );
        } else if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "{} {} rejected [{}] traceId={}: {}",
                    request.getMethod(), request.getPath(), code, traceId, exception.detail()
            );
        }
    }
}
