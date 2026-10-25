package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.annotation.Nullable;

import java.util.Map;

/**
 * The request body for evaluating an expression without storing it.
 *
 * @param expression the EvalEx formula
 * @param variables  the value of each variable, as text, keyed by variable name
 * @param types      the type of each variable, keyed by variable name, NUMBER when omitted
 */
@Schema(description = "An expression to evaluate with the given variable values")
@Introspected
@Serdeable
public record ExpressionEvaluateDTO(
        @Schema(description = "The EvalEx formula", requiredMode = RequiredMode.REQUIRED)
        String expression,
        @Schema(description = "Value of each variable as text, keyed by variable name", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        Map<String, String> variables,
        @Schema(description = "Type of each variable, keyed by variable name, NUMBER when omitted", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        Map<String, ExpressionVariableType> types
) {
}
