package net.onelitefeather.vulpes.backend.domain.expression;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.functions.FunctionParameterDefinition;
import com.ezylang.evalex.operators.OperatorIfc;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.ConstantDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.FunctionCategory;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.FunctionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.OperatorDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.OperatorKind;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO.ParameterDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Describes the EvalEx functions, operators and constants that {@link ExpressionEvaluator} evaluates with.
 *
 * <p>Everything is read from the configuration, nothing is listed by hand. Knows nothing about HTTP.
 */
public final class ExpressionCatalog {

    /**
     * The EvalEx version the backend is built against, used when the jar carries no implementation version.
     */
    static final String FALLBACK_EVALEX_VERSION = "3.8.0";

    /**
     * Maps the EvalEx package a function is implemented in to its category.
     */
    private static final Map<String, FunctionCategory> CATEGORIES_BY_PACKAGE = Map.of(
            "functions.basic", FunctionCategory.BASIC,
            "functions.string", FunctionCategory.STRING,
            "functions.datetime", FunctionCategory.DATE_TIME,
            "functions.trigonometric", FunctionCategory.TRIGONOMETRIC
    );

    /**
     * The catalog of {@link ExpressionEvaluator#CONFIGURATION}, built once when the class is loaded.
     */
    public static final ExpressionCatalogDTO DEFAULT = describe(ExpressionEvaluator.CONFIGURATION);

    private ExpressionCatalog() {
    }

    /**
     * Describes everything the given configuration makes available to expressions.
     *
     * @param configuration the EvalEx configuration to describe
     * @return the catalog, with functions sorted by category and name, operators by kind and symbol, constants by name
     */
    public static ExpressionCatalogDTO describe(ExpressionConfiguration configuration) {
        return new ExpressionCatalogDTO(
                evalexVersion(),
                functions(configuration.getFunctionDictionary()),
                operators(configuration),
                constants(configuration)
        );
    }

    private static String evalexVersion() {
        Package expressionPackage = Expression.class.getPackage();
        String version = expressionPackage == null ? null : expressionPackage.getImplementationVersion();
        return version == null ? FALLBACK_EVALEX_VERSION : version;
    }

    private static List<FunctionDTO> functions(FunctionDictionaryIfc dictionary) {
        List<FunctionDTO> functions = new ArrayList<>();
        for (String name : dictionary.getAvailableFunctionNames()) {
            FunctionIfc function = dictionary.getFunction(name);
            functions.add(new FunctionDTO(name, categoryOf(function), parametersOf(function)));
        }
        functions.sort(Comparator.comparing(FunctionDTO::category).thenComparing(FunctionDTO::name));
        return List.copyOf(functions);
    }

    private static FunctionCategory categoryOf(FunctionIfc function) {
        String packageName = function.getClass().getPackageName();
        return CATEGORIES_BY_PACKAGE.entrySet().stream()
                .filter(entry -> packageName.endsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(FunctionCategory.OTHER);
    }

    private static List<ParameterDTO> parametersOf(FunctionIfc function) {
        return function.getFunctionParameterDefinitions().stream()
                .map(ExpressionCatalog::parameterOf)
                .toList();
    }

    private static ParameterDTO parameterOf(FunctionParameterDefinition parameter) {
        return new ParameterDTO(
                parameter.getName(),
                parameter.isVarArg(),
                parameter.isLazy(),
                parameter.isNonZero(),
                parameter.isNonNegative()
        );
    }

    private static List<OperatorDTO> operators(ExpressionConfiguration configuration) {
        OperatorDictionaryIfc dictionary = configuration.getOperatorDictionary();
        List<OperatorDTO> operators = new ArrayList<>();
        for (String symbol : dictionary.getAvailablePrefixOperatorNames()) {
            operators.add(operatorOf(symbol, OperatorKind.PREFIX, dictionary.getPrefixOperator(symbol), configuration));
        }
        for (String symbol : dictionary.getAvailableInfixOperatorNames()) {
            operators.add(operatorOf(symbol, OperatorKind.INFIX, dictionary.getInfixOperator(symbol), configuration));
        }
        for (String symbol : dictionary.getAvailablePostfixOperatorNames()) {
            operators.add(operatorOf(symbol, OperatorKind.POSTFIX, dictionary.getPostfixOperator(symbol), configuration));
        }
        operators.sort(Comparator.comparing(OperatorDTO::kind).thenComparing(OperatorDTO::symbol));
        return List.copyOf(operators);
    }

    private static OperatorDTO operatorOf(
            String symbol,
            OperatorKind kind,
            OperatorIfc operator,
            ExpressionConfiguration configuration
    ) {
        return new OperatorDTO(symbol, kind, operator.getPrecedence(configuration), operator.isLeftAssociative());
    }

    private static List<ConstantDTO> constants(ExpressionConfiguration configuration) {
        List<ConstantDTO> constants = new ArrayList<>();
        for (Map.Entry<String, EvaluationValue> constant : configuration.getDefaultConstants().entrySet()) {
            EvaluationValue value = constant.getValue();
            constants.add(new ConstantDTO(constant.getKey(), value.getDataType().name(), valueText(value)));
        }
        constants.sort(Comparator.comparing(ConstantDTO::name));
        return List.copyOf(constants);
    }

    private static String valueText(EvaluationValue value) {
        return switch (value.getDataType()) {
            case NUMBER -> value.getNumberValue().stripTrailingZeros().toPlainString();
            case STRING -> value.getStringValue();
            default -> String.valueOf(value.getValue());
        };
    }
}
