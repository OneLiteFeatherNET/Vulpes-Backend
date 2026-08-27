package net.onelitefeather.vulpes.backend.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Unit tests for ApiExceptionHandler")
class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler(new ProblemDetailFactory());
    }

    @Test
    @DisplayName("the response status comes from the error code, not from a fixed 404")
    void handle_usesStatusOfErrorCode() {
        HttpRequest<?> request = HttpRequest.GET("/project/1/attribute");

        HttpResponse<ProblemDetail> response = handler.handle(request, ApiException.conflict("Key already taken."));

        assertEquals(409, response.getStatus().getCode());
        assertNotNull(response.body());
        assertEquals(409, response.body().status());
        assertEquals(ErrorCode.RESOURCE_CONFLICT, response.body().code());
    }

    @Test
    @DisplayName("the response is served as application/problem+json")
    void handle_setsProblemJsonContentType() {
        HttpRequest<?> request = HttpRequest.GET("/project/1/attribute");

        HttpResponse<ProblemDetail> response = handler.handle(request, ApiException.notFound("Attribute"));

        assertEquals(MediaType.APPLICATION_JSON_PROBLEM_TYPE, response.getContentType().orElse(null));
    }

    @Test
    @DisplayName("the request path is echoed as the problem instance")
    void handle_setsInstanceToRequestPath() {
        HttpRequest<?> request = HttpRequest.GET("/project/1/attribute/update");

        HttpResponse<ProblemDetail> response = handler.handle(request, ApiException.notFound("Attribute"));

        assertEquals("/project/1/attribute/update", response.body().instance());
    }

    @Test
    @DisplayName("the internal detail stays in the log and never reaches the body")
    void handle_neverSerializesInternalDetail() {
        HttpRequest<?> request = HttpRequest.GET("/project/1/attribute/delete/2");
        ApiException exception = ApiException.notOwnedByProject("Attribute", "attr-2", "project-9");

        HttpResponse<ProblemDetail> response = handler.handle(request, exception);

        ProblemDetail body = response.body();
        assertEquals(404, response.getStatus().getCode());
        assertEquals("Attribute not found.", body.detail());
        assertTrue(exception.internalDetail().contains("project-9"), "the log line keeps the real reason");
        assertTrue(body.detail().indexOf("project-9") < 0, "the caller must not learn the owning project");
    }

    @Test
    @DisplayName("every response carries a correlation id the caller can quote")
    void handle_alwaysSuppliesATraceId() {
        HttpRequest<?> request = HttpRequest.GET("/project/1/attribute");

        HttpResponse<ProblemDetail> response = handler.handle(request, ApiException.notFound("Attribute"));

        String traceId = response.body().traceId();
        assertNotNull(traceId);
        assertEquals(32, traceId.length(), "the fallback id keeps the shape of an OpenTelemetry trace id");
        assertTrue(traceId.matches("[0-9a-f]{32}"), traceId);
    }
}
