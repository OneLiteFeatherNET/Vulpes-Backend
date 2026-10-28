package net.onelitefeather.vulpes.backend.domain.expression;

import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail.Violation;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ExpressionFunctionValidator")
class ExpressionFunctionValidatorTest {

    private static ExpressionFunctionDTO function(String name, ExpressionFunctionKind kind, String body, String... parameters) {
        List<ExpressionVariableDTO> variables = Arrays.stream(parameters)
                .map(parameter -> new ExpressionVariableDTO(parameter, ExpressionVariableType.NUMBER, null))
                .toList();
        return new ExpressionFunctionDTO(null, "UI " + name, name, kind, ExpressionVariableType.NUMBER, variables, body, null);
    }

    private static List<Violation> violationsOf(Executable action) {
        ApiException exception = assertThrows(ApiException.class, action, "the function must be rejected");
        assertEquals(ErrorCode.VALIDATION_FAILED, exception.code(), "a rejected function must be a VALIDATION_FAILED error");
        return exception.violations();
    }

    private static Violation single(List<Violation> violations, String field) {
        List<Violation> onField = violations.stream().filter(violation -> violation.field().equals(field)).toList();
        assertEquals(1, onField.size(), "exactly one violation must name the field " + field + ": " + violations);
        return onField.getFirst();
    }

    @Test
    void testRequireValid_validFormula_isAccepted() {
        ExpressionFunctionDTO area = function("area", ExpressionFunctionKind.FORMULA, "width * height", "width", "height");

        assertDoesNotThrow(() -> ExpressionFunctionValidator.requireValid(area, List.of()),
                "a formula with a valid name, parameters and body must be accepted");
    }

    @Test
    void testRequireValid_invalidName_isRejectedWithNameInvalid() {
        ExpressionFunctionDTO function = function("1area", ExpressionFunctionKind.FORMULA, "1");

        Violation violation = single(violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of())), "name");

        assertEquals("FUNCTION_NAME_INVALID", violation.code(), "a name starting with a digit must be FUNCTION_NAME_INVALID");
    }

    @Test
    void testRequireValid_builtInName_isRejectedWithNameReserved() {
        ExpressionFunctionDTO function = function("round", ExpressionFunctionKind.FORMULA, "1");

        Violation violation = single(violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of())), "name");

        assertEquals("FUNCTION_NAME_RESERVED", violation.code(),
                "a built-in name must be rejected, also when the case differs");
    }

    @Test
    void testRequireValid_nameUsedByAnotherFunction_isRejectedWithNameTaken() {
        ExpressionFunctionDTO existing = function("Area", ExpressionFunctionKind.FORMULA, "1");
        ExpressionFunctionDTO function = function("area", ExpressionFunctionKind.FORMULA, "2");

        Violation violation = single(
                violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of(existing))), "name");

        assertEquals("FUNCTION_NAME_TAKEN", violation.code(),
                "a name used by another function must be FUNCTION_NAME_TAKEN, compared ignoring case");
    }

    @Test
    void testRequireValid_invalidParameterName_isRejectedOnParameters() {
        ExpressionFunctionDTO function = function("area", ExpressionFunctionKind.FORMULA, "1", "wide side");

        Violation violation = single(
                violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of())), "parameters");

        assertEquals("VARIABLE_INVALID", violation.code(), "an invalid parameter name must reuse the variable code");
    }

    @Test
    void testRequireValid_parameterNamedLikeConstant_isRejectedOnParameters() {
        ExpressionFunctionDTO function = function("circle", ExpressionFunctionKind.FORMULA, "PI * radius", "PI");

        Violation violation = single(
                violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of())), "parameters");

        assertEquals("VARIABLE_INVALID", violation.code(), "a parameter named like a constant must reuse the variable code");
    }

    @Test
    void testRequireValid_duplicateParameterName_isRejectedOnParameters() {
        ExpressionFunctionDTO function = function("area", ExpressionFunctionKind.FORMULA, "a", "a", "a");

        Violation violation = single(
                violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of())), "parameters");

        assertTrue(violation.message().contains("defined more than once"),
                "a repeated parameter must be reported as defined more than once: " + violation.message());
    }

    @Test
    void testRequireValid_formulaWithoutBody_isRejectedOnBody() {
        ExpressionFunctionDTO function = function("area", ExpressionFunctionKind.FORMULA, "  ", "width");

        Violation violation = single(violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of())), "body");

        assertEquals("EXPRESSION_INVALID", violation.code(), "a FORMULA without a body must be EXPRESSION_INVALID");
    }

    @Test
    void testRequireValid_formulaWithSyntaxError_isRejectedWithPosition() {
        ExpressionFunctionDTO function = function("area", ExpressionFunctionKind.FORMULA, "width * (height", "width", "height");

        Violation violation = single(violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of())), "body");

        assertEquals("EXPRESSION_INVALID", violation.code(), "a body that does not parse must be EXPRESSION_INVALID");
        assertTrue(violation.message().contains("(position "),
                "the message must name the position in the body: " + violation.message());
    }

    @Test
    void testRequireValid_formulaCallingBuiltInFunction_isAccepted() {
        ExpressionFunctionDTO function = function("rounded", ExpressionFunctionKind.FORMULA, "ROUND(value, 2)", "value");

        assertDoesNotThrow(() -> ExpressionFunctionValidator.requireValid(function, List.of()),
                "a body may call built-in functions");
    }

    @Test
    void testRequireValid_formulaCallingOtherProjectFunction_isAccepted() {
        ExpressionFunctionDTO add = function("add", ExpressionFunctionKind.FORMULA, "a + b", "a", "b");
        ExpressionFunctionDTO quadruple = function("quadruple", ExpressionFunctionKind.FORMULA, "add(x, x) * 2", "x");

        assertDoesNotThrow(() -> ExpressionFunctionValidator.requireValid(quadruple, List.of(add)),
                "a body may call another function of the project");
    }

    @Test
    void testRequireValid_bodyCallingUndefinedFunction_isRejectedOnBody() {
        ExpressionFunctionDTO function = function("area", ExpressionFunctionKind.FORMULA, "missing(width)", "width");

        Violation violation = single(violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of())), "body");

        assertEquals("EXPRESSION_INVALID", violation.code(), "a body calling an unknown function must be EXPRESSION_INVALID");
    }

    @Test
    void testRequireValid_formulaCallingItself_isAccepted() {
        ExpressionFunctionDTO loop = function("loop", ExpressionFunctionKind.FORMULA, "loop(n)", "n");

        assertDoesNotThrow(() -> ExpressionFunctionValidator.requireValid(loop, List.of()),
                "a body may name its own function, the recursion is caught when it is evaluated");
    }

    @Test
    void testRequireValid_missingKind_isRejectedOnKind() {
        ExpressionFunctionDTO function = function("area", null, "1");

        Violation violation = single(violationsOf(() -> ExpressionFunctionValidator.requireValid(function, List.of())), "kind");

        assertEquals("FUNCTION_KIND_REQUIRED", violation.code(), "a function without a kind must be FUNCTION_KIND_REQUIRED");
    }

    @Test
    void testRequireValid_interfaceWithoutBody_isAccepted() {
        ExpressionFunctionDTO damage = function("damage", ExpressionFunctionKind.INTERFACE, null, "attack");

        assertDoesNotThrow(() -> ExpressionFunctionValidator.requireValid(damage, List.of()),
                "an INTERFACE function needs no body, the runtime implements it");
    }

    @Test
    void testRequireValid_interfaceWithBrokenStub_isRejectedOnBody() {
        ExpressionFunctionDTO damage = function("damage", ExpressionFunctionKind.INTERFACE, "attack *", "attack");

        Violation violation = single(violationsOf(() -> ExpressionFunctionValidator.requireValid(damage, List.of())), "body");

        assertEquals("EXPRESSION_INVALID", violation.code(), "a stub that does not parse must be EXPRESSION_INVALID");
    }
}
