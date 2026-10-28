package net.onelitefeather.vulpes.backend.domain.expression;

import com.ezylang.evalex.config.ExpressionConfiguration;

import java.util.List;
import java.util.Map;

/**
 * Builds the EvalEx configuration a project's expressions are parsed and evaluated with.
 *
 * <p>Every built-in function of {@link ExpressionConfiguration#defaultConfiguration()} stays available. The project's
 * functions are added on top of them, each one as a {@link ProjectExpressionFunction}.
 */
public final class ProjectExpressionConfiguration {

    private ProjectExpressionConfiguration() {
    }

    /**
     * Builds the configuration with the given project functions added to the built-in ones.
     *
     * @param functions the project functions, names must be unique and valid
     * @return a new configuration, every call builds an independent one
     */
    public static ExpressionConfiguration of(List<ExpressionFunctionDTO> functions) {
        ExpressionConfiguration configuration = ExpressionConfiguration.defaultConfiguration();
        for (ExpressionFunctionDTO function : functions) {
            configuration = configuration.withAdditionalFunctions(
                    Map.entry(function.name(), new ProjectExpressionFunction(function)));
        }
        return configuration;
    }
}
