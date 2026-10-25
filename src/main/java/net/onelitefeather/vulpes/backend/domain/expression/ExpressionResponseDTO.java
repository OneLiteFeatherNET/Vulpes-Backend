package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Response DTO for Expression model")
@Serdeable
public interface ExpressionResponseDTO {

    /**
     * The {@link ExpressionModelDTO} represents an {@link ExpressionEntity} in responses.
     *
     * @param id               the unique identifier of the expression
     * @param uiName           the name to display in the UI
     * @param expression       the EvalEx formula
     * @param comment          an internal description of the expression
     * @param variables        the variables the expression references, in order
     * @param projectId        the unique identifier of the project this expression belongs to
     * @param creationDate     the point in time at which the expression was created
     * @param modificationDate the point in time at which the expression was last modified
     */
    @Schema(
            name = "ResponseExpressionModelDTO",
            description = "Data transfer object for Expression models"
    )
    @Serdeable
    record ExpressionModelDTO(
            @Schema(description = "Id of the Model") UUID id,
            @Schema(description = "Name to display it in the ui") String uiName,
            @Schema(description = "The EvalEx formula") String expression,
            @Schema(description = "Internal description of the expression") String comment,
            @Schema(description = "Variables the expression references") List<ExpressionVariableDTO> variables,
            @Schema(description = "ID of the project this expression belongs to") UUID projectId,
            @Schema(description = "The point in time at which the expression was created") Instant creationDate,
            @Schema(description = "The point in time at which the expression was last modified") Instant modificationDate
    ) implements ExpressionResponseDTO {

        /**
         * Creates a new {@link ExpressionModelDTO} from an {@link ExpressionEntity}.
         *
         * @param entity the entity to convert
         * @return a new ExpressionModelDTO instance
         */
        public static ExpressionModelDTO createDTO(ExpressionEntity entity) {
            List<ExpressionVariableDTO> variables = entity.getVariables().stream()
                    .map(variable -> new ExpressionVariableDTO(variable.getName(), variable.getType(), variable.getTestValue()))
                    .toList();
            return new ExpressionModelDTO(
                    entity.getId(),
                    entity.getUiName(),
                    entity.getExpression(),
                    entity.getComment(),
                    variables,
                    entity.getProject().getId(),
                    entity.getCreationDate(),
                    entity.getModificationDate()
            );
        }
    }
}
