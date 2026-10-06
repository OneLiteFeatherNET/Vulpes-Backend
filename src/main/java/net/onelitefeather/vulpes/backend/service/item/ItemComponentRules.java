package net.onelitefeather.vulpes.backend.service.item;

import io.micronaut.context.annotation.Context;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The rules which decide which data components an item can have.
 * <p>
 * The rules come from {@link ItemComponentConfiguration} and {@link RequiredComponentConfiguration}. They are
 * checked when the application starts, so a configuration which would break items stops the start with a message
 * instead of failing later.
 * </p>
 */
@Context
public final class ItemComponentRules {

    private final Set<String> managed;
    private final String customPrefix;
    private final Set<String> custom;
    private final Map<String, JsonNode> required;

    /**
     * Reads and checks the configured rules.
     *
     * @param configuration the managed and custom components
     * @param required      the components every item has, with the value a new item starts with
     * @param jsonMapper    reads the configured values
     * @throws IllegalStateException if the configuration is inconsistent
     */
    public ItemComponentRules(
            ItemComponentConfiguration configuration,
            List<RequiredComponentConfiguration> required,
            JsonMapper jsonMapper
    ) {
        this.managed = Set.copyOf(orEmpty(configuration.managed()));
        String namespace = configuration.customNamespace();
        if (namespace == null || namespace.isBlank()) {
            throw invalid("custom-namespace is missing");
        }
        this.customPrefix = namespace + ":";
        this.custom = Set.copyOf(orEmpty(configuration.custom()));

        List<String> problems = new ArrayList<>();
        for (String key : this.custom) {
            if (!key.startsWith(this.customPrefix)) {
                problems.add("the custom component " + key + " is not in the namespace " + namespace);
            }
            if (this.managed.contains(key)) {
                problems.add("the component " + key + " is both custom and managed");
            }
        }

        Map<String, JsonNode> requiredValues = new LinkedHashMap<>();
        for (RequiredComponentConfiguration component : required) {
            String key = component.key();
            if (key == null || key.isBlank()) {
                problems.add("a required component has no key");
                continue;
            }
            if (this.managed.contains(key)) {
                problems.add("the required component " + key + " is managed");
            }
            if (key.startsWith(this.customPrefix) && !this.custom.contains(key)) {
                problems.add("the required component " + key + " is not a custom component");
            }
            if (requiredValues.containsKey(key)) {
                problems.add("the required component " + key + " is listed twice");
            }
            try {
                requiredValues.put(key, jsonMapper.readValue(component.value(), JsonNode.class));
            } catch (IOException | RuntimeException exception) {
                problems.add("the value of the required component " + key + " is no JSON: " + component.value());
            }
        }
        if (!problems.isEmpty()) {
            throw invalid(String.join("; ", problems));
        }
        this.required = Collections.unmodifiableMap(requiredValues);
    }

    /**
     * Returns whether the component has its own storage and endpoints, so it can't be set as a component.
     *
     * @param key the key of the component
     * @return true if the component is managed
     */
    public boolean isManaged(String key) {
        return this.managed.contains(key);
    }

    /**
     * Returns whether the key is in the namespace of the custom components without being one of them.
     *
     * @param key the key of the component
     * @return true if the key must be rejected
     */
    public boolean isUnknownCustom(String key) {
        return key.startsWith(this.customPrefix) && !this.custom.contains(key);
    }

    /**
     * Returns whether every item has the component, so it can't be removed or renamed.
     *
     * @param key the key of the component
     * @return true if the component is required
     */
    public boolean isRequired(String key) {
        return this.required.containsKey(key);
    }

    /**
     * Returns the components every item has, with the value a new item starts with.
     *
     * @return the keys and values, in the configured order
     */
    public Map<String, JsonNode> required() {
        return this.required;
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static IllegalStateException invalid(String detail) {
        return new IllegalStateException("Invalid vulpes.item-components configuration: " + detail);
    }
}
