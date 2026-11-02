package net.onelitefeather.vulpes.backend.service.impl;

import net.onelitefeather.vulpes.api.model.expression.ExpressionVariableType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalog;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluator;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionResponseDTO;
import net.onelitefeather.vulpes.api.model.expression.ExpressionEntity;
import net.onelitefeather.vulpes.api.repository.expression.ExpressionRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidator;
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

    /**
     * Constructs a new ExpressionServiceImpl.
     *
     * @param expressionRepository the repository to manage expressions
     * @param projectRepository    the repository to resolve the owning project
     */
    @Inject
    public ExpressionServiceImpl(ExpressionRepository expressionRepository, ProjectRepository projectRepository) {
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
    }

    @Override
    public ExpressionResponseDTO.ExpressionModelDTO create(UUID projectId, ExpressionDTO dto) {
        ExpressionEvaluator.requireValidDefinition(dto.expression(), dto.variables());
        return super.create(projectId, dto);
    }

    @Override
    public ExpressionResponseDTO.ExpressionModelDTO update(UUID projectId, ExpressionDTO dto) {
        ExpressionEvaluator.requireValidDefinition(dto.expression(), dto.variables());
        return super.update(projectId, dto);
    }

    @Override
    public ExpressionEvaluationResultDTO evaluate(UUID projectId, ExpressionEvaluateDTO request) {
        requireProjectExists(projectId);
        Map<String, String> values = request.variables() == null ? Map.of() : request.variables();
        Map<String, ExpressionVariableType> types = request.types() == null ? Map.of() : request.types();
        return ExpressionEvaluator.evaluate(request.expression(), values, types);
    }

    @Override
    public ExpressionValidationResultDTO validate(UUID projectId, ExpressionValidateDTO request) {
        requireProjectExists(projectId);
        return ExpressionValidator.validate(request.expression(), request.variables());
    }

    @Override
    public ExpressionCatalogDTO catalog() {
        return ExpressionCatalog.DEFAULT;
    }

    private void requireProjectExists(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw ApiException.projectNotFound();
        }
    }
}
