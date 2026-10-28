package net.onelitefeather.vulpes.backend.domain.expression;

import com.ezylang.evalex.BaseException;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ParseException;
import io.micronaut.core.annotation.Nullable;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail.Violation;
import net.onelitefeather.vulpes.backend.exception.ApiException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parses and evaluates EvalEx expressions.
 *
 * <p>This class knows nothing about HTTP or persistence. Every failure is reported as an
 * {@link ApiException} carrying {@link Violation violations}, so the same error body is
 * produced whether the expression is checked on save or evaluated on request.
 *
 * <p>Methods without a configuration parameter use {@link #CONFIGURATION}. Project expressions pass the
 * configuration from {@link ProjectExpressionConfiguration}, which adds the project's own functions.
 */
public final class ExpressionEvaluator {

    /**
     * The EvalEx configuration every expression is parsed and evaluated with. The catalog is built from the
     * same configuration, so the functions, operators and constants offered to the UI are the ones evaluated.
     */
    public static final ExpressionConfiguration CONFIGURATION = ExpressionConfiguration.defaultConfiguration();

    private static final Pattern VARIABLE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final String FIELD_EXPRESSION = "expression";
    private static final String FIELD_VARIABLES = "variables";
    private static final String CODE_EXPRESSION_INVALID = "EXPRESSION_INVALID";
    private static final String CODE_VARIABLE_INVALID = "VARIABLE_INVALID";

    private ExpressionEvaluator() {
    }

    /**
     * Checks that an expression parses and that its variable names are usable.
     *
     * @param expression the EvalEx formula, {@code null} or empty is accepted without parsing
     * @param variables  the variables the expression references, {@code null} means none
     * @throws ApiException {@code VALIDATION_FAILED} listing every violation found
     */
    public static void requireValidDefinition(
            @Nullable String expression,
            @Nullable List<ExpressionVariableDTO> variables
    ) {
        requireValidDefinition(expression, variables, CONFIGURATION);
    }

    /**
     * Checks that an expression parses with the given configuration and that its variable names are usable.
     *
     * @param expression    the EvalEx formula, {@code null} or empty is accepted without parsing
     * @param variables     the variables the expression references, {@code null} means none
     * @param configuration the EvalEx configuration the expression is parsed with
     * @throws ApiException {@code VALIDATION_FAILED} listing every violation found
     */
    public static void requireValidDefinition(
            @Nullable String expression,
            @Nullable List<ExpressionVariableDTO> variables,
            ExpressionConfiguration configuration
    ) {
        List<Violation> violations = new ArrayList<>(variableNameViolations(FIELD_VARIABLES, variables));
        violations.addAll(syntaxViolations(expression, configuration));
        if (!violations.isEmpty()) {
            throw ApiException.validationFailed(violations);
        }
    }

    /**
     * Evaluates an expression with the given variable values.
     *
     * @param expression the EvalEx formula, {@code null} is treated as empty
     * @param values     the value of each variable as text, keyed by variable name
     * @param types      the type of each variable, a variable without an entry is a NUMBER
     * @return the result as text together with its EvalEx data type
     * @throws ApiException {@code VALIDATION_FAILED} if a name, a value, the expression or the evaluation is invalid
     */
    public static ExpressionEvaluationResultDTO evaluate(
            @Nullable String expression,
            Map<String, String> values,
            Map<String, ExpressionVariableType> types
    ) {
        return evaluate(expression, values, types, CONFIGURATION);
    }

    /**
     * Evaluates an expression with the given variable values and EvalEx configuration.
     *
     * @param expression    the EvalEx formula, {@code null} is treated as empty
     * @param values        the value of each variable as text, keyed by variable name, a {@code null} value is
     *                      a missing value
     * @param types         the type of each variable, a variable without an entry is a NUMBER
     * @param configuration the EvalEx configuration the expression is parsed and evaluated with
     * @return the result as text together with its EvalEx data type
     * @throws ApiException {@code VALIDATION_FAILED} if a name, a value, the expression or the evaluation is invalid
     */
    public static ExpressionEvaluationResultDTO evaluate(
            @Nullable String expression,
            Map<String, String> values,
            Map<String, ExpressionVariableType> types,
            ExpressionConfiguration configuration
    ) {
        List<Violation> violations = new ArrayList<>();
        Map<String, Object> converted = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String name = entry.getKey();
            if (!isValidName(name)) {
                violations.add(new Violation(FIELD_VARIABLES, CODE_VARIABLE_INVALID, invalidNameMessage(name)));
                continue;
            }
            ExpressionVariableType type = types.getOrDefault(name, ExpressionVariableType.NUMBER);
            try {
                converted.put(name, convert(type, entry.getValue()));
            } catch (IllegalArgumentException invalidValue) {
                violations.add(new Violation(
                        FIELD_VARIABLES + "." + name, CODE_VARIABLE_INVALID, invalidValue.getMessage()));
            }
        }
        if (!violations.isEmpty()) {
            throw ApiException.validationFailed(violations);
        }

        try {
            Expression evalExpression = new Expression(expression == null ? "" : expression, configuration);
            converted.forEach(evalExpression::with);
            return toResult(evalExpression.evaluate());
        } catch (ParseException | EvaluationException failure) {
            throw ApiException.validationFailed(List.of(expressionViolation(FIELD_EXPRESSION, failure)));
        }
    }

    /**
     * Lists a violation for every variable whose name is invalid or used more than once.
     *
     * @param field     the field the violations are reported on, {@code variables} or {@code parameters}
     * @param variables the variables to check, may be {@code null}
     * @return the violations, empty when all names are usable
     */
    static List<Violation> variableNameViolations(String field, @Nullable List<ExpressionVariableDTO> variables) {
        List<Violation> violations = new ArrayList<>();
        if (variables == null) {
            return violations;
        }
        Set<String> seen = new HashSet<>();
        for (ExpressionVariableDTO variable : variables) {
            String name = variable.name();
            if (!isValidName(name)) {
                violations.add(new Violation(field, CODE_VARIABLE_INVALID, invalidNameMessage(name)));
            } else if (!seen.add(name)) {
                violations.add(new Violation(
                        field,
                        CODE_VARIABLE_INVALID,
                        "Variable '%s' is defined more than once.".formatted(name)
                ));
            }
        }
        return violations;
    }

    /**
     * Parses the expression and reports the first syntax error.
     *
     * @param expression    the EvalEx formula, {@code null} or empty yields no violation
     * @param configuration the EvalEx configuration the expression is parsed with
     * @return the violation, or an empty list when the expression parses
     */
    private static List<Violation> syntaxViolations(@Nullable String expression, ExpressionConfiguration configuration) {
        if (expression == null || expression.isEmpty()) {
            return List.of();
        }
        try {
            new Expression(expression, configuration).validate();
            return List.of();
        } catch (ParseException parseException) {
            return List.of(expressionViolation(FIELD_EXPRESSION, parseException));
        }
    }

    /**
     * Converts a variable's text value to the object EvalEx computes with.
     *
     * @param type  the type the value must have
     * @param raw   the value as sent by the client, may be {@code null}
     * @return the converted value
     * @throws IllegalArgumentException with a client-facing message if the value does not fit the type
     */
    private static Object convert(ExpressionVariableType type, @Nullable String raw) {
        if (raw == null) {
            if (type == ExpressionVariableType.STRING) {
                return "";
            }
            throw new IllegalArgumentException("A value is required for this variable.");
        }
        return switch (type) {
            case NUMBER -> {
                try {
                    yield new BigDecimal(raw);
                } catch (NumberFormatException notANumber) {
                    throw new IllegalArgumentException("'%s' is not a valid number.".formatted(raw), notANumber);
                }
            }
            case BOOLEAN -> {
                if ("true".equalsIgnoreCase(raw)) {
                    yield Boolean.TRUE;
                }
                if ("false".equalsIgnoreCase(raw)) {
                    yield Boolean.FALSE;
                }
                throw new IllegalArgumentException("'%s' is not a valid boolean, use true or false.".formatted(raw));
            }
            case STRING -> raw;
        };
    }

    /**
     * Renders an EvalEx result as text together with its data type name.
     *
     * @param result the value EvalEx computed
     * @return the result DTO
     */
    private static ExpressionEvaluationResultDTO toResult(EvaluationValue result) {
        String value = switch (result.getDataType()) {
            case NUMBER -> result.getNumberValue().stripTrailingZeros().toPlainString();
            case BOOLEAN -> String.valueOf(result.getBooleanValue());
            case STRING -> result.getStringValue();
            default -> String.valueOf(result.getValue());
        };
        return new ExpressionEvaluationResultDTO(value, result.getDataType().name());
    }

    /**
     * Describes a parse or evaluation failure with the position in the expression it refers to.
     *
     * @param field   the field the violation is reported on
     * @param failure the EvalEx exception
     * @return the violation on the given field
     */
    static Violation expressionViolation(String field, BaseException failure) {
        return new Violation(
                field,
                CODE_EXPRESSION_INVALID,
                "%s (position %d-%d)".formatted(failure.getMessage(), failure.getStartPosition(), failure.getEndPosition())
        );
    }

    /**
     * Checks a variable name against {@code [A-Za-z_][A-Za-z0-9_]*}.
     *
     * @param name the name to check, {@code null} is invalid
     * @return {@code true} if the name may be used in an expression
     */
    static boolean isValidName(@Nullable String name) {
        return name != null && VARIABLE_NAME.matcher(name).matches();
    }

    private static String invalidNameMessage(@Nullable String name) {
        return "Variable name '%s' is invalid, it must match [A-Za-z_][A-Za-z0-9_]*.".formatted(name);
    }
}
