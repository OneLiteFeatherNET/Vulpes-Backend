package net.onelitefeather.vulpes.backend.entity.expression;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import net.onelitefeather.vulpes.api.model.IdentifiableEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An EvalEx formula owned by a project, together with the variables it references.
 *
 * <p>Creation and modification timestamps come from {@link IdentifiableEntity}.
 */
@Entity
@Table(name = "expression")
public class ExpressionEntity extends IdentifiableEntity {

    @Column(name = "ui_name", nullable = false)
    private String uiName;

    @Column(name = "expression", nullable = false, columnDefinition = "text")
    private String expression;

    @Column(name = "comment")
    private String comment;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expression_variable", joinColumns = @JoinColumn(name = "expression_id"))
    @OrderColumn(name = "variable_order")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ExpressionVariable> variables = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private ProjectEntity project;

    protected ExpressionEntity() {
    }

    /**
     * Creates a new expression entity.
     *
     * @param id         the identifier, {@code null} for a new entity
     * @param uiName     the name to display in the UI
     * @param expression the EvalEx formula, may be empty
     * @param comment    an internal description, may be {@code null}
     * @param variables  the variables the expression references, in order
     * @param project    the owning project
     */
    public ExpressionEntity(
            UUID id,
            String uiName,
            String expression,
            String comment,
            List<ExpressionVariable> variables,
            ProjectEntity project
    ) {
        setId(id);
        this.uiName = uiName;
        this.expression = expression;
        this.comment = comment;
        this.variables = new ArrayList<>(variables);
        this.project = project;
    }

    public String getUiName() {
        return uiName;
    }

    public String getExpression() {
        return expression;
    }

    public String getComment() {
        return comment;
    }

    public List<ExpressionVariable> getVariables() {
        return variables;
    }

    public ProjectEntity getProject() {
        return project;
    }
}
