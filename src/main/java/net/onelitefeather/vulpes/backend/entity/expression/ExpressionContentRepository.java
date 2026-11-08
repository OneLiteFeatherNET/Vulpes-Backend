package net.onelitefeather.vulpes.backend.entity.expression;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.expression.ExpressionEntity;
import net.onelitefeather.vulpes.api.model.expression.ExpressionFunctionEntity;

import java.util.List;
import java.util.UUID;

/**
 * Reads every expression and expression function of a project together with their ordered element
 * collections, so that no element collection is loaded row by row.
 */
@Repository
public interface ExpressionContentRepository extends JpaRepository<ExpressionEntity, UUID> {

    /**
     * Returns all expression functions of the project with their parameters, without paging.
     *
     * @param projectId the project of the functions
     * @return the expression functions, with their parameters in stored order
     */
    @Query("SELECT f FROM ExpressionFunctionEntity f LEFT JOIN FETCH f.parameters WHERE f.project.id = :projectId")
    List<ExpressionFunctionEntity> findFunctionsByProjectId(UUID projectId);

    /**
     * Returns all expressions of the project with their variables, without paging.
     *
     * @param projectId the project of the expressions
     * @return the expressions, with their variables in stored order
     */
    @Query("SELECT e FROM ExpressionEntity e LEFT JOIN FETCH e.variables WHERE e.project.id = :projectId")
    List<ExpressionEntity> findExpressionsByProjectId(UUID projectId);
}
