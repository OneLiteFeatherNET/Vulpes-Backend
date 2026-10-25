package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The result of an evaluated expression.
 *
 * @param value the result as text
 * @param type  the EvalEx data type of the result, for example {@code NUMBER}
 */
@Schema(description = "The result of an evaluated expression")
@Serdeable
public record ExpressionEvaluationResultDTO(
        @Schema(description = "The result as text") String value,
        @Schema(description = "The EvalEx data type of the result, for example NUMBER") String type
) {
}
