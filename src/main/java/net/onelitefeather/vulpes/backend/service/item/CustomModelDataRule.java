package net.onelitefeather.vulpes.backend.service.item;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemComponentEntity;
import net.onelitefeather.vulpes.backend.entity.item.ItemModelComponentRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Keeps the {@code minecraft:custom_model_data} of the items in a project unique.
 * <p>
 * The client picks the model by the custom model data in the item model definition of the item. That definition
 * is the one named by {@code minecraft:item_model}, or the one of the material if the item has no item model.
 * Two items with the same definition and the same custom model data would get the same model, so the second one
 * is rejected. Values are compared by their content: {@code {"floats":[1001]}} and
 * {@code {"floats":[1001.0],"strings":[]}} are the same.
 * </p>
 */
@Singleton
public class CustomModelDataRule {

    public static final String CUSTOM_MODEL_DATA = "minecraft:custom_model_data";
    public static final String ITEM_MODEL = "minecraft:item_model";
    public static final String MATERIAL = "stelaris:material";

    private static final Set<String> KEYS = Set.of(CUSTOM_MODEL_DATA, ITEM_MODEL, MATERIAL);

    private final ItemModelComponentRepository repository;
    private final JsonMapper jsonMapper;

    public CustomModelDataRule(ItemModelComponentRepository repository, JsonMapper jsonMapper) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Returns whether a change of the component can make the custom model data of an item collide.
     *
     * @param componentKey the key of the changed component
     * @return true if the key takes part in the rule
     */
    public boolean concerns(String componentKey) {
        return KEYS.contains(componentKey);
    }

    /**
     * Finds another item in the project with the same model definition and custom model data.
     *
     * @param projectId  the project of the item
     * @param itemId     the item itself, which is skipped, or null for an item which isn't saved yet
     * @param components the components the item will have, as key and stored JSON value
     * @return the other item, if there is one
     */
    public Optional<ItemEntity> findConflict(UUID projectId, @Nullable UUID itemId, Map<String, String> components) {
        Key own = keyOf(components);
        if (own == null) {
            return Optional.empty();
        }
        Map<UUID, Map<String, String>> others = new HashMap<>();
        Map<UUID, ItemEntity> items = new HashMap<>();
        for (ItemComponentEntity component : this.repository.findByProjectIdAndComponentKeys(projectId, KEYS)) {
            ItemEntity item = component.getItem();
            if (item.getId().equals(itemId)) continue;
            items.putIfAbsent(item.getId(), item);
            others.computeIfAbsent(item.getId(), id -> new HashMap<>())
                    .put(component.getComponentKey(), component.getComponentValue());
        }
        return others.entrySet().stream()
                .filter(entry -> own.equals(keyOf(entry.getValue())))
                .map(entry -> items.get(entry.getKey()))
                .findFirst();
    }

    /**
     * Returns the model definition and custom model data of an item, or null if it has no custom model data.
     */
    private @Nullable Key keyOf(Map<String, String> components) {
        String modelData = components.get(CUSTOM_MODEL_DATA);
        if (modelData == null) {
            return null;
        }
        String definition = components.containsKey(ITEM_MODEL) ? components.get(ITEM_MODEL) : components.get(MATERIAL);
        return new Key(canonical(definition), canonical(modelData));
    }

    private @Nullable Object canonical(@Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return canonical(this.jsonMapper.readValue(value, JsonNode.class));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read the stored value of a component", exception);
        }
    }

    /**
     * Turns a JSON value into plain Java values which are equal for the same content. Numbers lose their trailing
     * zeros and empty lists of an object are left out, they mean the same as a missing list.
     */
    private static @Nullable Object canonical(JsonNode node) {
        if (node.isObject()) {
            Map<String, Object> map = new TreeMap<>();
            for (Map.Entry<String, JsonNode> entry : node.entries()) {
                JsonNode value = entry.getValue();
                if (value.isArray() && value.size() == 0) continue;
                map.put(entry.getKey(), canonical(value));
            }
            return map;
        }
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode value : node.values()) {
                list.add(canonical(value));
            }
            return list;
        }
        if (node.isNumber()) {
            return node.getBigDecimalValue().stripTrailingZeros();
        }
        if (node.isNull()) {
            return null;
        }
        return node.getValue();
    }

    private record Key(@Nullable Object definition, @Nullable Object modelData) {
    }
}
