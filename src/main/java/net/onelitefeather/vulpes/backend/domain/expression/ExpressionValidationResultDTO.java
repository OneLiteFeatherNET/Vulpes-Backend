package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The result of checking an expression. Always answered, also for an expression that does not parse.
 *
 * @param valid              {@code true} if the expression parses
 * @param errors             the parse error, empty when the expression is valid
 * @param usedVariables      the variables the expression uses, sorted, empty when it does not parse
 * @param undefinedVariables the used variables that are neither declared nor constants, sorted
 * @param unusedVariables    the declared variables the expression does not use, sorted
 */
@Schema(description = "The result of checking an expression against its declared variables")
@Serdeable
public record ExpressionValidationResultDTO(
        @Schema(description = "True if the expression parses") boolean valid,
        @Schema(description = "The parse error, empty when the expression is valid") List<ErrorDTO> errors,
        @Schema(description = "Variables the expression uses, sorted") List<String> usedVariables,
        @Schema(description = "Used variables that are neither declared nor constants, sorted") List<String> undefinedVariables,
        @Schema(description = "Declared variables the expression does not use, sorted") List<String> unusedVariables
) {

    /**
     * A parse error with the part of the expression it refers to.
     *
     * @param message the error message
     * @param start   the 0-based index of the first character of the faulty part
     * @param end     the 0-based index after the last character of the faulty part, exclusive
     */
    @Schema(description = "A parse error with its position in the expression")
    @Serdeable
    public record ErrorDTO(
            @Schema(description = "The error message") String message,
            @Schema(description = "0-based index of the first faulty character") int start,
            @Schema(description = "0-based index after the last faulty character, exclusive") int end
    ) {
    }
}
