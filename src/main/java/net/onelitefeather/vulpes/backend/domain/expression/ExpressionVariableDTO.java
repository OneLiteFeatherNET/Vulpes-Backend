package net.onelitefeather.vulpes.backend.domain.expression;

import jakarta.validation.constraints.Size;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariableType;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.annotation.Nullable;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariable;
import net.onelitefeather.vulpes.backend.validation.ColumnLength;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;

/**
 * A variable an expression can reference, with an optional value used to test the expression.
 *
 * @param name      the variable name, matching {@code [A-Za-z_][A-Za-z0-9_]*}
 * @param type      the type the value is converted to
 * @param testValue the value used when the expression is tested, may be {@code null}
 */
@Schema(description = "A variable an expression can reference")
@Introspected
@Serdeable
public record ExpressionVariableDTO(
        @Schema(description = "Name of the variable, matching [A-Za-z_][A-Za-z0-9_]*", requiredMode = RequiredMode.REQUIRED)
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String name,
        @Schema(description = "Type the value is converted to, NUMBER when omitted", requiredMode = RequiredMode.NOT_REQUIRED)
        ExpressionVariableType type,
        @Schema(description = "Value used when the expression is tested", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String testValue
) {

    /**
     * Converts this variable to its stored form. A missing type is stored as {@link ExpressionVariableType#NUMBER}.
     *
     * @return the variable as stored with the expression
     */
    public ExpressionVariable toEntityVariable() {
        return new ExpressionVariable(name, type == null ? ExpressionVariableType.NUMBER : type, testValue);
    }
}
