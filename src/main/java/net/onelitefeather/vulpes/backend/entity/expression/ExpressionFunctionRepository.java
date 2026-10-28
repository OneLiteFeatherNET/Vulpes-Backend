package net.onelitefeather.vulpes.backend.entity.expression;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import java.util.UUID;

/**
 * Persistence access for {@link ExpressionFunctionEntity}.
 */
@Repository
public interface ExpressionFunctionRepository extends PageableRepository<ExpressionFunctionEntity, UUID> {

    /**
     * Finds a page of expression functions belonging to a project.
     *
     * @param projectId the owning project
     * @param pageable  pagination details
     * @return the page of expression functions
     */
    Page<ExpressionFunctionEntity> findByProjectId(UUID projectId, Pageable pageable);
}
