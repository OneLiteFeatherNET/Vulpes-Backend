package net.onelitefeather.vulpes.backend.domain.expression;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.ASTNode;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import io.micronaut.core.annotation.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds the functions a formula calls, so a project function that is still in use is not removed or renamed.
 */
public final class FunctionReferences {

    private FunctionReferences() {
    }

    /**
     * Lists the names of the functions a formula calls, in lower case, the way EvalEx compares them.
     *
     * @param formula       the EvalEx formula, {@code null} or blank calls nothing
     * @param configuration the configuration the formula is parsed with, it must know the project's functions
     * @return the called function names, empty when the formula does not parse
     */
    public static Set<String> calledFunctions(@Nullable String formula, ExpressionConfiguration configuration) {
        if (formula == null || formula.isBlank()) {
            return Set.of();
        }
        try {
            return new Expression(formula, configuration).getAllASTNodes().stream()
                    .map(ASTNode::getToken)
                    .filter(token -> token.getType() == Token.TokenType.FUNCTION)
                    .map(token -> token.getValue().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
        } catch (ParseException unparsable) {
            return Set.of();
        }
    }

    /**
     * Checks whether a formula calls the given function.
     *
     * @param formula       the EvalEx formula, {@code null} or blank calls nothing
     * @param functionName  the function to look for, compared ignoring case
     * @param configuration the configuration the formula is parsed with, it must know the project's functions
     * @return {@code true} if the formula calls the function
     */
    public static boolean calls(@Nullable String formula, String functionName, ExpressionConfiguration configuration) {
        return calledFunctions(formula, configuration).contains(functionName.toLowerCase(Locale.ROOT));
    }
}
