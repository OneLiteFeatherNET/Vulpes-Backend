package net.theevilreaper.vulpes.backend.exception

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import java.util.stream.Stream

@DisplayName("Test specific cases from the ErrorResponse class")
class ErrorResponseTest {

    companion object {

        @JvmStatic
        fun invalidResponseCreation(): Stream<Arguments> = Stream.of(
            Arguments.of(
                HttpMethod.GET,
                -1,
                "Test",
                "The status code is not in the valid range"
            ),
            Arguments.of(
                HttpMethod.OPTIONS,
                1000,
                "Test",
                "The status code is not in the valid range"
            ),
        )
    }

    @ParameterizedTest(name = "Test creation for status {0} and message {1}")
    @MethodSource("invalidResponseCreation")
    fun testInvalidErrorResponseCreation(
        httpMethod: HttpMethod,
        statusCode: Int,
        message: String,
        expectedMessage: String
    ) {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            ErrorResponse(httpMethod, statusCode, message)
        }
        assertEquals(expectedMessage, exception.message)
    }
}
