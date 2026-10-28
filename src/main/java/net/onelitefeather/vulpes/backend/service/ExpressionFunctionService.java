package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionInterfaceDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionResponseDTO;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionFunctionEntity;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing the functions a project defines for expressions.
 */
public interface ExpressionFunctionService
        extends CrudService<ExpressionFunctionEntity, UUID, ExpressionFunctionDTO, ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> {

    /**
     * Lists the stored functions of a project, unpaged.
     *
     * @param projectId the owning project
     * @return every stored function of the project, empty when the project has none
     */
    List<ExpressionFunctionDTO> definitions(UUID projectId);

    /**
     * Evaluates a function with the given argument values. The function may be unsaved, the other stored functions of
     * the project are available to it. Nothing is stored.
     *
     * @param projectId the owning project
     * @param request   the function definition and the argument values
     * @return the result as text with its data type
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException {@code VALIDATION_FAILED} if the definition is
     *         invalid, an argument does not fit its type, or the function cannot be evaluated
     */
    ExpressionEvaluationResultDTO evaluate(UUID projectId, ExpressionFunctionEvaluateDTO request);

    /**
     * Generates the Java interface for the INTERFACE functions of a project.
     *
     * @param projectId the owning project
     * @return the generated source
     */
    ExpressionFunctionInterfaceDTO interfaceSource(UUID projectId);
}
