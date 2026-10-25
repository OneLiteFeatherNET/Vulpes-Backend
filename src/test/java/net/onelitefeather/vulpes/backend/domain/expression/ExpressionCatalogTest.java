package net.onelitefeather.vulpes.backend.domain.expression;

import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.ConstantDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.FunctionCategory;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.FunctionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.OperatorDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.OperatorKind;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.ParameterDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ExpressionCatalog")
class ExpressionCatalogTest {

    private static final ExpressionCatalogDTO CATALOG = ExpressionCatalog.DEFAULT;

    private static FunctionDTO function(String name) {
        return CATALOG.functions().stream()
                .filter(function -> function.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("function " + name + " must be in the catalog"));
    }

    @Test
    void testDescribe_roundHasValueAndScaleParameters() {
        List<String> names = function("ROUND").parameters().stream().map(ParameterDTO::name).toList();

        assertEquals(List.of("value", "scale"), names, "ROUND must take a value and a scale, in that order");
        assertTrue(function("ROUND").parameters().stream().noneMatch(ParameterDTO::varArg),
                "ROUND must not take a variable number of arguments");
    }

    @Test
    void testDescribe_maxHasVarArgParameter() {
        assertTrue(function("MAX").parameters().stream().anyMatch(ParameterDTO::varArg),
                "MAX must declare a variable argument parameter");
    }

    @Test
    void testDescribe_roundIsBasicFunction() {
        assertEquals(FunctionCategory.BASIC, function("ROUND").category(), "ROUND lives in the basic package");
    }

    @Test
    void testDescribe_upperIsStringFunction() {
        assertEquals(FunctionCategory.STRING, function("STR_UPPER").category(), "STR_UPPER lives in the string package");
    }

    @Test
    void testDescribe_nowIsDateTimeFunction() {
        assertEquals(FunctionCategory.DATE_TIME, function("DT_NOW").category(), "DT_NOW lives in the datetime package");
    }

    @Test
    void testDescribe_sinIsTrigonometricFunction() {
        assertEquals(FunctionCategory.TRIGONOMETRIC, function("SIN").category(), "SIN lives in the trigonometric package");
    }

    @Test
    void testDescribe_functionsSortedByCategoryThenName() {
        List<FunctionDTO> functions = CATALOG.functions();
        Comparator<FunctionDTO> order = Comparator.comparing(FunctionDTO::category).thenComparing(FunctionDTO::name);

        assertEquals(functions.stream().sorted(order).toList(), functions,
                "functions must be sorted by category in enum order, then by name");
    }

    @Test
    void testDescribe_plusIsPrefixAndInfixOperator() {
        List<OperatorDTO> plus = CATALOG.operators().stream().filter(operator -> operator.symbol().equals("+")).toList();

        assertTrue(plus.stream().anyMatch(operator -> operator.kind() == OperatorKind.PREFIX),
                "+ must exist as a prefix operator");
        assertTrue(plus.stream().anyMatch(operator -> operator.kind() == OperatorKind.INFIX),
                "+ must exist as an infix operator");
    }

    @Test
    void testDescribe_infixPlusHasPrecedence20AndIsLeftAssociative() {
        OperatorDTO infixPlus = CATALOG.operators().stream()
                .filter(operator -> operator.symbol().equals("+") && operator.kind() == OperatorKind.INFIX)
                .findFirst()
                .orElseThrow();

        assertEquals(20, infixPlus.precedence(), "infix + must have precedence 20 in EvalEx 3.8.0");
        assertTrue(infixPlus.leftAssociative(), "infix + must be left associative");
    }

    @Test
    void testDescribe_containsPiAsNumberConstant() {
        ConstantDTO pi = CATALOG.constants().stream()
                .filter(constant -> constant.name().equals("PI"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("PI must be in the catalog"));

        assertEquals("NUMBER", pi.type(), "PI must be a NUMBER");
        assertTrue(pi.value().startsWith("3.14159"), "PI must carry its value as text: " + pi.value());
    }

    @Test
    void testDescribe_evalexVersionIsPinnedVersion() {
        assertEquals("3.8.0", CATALOG.evalexVersion(), "the catalog must describe the EvalEx version the backend depends on");
    }

    @Test
    void testDescribe_everyFunctionAcceptsCallWithItsArguments() {
        for (FunctionDTO function : CATALOG.functions()) {
            String call = function.name() + "(" + argumentsFor(function) + ")";

            ExpressionValidationResultDTO result = ExpressionValidator.validate(call, List.of());

            assertTrue(result.valid(), "catalog function must parse with its arguments: " + call + " " + result.errors());
        }
    }

    /**
     * Literal arguments are enough here: the check is about the parser, not the evaluation.
     */
    private static String argumentsFor(FunctionDTO function) {
        long fixed = function.parameters().stream().filter(parameter -> !parameter.varArg()).count();
        boolean hasVarArg = function.parameters().stream().anyMatch(ParameterDTO::varArg);
        int count = (int) fixed + (hasVarArg ? 1 : 0);
        return IntStream.range(0, count).mapToObj(index -> "1").collect(joining(", "));
    }
}
