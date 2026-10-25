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
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionEntity;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionVariable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

/**
 * The request body for creating and updating an {@link ExpressionEntity}.
 *
 * <p>{@code projectId}, {@code creationDate} and {@code modificationDate} are part of the shared JSON
 * model but are never read from a request: the project comes from the path and the dates are set by
 * the server.
 *
 * @param id               the unique identifier, {@code null} on create
 * @param uiName           the name to display in the UI
 * @param expression       the EvalEx formula, an empty string when {@code null}
 * @param comment          an internal description of the expression
 * @param variables        the variables the expression references, {@code null} means none
 * @param projectId        ignored on requests
 * @param creationDate    ignored on requests
 * @param modificationDate ignored on requests
 */
@Schema(
        requiredProperties = {
                "uiName",
                "expression"
        }
)
@Introspected
@Serdeable
public record ExpressionDTO(
        @Schema(description = "Id of the Model", requiredMode = RequiredMode.REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Name to display it in the ui", requiredMode = RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "The EvalEx formula, may be empty", requiredMode = RequiredMode.REQUIRED)
        @Nullable
        String expression,
        @Schema(description = "Internal description of the expression", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        String comment,
        @Schema(description = "Variables the expression references", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        List<ExpressionVariableDTO> variables,
        @Schema(description = "Ignored on requests, the project is taken from the path", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        UUID projectId,
        @Schema(description = "Ignored on requests, set by the server", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        Instant creationDate,
        @Schema(description = "Ignored on requests, set by the server", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        Instant modificationDate
) {
    /**
     * Converts this DTO to an {@link ExpressionEntity}.
     *
     * @param project the project this expression belongs to
     * @return a new {@link ExpressionEntity} instance with the data from this DTO
     */
    public @NotNull ExpressionEntity toEntity(ProjectEntity project) {
        List<ExpressionVariable> entityVariables = variables == null
                ? List.of()
                : variables.stream().map(ExpressionVariableDTO::toEntityVariable).toList();
        return new ExpressionEntity(
                id,
                uiName,
                expression == null ? "" : expression,
                comment,
                entityVariables,
                project
        );
    }
}
