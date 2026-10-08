package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Pageable;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.AdvancementRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.AdvancementService;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the {@link AdvancementService} interface.
 *
 * <p>On top of the generic CRUD handling it resolves the parent of an advancement. The parent must
 * belong to the same project and must not be the advancement itself or one of its descendants, since
 * that would turn the tree into a cycle. The title and description are stored in JSON columns, so they
 * are rejected unless they are valid JSON.
 */
@Singleton
public class AdvancementServiceImpl
        extends AbstractCrudService<AdvancementEntity, UUID, AdvancementModelDTO, AdvancementModelResponseDTO.AdvancementModelDTO>
        implements AdvancementService {

    private static final String ENTITY_NAME = "Advancement";

    private final AdvancementRepository advancementRepository;

    @Inject
    public AdvancementServiceImpl(
            AdvancementRepository advancementRepository,
            ProjectRepository projectRepository,
            JsonMapper jsonMapper
    ) {
        super(
                advancementRepository,
                projectRepository,
                (dto, project) -> toEntity(advancementRepository, jsonMapper, dto, project),
                AdvancementModelResponseDTO.AdvancementModelDTO::createDTO,
                entity -> entity.getProject().getId(),
                advancementRepository::findByProjectId,
                AdvancementModelDTO::id,
                ENTITY_NAME
        );
        this.advancementRepository = advancementRepository;
    }

    /**
     * Deletes the advancements of a project, children before their parents. Deleting a parent
     * cascades to its children in the database, so deleting it first would leave the children
     * already gone when their own delete is executed.
     *
     * @param projectId the project identifier
     */
    @Override
    public void deleteAll(UUID projectId) {
        List<AdvancementEntity> advancements = advancementRepository
                .findByProjectId(projectId, Pageable.unpaged())
                .getContent()
                .stream()
                .sorted(Comparator.comparingInt(AdvancementServiceImpl::depthOf).reversed())
                .toList();
        advancementRepository.deleteAll(advancements);
    }

    private static AdvancementEntity toEntity(
            AdvancementRepository repository,
            JsonMapper jsonMapper,
            AdvancementModelDTO dto,
            ProjectEntity project
    ) {
        requireTextComponent(jsonMapper, dto.title(), "title");
        requireTextComponent(jsonMapper, dto.description(), "description");
        return dto.toAdvancementEntity(project, resolveParent(repository, dto, project));
    }

    /**
     * Resolves the parent an advancement refers to.
     *
     * @return the parent, or null for a root advancement
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the parent does not exist or belongs to another
     *                      project, {@code INVALID_REQUEST} if the parent would create a cycle
     */
    private static @Nullable AdvancementEntity resolveParent(
            AdvancementRepository repository,
            AdvancementModelDTO dto,
            ProjectEntity project
    ) {
        UUID parentId = dto.parentId();
        if (parentId == null) return null;

        AdvancementEntity parent = repository.findById(parentId)
                .orElseThrow(() -> ApiException.notFound("Parent advancement"));
        if (!project.getId().equals(parent.getProject().getId())) {
            throw ApiException.notOwnedByProject("Parent advancement", parentId, project.getId());
        }

        Set<UUID> visited = new HashSet<>();
        for (AdvancementEntity ancestor = parent; ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor.getId().equals(dto.id()) || !visited.add(ancestor.getId())) {
                throw ApiException.invalidRequest(
                        "An advancement can't be its own parent or the parent of one of its ancestors.");
            }
        }
        return parent;
    }

    /**
     * Rejects a value that is not valid JSON, since it is stored in a JSON column.
     *
     * @throws ApiException {@code INVALID_REQUEST} if the value is not valid JSON
     */
    private static void requireTextComponent(JsonMapper jsonMapper, @Nullable String value, String field) {
        if (value == null) return;
        try {
            jsonMapper.readValue(value, JsonNode.class);
        } catch (IOException exception) {
            throw ApiException.invalidRequest("The " + field + " of an advancement must be a JSON text component.");
        }
    }

    /**
     * Returns the number of ancestors of the advancement.
     */
    private static int depthOf(AdvancementEntity advancement) {
        int depth = 0;
        Set<UUID> visited = new HashSet<>();
        for (AdvancementEntity parent = advancement.getParent();
             parent != null && visited.add(parent.getId());
             parent = parent.getParent()) {
            depth++;
        }
        return depth;
    }
}
