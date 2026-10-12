package net.onelitefeather.vulpes.backend.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the {@code key} constraint of every model DTO in the groups the controllers validate with.
 * The per-DTO validation tests only cover the default group, which never sees these constraints.
 */
class ModelKeyValidationGroupTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        try (ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    /**
     * Each DTO as a factory for an instance that is valid except for the given id and key.
     */
    private static Stream<Arguments> dtos() {
        return Stream.of(
                dto("attribute", (id, key) -> new AttributeModelDTO(id, "Speed", key, 1.0, 10.0, null)),
                dto("font", (id, key) -> new FontModelDTO(id, "Font", key, "bitmap", "mapper", "texture.png", null, 1, 1)),
                dto("item", (id, key) -> new ItemModelDTO(id, "Item", key, null, "group")),
                dto("sound", (id, key) -> new SoundEventDTO(id, "Sound", key, "block.stone.step", null, null)),
                dto("advancement", (id, key) -> new AdvancementModelDTO(id, "Advancement", key, null, "STONE",
                        AdvancementFrameType.TASK, null, null, null, null, 0f, 0f, true, true, false)),
                dto("dimension", (id, key) -> new DimensionModelDTO(id, "Overworld", key, false, true, false, false,
                        1.0, -64, 384, 384, "#minecraft:infiniburn_overworld", 0.0f,
                        "constant:11", 15, Skybox.OVERWORLD, CardinalLight.DEFAULT, null)),
                dto("project", (id, key) -> new ProjectModelDTO(id, "Project", key, null, null, null, false))
        );
    }

    private static Arguments dto(String name, BiFunction<UUID, String, Object> factory) {
        return Arguments.of(name, factory);
    }

    private static boolean hasKeyViolation(Object dto, Class<?> group) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto, group);
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("key"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dtos")
    void createGroupRejectsMissingKey(String name, BiFunction<UUID, String, Object> factory) {
        assertTrue(hasKeyViolation(factory.apply(null, null), ValidationGroup.Create.class));
        assertTrue(hasKeyViolation(factory.apply(null, " "), ValidationGroup.Create.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dtos")
    void createGroupAcceptsKey(String name, BiFunction<UUID, String, Object> factory) {
        assertTrue(validator.validate(factory.apply(null, "my-key"), ValidationGroup.Create.class).isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dtos")
    void updateGroupRejectsMissingKey(String name, BiFunction<UUID, String, Object> factory) {
        assertTrue(hasKeyViolation(factory.apply(UUID.randomUUID(), null), ValidationGroup.Update.class));
        assertTrue(hasKeyViolation(factory.apply(UUID.randomUUID(), " "), ValidationGroup.Update.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dtos")
    void updateGroupAcceptsKey(String name, BiFunction<UUID, String, Object> factory) {
        assertFalse(hasKeyViolation(factory.apply(UUID.randomUUID(), "my-key"), ValidationGroup.Update.class));
    }
}
