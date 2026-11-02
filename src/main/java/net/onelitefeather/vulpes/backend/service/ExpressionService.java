package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionResponseDTO;
import net.onelitefeather.vulpes.api.model.expression.ExpressionEntity;

import java.util.UUID;

/**
 * Service interface for managing EvalEx expressions.
 */
public interface ExpressionService extends CrudService<ExpressionEntity, UUID, ExpressionDTO, ExpressionResponseDTO.ExpressionModelDTO> {

    /**
     * Evaluates an expression of a project with the given variable values. Nothing is stored.
     *
     * @param projectId the project the expression is evaluated for
     * @param request   the expression and the variable values to evaluate it with
     * @return the result as text with its data type
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException {@code PROJECT_NOT_FOUND} if the project
     *         does not exist, {@code VALIDATION_FAILED} if the expression does not parse, cannot be evaluated, or a
     *         variable value does not fit its type
     */
    ExpressionEvaluationResultDTO evaluate(UUID projectId, ExpressionEvaluateDTO request);
}
