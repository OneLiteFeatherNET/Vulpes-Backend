package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD service interface providing common persistence operations.
 *
 * @param <E>       the entity type
 * @param <ID>      the entity identifier type
 * @param <REQ>     the request DTO type
 * @param <RES>     the response DTO interface type
 * @param <SUCCESS> the concrete success response DTO type
 */
public interface CrudService<E, ID, REQ, RES, SUCCESS extends RES> {

    /**
     * Creates a new entity from the given request DTO.
     *
     * @param dto the request DTO
     * @return the created entity mapped to the success DTO
     */
    SUCCESS create(REQ dto);

    /**
     * Updates an existing entity with the data from the given request DTO.
     *
     * @param dto the request DTO
     * @return the updated entity mapped to the success DTO, or an error DTO if not found
     */
    RES update(REQ dto);

    /**
     * Deletes an entity by its identifier.
     *
     * @param id the identifier of the entity to delete
     * @return the deleted entity mapped to the success DTO, or an error DTO if not found
     */
    RES delete(ID id);

    /**
     * Deletes all entities.
     *
     * @return a list containing the result (or empty list)
     */
    List<RES> deleteAll();

    /**
     * Retrieves all entities with pagination support.
     *
     * @param pageable pagination details
     * @return a page of success DTOs
     */
    Page<SUCCESS> getAll(Pageable pageable);

    /**
     * Finds an entity by its identifier.
     *
     * @param id the identifier to look for
     * @return an optional containing the entity if present
     */
    Optional<E> findById(ID id);
}
