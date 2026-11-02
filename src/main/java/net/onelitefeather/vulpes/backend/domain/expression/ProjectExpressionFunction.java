package net.onelitefeather.vulpes.backend.domain.expression;

import net.onelitefeather.vulpes.api.model.expression.ExpressionFunctionKind;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariableType;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.functions.FunctionParameterDefinition;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;

import java.util.List;

/**
 * A function the project defined, as EvalEx calls it.
 *
 * <p>A FORMULA function evaluates its body with the parameters bound to the argument values. An INTERFACE function
 * evaluates its body only when a test stub is present, otherwise the call fails, because the game runtime implements it.
 * Arguments and the returned value must have the declared types. Calls may nest, and every evaluation is limited by
 * a {@link CallBudget}, so a call chain that is too deep or makes too many calls is rejected.
 *
 * <p>The body is parsed with the configuration of the calling expression, so project functions can call built-in
 * functions and other project functions.
 */
public final class ProjectExpressionFunction implements FunctionIfc {

    private final ExpressionFunctionDTO definition;
    private final List<ExpressionVariableDTO> parameters;
    private final ExpressionVariableType returnType;
    private final List<FunctionParameterDefinition> parameterDefinitions;

    /**
     * Creates the function for a definition.
     *
     * @param definition the function as stored for the project, its name must already be validated
     */
    public ProjectExpressionFunction(ExpressionFunctionDTO definition) {
        this.definition = definition;
        this.parameters = definition.parameters() == null ? List.of() : List.copyOf(definition.parameters());
        this.returnType = definition.returnType() == null ? ExpressionVariableType.NUMBER : definition.returnType();
        this.parameterDefinitions = parameters.stream()
                .map(parameter -> FunctionParameterDefinition.builder().name(parameter.name()).build())
                .toList();
    }

    /**
     * Returns the name used in expressions.
     *
     * @return the function name
     */
    public String name() {
        return definition.name();
    }

    /**
     * Returns how the function is implemented.
     *
     * @return FORMULA or INTERFACE
     */
    public ExpressionFunctionKind kind() {
        return definition.kind();
    }

    /**
     * Returns the type of the returned value.
     *
     * @return the return type, NUMBER when the definition has none
     */
    public ExpressionVariableType returnType() {
        return returnType;
    }

    /**
     * Returns the parameters in call order, with their types.
     *
     * @return the parameters, empty when the function takes none
     */
    public List<ExpressionVariableDTO> parameters() {
        return parameters;
    }

    /**
     * Returns the description of the function.
     *
     * @return the comment, may be {@code null}
     */
    public String description() {
        return definition.comment();
    }

    /**
     * Returns the stored definition.
     *
     * @return the definition the function was built from
     */
    public ExpressionFunctionDTO definition() {
        return definition;
    }

    @Override
    public List<FunctionParameterDefinition> getFunctionParameterDefinitions() {
        return parameterDefinitions;
    }

    @Override
    public boolean hasVarArgs() {
        return false;
    }

    @Override
    public void validatePreEvaluation(Token functionToken, EvaluationValue... parameterValues) throws EvaluationException {
        requireParameterCount(functionToken, parameterValues);
    }

    /**
     * Evaluates the function body with the argument values bound to the parameters.
     *
     * @param expression      the calling expression, its configuration is used to parse the body
     * @param functionToken   the token of the call, used to report errors
     * @param parameterValues the argument values in call order
     * @return the value of the body
     * @throws EvaluationException if the argument count or an argument type is wrong, the function has no body,
     *                             the body fails or returns another type than the declared one, or the evaluation
     *                             exceeds its {@link CallBudget}
     */
    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        requireParameterCount(functionToken, parameterValues);
        String body = definition.body();
        if (body == null || body.isBlank()) {
            if (kind() == ExpressionFunctionKind.INTERFACE) {
                throw new EvaluationException(functionToken, "Function '%s' is implemented at runtime; add a test stub to evaluate it here."
                        .formatted(name()));
            }
            throw new EvaluationException(functionToken, "Function '%s' has no body.".formatted(name()));
        }

        requireParameterTypes(functionToken, parameterValues);

        EvaluationValue result;
        try (CallBudget.Scope ignored = CallBudget.open()) {
            CallBudget budget = CallBudget.current();
            budget.enter(functionToken);
            try {
                result = evaluateBody(expression, functionToken, body, parameterValues);
            } finally {
                budget.leave();
            }
        }
        if (!hasType(result, returnType)) {
            throw new EvaluationException(functionToken, "Function '%s' must return %s, but returned %s."
                    .formatted(name(), returnType, result.getDataType()));
        }
        return result;
    }

    private EvaluationValue evaluateBody(
            Expression expression,
            Token functionToken,
            String body,
            EvaluationValue... parameterValues
    ) throws EvaluationException {
        Expression bodyExpression = new Expression(body, expression.getConfiguration());
        for (int index = 0; index < parameters.size(); index++) {
            bodyExpression.with(parameters.get(index).name(), parameterValues[index]);
        }
        try {
            return bodyExpression.evaluate();
        } catch (ParseException failure) {
            throw new EvaluationException(functionToken,
                    "Function '%s' has an invalid body: %s".formatted(name(), failure.getMessage()));
        }
    }

    private void requireParameterTypes(Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        for (int index = 0; index < parameters.size(); index++) {
            ExpressionVariableDTO parameter = parameters.get(index);
            ExpressionVariableType type = parameter.type() == null ? ExpressionVariableType.NUMBER : parameter.type();
            if (!hasType(parameterValues[index], type)) {
                throw new EvaluationException(functionToken, "Function '%s' expects %s for parameter '%s', but got %s."
                        .formatted(name(), type, parameter.name(), parameterValues[index].getDataType()));
            }
        }
    }

    private static boolean hasType(EvaluationValue value, ExpressionVariableType type) {
        return switch (type) {
            case NUMBER -> value.isNumberValue();
            case BOOLEAN -> value.isBooleanValue();
            case STRING -> value.isStringValue();
        };
    }

    private void requireParameterCount(Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        if (parameterValues.length != parameters.size()) {
            throw new EvaluationException(functionToken, "Function '%s' expects %d parameter(s), but got %d."
                    .formatted(name(), parameters.size(), parameterValues.length));
        }
    }
}
