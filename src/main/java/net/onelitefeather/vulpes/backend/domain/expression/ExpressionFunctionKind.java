package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * How a project-defined expression function is implemented.
 */
@Schema(name = "ExpressionFunctionKind", description = "How a project-defined expression function is implemented")
@Serdeable
public enum ExpressionFunctionKind {
    /**
     * The body is an EvalEx formula over the parameters. It may call built-in and other project functions.
     */
    FORMULA,
    /**
     * Only the signature is defined. The game runtime implements the function, and a Java interface is generated for
     * it. An optional body is a test stub that makes the function evaluable in the editor.
     */
    INTERFACE
}
