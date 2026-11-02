package net.onelitefeather.vulpes.backend.domain.expression;

import com.ezylang.evalex.config.ExpressionConfiguration;

import java.util.Map;

/**
 * Builds the EvalEx configuration every expression starts from.
 *
 * <p>It is EvalEx's default configuration with the functions that could exhaust the server replaced by bounded
 * versions, see {@link BoundedStringFormatFunction}.
 */
public final class BaseExpressionConfiguration {

    private BaseExpressionConfiguration() {
    }

    /**
     * Builds a new configuration. Adding functions to a configuration changes it, so every caller that adds its own
     * functions needs an independent one.
     *
     * @return a new configuration with the bounded built-in functions
     */
    public static ExpressionConfiguration create() {
        return ExpressionConfiguration.defaultConfiguration()
                .withAdditionalFunctions(Map.entry("STR_FORMAT", new BoundedStringFormatFunction()));
    }
}
