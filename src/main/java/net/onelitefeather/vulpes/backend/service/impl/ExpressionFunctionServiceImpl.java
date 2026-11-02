package net.onelitefeather.vulpes.backend.service.impl;

import com.ezylang.evalex.config.ExpressionConfiguration;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail.Violation;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluator;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionInterfaceDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionValidator;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionInterfaceGenerator;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableDTO;
import net.onelitefeather.vulpes.backend.domain.expression.FunctionReferences;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariableType;
import net.onelitefeather.vulpes.backend.domain.expression.ProjectExpressionConfiguration;
import net.onelitefeather.vulpes.api.model.expression.ExpressionFunctionEntity;
import net.onelitefeather.vulpes.api.model.expression.ExpressionEntity;
import net.onelitefeather.vulpes.api.repository.expression.ExpressionFunctionRepository;
import net.onelitefeather.vulpes.api.repository.expression.ExpressionRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.ExpressionFunctionService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link ExpressionFunctionService} interface.
 */
@Singleton
public class ExpressionFunctionServiceImpl
        extends AbstractCrudService<ExpressionFunctionEntity, UUID, ExpressionFunctionDTO, ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO>
        implements ExpressionFunctionService {

    private static final String EXPRESSION_FUNCTION = "Expression function";
    private static final String FIELD_FUNCTION = "function";
    private static final String CODE_FUNCTION_REQUIRED = "FUNCTION_REQUIRED";

    private final ExpressionFunctionRepository functionRepository;
    private final ExpressionRepository expressionRepository;

    /**
     * Constructs a new ExpressionFunctionServiceImpl.
     *
     * @param functionRepository   the repository to manage expression functions
     * @param projectRepository    the repository to resolve the owning project
     * @param expressionRepository the repository of the expressions that may call the functions
     */
    @Inject
    public ExpressionFunctionServiceImpl(
            ExpressionFunctionRepository functionRepository,
            ProjectRepository projectRepository,
            ExpressionRepository expressionRepository
    ) {
        super(
                functionRepository,
                projectRepository,
                ExpressionFunctionDTO::toEntity,
                ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO::createDTO,
                entity -> entity.getProject().getId(),
                functionRepository::findByProjectId,
                ExpressionFunctionDTO::id,
                EXPRESSION_FUNCTION
        );
        this.functionRepository = functionRepository;
        this.expressionRepository = expressionRepository;
    }

    @Override
    public ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO create(UUID projectId, ExpressionFunctionDTO dto) {
        ExpressionFunctionValidator.requireValid(dto, definitions(projectId));
        return super.create(projectId, dto);
    }

    @Override
    public ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO update(UUID projectId, ExpressionFunctionDTO dto) {
        ExpressionFunctionValidator.requireValid(dto, othersThan(projectId, dto.id()));
        Optional<ExpressionFunctionDTO> stored = dto.id() == null
                ? Optional.empty()
                : findById(projectId, dto.id()).map(ExpressionFunctionDTO::fromEntity);
        stored.filter(previous -> breaksCallers(previous, dto))
                .ifPresent(previous -> requireUnused(projectId, previous, "changed"));
        return super.update(projectId, dto);
    }

    @Override
    public ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO delete(UUID projectId, UUID id) {
        findById(projectId, id)
                .map(ExpressionFunctionDTO::fromEntity)
                .ifPresent(stored -> requireUnused(projectId, stored, "deleted"));
        return super.delete(projectId, id);
    }

    @Override
    public List<ExpressionFunctionDTO> definitions(UUID projectId) {
        return functionRepository.findByProjectId(projectId, Pageable.unpaged()).getContent().stream()
                .map(ExpressionFunctionDTO::fromEntity)
                .toList();
    }

    @Override
    public ExpressionEvaluationResultDTO evaluate(UUID projectId, ExpressionFunctionEvaluateDTO request) {
        requireProjectExists(projectId);
        ExpressionFunctionDTO function = request.function();
        if (function == null) {
            throw ApiException.validationFailed(List.of(new Violation(
                    FIELD_FUNCTION, CODE_FUNCTION_REQUIRED, "A function definition is required.")));
        }
        List<ExpressionFunctionDTO> others = othersThan(projectId, function.id());
        ExpressionFunctionValidator.requireValid(function, others);

        ExpressionConfiguration configuration = ProjectExpressionConfiguration.of(
                ExpressionFunctionValidator.withFunction(others, function));
        Map<String, String> arguments = request.arguments() == null ? Map.of() : request.arguments();
        List<ExpressionVariableDTO> parameters = function.parameters() == null ? List.of() : function.parameters();
        Map<String, String> values = new LinkedHashMap<>();
        Map<String, ExpressionVariableType> types = new LinkedHashMap<>();
        for (ExpressionVariableDTO parameter : parameters) {
            values.put(parameter.name(), arguments.get(parameter.name()));
            types.put(parameter.name(), parameter.type() == null ? ExpressionVariableType.NUMBER : parameter.type());
        }
        String parameterNames = parameters.stream().map(ExpressionVariableDTO::name).collect(Collectors.joining(", "));
        return ExpressionEvaluator.evaluate(function.name() + "(" + parameterNames + ")", values, types, configuration);
    }

    @Override
    public ExpressionFunctionInterfaceDTO interfaceSource(UUID projectId) {
        requireProjectExists(projectId);
        return ExpressionInterfaceGenerator.generate(definitions(projectId));
    }

    /**
     * Rejects removing or changing a function that expressions or other functions of the project still call.
     *
     * @param projectId the owning project
     * @param function  the stored function
     * @param action    what is done to the function, used in the message
     * @throws ApiException {@code RESOURCE_CONFLICT} naming the callers
     */
    private void requireUnused(UUID projectId, ExpressionFunctionDTO function, String action) {
        List<ExpressionFunctionDTO> functions = definitions(projectId);
        ExpressionConfiguration configuration = ProjectExpressionConfiguration.of(functions);
        List<String> callers = new ArrayList<>();
        for (ExpressionEntity expression : expressionsOf(projectId)) {
            if (FunctionReferences.calls(expression.getExpression(), function.name(), configuration)) {
                callers.add("expression '%s'".formatted(expression.getUiName()));
            }
        }
        for (ExpressionFunctionDTO other : functions) {
            if (!other.id().equals(function.id())
                    && FunctionReferences.calls(other.body(), function.name(), configuration)) {
                callers.add("function '%s'".formatted(other.name()));
            }
        }
        if (!callers.isEmpty()) {
            throw ApiException.conflict("Function '%s' cannot be %s, it is still used by %s."
                    .formatted(function.name(), action, String.join(", ", callers)));
        }
    }

    /**
     * A rename or a different number of parameters makes every existing call of the function fail.
     *
     * @param previous the stored function
     * @param next     the requested change
     * @return {@code true} if callers of the stored function would break
     */
    private static boolean breaksCallers(ExpressionFunctionDTO previous, ExpressionFunctionDTO next) {
        boolean renamed = next.name() == null || !previous.name().equalsIgnoreCase(next.name());
        int previousCount = previous.parameters() == null ? 0 : previous.parameters().size();
        int nextCount = next.parameters() == null ? 0 : next.parameters().size();
        return renamed || previousCount != nextCount;
    }

    private List<ExpressionEntity> expressionsOf(UUID projectId) {
        return expressionRepository.findByProjectId(projectId, Pageable.unpaged()).getContent();
    }

    private void requireProjectExists(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw ApiException.projectNotFound();
        }
    }

    /**
     * Lists the stored functions of a project except the one with the given id.
     *
     * @param projectId the owning project
     * @param id        the function to leave out, {@code null} leaves out none
     * @return the other functions of the project
     */
    private List<ExpressionFunctionDTO> othersThan(UUID projectId, UUID id) {
        return definitions(projectId).stream()
                .filter(definition -> id == null || !id.equals(definition.id()))
                .toList();
    }
}
