package net.onelitefeather.vulpes.backend.domain.expression;

import com.ezylang.evalex.BaseException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import io.micronaut.core.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Checks an expression against its declared variables while it is being edited.
 *
 * <p>Unlike {@link ExpressionEvaluator#requireValidDefinition}, this never throws: a parse error is part of the
 * answer. Declared variable names that do not match {@code [A-Za-z_][A-Za-z0-9_]*} are ignored, because they can
 * never be used in an expression. Variable names are compared ignoring case, the way EvalEx resolves them.
 */
public final class ExpressionValidator {

    private ExpressionValidator() {
    }

    /**
     * Checks an expression and compares the variables it uses with the declared ones.
     *
     * @param expression the EvalEx formula, {@code null} or empty is valid and reports nothing
     * @param variables  the declared variable names, {@code null} means none
     * @return the result, always answered, also when the expression does not parse
     */
    public static ExpressionValidationResultDTO validate(@Nullable String expression, @Nullable List<String> variables) {
        if (expression == null || expression.isEmpty()) {
            return new ExpressionValidationResultDTO(true, List.of(), List.of(), List.of(), List.of());
        }
        Expression parsed = new Expression(expression, ExpressionEvaluator.CONFIGURATION);
        TreeSet<String> used;
        try {
            parsed.validate();
            used = new TreeSet<>(parsed.getUsedVariables());
        } catch (ParseException failure) {
            return new ExpressionValidationResultDTO(
                    false,
                    List.of(errorOf(failure, expression)),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        TreeSet<String> declared = declaredNames(variables);
        Set<String> foldedDeclared = fold(declared);
        Set<String> foldedUsed = fold(used);
        Set<String> foldedConstants = fold(ExpressionEvaluator.CONFIGURATION.getDefaultConstants().keySet());

        List<String> undefined = used.stream()
                .filter(name -> !foldedDeclared.contains(fold(name)) && !foldedConstants.contains(fold(name)))
                .toList();
        List<String> unused = declared.stream()
                .filter(name -> !foldedUsed.contains(fold(name)))
                .toList();
        return new ExpressionValidationResultDTO(true, List.of(), List.copyOf(used), undefined, unused);
    }

    private static TreeSet<String> declaredNames(@Nullable List<String> variables) {
        if (variables == null) {
            return new TreeSet<>();
        }
        return variables.stream()
                .filter(ExpressionEvaluator::isValidName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static String fold(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private static Set<String> fold(Set<String> names) {
        return names.stream().map(ExpressionValidator::fold).collect(Collectors.toSet());
    }

    /**
     * Maps an EvalEx error to 0-based offsets. EvalEx reports 1-based inclusive positions. A failure without a
     * usable position spans the whole expression, and an empty span is widened by one character.
     */
    private static ExpressionValidationResultDTO.ErrorDTO errorOf(BaseException failure, String expression) {
        int length = expression.length();
        int reportedStart = failure.getStartPosition();
        int reportedEnd = failure.getEndPosition();
        int start;
        int end;
        if (reportedStart < 1 || reportedEnd < reportedStart) {
            start = 0;
            end = length;
        } else {
            start = Math.min(reportedStart - 1, length);
            end = Math.min(reportedEnd, length);
            if (end <= start) {
                end = Math.min(start + 1, length);
            }
        }
        return new ExpressionValidationResultDTO.ErrorDTO(
                Objects.requireNonNullElse(failure.getMessage(), "The expression is invalid."),
                start,
                end
        );
    }
}
