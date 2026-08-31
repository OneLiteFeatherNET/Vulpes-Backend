package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import net.onelitefeather.vulpes.backend.exception.ApiException;

import java.util.Optional;
import java.util.UUID;

/**
 * Generic CRUD service interface providing common persistence operations.
 *
 * <p>Every operation either succeeds and returns the response DTO, or raises an {@link ApiException}
 * naming the HTTP status and the machine-readable error code the caller should see. Failures are not
 * encoded in the return type, so a controller never has to test what it got back before answering.
 *
 * @param <E>   the entity type
 * @param <ID>  the entity identifier type
 * @param <REQ> the request DTO type
 * @param <RES> the response DTO type
 */
public interface CrudService<E, ID, REQ, RES> {

    /**
     * Creates a new entity from the given request DTO.
     *
     * @param dto the request DTO
     * @return the created entity mapped to the response DTO
     */
    RES create(REQ dto);

    /**
     * Updates an existing entity with the data from the given request DTO.
     *
     * @param dto the request DTO
     * @return the updated entity mapped to the response DTO
     * @throws ApiException {@code INVALID_REQUEST} if the DTO carries no identifier,
     *                      {@code RESOURCE_NOT_FOUND} if no entity has that identifier
     */
    RES update(REQ dto);

    /**
     * Deletes an entity by its identifier.
     *
     * @param id the identifier of the entity to delete
     * @return the deleted entity mapped to the response DTO
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if no entity has that identifier
     */
    RES delete(ID id);

    /**
     * Deletes all entities.
     */
    void deleteAll();

    /**
     * Retrieves all entities with pagination support.
     *
     * @param pageable pagination details
     * @return a page of response DTOs
     */
    Page<RES> getAll(Pageable pageable);

    /**
     * Finds an entity by its identifier.
     *
     * @param id the identifier to look for
     * @return an optional containing the entity if present
     */
    Optional<E> findById(ID id);

    /**
     * Creates a new entity from the given request DTO, scoped to a project.
     *
     * @param projectId the identifier of the owning project
     * @param dto       the request DTO
     * @return the created entity mapped to the response DTO
     * @throws ApiException {@code PROJECT_NOT_FOUND} if the project does not exist
     */
    RES create(UUID projectId, REQ dto);

    /**
     * Updates an existing entity with the data from the given request DTO, scoped to a project.
     *
     * @param projectId the identifier of the owning project
     * @param dto       the request DTO
     * @return the updated entity mapped to the response DTO
     * @throws ApiException {@code INVALID_REQUEST} if the DTO carries no identifier,
     *                      {@code RESOURCE_NOT_FOUND} if no such entity exists or it belongs to a
     *                      different project, {@code PROJECT_NOT_FOUND} if the project does not exist
     */
    RES update(UUID projectId, REQ dto);

    /**
     * Deletes an entity by its identifier, scoped to a project.
     *
     * @param projectId the identifier of the owning project
     * @param id        the identifier of the entity to delete
     * @return the deleted entity mapped to the response DTO
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if no such entity exists or it belongs to a
     *                      different project
     */
    RES delete(UUID projectId, ID id);

    /**
     * Deletes all entities belonging to a project.
     *
     * @param projectId the identifier of the owning project
     */
    void deleteAll(UUID projectId);

    /**
     * Retrieves all entities belonging to a project, with pagination support.
     *
     * @param projectId the identifier of the owning project
     * @param pageable  pagination details
     * @return a page of response DTOs belonging to the project
     */
    Page<RES> getAll(UUID projectId, Pageable pageable);

    /**
     * Finds an entity by its identifier, scoped to a project.
     *
     * @param projectId the identifier of the owning project
     * @param id        the identifier to look for
     * @return an optional containing the entity if present and owned by the project
     */
    Optional<E> findById(UUID projectId, ID id);
}
