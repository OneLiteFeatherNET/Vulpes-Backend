package net.onelitefeather.vulpes.backend.domain.expression;

import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail.Violation;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ExpressionEvaluator")
class ExpressionEvaluatorTest {

    private static List<Violation> violationsOf(Executable action) {
        ApiException exception = assertThrows(ApiException.class, action, "Expected an ApiException");
        assertEquals(ErrorCode.VALIDATION_FAILED, exception.code(), "Expected a VALIDATION_FAILED error");
        return exception.violations();
    }

    @Test
    void testEvaluate_numberExpression_returnsNumberResult() {
        ExpressionEvaluationResultDTO result = ExpressionEvaluator.evaluate(
                "a * 2 + b",
                Map.of("a", "3", "b", "1"),
                Map.of("a", ExpressionVariableType.NUMBER, "b", ExpressionVariableType.NUMBER)
        );

        assertEquals("7", result.value(), "3 * 2 + 1 must be 7");
        assertEquals("NUMBER", result.type(), "the result of arithmetic must be a NUMBER");
    }

    @Test
    void testEvaluate_variableWithoutType_isTreatedAsNumber() {
        ExpressionEvaluationResultDTO result = ExpressionEvaluator.evaluate("a + 1", Map.of("a", "4"), Map.of());

        assertEquals("5", result.value(), "an untyped variable must be read as a number");
    }

    @Test
    void testEvaluate_booleanExpression_returnsBooleanResult() {
        ExpressionEvaluationResultDTO result = ExpressionEvaluator.evaluate(
                "flag && count > 2",
                Map.of("flag", "true", "count", "3"),
                Map.of("flag", ExpressionVariableType.BOOLEAN, "count", ExpressionVariableType.NUMBER)
        );

        assertEquals("true", result.value(), "true and 3 > 2 must be true");
        assertEquals("BOOLEAN", result.type(), "a comparison must yield a BOOLEAN");
    }

    @Test
    void testEvaluate_stringVariable_concatenatesToStringResult() {
        ExpressionEvaluationResultDTO result = ExpressionEvaluator.evaluate(
                "\"Hello \" + name",
                Map.of("name", "Ada"),
                Map.of("name", ExpressionVariableType.STRING)
        );

        assertEquals("Hello Ada", result.value(), "the string variable must be concatenated as text");
        assertEquals("STRING", result.type(), "a string concatenation must yield a STRING");
    }

    @Test
    void testEvaluate_parseError_reportsExpressionViolationWithPosition() {
        List<Violation> violations = violationsOf(() -> ExpressionEvaluator.evaluate(
                "a * (2 + b",
                Map.of("a", "3", "b", "1"),
                Map.of()
        ));

        assertEquals(1, violations.size(), "a single parse error must produce a single violation");
        assertEquals("expression", violations.getFirst().field(), "a parse error must be reported on the expression");
        assertEquals("EXPRESSION_INVALID", violations.getFirst().code(), "a parse error must carry EXPRESSION_INVALID");
        assertTrue(violations.getFirst().message().contains("(position "),
                "the message must name the position in the expression: " + violations.getFirst().message());
    }

    @Test
    void testEvaluate_undefinedVariable_reportsExpressionViolation() {
        List<Violation> violations = violationsOf(() -> ExpressionEvaluator.evaluate(
                "a + missing",
                Map.of("a", "1"),
                Map.of()
        ));

        assertEquals("expression", violations.getFirst().field(), "an undefined variable must be reported on the expression");
        assertEquals("EXPRESSION_INVALID", violations.getFirst().code(), "an undefined variable must carry EXPRESSION_INVALID");
    }

    @Test
    void testEvaluate_invalidNumber_reportsVariableViolation() {
        List<Violation> violations = violationsOf(() -> ExpressionEvaluator.evaluate(
                "a * 2",
                Map.of("a", "abc"),
                Map.of("a", ExpressionVariableType.NUMBER)
        ));

        assertEquals(1, violations.size(), "one bad value must produce one violation");
        assertEquals("variables.a", violations.getFirst().field(), "a wrong value must be reported on its variable");
        assertEquals("VARIABLE_INVALID", violations.getFirst().code(), "a wrong value must carry VARIABLE_INVALID");
    }

    @Test
    void testEvaluate_invalidBoolean_reportsVariableViolation() {
        List<Violation> violations = violationsOf(() -> ExpressionEvaluator.evaluate(
                "flag",
                Map.of("flag", "yes"),
                Map.of("flag", ExpressionVariableType.BOOLEAN)
        ));

        assertEquals("variables.flag", violations.getFirst().field(), "a value that is not a boolean must be reported on its variable");
    }

    @Test
    void testEvaluate_invalidVariableName_reportsVariablesViolation() {
        List<Violation> violations = violationsOf(() -> ExpressionEvaluator.evaluate(
                "1abc + 1",
                Map.of("1abc", "1"),
                Map.of()
        ));

        assertEquals("variables", violations.getFirst().field(), "an invalid variable name must be reported on variables");
    }

    @Test
    void testRequireValidDefinition_emptyExpression_isAccepted() {
        assertDoesNotThrow(() -> ExpressionEvaluator.requireValidDefinition("", null),
                "an empty expression is stored as is and must not be parsed");
    }

    @Test
    void testRequireValidDefinition_validExpression_isAccepted() {
        assertDoesNotThrow(() -> ExpressionEvaluator.requireValidDefinition(
                "a * 2 + b",
                List.of(new ExpressionVariableDTO("a", ExpressionVariableType.NUMBER, "3"))
        ), "a parsable expression with a valid variable must be accepted");
    }

    @Test
    void testRequireValidDefinition_parseError_reportsExpressionViolation() {
        List<Violation> violations = violationsOf(() ->
                ExpressionEvaluator.requireValidDefinition("a * (2 + b", null));

        assertEquals("expression", violations.getFirst().field(), "a parse error must be reported on the expression");
        assertEquals("EXPRESSION_INVALID", violations.getFirst().code(), "a parse error must carry EXPRESSION_INVALID");
    }

    @Test
    void testRequireValidDefinition_duplicateVariableName_reportsVariablesViolation() {
        List<Violation> violations = violationsOf(() -> ExpressionEvaluator.requireValidDefinition(
                "a",
                List.of(
                        new ExpressionVariableDTO("a", ExpressionVariableType.NUMBER, null),
                        new ExpressionVariableDTO("a", ExpressionVariableType.STRING, null)
                )
        ));

        assertEquals(1, violations.size(), "one duplicate must produce one violation");
        assertEquals("variables", violations.getFirst().field(), "a duplicate name must be reported on variables");
    }

    @Test
    void testRequireValidDefinition_invalidVariableName_reportsVariablesViolation() {
        List<Violation> violations = violationsOf(() -> ExpressionEvaluator.requireValidDefinition(
                "a",
                List.of(new ExpressionVariableDTO("two words", ExpressionVariableType.NUMBER, null))
        ));

        assertEquals("variables", violations.getFirst().field(), "a name with a space must be reported on variables");
        assertEquals("VARIABLE_INVALID", violations.getFirst().code(), "an invalid name must carry VARIABLE_INVALID");
    }
}
