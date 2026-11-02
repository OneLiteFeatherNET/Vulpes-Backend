package net.onelitefeather.vulpes.backend.domain.expression;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.functions.FunctionParameterDefinition;
import com.ezylang.evalex.functions.string.StringFormatFunction;
import com.ezylang.evalex.parser.Token;

import java.util.IllegalFormatException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EvalEx's {@code STR_FORMAT} with a bounded field width and precision.
 *
 * <p>The built-in function passes the format to {@link String#format}, so {@code STR_FORMAT("%999999999s", 1)} would
 * build a string of a billion characters. Every width and precision above {@link #MAX_WIDTH} is rejected, and an
 * invalid format is reported as an evaluation error instead of escaping as an unchecked exception.
 */
public final class BoundedStringFormatFunction implements FunctionIfc {

    /**
     * The largest field width or precision a format specifier may use.
     */
    static final int MAX_WIDTH = 1000;

    private static final Pattern SPECIFIER = Pattern.compile("%(?:\\d+\\$)?[-#+ 0,(<]*(\\d+)?(?:\\.(\\d+))?[a-zA-Z%]");

    private final StringFormatFunction delegate = new StringFormatFunction();

    /**
     * Returns the built-in function this one guards, so it can be described like the original.
     *
     * @return the wrapped {@code STR_FORMAT}
     */
    public FunctionIfc delegate() {
        return delegate;
    }

    @Override
    public List<FunctionParameterDefinition> getFunctionParameterDefinitions() {
        return delegate.getFunctionParameterDefinitions();
    }

    @Override
    public boolean hasVarArgs() {
        return delegate.hasVarArgs();
    }

    @Override
    public boolean isParameterLazy(int parameterIndex) {
        return delegate.isParameterLazy(parameterIndex);
    }

    @Override
    public int getCountOfNonVarArgParameters() {
        return delegate.getCountOfNonVarArgParameters();
    }

    @Override
    public void validatePreEvaluation(Token token, EvaluationValue... parameterValues) throws EvaluationException {
        delegate.validatePreEvaluation(token, parameterValues);
    }

    @Override
    public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues)
            throws EvaluationException {
        if (parameterValues.length > 0) {
            requireBoundedWidths(functionToken, parameterValues[0].getStringValue());
        }
        try {
            return delegate.evaluate(expression, functionToken, parameterValues);
        } catch (IllegalFormatException invalidFormat) {
            throw new EvaluationException(functionToken, "Invalid format: " + invalidFormat.getMessage());
        }
    }

    private static void requireBoundedWidths(Token functionToken, String format) throws EvaluationException {
        Matcher matcher = SPECIFIER.matcher(format);
        while (matcher.find()) {
            if (exceeds(matcher.group(1)) || exceeds(matcher.group(2))) {
                throw new EvaluationException(functionToken,
                        "Format widths and precisions above %d are not allowed.".formatted(MAX_WIDTH));
            }
        }
    }

    private static boolean exceeds(String number) {
        return number != null && (number.length() > 4 || Integer.parseInt(number) > MAX_WIDTH);
    }
}
