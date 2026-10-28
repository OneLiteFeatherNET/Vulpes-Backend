package net.onelitefeather.vulpes.backend.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import com.ezylang.evalex.config.ExpressionConfiguration;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalog;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluator;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidator;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableType;
import net.onelitefeather.vulpes.backend.domain.expression.ProjectExpressionConfiguration;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionEntity;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionRepository;
import net.onelitefeather.vulpes.backend.service.ExpressionFunctionService;
import net.onelitefeather.vulpes.backend.service.ExpressionService;

import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the {@link ExpressionService} interface.
 */
@Singleton
public class ExpressionServiceImpl
        extends AbstractCrudService<ExpressionEntity, UUID, ExpressionDTO, ExpressionResponseDTO.ExpressionModelDTO>
        implements ExpressionService {

    private static final String EXPRESSION = "Expression";

    private final ExpressionFunctionService functionService;

    /**
     * Constructs a new ExpressionServiceImpl.
     *
     * @param expressionRepository the repository to manage expressions
     * @param projectRepository    the repository to resolve the owning project
     * @param functionService      the service that provides the functions a project defines
     */
    @Inject
    public ExpressionServiceImpl(
            ExpressionRepository expressionRepository,
            ProjectRepository projectRepository,
            ExpressionFunctionService functionService
    ) {
        super(
                expressionRepository,
                projectRepository,
                ExpressionDTO::toEntity,
                ExpressionResponseDTO.ExpressionModelDTO::createDTO,
                entity -> entity.getProject().getId(),
                expressionRepository::findByProjectId,
                ExpressionDTO::id,
                EXPRESSION
        );
        this.functionService = functionService;
    }

    @Override
    public ExpressionResponseDTO.ExpressionModelDTO create(UUID projectId, ExpressionDTO dto) {
        ExpressionEvaluator.requireValidDefinition(dto.expression(), dto.variables(), configurationOf(projectId));
        return super.create(projectId, dto);
    }

    @Override
    public ExpressionResponseDTO.ExpressionModelDTO update(UUID projectId, ExpressionDTO dto) {
        ExpressionEvaluator.requireValidDefinition(dto.expression(), dto.variables(), configurationOf(projectId));
        return super.update(projectId, dto);
    }

    @Override
    public ExpressionEvaluationResultDTO evaluate(UUID projectId, ExpressionEvaluateDTO request) {
        Map<String, String> values = request.variables() == null ? Map.of() : request.variables();
        Map<String, ExpressionVariableType> types = request.types() == null ? Map.of() : request.types();
        return ExpressionEvaluator.evaluate(request.expression(), values, types, configurationOf(projectId));
    }

    @Override
    public ExpressionValidationResultDTO validate(UUID projectId, ExpressionValidateDTO request) {
        return ExpressionValidator.validate(request.expression(), request.variables(), configurationOf(projectId));
    }

    @Override
    public ExpressionCatalogDTO catalog() {
        return ExpressionCatalog.DEFAULT;
    }

    @Override
    public ExpressionCatalogDTO catalog(UUID projectId) {
        return ExpressionCatalog.describe(configurationOf(projectId));
    }

    /**
     * Builds the configuration a project's expressions are checked and evaluated with.
     *
     * @param projectId the owning project
     * @return the built-in functions together with the project's functions
     */
    private ExpressionConfiguration configurationOf(UUID projectId) {
        return ProjectExpressionConfiguration.of(functionService.definitions(projectId));
    }
}
