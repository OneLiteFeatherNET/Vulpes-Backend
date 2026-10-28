package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.annotation.Nullable;

import java.util.Map;

/**
 * The request body for evaluating a project function without storing it.
 *
 * @param function  the function definition to evaluate, it may not be saved yet
 * @param arguments the value of each parameter as text, keyed by parameter name
 */
@Schema(description = "A project function to evaluate with the given argument values")
@Introspected
@Serdeable
public record ExpressionFunctionEvaluateDTO(
        @Schema(description = "The function definition, it may not be saved yet", requiredMode = RequiredMode.REQUIRED)
        ExpressionFunctionDTO function,
        @Schema(description = "Value of each parameter as text, keyed by parameter name", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        Map<String, String> arguments
) {
}
