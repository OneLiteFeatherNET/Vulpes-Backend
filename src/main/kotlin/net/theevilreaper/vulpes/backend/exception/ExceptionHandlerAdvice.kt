package net.theevilreaper.vulpes.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Handles each incoming exception and returns a [ResponseEntity] which contains a better overview over the exception.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@ControllerAdvice
class ExceptionHandlerAdvice {

    /**
     * Handles each incoming [ResourceNotFoundException] and returns a [ResponseEntity] which contains a better overview over the exception.
     * The information about the thrown exception is stored in the [ErrorResponse] object.
     * @param exception the thrown exception
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(exception: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        val message = "The resource with the id ${exception.message} was not found"
        val error = ErrorResponse(exception.httpMethod.name(), HttpStatus.NOT_FOUND.value(), message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }
}
