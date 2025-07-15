package net.onelitefeather.vulpes.backend.exception;

import io.micronaut.http.HttpMethod;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test specific cases from the ErrorResponse class")
class ErrorResponseTest {

    static Stream<Arguments> invalidResponseCreation() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, -1, "Test", "The status code is not in the valid range"),
                Arguments.of(HttpMethod.OPTIONS, 1000, "Test", "The status code is not in the valid range")
        );
    }

    @ParameterizedTest(name = "Test creation for status {0} and message {1}")
    @MethodSource("invalidResponseCreation")
    @Disabled
    void testInvalidErrorResponseCreation(HttpMethod httpMethod, int statusCode, String message, String expectedMessage) {
        String httpMethodName = httpMethod.name();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ErrorResponse.ErrorResponseDTO(message)
        );
        assertEquals(expectedMessage, exception.getMessage());
    }
}