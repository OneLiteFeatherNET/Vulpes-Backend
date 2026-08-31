package net.onelitefeather.vulpes.backend.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Unit tests for ProblemErrorResponseProcessor")
class ProblemErrorResponseProcessorTest {

    private static Validator validator;

    private ProblemErrorResponseProcessor processor;

    /**
     * Stands in for a controller so that the executable validator produces the same property paths
     * Micronaut sees at runtime: {@code method.parameter.field}.
     */
    static class FakeController {
        @SuppressWarnings("unused")
        public void add(@Valid ProjectModelDTO model) {
            // never invoked; only its signature is validated
        }
    }

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setUp() {
        processor = new ProblemErrorResponseProcessor(new ProblemDetailFactory());
    }

    private MutableHttpResponse<?> responseOf(HttpStatus status) {
        return HttpResponse.status(status);
    }

    private ErrorContext contextOf(Throwable cause) {
        return ErrorContext.builder(HttpRequest.POST("/project", "{}")).cause(cause).build();
    }

    @Test
    @DisplayName("a server-side failure never echoes the exception message")
    void serverError_doesNotLeakTheCause() {
        String secret = "Duplicate entry 'abc' for key 'project.UK_project_key' in table vulpes.project";

        MutableHttpResponse<ProblemDetail> response =
                processor.processResponse(contextOf(new IllegalStateException(secret)), responseOf(HttpStatus.INTERNAL_SERVER_ERROR));

        ProblemDetail body = response.body();
        assertEquals(500, response.getStatus().getCode());
        assertEquals(ErrorCode.INTERNAL_ERROR, body.code());
        assertFalse(body.detail().contains("vulpes.project"), "the schema must not reach the client");
        assertFalse(body.detail().contains(secret));
        assertNotNull(body.traceId(), "the caller gets an id to quote instead of the cause");
    }

    @Test
    @DisplayName("every error response is served as application/problem+json")
    void anyError_isProblemJson() {
        MutableHttpResponse<ProblemDetail> response =
                processor.processResponse(contextOf(new IllegalStateException("boom")), responseOf(HttpStatus.INTERNAL_SERVER_ERROR));

        assertEquals(MediaType.APPLICATION_JSON_PROBLEM_TYPE, response.getContentType().orElse(null));
    }

    @Test
    @DisplayName("an integrity constraint violation from the driver becomes 409, not 500")
    void integrityViolation_becomesConflict() {
        SQLException driverError = new SQLException("Duplicate entry", "23000", 1062);
        Throwable wrapped = new RuntimeException("could not execute statement", driverError);

        MutableHttpResponse<ProblemDetail> response =
                processor.processResponse(contextOf(wrapped), responseOf(HttpStatus.INTERNAL_SERVER_ERROR));

        assertEquals(409, response.getStatus().getCode());
        assertEquals(ErrorCode.RESOURCE_CONFLICT, response.body().code());
        assertFalse(response.body().detail().contains("Duplicate entry"));
    }

    @Test
    @DisplayName("a cause chain that loops back on itself does not hang the processor")
    void selfReferencingCause_terminates() {
        RuntimeException looping = new RuntimeException("loop") {
            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };

        MutableHttpResponse<ProblemDetail> response =
                processor.processResponse(contextOf(looping), responseOf(HttpStatus.INTERNAL_SERVER_ERROR));

        assertEquals(ErrorCode.INTERNAL_ERROR, response.body().code());
    }

    @Test
    @DisplayName("validation failures name the rejected fields without the handler method prefix")
    void validationFailure_reportsClientFacingFieldNames() throws NoSuchMethodException {
        ExecutableValidator executableValidator = validator.forExecutables();
        Method add = FakeController.class.getMethod("add", ProjectModelDTO.class);
        ProjectModelDTO invalid = new ProjectModelDTO(null, "  ", "", null, null, null, false);
        Set<ConstraintViolation<FakeController>> violations = executableValidator.validateParameters(
                new FakeController(), add, new Object[]{invalid}, ValidationGroup.Create.class);
        assertFalse(violations.isEmpty(), "the fixture must actually violate something");

        MutableHttpResponse<ProblemDetail> response = processor.processResponse(
                contextOf(new ConstraintViolationException(violations)), responseOf(HttpStatus.BAD_REQUEST));

        ProblemDetail body = response.body();
        assertEquals(400, response.getStatus().getCode());
        assertEquals(ErrorCode.VALIDATION_FAILED, body.code());
        List<String> fields = body.errors().stream().map(ProblemDetail.Violation::field).toList();
        assertTrue(fields.contains("displayName"), "expected a violation on displayName, got " + fields);
        assertTrue(fields.contains("key"), "expected a violation on key, got " + fields);
        assertTrue(fields.stream().noneMatch(field -> field.contains("add")),
                "the handler method name must not leak into the field path: " + fields);
        assertTrue(body.errors().stream().allMatch(violation -> "NotBlank".equals(violation.code())),
                "the violated constraint is reported so the client can localize it");
    }

    @Test
    @DisplayName("an ApiException reaching the processor keeps its own status and code")
    void apiException_keepsItsCode() {
        MutableHttpResponse<ProblemDetail> response =
                processor.processResponse(contextOf(ApiException.projectNotFound()), responseOf(HttpStatus.INTERNAL_SERVER_ERROR));

        assertEquals(404, response.getStatus().getCode());
        assertEquals(ErrorCode.PROJECT_NOT_FOUND, response.body().code());
    }

    @Test
    @DisplayName("a framework client error keeps the message that names what the caller got wrong")
    void clientError_keepsFrameworkMessage() {
        ErrorContext context = ErrorContext.builder(HttpRequest.GET("/project/nope"))
                .errorMessage("Required argument [UUID projectId] not specified")
                .build();

        MutableHttpResponse<ProblemDetail> response = processor.processResponse(context, responseOf(HttpStatus.BAD_REQUEST));

        assertEquals(400, response.getStatus().getCode());
        assertEquals(ErrorCode.INVALID_REQUEST, response.body().code());
        assertEquals("Required argument [UUID projectId] not specified", response.body().detail());
    }
}
