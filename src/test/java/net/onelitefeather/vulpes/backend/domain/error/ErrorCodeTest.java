package net.onelitefeather.vulpes.backend.domain.error;

import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Unit tests for ErrorCode")
class ErrorCodeTest {

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    @DisplayName("every code exposes a dereferenceable problem type derived from its name")
    void type_isDerivedFromName(ErrorCode code) {
        String type = code.type();

        assertTrue(type.startsWith("https://vulpes.onelitefeather.net/errors/"), type);
        assertFalse(type.contains("_"), "the slug must be kebab-case, was: " + type);
        assertEquals(type.toLowerCase(java.util.Locale.ROOT), type, "the slug must be lower-case");
    }

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    @DisplayName("every code carries a non-blank title")
    void title_isPresent(ErrorCode code) {
        assertFalse(code.title().isBlank());
    }

    static Stream<Arguments> statusMappings() {
        return Stream.of(
                Arguments.of(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND),
                Arguments.of(HttpStatus.CONFLICT, ErrorCode.RESOURCE_CONFLICT),
                Arguments.of(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED),
                Arguments.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCode.UNSUPPORTED_MEDIA_TYPE),
                Arguments.of(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST),
                Arguments.of(HttpStatus.NOT_ACCEPTABLE, ErrorCode.INVALID_REQUEST),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR),
                Arguments.of(HttpStatus.BAD_GATEWAY, ErrorCode.INTERNAL_ERROR)
        );
    }

    @ParameterizedTest(name = "{0} maps to {1}")
    @MethodSource("statusMappings")
    @DisplayName("fromStatus() keeps unmapped statuses on the right side of the 4xx/5xx split")
    void fromStatus_mapsFrameworkStatuses(HttpStatus status, ErrorCode expected) {
        assertEquals(expected, ErrorCode.fromStatus(status));
    }

    @Test
    @DisplayName("validation failures answer 400, not 422, so generic clients only see one shape")
    void validationFailed_mapsToBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.status());
    }

    @Test
    @DisplayName("of() mirrors the status of its code into the body, as RFC 9457 requires")
    void problemDetail_mirrorsStatus() {
        ProblemDetail problem = ProblemDetail.of(ErrorCode.RESOURCE_NOT_FOUND, "Attribute not found.", "/attribute/1", "abc");

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.status().getCode(), problem.status());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.type(), problem.type());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.title(), problem.title());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, problem.code());
        assertEquals("/attribute/1", problem.instance());
        assertEquals("abc", problem.traceId());
        assertEquals(List.of(), problem.errors());
    }
}
