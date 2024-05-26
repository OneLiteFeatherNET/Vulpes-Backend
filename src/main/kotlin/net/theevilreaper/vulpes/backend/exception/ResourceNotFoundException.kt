package net.theevilreaper.vulpes.backend.exception

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Thrown if a resource was not found in the database.
 * @param httpMethod the method which was used to access the resource
 * @param resourceId the id of the resource
 * @author theEvilReaper
 * @since 1.0.0
 * @version 1.0.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(val httpMethod: HttpMethod, resourceId: String) : RuntimeException(resourceId)
