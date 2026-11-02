package net.onelitefeather.vulpes.backend.domain.expression;

import net.onelitefeather.vulpes.api.model.expression.ExpressionFunctionKind;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariableType;
import com.ezylang.evalex.config.ExpressionConfiguration;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail.Violation;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for project expression functions")
class ProjectExpressionFunctionTest {

    private static ExpressionFunctionDTO formula(String name, String body, String... parameters) {
        return function(name, ExpressionFunctionKind.FORMULA, body, parameters);
    }

    private static ExpressionFunctionDTO runtime(String name, String stub, String... parameters) {
        return function(name, ExpressionFunctionKind.INTERFACE, stub, parameters);
    }

    private static ExpressionFunctionDTO function(String name, ExpressionFunctionKind kind, String body, String... parameters) {
        List<ExpressionVariableDTO> variables = Arrays.stream(parameters)
                .map(parameter -> new ExpressionVariableDTO(parameter, ExpressionVariableType.NUMBER, null))
                .toList();
        return new ExpressionFunctionDTO(null, "UI " + name, name, kind, ExpressionVariableType.NUMBER, variables, body, null);
    }

    private static String evaluate(String expression, List<ExpressionFunctionDTO> functions, Map<String, String> values) {
        ExpressionConfiguration configuration = ProjectExpressionConfiguration.of(functions);
        return ExpressionEvaluator.evaluate(expression, values, Map.of(), configuration).value();
    }

    private static List<Violation> violationsOf(Executable action) {
        ApiException exception = assertThrows(ApiException.class, action, "the evaluation must fail");
        assertEquals(ErrorCode.VALIDATION_FAILED, exception.code(), "a failed evaluation must be a VALIDATION_FAILED error");
        return exception.violations();
    }

    @Test
    void testEvaluate_formulaWithParameters_bindsArgumentsToParameters() {
        ExpressionFunctionDTO scale = formula("scale", "value * factor", "value", "factor");

        assertEquals("12", evaluate("scale(4, 3)", List.of(scale), Map.of()),
                "scale(4, 3) must bind value to 4 and factor to 3");
    }

    @Test
    void testEvaluate_formulaWithParameterFromVariable_usesVariableValue() {
        ExpressionFunctionDTO scale = formula("scale", "value * 2", "value");

        assertEquals("10", evaluate("scale(a)", List.of(scale), Map.of("a", "5")),
                "an argument taken from a variable must be passed on to the function");
    }

    @Test
    void testEvaluate_builtInFunctionStillAvailableNextToProjectFunction() {
        ExpressionFunctionDTO scale = formula("scale", "value * 2", "value");

        assertEquals("5", evaluate("MAX(scale(1), 5)", List.of(scale), Map.of()),
                "a built-in function must stay callable next to a project function");
    }

    @Test
    void testEvaluate_functionCallingAnotherFunction_resolvesNestedCall() {
        ExpressionFunctionDTO add = formula("add", "a + b", "a", "b");
        ExpressionFunctionDTO quadruple = formula("quadruple", "add(x, x) + add(x, x)", "x");

        assertEquals("12", evaluate("quadruple(3)", List.of(add, quadruple), Map.of()),
                "quadruple must call add, which must see its own arguments");
    }

    @Test
    void testEvaluate_functionNameIsCaseInsensitive() {
        ExpressionFunctionDTO scale = formula("scale", "value * 2", "value");

        assertEquals("8", evaluate("SCALE(4)", List.of(scale), Map.of()),
                "EvalEx resolves function names ignoring case");
    }

    @Test
    void testEvaluate_selfRecursiveFunction_failsWithDepthMessage() {
        ExpressionFunctionDTO loop = formula("loop", "loop(n)", "n");

        List<Violation> violations = violationsOf(() -> evaluate("loop(1)", List.of(loop), Map.of()));

        assertTrue(violations.getFirst().message().contains("Function call depth exceeded (recursive function?)"),
                "a recursive function must fail with the depth message: " + violations.getFirst().message());
    }

    @Test
    void testEvaluate_mutuallyRecursiveFunctions_failsWithDepthMessage() {
        ExpressionFunctionDTO ping = formula("ping", "pong(n)", "n");
        ExpressionFunctionDTO pong = formula("pong", "ping(n)", "n");

        List<Violation> violations = violationsOf(() -> evaluate("ping(1)", List.of(ping, pong), Map.of()));

        assertTrue(violations.getFirst().message().contains("Function call depth exceeded (recursive function?)"),
                "mutually recursive functions must fail with the depth message: " + violations.getFirst().message());
    }

    @Test
    void testEvaluate_afterRecursionFailure_nextEvaluationSucceeds() {
        ExpressionFunctionDTO loop = formula("loop", "loop(n)", "n");
        ExpressionFunctionDTO scale = formula("scale", "value * 2", "value");
        List<ExpressionFunctionDTO> functions = List.of(loop, scale);
        violationsOf(() -> evaluate("loop(1)", functions, Map.of()));

        assertEquals("6", evaluate("scale(3)", functions, Map.of()),
                "the call depth must be reset after a failed recursive evaluation");
    }

    @Test
    void testEvaluate_interfaceFunctionWithoutStub_failsWithRuntimeMessage() {
        ExpressionFunctionDTO damage = runtime("damage", null, "attack");

        List<Violation> violations = violationsOf(() -> evaluate("damage(4)", List.of(damage), Map.of()));

        assertTrue(violations.getFirst().message().contains(
                        "Function 'damage' is implemented at runtime; add a test stub to evaluate it here."),
                "an INTERFACE function without a stub must say it is implemented at runtime: " + violations.getFirst().message());
    }

    @Test
    void testEvaluate_interfaceFunctionWithStub_evaluatesStub() {
        ExpressionFunctionDTO damage = runtime("damage", "attack * 10", "attack");

        assertEquals("30", evaluate("damage(3)", List.of(damage), Map.of()),
                "an INTERFACE function with a test stub must evaluate the stub");
    }

    @Test
    void testEvaluate_formulaCallingInterfaceFunctionWithStub_usesStub() {
        ExpressionFunctionDTO damage = runtime("damage", "attack * 10", "attack");
        ExpressionFunctionDTO doubled = formula("doubled", "damage(attack) * 2", "attack");

        assertEquals("60", evaluate("doubled(3)", List.of(damage, doubled), Map.of()),
                "a formula calling an INTERFACE function must get the stub's value");
    }

    @Test
    void testEvaluate_wrongArgumentCount_isRejectedOnExpression() {
        ExpressionFunctionDTO add = formula("add", "a + b", "a", "b");

        List<Violation> violations = violationsOf(() -> evaluate("add(1)", List.of(add), Map.of()));

        assertTrue(violations.getFirst().message().contains("Not enough parameters for function"),
                "a call with too few arguments must be rejected by EvalEx: " + violations.getFirst().message());
        assertEquals("expression", violations.getFirst().field(), "the rejected call must be reported on the expression");
    }

    @Test
    void testConfiguration_withoutProjectFunctions_matchesDefaultFunctionSet() {
        ExpressionConfiguration configuration = ProjectExpressionConfiguration.of(List.of());

        assertEquals(
                ExpressionEvaluator.CONFIGURATION.getFunctionDictionary().getAvailableFunctionNames(),
                configuration.getFunctionDictionary().getAvailableFunctionNames(),
                "a project without functions must have exactly the built-in functions");
    }

    private static ExpressionFunctionDTO typed(
            String name,
            ExpressionVariableType returnType,
            String body,
            ExpressionVariableDTO... parameters
    ) {
        return new ExpressionFunctionDTO(null, "UI " + name, name, ExpressionFunctionKind.FORMULA, returnType,
                List.of(parameters), body, null);
    }

    @Test
    void testEvaluate_functionDoublingItsCallsPerLevel_failsWithCallLimit() {
        ExpressionFunctionDTO tree = formula("tree", "IF(n <= 0, 1, tree(n - 1) + tree(n - 1))", "n");

        List<Violation> violations = violationsOf(() -> evaluate("tree(30)", List.of(tree), Map.of()));

        assertTrue(violations.getFirst().message().contains("Function call limit of %d calls per evaluation exceeded."
                        .formatted(CallBudget.MAX_CALLS)),
                "a call tree that doubles per level must stop at the call limit: " + violations.getFirst().message());
    }

    @Test
    void testEvaluate_callLimitCountsAllCallsOfTheExpression() {
        ExpressionFunctionDTO tree = formula("tree", "IF(n <= 0, 1, tree(n - 1) + tree(n - 1))", "n");

        List<Violation> violations = violationsOf(
                () -> evaluate("tree(12) + tree(12) + tree(12)", List.of(tree), Map.of()));

        assertTrue(violations.getFirst().message().contains("Function call limit"),
                "calls of separate top-level calls must share one budget: " + violations.getFirst().message());
    }

    @Test
    void testEvaluate_callTreeWithinLimit_succeeds() {
        ExpressionFunctionDTO tree = formula("tree", "IF(n <= 0, 1, tree(n - 1) + tree(n - 1))", "n");

        assertEquals("1024", evaluate("tree(10)", List.of(tree), Map.of()),
                "a call tree below the call limit must still be evaluated");
    }

    @Test
    void testEvaluate_afterCallLimitFailure_nextEvaluationHasFullBudget() {
        ExpressionFunctionDTO tree = formula("tree", "IF(n <= 0, 1, tree(n - 1) + tree(n - 1))", "n");
        violationsOf(() -> evaluate("tree(30)", List.of(tree), Map.of()));

        assertEquals("1024", evaluate("tree(10)", List.of(tree), Map.of()),
                "the call budget must be released after a failed evaluation");
    }

    @Test
    void testEvaluate_returnValueOfOtherType_isRejected() {
        ExpressionFunctionDTO label = typed("label", ExpressionVariableType.NUMBER, "\"text\"");

        List<Violation> violations = violationsOf(() -> evaluate("label()", List.of(label), Map.of()));

        assertTrue(violations.getFirst().message().contains("Function 'label' must return NUMBER, but returned STRING."),
                "a NUMBER function returning text must be rejected: " + violations.getFirst().message());
    }

    @Test
    void testEvaluate_booleanReturnType_acceptsComparison() {
        ExpressionFunctionDTO positive = typed("positive", ExpressionVariableType.BOOLEAN, "value > 0",
                new ExpressionVariableDTO("value", ExpressionVariableType.NUMBER, null));

        assertEquals("true", evaluate("positive(3)", List.of(positive), Map.of()),
                "a comparison must satisfy a BOOLEAN return type");
    }

    @Test
    void testEvaluate_argumentOfOtherType_isRejected() {
        ExpressionFunctionDTO scale = formula("scale", "value * 2", "value");

        List<Violation> violations = violationsOf(() -> evaluate("scale(\"text\")", List.of(scale), Map.of()));

        assertTrue(violations.getFirst().message().contains(
                        "Function 'scale' expects NUMBER for parameter 'value', but got STRING."),
                "a text argument for a NUMBER parameter must be rejected: " + violations.getFirst().message());
    }

    @Test
    void testEvaluate_stringParameter_acceptsText() {
        ExpressionFunctionDTO shout = typed("shout", ExpressionVariableType.STRING, "text + \"!\"",
                new ExpressionVariableDTO("text", ExpressionVariableType.STRING, null));

        assertEquals("hey!", evaluate("shout(\"hey\")", List.of(shout), Map.of()),
                "a STRING parameter must accept text");
    }
}
