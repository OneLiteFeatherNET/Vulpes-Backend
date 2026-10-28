package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionFunctionEntity;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionVariable;

import java.util.List;
import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

/**
 * A project-defined expression function, as sent in requests and used to build the EvalEx configuration.
 *
 * @param id          the unique identifier, {@code null} on create
 * @param uiName      the name to display in the UI
 * @param name        the identifier used in formulas, matching {@code [A-Za-z_][A-Za-z0-9_]*}
 * @param kind        whether the body implements the function or the runtime does
 * @param returnType  the type of the returned value, {@code null} means NUMBER
 * @param parameters  the parameters in call order, {@code null} means none
 * @param body        the EvalEx formula, required for FORMULA, an optional test stub for INTERFACE
 * @param comment     an internal description of the function, may be {@code null}
 */
@Schema(requiredProperties = {"uiName", "name", "kind"})
@Introspected
@Serdeable
public record ExpressionFunctionDTO(
        @Schema(description = "Id of the Model", requiredMode = RequiredMode.REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Name to display it in the ui", requiredMode = RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "Identifier used in formulas, matching [A-Za-z_][A-Za-z0-9_]*, compared ignoring case", requiredMode = RequiredMode.REQUIRED)
        String name,
        @Schema(description = "FORMULA or INTERFACE", requiredMode = RequiredMode.REQUIRED)
        ExpressionFunctionKind kind,
        @Schema(description = "Type of the returned value, NUMBER when omitted", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        ExpressionVariableType returnType,
        @Schema(description = "Parameters in call order", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        List<ExpressionVariableDTO> parameters,
        @Schema(description = "The EvalEx formula, required for FORMULA; an optional test stub for INTERFACE", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        String body,
        @Schema(description = "Internal description of the function", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        String comment
) {

    /**
     * Converts this DTO to an {@link ExpressionFunctionEntity}.
     *
     * @param project the project this function belongs to
     * @return a new {@link ExpressionFunctionEntity} instance with the data from this DTO
     */
    public ExpressionFunctionEntity toEntity(ProjectEntity project) {
        List<ExpressionVariable> entityParameters = parameters == null
                ? List.of()
                : parameters.stream().map(ExpressionVariableDTO::toEntityVariable).toList();
        return new ExpressionFunctionEntity(
                id,
                uiName,
                name,
                kind,
                returnType == null ? ExpressionVariableType.NUMBER : returnType,
                body,
                comment,
                entityParameters,
                project
        );
    }

    /**
     * Creates the request-shaped view of a stored function.
     *
     * @param entity the stored function
     * @return the DTO with the same values
     */
    public static ExpressionFunctionDTO fromEntity(ExpressionFunctionEntity entity) {
        List<ExpressionVariableDTO> entityParameters = entity.getParameters().stream()
                .map(variable -> new ExpressionVariableDTO(variable.getName(), variable.getType(), variable.getTestValue()))
                .toList();
        return new ExpressionFunctionDTO(
                entity.getId(),
                entity.getUiName(),
                entity.getName(),
                entity.getKind(),
                entity.getReturnType(),
                entityParameters,
                entity.getBody(),
                entity.getComment()
        );
    }
}
