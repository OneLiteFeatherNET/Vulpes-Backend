package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.json.JsonMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemComponentEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemComponentRepository;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.item.ItemComponentRules;
import org.jspecify.annotations.NullMarked;

/**
 * The transactional write of a new item together with the components every item has.
 *
 * <p>Like {@link AssetPersistence}, the write lives in its own bean and returns the id. A
 * transactional save does not fill in the audit timestamps on the instance, so the caller reads the
 * stored item back after the commit.
 */
@Singleton
@NullMarked
public class ItemPersistence {

    private final ItemRepository itemRepository;
    private final ItemComponentRepository itemComponentRepository;
    private final ProjectRepository projectRepository;
    private final ItemComponentRules componentRules;
    private final JsonMapper jsonMapper;

    /**
     * Constructs a new ItemPersistence.
     *
     * @param itemRepository          the repository of items
     * @param itemComponentRepository the repository of item components
     * @param projectRepository       the repository of projects
     * @param componentRules          names the components every item has
     * @param jsonMapper              writes the values of the components
     */
    @Inject
    public ItemPersistence(
            ItemRepository itemRepository,
            ItemComponentRepository itemComponentRepository,
            ProjectRepository projectRepository,
            ItemComponentRules componentRules,
            JsonMapper jsonMapper) {
        this.itemRepository = itemRepository;
        this.itemComponentRepository = itemComponentRepository;
        this.projectRepository = projectRepository;
        this.componentRules = componentRules;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Stores the item together with its required components in one transaction, so there is
     * never an item without them.
     *
     * @param projectId the project the item belongs to
     * @param dto       the item to create
     * @return the id of the stored item
     * @throws ApiException {@code PROJECT_NOT_FOUND} if the project does not exist
     */
    @Transactional
    public UUID create(UUID projectId, ItemModelDTO dto) {
        ProjectEntity project = projectRepository.findById(projectId).orElseThrow(ApiException::projectNotFound);
        ItemEntity item = itemRepository.save(dto.toItemEntity(project));
        List<ItemComponentEntity> components = new ArrayList<>();
        componentRules.required().forEach((key, value) -> {
            var component = new ItemComponentEntity(null, key, writeJson(value));
            component.setItem(item);
            components.add(component);
        });
        itemComponentRepository.saveAll(components);
        return item.getId();
    }

    private String writeJson(Object value) {
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not write the value of a required component", exception);
        }
    }
}
