package net.theevilreaper.vulpes.backend.exception

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

/**
 * Represents an error object which would be sent to each client if an error occurs.
 * The object contains the status code from the [HttpStatus] and a message which describes the error.
 * The message can be null but the case is not recommended otherwise the client could run into the problem that he doesn't know what's wrong.
 * @param httpMethod the http method which was used to trigger the error
 * @param statusCode the status code from the [HttpStatus]
 * @param message the message which describes the error
 * @throws IllegalArgumentException if the status code is not in the valid range
 * @see HttpStatus
 * @author theEvilReaper
 * @since 1.0.0
 */
data class ErrorResponse(val httpMethod: String, val statusCode: Int, val message: String?) {

    constructor(httpMethod: HttpMethod, statusCode: Int, message: String?) : this(
        httpMethod.name(), statusCode, message
    )

    init {
        require(httpMethod.isNotEmpty()) { "The http method can't be empty" }
        val values = HttpStatus.entries
        require(!(statusCode <= values.first().value() || statusCode >= values.last().value())) {
            "The status code is not in the valid range"
        }
    }
}