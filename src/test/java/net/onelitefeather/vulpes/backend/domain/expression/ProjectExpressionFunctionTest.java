package net.onelitefeather.vulpes.backend.domain.expression;

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
}
