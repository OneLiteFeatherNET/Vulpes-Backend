package net.onelitefeather.vulpes.backend.domain.expression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Positions are 0-based with an exclusive end. EvalEx reports 1-based inclusive positions:
 * {@code a + * b} gives start 5 and end 5 for the {@code *} at index 4, and {@code a $ b} gives start 3
 * for the {@code $} at index 2. An unclosed bracket reports the whole expression.
 */
@DisplayName("Unit tests for ExpressionValidator")
class ExpressionValidatorTest {

    private static ExpressionValidationResultDTO.ErrorDTO singleError(ExpressionValidationResultDTO result) {
        assertFalse(result.valid(), "the expression must be reported as invalid");
        assertEquals(1, result.errors().size(), "a parse error must produce exactly one error");
        return result.errors().getFirst();
    }

    @Test
    void testValidate_unexpectedOperand_pointsAtOffendingOperator() {
        ExpressionValidationResultDTO.ErrorDTO error = singleError(ExpressionValidator.validate("a + * b", List.of("a", "b")));

        assertEquals(4, error.start(), "the error must start at the * at index 4");
        assertEquals(5, error.end(), "the error must end right after the * at index 4");
    }

    @Test
    void testValidate_unclosedBracket_spansWholeExpression() {
        ExpressionValidationResultDTO.ErrorDTO error = singleError(ExpressionValidator.validate("a * (2", List.of("a")));

        assertEquals(0, error.start(), "EvalEx reports the whole expression for an unclosed bracket");
        assertEquals(6, error.end(), "the span must end at the length of the expression");
    }

    @Test
    void testValidate_unclosedFunctionCall_spansWholeExpression() {
        ExpressionValidationResultDTO.ErrorDTO error = singleError(ExpressionValidator.validate("ROUND(a", List.of("a")));

        assertEquals(0, error.start(), "an unclosed function call must start at index 0");
        assertEquals(7, error.end(), "an unclosed function call must end at the length of the expression");
    }

    @Test
    void testValidate_unknownOperator_pointsAtOperator() {
        ExpressionValidationResultDTO.ErrorDTO error = singleError(ExpressionValidator.validate("a $ b", List.of("a", "b")));

        assertEquals(2, error.start(), "the error must start at the $ at index 2");
        assertEquals(3, error.end(), "the error must end right after the $ at index 2");
    }

    @Test
    void testValidate_parseError_reportsNoVariables() {
        ExpressionValidationResultDTO result = ExpressionValidator.validate("a * (2", List.of("a", "d"));

        assertTrue(result.usedVariables().isEmpty(), "a parse error must not report used variables");
        assertTrue(result.undefinedVariables().isEmpty(), "a parse error must not report undefined variables");
        assertTrue(result.unusedVariables().isEmpty(), "a parse error must not report declared variables as unused");
    }

    @Test
    void testValidate_parseError_carriesMessage() {
        ExpressionValidationResultDTO.ErrorDTO error = singleError(ExpressionValidator.validate("a $ b", List.of()));

        assertFalse(error.message().isBlank(), "the error must carry the EvalEx message");
    }

    @Test
    void testValidate_nullExpression_isValidWithEmptyLists() {
        ExpressionValidationResultDTO result = ExpressionValidator.validate(null, List.of("a"));

        assertTrue(result.valid(), "a null expression must be valid");
        assertTrue(result.errors().isEmpty(), "a null expression must have no errors");
        assertTrue(result.usedVariables().isEmpty(), "a null expression must use no variables");
        assertTrue(result.undefinedVariables().isEmpty(), "a null expression must have no undefined variables");
        assertTrue(result.unusedVariables().isEmpty(), "a null expression must report no unused variables");
    }

    @Test
    void testValidate_emptyExpression_isValidWithEmptyLists() {
        ExpressionValidationResultDTO result = ExpressionValidator.validate("", List.of("a"));

        assertTrue(result.valid(), "an empty expression must be valid");
        assertTrue(result.unusedVariables().isEmpty(), "an empty expression must report no unused variables");
    }

    @Test
    void testValidate_usedVariables_areSortedAndUndefinedAreReported() {
        ExpressionValidationResultDTO result = ExpressionValidator.validate("b + a * c", List.of("a", "d"));

        assertTrue(result.valid(), "the expression must parse");
        assertEquals(List.of("a", "b", "c"), result.usedVariables(), "used variables must be sorted");
        assertEquals(List.of("b", "c"), result.undefinedVariables(), "used variables that are not declared must be undefined");
        assertEquals(List.of("d"), result.unusedVariables(), "declared variables that are not used must be unused");
    }

    @Test
    void testValidate_constantIsNotUndefined() {
        ExpressionValidationResultDTO result = ExpressionValidator.validate("PI * r", List.of("r"));

        assertEquals(List.of("r"), result.usedVariables(), "a constant must not count as a used variable");
        assertTrue(result.undefinedVariables().isEmpty(), "a constant must not be reported as undefined");
    }

    @Test
    void testValidate_declaredNameDiffersOnlyInCase_isNotUndefined() {
        ExpressionValidationResultDTO result = ExpressionValidator.validate("Rate * 2", List.of("rate"));

        assertTrue(result.undefinedVariables().isEmpty(),
                "EvalEx resolves variables case-insensitively, so Rate must match the declared rate");
        assertTrue(result.unusedVariables().isEmpty(), "the declared rate must count as used");
    }

    @Test
    void testValidate_invalidDeclaredName_isIgnored() {
        ExpressionValidationResultDTO result = ExpressionValidator.validate("a + 1", List.of("two words", "a"));

        assertTrue(result.valid(), "an invalid declared name must not make the expression invalid");
        assertTrue(result.unusedVariables().isEmpty(), "an invalid declared name must not be reported as unused");
    }
}
