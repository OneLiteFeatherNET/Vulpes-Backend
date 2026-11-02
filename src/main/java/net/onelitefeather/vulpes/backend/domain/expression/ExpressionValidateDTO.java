package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * The request body for checking an expression while it is being edited. Nothing is stored.
 *
 * @param expression the EvalEx formula, {@code null} or empty is valid
 * @param variables  the names of the variables declared for the expression, {@code null} means none
 */
@Schema(description = "An expression to check against its declared variables")
@Introspected
@Serdeable
public record ExpressionValidateDTO(
        @Schema(description = "The EvalEx formula to check", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        String expression,
        @Schema(description = "Names of the declared variables, names that do not match [A-Za-z_][A-Za-z0-9_]* are ignored", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        List<String> variables
) {
}
