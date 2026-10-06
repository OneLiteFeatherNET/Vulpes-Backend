package net.onelitefeather.vulpes.backend.service.item;

import io.micronaut.context.ApplicationContext;
import io.micronaut.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rules are checked against the real {@code application.yml} without a database, the same way
 * {@code RelationalCopyDTOSerdeTest} starts its context.
 */
@DisplayName("Rules for the data components of an item")
class ItemComponentRulesTest {

    private static final Map<String, Object> NO_DATABASE = Map.of("datasources.default.enabled", false);

    private static ItemComponentRules rulesWith(Map<String, Object> properties) {
        Map<String, Object> all = new HashMap<>(NO_DATABASE);
        all.putAll(properties);
        try (ApplicationContext context = ApplicationContext.run(all)) {
            return context.getBean(ItemComponentRules.class);
        }
    }

    @Test
    @DisplayName("the application configuration is read as written")
    void applicationConfiguration_isReadAsWritten() {
        ItemComponentRules rules = rulesWith(Map.of());

        assertTrue(rules.isManaged("minecraft:lore"));
        assertTrue(rules.isManaged("minecraft:enchantments"));
        assertFalse(rules.isManaged("minecraft:custom_name"));

        assertFalse(rules.isUnknownCustom("stelaris:material"));
        assertFalse(rules.isUnknownCustom("stelaris:amount"));
        assertTrue(rules.isUnknownCustom("stelaris:foo"));
        assertFalse(rules.isUnknownCustom("minecraft:food"));

        assertTrue(rules.isRequired("stelaris:material"));
        assertFalse(rules.isRequired("stelaris:amount"));
        assertEquals(List.of("stelaris:material"), List.copyOf(rules.required().keySet()));
        assertEquals("minecraft:dirt", rules.required().get("stelaris:material").getStringValue());
    }

    @Test
    @DisplayName("an inconsistent configuration stops the start")
    void inconsistentConfiguration_isRejected() {
        JsonMapper mapper = JsonMapper.createDefault();
        ItemComponentConfiguration valid = new ItemComponentConfiguration(
                List.of("minecraft:lore"), "stelaris", List.of("stelaris:material"));

        assertInvalid("not in the namespace", () -> new ItemComponentRules(
                new ItemComponentConfiguration(List.of(), "stelaris", List.of("minecraft:material")), List.of(), mapper));
        assertInvalid("both custom and managed", () -> new ItemComponentRules(
                new ItemComponentConfiguration(List.of("stelaris:material"), "stelaris", List.of("stelaris:material")),
                List.of(), mapper));
        assertInvalid("is managed", () -> new ItemComponentRules(valid,
                List.of(new RequiredComponentConfiguration("minecraft:lore", "[]")), mapper));
        assertInvalid("not a custom component", () -> new ItemComponentRules(valid,
                List.of(new RequiredComponentConfiguration("stelaris:amount", "1")), mapper));
        assertInvalid("listed twice", () -> new ItemComponentRules(valid, List.of(
                new RequiredComponentConfiguration("stelaris:material", "\"minecraft:dirt\""),
                new RequiredComponentConfiguration("stelaris:material", "\"minecraft:stone\"")), mapper));
        assertInvalid("no JSON", () -> new ItemComponentRules(valid,
                List.of(new RequiredComponentConfiguration("stelaris:material", "minecraft:dirt")), mapper));
        assertInvalid("custom-namespace is missing", () -> new ItemComponentRules(
                new ItemComponentConfiguration(List.of(), " ", List.of()), List.of(), mapper));
    }

    @Test
    @DisplayName("a broken value in the configuration fails the application start")
    void brokenRequiredValue_failsTheStart() {
        Map<String, Object> properties = new HashMap<>(NO_DATABASE);
        properties.put("vulpes.item-components.required[0].value", "minecraft:dirt");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> ApplicationContext.run(properties).close());
        assertTrue(rootMessage(exception).contains("no JSON"), rootMessage(exception));
    }

    private static void assertInvalid(String expected, Runnable creation) {
        IllegalStateException exception = assertThrows(IllegalStateException.class, creation::run);
        assertTrue(exception.getMessage().contains(expected), exception.getMessage());
    }

    private static String rootMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return String.valueOf(cause.getMessage());
    }
}
