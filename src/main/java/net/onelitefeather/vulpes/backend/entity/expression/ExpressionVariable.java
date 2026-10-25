package net.onelitefeather.vulpes.backend.entity.expression;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableType;

/**
 * A variable stored with an {@link ExpressionEntity}. Stored as an element collection, so the position
 * in the list is kept by an order column.
 */
@Embeddable
public class ExpressionVariable {

    @Column(name = "variable_name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "variable_type", nullable = false)
    private ExpressionVariableType type;

    @Column(name = "test_value")
    private String testValue;

    protected ExpressionVariable() {
    }

    /**
     * Creates a stored variable.
     *
     * @param name      the variable name
     * @param type      the type the value is converted to
     * @param testValue the value used when the expression is tested, may be {@code null}
     */
    public ExpressionVariable(String name, ExpressionVariableType type, String testValue) {
        this.name = name;
        this.type = type;
        this.testValue = testValue;
    }

    public String getName() {
        return name;
    }

    public ExpressionVariableType getType() {
        return type;
    }

    public String getTestValue() {
        return testValue;
    }
}
