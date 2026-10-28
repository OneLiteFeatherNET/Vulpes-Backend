package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionFunctionEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Response DTO for Expression function model")
@Serdeable
public interface ExpressionFunctionResponseDTO {

    /**
     * The {@link ExpressionFunctionModelDTO} represents an {@link ExpressionFunctionEntity} in responses.
     *
     * @param id               the unique identifier of the function
     * @param uiName           the name to display in the UI
     * @param name             the identifier used in formulas
     * @param kind             whether the body implements the function or the runtime does
     * @param returnType       the type of the returned value
     * @param parameters       the parameters in call order
     * @param body             the EvalEx formula or the test stub, may be {@code null}
     * @param comment          an internal description of the function
     * @param projectId        the unique identifier of the project this function belongs to
     * @param creationDate     the point in time at which the function was created
     * @param modificationDate the point in time at which the function was last modified
     */
    @Schema(
            name = "ResponseExpressionFunctionModelDTO",
            description = "Data transfer object for expression function models"
    )
    @Serdeable
    record ExpressionFunctionModelDTO(
            @Schema(description = "Id of the Model") UUID id,
            @Schema(description = "Name to display it in the ui") String uiName,
            @Schema(description = "Identifier used in formulas") String name,
            @Schema(description = "FORMULA or INTERFACE") ExpressionFunctionKind kind,
            @Schema(description = "Type of the returned value") ExpressionVariableType returnType,
            @Schema(description = "Parameters in call order") List<ExpressionVariableDTO> parameters,
            @Schema(description = "The EvalEx formula or the test stub, may be null") String body,
            @Schema(description = "Internal description of the function") String comment,
            @Schema(description = "ID of the project this function belongs to") UUID projectId,
            @Schema(description = "The point in time at which the function was created") Instant creationDate,
            @Schema(description = "The point in time at which the function was last modified") Instant modificationDate
    ) implements ExpressionFunctionResponseDTO {

        /**
         * Creates a new {@link ExpressionFunctionModelDTO} from an {@link ExpressionFunctionEntity}.
         *
         * @param entity the entity to convert
         * @return a new ExpressionFunctionModelDTO instance
         */
        public static ExpressionFunctionModelDTO createDTO(ExpressionFunctionEntity entity) {
            List<ExpressionVariableDTO> parameters = entity.getParameters().stream()
                    .map(variable -> new ExpressionVariableDTO(variable.getName(), variable.getType(), variable.getTestValue()))
                    .toList();
            return new ExpressionFunctionModelDTO(
                    entity.getId(),
                    entity.getUiName(),
                    entity.getName(),
                    entity.getKind(),
                    entity.getReturnType(),
                    parameters,
                    entity.getBody(),
                    entity.getComment(),
                    entity.getProject().getId(),
                    entity.getCreationDate(),
                    entity.getModificationDate()
            );
        }
    }
}
