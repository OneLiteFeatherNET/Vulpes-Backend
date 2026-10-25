package net.onelitefeather.vulpes.backend.entity.expression;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import java.util.UUID;

/**
 * Persistence access for {@link ExpressionEntity}.
 */
@Repository
public interface ExpressionRepository extends PageableRepository<ExpressionEntity, UUID> {

    /**
     * Finds a page of expressions belonging to a project.
     *
     * @param projectId the owning project
     * @param pageable  pagination details
     * @return the page of expressions
     */
    Page<ExpressionEntity> findByProjectId(UUID projectId, Pageable pageable);
}
