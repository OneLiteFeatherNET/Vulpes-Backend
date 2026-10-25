package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidationResultDTO;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionEntity;

import java.util.UUID;

/**
 * Service interface for managing EvalEx expressions.
 */
public interface ExpressionService extends CrudService<ExpressionEntity, UUID, ExpressionDTO, ExpressionResponseDTO.ExpressionModelDTO> {

    /**
     * Evaluates an expression with the given variable values. Nothing is stored.
     *
     * @param request the expression and the variable values to evaluate it with
     * @return the result as text with its data type
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException {@code VALIDATION_FAILED} if the
     *         expression does not parse, cannot be evaluated, or a variable value does not fit its type
     */
    ExpressionEvaluationResultDTO evaluate(ExpressionEvaluateDTO request);

    /**
     * Checks an expression against its declared variables. Nothing is stored, and a parse error is part of the result.
     *
     * @param request the expression and the names of the variables declared for it
     * @return the parse result with the used, undefined and unused variables
     */
    ExpressionValidationResultDTO validate(ExpressionValidateDTO request);

    /**
     * Describes the functions, operators and constants available to expressions.
     *
     * @return the catalog of the configuration expressions are evaluated with
     */
    ExpressionCatalogDTO catalog();
}
