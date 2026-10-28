package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The EvalEx functions, operators and constants an expression can use, as configured for evaluation.
 *
 * @param evalexVersion the EvalEx version the catalog describes
 * @param functions     every available function, sorted by category and name
 * @param operators     every prefix, infix and postfix operator, sorted by kind and symbol
 * @param constants     every constant, sorted by name
 */
@Schema(description = "The EvalEx functions, operators and constants available to expressions")
@Serdeable
public record ExpressionCatalogDTO(
        @Schema(description = "The EvalEx version the catalog describes, for example 3.8.0") String evalexVersion,
        @Schema(description = "Every available function") List<FunctionDTO> functions,
        @Schema(description = "Every prefix, infix and postfix operator") List<OperatorDTO> operators,
        @Schema(description = "Every constant") List<ConstantDTO> constants
) {

    /**
     * The category a function belongs to, derived from the EvalEx package it is implemented in.
     */
    @Schema(description = "The category of a function")
    @Serdeable
    public enum FunctionCategory {
        BASIC,
        STRING,
        DATE_TIME,
        TRIGONOMETRIC,
        OTHER,
        /**
         * A function the project defined, see {@link ExpressionFunctionKind}.
         */
        PROJECT
    }

    /**
     * The kind of an operator, depending on the position of its operand.
     */
    @Schema(description = "The position of an operator relative to its operands")
    @Serdeable
    public enum OperatorKind {
        PREFIX,
        INFIX,
        POSTFIX
    }

    /**
     * A function with its parameters.
     *
     * @param name        the name as used in expressions
     * @param category    the category the function belongs to
     * @param parameters  the parameters in call order
     * @param description the description of a project function, {@code null} for built-in functions
     * @param kind        how a project function is implemented, {@code null} for built-in functions
     * @param returnType  the type a project function returns, {@code null} for built-in functions
     */
    @Schema(description = "An EvalEx function")
    @Serdeable
    public record FunctionDTO(
            @Schema(description = "Name of the function as used in expressions") String name,
            @Schema(description = "Category of the function") FunctionCategory category,
            @Schema(description = "Parameters in call order") List<ParameterDTO> parameters,
            @Schema(description = "Description of a project function, null for built-in functions", nullable = true)
            String description,
            @Schema(description = "FORMULA or INTERFACE for a project function, null for built-in functions", nullable = true)
            ExpressionFunctionKind kind,
            @Schema(description = "Type a project function returns, null for built-in functions", nullable = true)
            ExpressionVariableType returnType
    ) {
    }

    /**
     * A parameter of a function.
     *
     * @param name        the parameter name
     * @param varArg      {@code true} if the parameter may be repeated
     * @param lazy        {@code true} if the argument is evaluated only when needed
     * @param nonZero     {@code true} if the argument must not be zero
     * @param nonNegative {@code true} if the argument must not be negative
     * @param type        the type of a project function parameter, {@code null} for built-in functions
     */
    @Schema(description = "A parameter of an EvalEx function")
    @Serdeable
    public record ParameterDTO(
            @Schema(description = "Parameter name") String name,
            @Schema(description = "True if the parameter may be repeated") boolean varArg,
            @Schema(description = "True if the argument is evaluated lazily") boolean lazy,
            @Schema(description = "True if the argument must not be zero") boolean nonZero,
            @Schema(description = "True if the argument must not be negative") boolean nonNegative,
            @Schema(description = "Type of a project function parameter, null for built-in functions", nullable = true)
            ExpressionVariableType type
    ) {
    }

    /**
     * An operator.
     *
     * @param symbol           the symbol as used in expressions
     * @param kind             the position of the operator relative to its operands
     * @param precedence       the binding strength, higher binds tighter
     * @param leftAssociative  {@code true} if operators of equal precedence group from the left
     */
    @Schema(description = "An EvalEx operator")
    @Serdeable
    public record OperatorDTO(
            @Schema(description = "Symbol of the operator as used in expressions") String symbol,
            @Schema(description = "Position of the operator relative to its operands") OperatorKind kind,
            @Schema(description = "Binding strength, higher binds tighter") int precedence,
            @Schema(description = "True if operators of equal precedence group from the left") boolean leftAssociative
    ) {
    }

    /**
     * A constant.
     *
     * @param name  the name as used in expressions
     * @param type  the EvalEx data type of the constant, for example {@code NUMBER}
     * @param value the value as text
     */
    @Schema(description = "An EvalEx constant")
    @Serdeable
    public record ConstantDTO(
            @Schema(description = "Name of the constant as used in expressions") String name,
            @Schema(description = "EvalEx data type of the constant, for example NUMBER") String type,
            @Schema(description = "Value of the constant as text") String value
    ) {
    }
}
