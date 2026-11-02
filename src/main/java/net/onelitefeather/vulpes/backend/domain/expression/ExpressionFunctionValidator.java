package net.onelitefeather.vulpes.backend.domain.expression;

import net.onelitefeather.vulpes.api.model.expression.ExpressionFunctionKind;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.ParseException;
import io.micronaut.core.annotation.Nullable;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail.Violation;
import net.onelitefeather.vulpes.backend.exception.ApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Checks a project function before it is stored or evaluated.
 *
 * <p>The name must be a valid identifier, must not be a built-in function (compared ignoring case, the way EvalEx
 * resolves names), and must not be used by another function of the project. An INTERFACE function must also not
 * generate the same Java method name as another INTERFACE function. Parameters are checked like expression
 * variables. A FORMULA function needs a body, and any body must parse with the project's functions in place.
 */
public final class ExpressionFunctionValidator {

    private static final String FIELD_NAME = "name";
    private static final String FIELD_KIND = "kind";
    private static final String FIELD_PARAMETERS = "parameters";
    private static final String FIELD_BODY = "body";
    private static final String CODE_NAME_INVALID = "FUNCTION_NAME_INVALID";
    private static final String CODE_NAME_RESERVED = "FUNCTION_NAME_RESERVED";
    private static final String CODE_NAME_TAKEN = "FUNCTION_NAME_TAKEN";
    private static final String CODE_KIND_REQUIRED = "FUNCTION_KIND_REQUIRED";
    private static final String CODE_EXPRESSION_INVALID = "EXPRESSION_INVALID";
    private static final String CODE_VARIABLE_INVALID = "VARIABLE_INVALID";

    private ExpressionFunctionValidator() {
    }

    /**
     * Checks a function against the other functions of its project.
     *
     * @param function the function to check
     * @param others   the other functions of the project, the function's own stored version must not be included
     * @throws ApiException {@code VALIDATION_FAILED} listing every violation found
     */
    public static void requireValid(ExpressionFunctionDTO function, List<ExpressionFunctionDTO> others) {
        List<Violation> violations = new ArrayList<>(nameViolations(function.name(), others));
        if (violations.isEmpty()) {
            violations.addAll(methodNameViolations(function, others));
        }
        if (function.kind() == null) {
            violations.add(new Violation(FIELD_KIND, CODE_KIND_REQUIRED, "A function needs a kind, FORMULA or INTERFACE."));
        }
        violations.addAll(ExpressionEvaluator.variableNameViolations(FIELD_PARAMETERS, function.parameters()));
        violations.addAll(constantNameViolations(function.parameters()));
        violations.addAll(bodyViolations(function, others));
        if (!violations.isEmpty()) {
            throw ApiException.validationFailed(violations);
        }
    }

    private static List<Violation> nameViolations(@Nullable String name, List<ExpressionFunctionDTO> others) {
        if (!ExpressionEvaluator.isValidName(name)) {
            return List.of(new Violation(
                    FIELD_NAME,
                    CODE_NAME_INVALID,
                    "Function name '%s' is invalid, it must match [A-Za-z_][A-Za-z0-9_]*.".formatted(name)
            ));
        }
        if (ExpressionEvaluator.CONFIGURATION.getFunctionDictionary().hasFunction(name)) {
            return List.of(new Violation(
                    FIELD_NAME,
                    CODE_NAME_RESERVED,
                    "Function name '%s' is a built-in function and cannot be used.".formatted(name)
            ));
        }
        boolean taken = others.stream()
                .map(ExpressionFunctionDTO::name)
                .anyMatch(name::equalsIgnoreCase);
        if (taken) {
            return List.of(new Violation(
                    FIELD_NAME,
                    CODE_NAME_TAKEN,
                    "Function name '%s' is already used by another function of the project.".formatted(name)
            ));
        }
        return List.of();
    }

    /**
     * Two INTERFACE functions must not map to the same method of the generated Java interface, which would not
     * compile, as {@code foo_bar} and {@code foo__bar} would.
     *
     * @param function the function to check, its name is valid
     * @param others   the other functions of the project
     * @return the violation, or an empty list when the method name is free
     */
    private static List<Violation> methodNameViolations(ExpressionFunctionDTO function, List<ExpressionFunctionDTO> others) {
        if (function.kind() != ExpressionFunctionKind.INTERFACE) {
            return List.of();
        }
        String methodName = ExpressionInterfaceGenerator.methodName(function.name());
        return others.stream()
                .filter(other -> other.kind() == ExpressionFunctionKind.INTERFACE)
                .filter(other -> ExpressionEvaluator.isValidName(other.name()))
                .filter(other -> ExpressionInterfaceGenerator.methodName(other.name()).equals(methodName))
                .findFirst()
                .map(other -> List.of(new Violation(
                        FIELD_NAME,
                        CODE_NAME_TAKEN,
                        "Function name '%s' generates the same Java method '%s' as the function '%s'."
                                .formatted(function.name(), methodName, other.name())
                )))
                .orElse(List.of());
    }

    /**
     * A parameter cannot share its name with a built-in constant, EvalEx refuses to bind a value to a constant.
     *
     * @param parameters the parameters to check, may be {@code null}
     * @return one violation per parameter named like a constant
     */
    private static List<Violation> constantNameViolations(@Nullable List<ExpressionVariableDTO> parameters) {
        if (parameters == null) {
            return List.of();
        }
        Set<String> constants = ExpressionEvaluator.CONFIGURATION.getDefaultConstants().keySet();
        return parameters.stream()
                .map(ExpressionVariableDTO::name)
                .filter(name -> ExpressionEvaluator.isValidName(name) && constants.stream().anyMatch(name::equalsIgnoreCase))
                .map(name -> new Violation(
                        FIELD_PARAMETERS,
                        CODE_VARIABLE_INVALID,
                        "Parameter '%s' is the name of a built-in constant.".formatted(name)
                ))
                .toList();
    }

    private static List<Violation> bodyViolations(ExpressionFunctionDTO function, List<ExpressionFunctionDTO> others) {
        boolean hasBody = function.body() != null && !function.body().isBlank();
        if (!hasBody) {
            if (function.kind() == ExpressionFunctionKind.FORMULA) {
                return List.of(new Violation(FIELD_BODY, CODE_EXPRESSION_INVALID, "A FORMULA function needs a body."));
            }
            return List.of();
        }
        ExpressionConfiguration configuration = ProjectExpressionConfiguration.of(withFunction(others, function));
        try {
            new Expression(function.body(), configuration).validate();
            return List.of();
        } catch (ParseException parseException) {
            return List.of(ExpressionEvaluator.expressionViolation(FIELD_BODY, parseException));
        }
    }

    /**
     * Lists the other functions with the given function added, so a body may call the function itself. A function
     * whose name is not a valid identifier is left out, it cannot be called anyway and has already been reported.
     *
     * @param others   the other functions of the project
     * @param function the function being checked
     * @return the functions the body is parsed with
     */
    public static List<ExpressionFunctionDTO> withFunction(List<ExpressionFunctionDTO> others, ExpressionFunctionDTO function) {
        List<ExpressionFunctionDTO> all = new ArrayList<>(others);
        if (ExpressionEvaluator.isValidName(function.name())) {
            all.add(function);
        }
        return all;
    }
}
