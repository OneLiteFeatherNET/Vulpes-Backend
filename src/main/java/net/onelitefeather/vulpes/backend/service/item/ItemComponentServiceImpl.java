package net.onelitefeather.vulpes.backend.service.item;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemComponentEntity;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemComponentRepository;
import net.onelitefeather.vulpes.backend.domain.item.ItemComponentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemComponentResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ItemComponentServiceImpl implements ItemComponentService {

    private static final String ITEM = "Item";
    private static final String COMPONENT = "Component";

    private final ItemRepository itemRepository;
    private final ItemComponentRepository componentRepository;
    private final ItemComponentRules rules;
    private final JsonMapper jsonMapper;

    @Inject
    public ItemComponentServiceImpl(
            ItemRepository itemRepository,
            ItemComponentRepository componentRepository,
            ItemComponentRules rules,
            JsonMapper jsonMapper
    ) {
        this.itemRepository = itemRepository;
        this.componentRepository = componentRepository;
        this.rules = rules;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Page<ItemComponentResponseDTO.ItemComponentDTO> findComponents(UUID itemId, Pageable pageable) {
        var item = requireItem(itemId);
        return this.componentRepository.findComponentsById(item.getId(), pageable).map(this::toDTO);
    }

    @Override
    @Transactional
    public ItemComponentResponseDTO.ItemComponentDTO createComponent(UUID itemId, ItemComponentDTO component) {
        var item = requireItem(itemId);
        requireAllowedKey(component.componentKey());
        if (this.componentRepository.findByItemIdAndComponentKey(item.getId(), component.componentKey()).isPresent()) {
            throw duplicate(component.componentKey(), itemId);
        }
        var entity = new ItemComponentEntity(null, component.componentKey(), writeValue(component.value()));
        entity.setItem(item);
        return toDTO(this.componentRepository.save(entity));
    }

    @Override
    @Transactional
    public ItemComponentResponseDTO.ItemComponentDTO updateComponent(UUID itemId, ItemComponentDTO component) {
        var item = requireItem(itemId);
        if (component.id() == null) {
            throw ApiException.invalidRequest("An id is required to update a component.");
        }
        requireAllowedKey(component.componentKey());
        var entity = requireOwnedComponent(item, component.id());
        var keyChanged = !entity.getComponentKey().equals(component.componentKey());
        if (keyChanged && this.rules.isRequired(entity.getComponentKey())) {
            throw required(entity.getComponentKey(), "renamed");
        }
        if (keyChanged && this.componentRepository.findByItemIdAndComponentKey(item.getId(), component.componentKey()).isPresent()) {
            throw duplicate(component.componentKey(), itemId);
        }
        entity.setComponentKey(component.componentKey());
        entity.setComponentValue(writeValue(component.value()));
        return toDTO(this.componentRepository.update(entity));
    }

    @Override
    public ItemComponentResponseDTO.ItemComponentDTO deleteComponent(UUID itemId, UUID componentId) {
        var item = requireItem(itemId);
        var entity = requireOwnedComponent(item, componentId);
        if (this.rules.isRequired(entity.getComponentKey())) {
            throw required(entity.getComponentKey(), "removed");
        }
        this.componentRepository.deleteById(entity.getId());
        return toDTO(entity);
    }

    @Override
    public List<ItemComponentResponseDTO.ItemComponentDTO> deleteAllComponents(UUID itemId) {
        var item = requireItem(itemId);
        // The required components stay, an item without them is incomplete
        List<ItemComponentEntity> components =
                this.componentRepository.findComponentsById(item.getId(), Pageable.unpaged()).getContent().stream()
                        .filter(component -> !this.rules.isRequired(component.getComponentKey()))
                        .toList();
        this.componentRepository.deleteAll(components);
        return components.stream().map(this::toDTO).toList();
    }

    private ItemEntity requireItem(UUID id) {
        return this.itemRepository.findById(id).orElseThrow(() -> ApiException.notFound(ITEM));
    }

    private ItemComponentEntity requireOwnedComponent(ItemEntity item, UUID componentId) {
        var entity = this.componentRepository.findById(componentId)
                .orElseThrow(() -> ApiException.notFound(COMPONENT));
        if (!entity.getItem().getId().equals(item.getId())) {
            throw ApiException.notOwnedBy(COMPONENT, componentId, "item", item.getId());
        }
        return entity;
    }

    private void requireAllowedKey(String componentKey) {
        if (this.rules.isManaged(componentKey)) {
            throw ApiException.invalidRequest(
                    "The component " + componentKey + " has a dedicated field on the item and can't be set as a component."
            );
        }
        if (this.rules.isUnknownCustom(componentKey)) {
            throw ApiException.invalidRequest("The component " + componentKey + " is not a known component of its namespace.");
        }
    }

    private static ApiException required(String componentKey, String action) {
        return ApiException.invalidRequest("The component " + componentKey + " is required and can't be " + action + ".");
    }

    private static ApiException duplicate(String componentKey, UUID itemId) {
        return ApiException.conflict("The item " + itemId + " already has the component " + componentKey + ".");
    }

    private ItemComponentResponseDTO.ItemComponentDTO toDTO(ItemComponentEntity entity) {
        return new ItemComponentResponseDTO.ItemComponentDTO(
                entity.getId(),
                entity.getComponentKey(),
                readValue(entity.getComponentValue())
        );
    }

    private String writeValue(JsonNode value) {
        try {
            return this.jsonMapper.writeValueAsString(value);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not write the value of a component", exception);
        }
    }

    private JsonNode readValue(String value) {
        if (value == null) return JsonNode.createObjectNode(Map.of());
        try {
            return this.jsonMapper.readValue(value, JsonNode.class);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read the stored value of a component", exception);
        }
    }
}
