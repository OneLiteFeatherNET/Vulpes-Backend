package net.onelitefeather.vulpes.backend.domain.expression;

import javax.lang.model.SourceVersion;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Generates the Java interface the game runtime implements for the INTERFACE functions of a project.
 *
 * <p>Every INTERFACE function becomes one abstract method, named in camelCase after the function. FORMULA functions
 * are implemented by the backend and get no method. Pure: nothing is read from or written to storage.
 */
public final class ExpressionInterfaceGenerator {

    /**
     * The language of the generated source.
     */
    static final String LANGUAGE = "java";

    /**
     * The simple name of the generated interface.
     */
    static final String TYPE_NAME = "ProjectExpressionFunctions";

    private static final String BIG_DECIMAL = "java.math.BigDecimal";

    private ExpressionInterfaceGenerator() {
    }

    /**
     * Generates the interface for the INTERFACE functions among the given ones.
     *
     * @param functions every function of the project, FORMULA functions are skipped
     * @return the generated source, methods sorted by function name
     */
    public static ExpressionFunctionInterfaceDTO generate(List<ExpressionFunctionDTO> functions) {
        List<ExpressionFunctionDTO> interfaceFunctions = functions.stream()
                .filter(function -> function.kind() == ExpressionFunctionKind.INTERFACE)
                .sorted(Comparator.comparing(ExpressionFunctionDTO::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        boolean usesBigDecimal = interfaceFunctions.stream().anyMatch(ExpressionInterfaceGenerator::usesBigDecimal);

        StringBuilder source = new StringBuilder();
        if (usesBigDecimal) {
            source.append("import ").append(BIG_DECIMAL).append(";\n\n");
        }
        source.append("/**\n")
                .append(" * Runtime implementations of the INTERFACE functions of the project.\n")
                .append(" *\n")
                .append(" * <p>Generated from the project's expression functions, do not edit by hand.\n")
                .append(" */\n")
                .append("public interface ").append(TYPE_NAME).append(" {\n");
        for (ExpressionFunctionDTO function : interfaceFunctions) {
            source.append('\n').append(method(function));
        }
        source.append("}\n");
        return new ExpressionFunctionInterfaceDTO(LANGUAGE, TYPE_NAME, source.toString());
    }

    private static boolean usesBigDecimal(ExpressionFunctionDTO function) {
        boolean returnsNumber = javaType(function.returnType()).equals("BigDecimal");
        boolean takesNumber = function.parameters() != null && function.parameters().stream()
                .anyMatch(parameter -> javaType(parameter.type()).equals("BigDecimal"));
        return returnsNumber || takesNumber;
    }

    private static String method(ExpressionFunctionDTO function) {
        StringBuilder method = new StringBuilder();
        method.append("    /**\n");
        if (function.comment() != null && !function.comment().isBlank()) {
            for (String line : function.comment().strip().split("\\R")) {
                method.append("     * ").append(javadocText(line)).append('\n');
            }
            method.append("     *\n");
        }
        method.append("     * <p>Formula identifier: {@code ").append(javadocText(function.name())).append("}\n")
                .append("     */\n")
                .append("    ").append(javaType(function.returnType())).append(' ')
                .append(methodName(function.name())).append('(');
        if (function.parameters() != null) {
            List<String> parameters = function.parameters().stream()
                    .map(parameter -> javaType(parameter.type()) + " " + parameterName(parameter.name()))
                    .toList();
            method.append(String.join(", ", parameters));
        }
        return method.append(");\n").toString();
    }

    /**
     * Maps an expression type to the Java type that carries its values.
     *
     * @param type the expression type, {@code null} means NUMBER
     * @return the simple Java type name
     */
    static String javaType(ExpressionVariableType type) {
        if (type == null) {
            return "BigDecimal";
        }
        return switch (type) {
            case NUMBER -> "BigDecimal";
            case BOOLEAN -> "boolean";
            case STRING -> "String";
        };
    }

    /**
     * Converts a function name to a camelCase Java method name. Underscores separate words, each word after the
     * first starts with a capital letter, and the rest of each word is lower case.
     *
     * @param functionName the function name, matching {@code [A-Za-z_][A-Za-z0-9_]*}
     * @return a method name that is a legal Java identifier
     */
    static String methodName(String functionName) {
        StringBuilder name = new StringBuilder();
        for (String word : functionName.split("_+")) {
            if (word.isEmpty()) {
                continue;
            }
            String lower = word.toLowerCase(Locale.ROOT);
            if (name.isEmpty()) {
                name.append(lower);
            } else {
                name.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
            }
        }
        String result = name.isEmpty() ? "function" : name.toString();
        if (Character.isDigit(result.charAt(0))) {
            result = "_" + result;
        }
        return SourceVersion.isKeyword(result) ? result + "Function" : result;
    }

    private static String parameterName(String parameterName) {
        return SourceVersion.isKeyword(parameterName) ? parameterName + "_" : parameterName;
    }

    /**
     * Escapes text for a javadoc comment, so it cannot end the comment or start a tag.
     *
     * @param text the text to write into the comment
     * @return the escaped text
     */
    private static String javadocText(String text) {
        return text.replace("*/", "*&#47;").replace("@", "&#64;");
    }
}
