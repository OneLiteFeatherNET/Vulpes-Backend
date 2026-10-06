package net.onelitefeather.vulpes.backend.service.item;

import io.micronaut.context.annotation.EachProperty;

/**
 * A component every item has, read from the list {@code vulpes.item-components.required}.
 *
 * @param key   the key of the component, e.g. {@code stelaris:material}
 * @param value the value a new item starts with, as JSON, e.g. {@code "minecraft:dirt"} with quotes
 * @see ItemComponentRules
 */
@EachProperty(value = "vulpes.item-components.required", list = true)
public record RequiredComponentConfiguration(String key, String value) {
}
