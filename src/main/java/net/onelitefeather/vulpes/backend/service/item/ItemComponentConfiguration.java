package net.onelitefeather.vulpes.backend.service.item;

import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.List;

/**
 * The configured rules for the data components of an item, read from {@code vulpes.item-components}.
 *
 * @param managed         the components with their own storage and endpoints, rejected as components
 * @param customNamespace the namespace of the components Stelaris adds, e.g. {@code stelaris}
 * @param custom          the components Stelaris adds; other keys in their namespace are rejected
 * @see ItemComponentRules
 */
@ConfigurationProperties("vulpes.item-components")
public record ItemComponentConfiguration(List<String> managed, String customNamespace, List<String> custom) {
}
