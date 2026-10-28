package net.onelitefeather.vulpes.backend.domain.expression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ExpressionInterfaceGenerator")
class ExpressionInterfaceGeneratorTest {

    private static ExpressionFunctionDTO runtime(String name, ExpressionVariableType returnType, String comment, Parameter... parameters) {
        return new ExpressionFunctionDTO(null, "UI " + name, name, ExpressionFunctionKind.INTERFACE, returnType,
                Arrays.stream(parameters)
                        .map(parameter -> new ExpressionVariableDTO(parameter.name(), parameter.type(), null))
                        .toList(),
                null, comment);
    }

    private static ExpressionFunctionDTO formula(String name) {
        return new ExpressionFunctionDTO(null, "UI " + name, name, ExpressionFunctionKind.FORMULA, ExpressionVariableType.NUMBER,
                List.of(), "1", null);
    }

    private record Parameter(String name, ExpressionVariableType type) {
    }

    @Test
    void testGenerate_interfaceFunction_becomesAbstractMethodWithMappedTypes() {
        ExpressionFunctionDTO damage = runtime("damage_bonus", ExpressionVariableType.NUMBER, null,
                new Parameter("attack", ExpressionVariableType.NUMBER),
                new Parameter("flag", ExpressionVariableType.BOOLEAN));

        String source = ExpressionInterfaceGenerator.generate(List.of(damage)).source();

        assertTrue(source.contains("    BigDecimal damageBonus(BigDecimal attack, boolean flag);"),
                "the method must map NUMBER to BigDecimal and BOOLEAN to boolean: " + source);
    }

    @Test
    void testGenerate_stringReturnType_mapsToString() {
        ExpressionFunctionDTO label = runtime("label", ExpressionVariableType.STRING, null,
                new Parameter("text", ExpressionVariableType.STRING));

        String source = ExpressionInterfaceGenerator.generate(List.of(label)).source();

        assertTrue(source.contains("    String label(String text);"),
                "STRING must map to String for parameters and return type: " + source);
    }

    @Test
    void testGenerate_formulaFunction_getsNoMethod() {
        String source = ExpressionInterfaceGenerator.generate(List.of(formula("double_it"))).source();

        assertFalse(source.contains("doubleIt"), "a FORMULA function is implemented by the backend and must get no method");
    }

    @Test
    void testGenerate_noInterfaceFunctions_declaresEmptyInterface() {
        String source = ExpressionInterfaceGenerator.generate(List.of(formula("double_it"))).source();

        assertTrue(source.contains("public interface ProjectExpressionFunctions {"), "the interface must still be declared");
        assertFalse(source.contains(");"), "an interface without INTERFACE functions must declare no method: " + source);
    }

    @Test
    void testGenerate_comment_becomesJavadocWithFormulaIdentifier() {
        ExpressionFunctionDTO damage = runtime("damage_bonus", ExpressionVariableType.NUMBER, "Bonus from gear.");

        String source = ExpressionInterfaceGenerator.generate(List.of(damage)).source();

        assertTrue(source.contains("     * Bonus from gear."), "the comment must be the javadoc text: " + source);
        assertTrue(source.contains("     * <p>Formula identifier: {@code damage_bonus}"),
                "the javadoc must name the formula identifier: " + source);
    }

    @Test
    void testGenerate_commentWithCommentEnd_cannotCloseJavadoc() {
        ExpressionFunctionDTO damage = runtime("damage", ExpressionVariableType.NUMBER, "Ends */ here, @param x");

        String source = ExpressionInterfaceGenerator.generate(List.of(damage)).source();

        assertFalse(source.contains("Ends */"), "the javadoc text must not contain a comment end: " + source);
        assertTrue(source.contains("Ends *&#47; here, &#64;param x"), "the comment end and the tag marker must be escaped: " + source);
    }

    @Test
    void testGenerate_noNumberTypes_omitsBigDecimalImport() {
        ExpressionFunctionDTO flag = runtime("is_ready", ExpressionVariableType.BOOLEAN, null,
                new Parameter("count", ExpressionVariableType.BOOLEAN));

        String source = ExpressionInterfaceGenerator.generate(List.of(flag)).source();

        assertFalse(source.contains("import java.math.BigDecimal;"), "BigDecimal must be imported only when used: " + source);
    }

    @Test
    void testGenerate_numberTypes_importBigDecimal() {
        ExpressionFunctionDTO damage = runtime("damage", ExpressionVariableType.NUMBER, null);

        String source = ExpressionInterfaceGenerator.generate(List.of(damage)).source();

        assertTrue(source.contains("import java.math.BigDecimal;"), "a NUMBER type must import BigDecimal: " + source);
    }

    @Test
    void testGenerate_result_namesLanguageAndType() {
        ExpressionFunctionInterfaceDTO result = ExpressionInterfaceGenerator.generate(List.of());

        assertEquals("java", result.language(), "the generated source must be Java");
        assertEquals("ProjectExpressionFunctions", result.typeName(), "the generated type must have the fixed name");
    }

    @Test
    void testMethodName_wordsBecomeCamelCase() {
        assertEquals("hitCount", ExpressionInterfaceGenerator.methodName("HIT_COUNT"), "upper-case words must be joined camelCase");
        assertEquals("damageBonus", ExpressionInterfaceGenerator.methodName("damage_bonus"), "lower-case words must be joined camelCase");
        assertEquals("damage", ExpressionInterfaceGenerator.methodName("damage"), "a single word must stay as it is in lower case");
    }

    @Test
    void testMethodName_javaKeyword_getsSuffix() {
        assertEquals("classFunction", ExpressionInterfaceGenerator.methodName("class"),
                "a method name that is a Java keyword must be made legal");
    }
}
