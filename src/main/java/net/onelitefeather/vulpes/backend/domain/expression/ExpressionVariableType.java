package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The type a variable value is converted to before an expression is evaluated.
 */
@Schema(name = "ExpressionVariableType", description = "The type a variable value is converted to before evaluation")
@Serdeable
public enum ExpressionVariableType {
    NUMBER,
    BOOLEAN,
    STRING
}
