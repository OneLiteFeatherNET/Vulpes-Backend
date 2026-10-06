package net.onelitefeather.vulpes.backend.seed;

import io.micronaut.context.annotation.Requires;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.validation.validator.Validator;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import net.onelitefeather.vulpes.api.model.AbstractEntity;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemComponentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.service.item.ItemComponentRules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks each seed aggregate against the request DTO constraints the API enforces, before it is
 * persisted, and fails with a message naming the offending record and field.
 *
 * <p>Only the {@code Default} group is used: the API does not apply the {@code Create}/{@code Update}
 * group markers at runtime (see design.md, D3), so validating with them would reject realistic data
 * the API happily accepts.
 */
@Singleton
@Requires(env = SeedRunner.SEED_ENVIRONMENT)
public class SeedValidator {

    private final Validator validator;
    private final ItemComponentRules componentRules;
    private final JsonMapper jsonMapper;

    public SeedValidator(Validator validator, ItemComponentRules componentRules, JsonMapper jsonMapper) {
        this.validator = validator;
        this.componentRules = componentRules;
        this.jsonMapper = jsonMapper;
    }

    void project(ProjectEntity project) {
        List<String> problems = new ArrayList<>();
        check(problems, "project " + project.getKey(), new ProjectModelDTO(project.getId(), project.getDisplayName(),
                project.getKey(), project.getProjectUrl(), project.getDocuUrl(), project.getDescription(),
                project.isLabor()));
        fail(problems);
    }

    void item(ItemEntity item) {
        List<String> problems = new ArrayList<>();
        String where = where("item", item);
        check(problems, where, new ItemModelDTO(item.getId(), item.getUiName(), item.getKey(), item.getComment(),
                item.getGroupName()));
        item.getEnchantments().forEach(e -> check(problems, where + " enchantment " + e.getName(),
                new ItemEnchantmentDTO(e.getId(), e.getName(), e.getLevel(), e.isUnsafe())));
        item.getLore().forEach(l -> check(problems, where + " lore #" + l.getOrderIndex(),
                new ItemLoreDTO(l.getId(), l.getText())));
        item.getComponents().forEach(c -> component(problems, where + " component " + c.getComponentKey(),
                c.getComponentKey(), c.getComponentValue()));
        componentRules.required().keySet().stream()
                .filter(key -> item.getComponents().stream().noneMatch(c -> c.getComponentKey().equals(key)))
                .forEach(key -> problems.add(where + ": the required component " + key + " is missing"));
        fail(problems);
    }

    void font(FontEntity font) {
        List<String> problems = new ArrayList<>();
        String where = where("font", font);
        check(problems, where, new FontModelDTO(font.getId(), font.getUiName(), font.getKey(), font.getProvider(),
                font.getMapper(), font.getTexturePath(), font.getComment(), font.getAscent(), font.getHeight()));
        font.getChars().forEach(c -> check(problems, where + " char #" + c.getOrderIndex(),
                new FontStringDTO(c.getId(), c.getLine(), c.getOrderIndex())));
        fail(problems);
    }

    void sound(SoundEventEntity sound) {
        List<String> problems = new ArrayList<>();
        String where = where("sound", sound);
        check(problems, where, new SoundEventDTO(sound.getId(), sound.getUiName(), sound.getKey(),
                sound.getKeyName(), sound.getSubTitle(), sound.getComment()));
        sound.getSoundData().forEach(s -> check(problems, where + " source " + s.getName(),
                new SoundFileSourceDTO(s.getId(), s.getName(), s.getVolume(), s.getPitch(), s.getWeight(),
                        s.isStreamable(), s.getAttenuationDistance(), s.isPreloadable(), s.getType())));
        fail(problems);
    }

    void attribute(AttributeEntity attribute) {
        List<String> problems = new ArrayList<>();
        check(problems, where("attribute", attribute), new AttributeModelDTO(attribute.getId(),
                attribute.getUiName(), attribute.getKey(), attribute.getDefaultValue(), attribute.getMaximumValue(),
                attribute.getComment()));
        fail(problems);
    }

    void notification(NotificationEntity n) {
        List<String> problems = new ArrayList<>();
        check(problems, where("notification", n), new NotificationModelDTO(n.getId(), n.getUiName(), n.getKey(),
                n.getComment(), n.getMaterial(), n.getFrameType(), n.getTitle()));
        fail(problems);
    }

    void dimension(DimensionTypeEntity d) {
        List<String> problems = new ArrayList<>();
        String where = where("dimension", d);
        check(problems, where, new DimensionModelDTO(d.getId(), d.getUiName(), d.getKey(), d.isHasFixedTime(),
                d.isHasSkylight(), d.isHasCeiling(), d.isHasEnderDragonFight(), d.getCoordinateScale(), d.getMinY(),
                d.getHeight(), d.getLogicalHeight(), d.getInfiniburn(), d.getAmbientLight(),
                d.getMonsterSpawnLightLevel(), d.getMonsterSpawnBlockLightLimit(), d.getSkybox(),
                d.getCardinalLight(), d.getDefaultClock()));
        d.getAttributes().forEach(a -> check(problems, where + " attribute " + a.getAttributeKey(),
                new DimensionAttributeDTO(a.getId(), a.getAttributeKey(), a.getOperator(), a.getAttributeValue())));
        d.getTimelines().forEach(t -> check(problems, where + " timeline " + t.getTimelineKey(),
                new DimensionTimelineDTO(t.getId(), t.getTimelineKey())));
        fail(problems);
    }

    /**
     * Checks a component like the component endpoints do: the DTO constraints and the configured rules.
     */
    private void component(List<String> problems, String where, String key, String value) {
        JsonNode node;
        try {
            node = jsonMapper.readValue(value, JsonNode.class);
        } catch (IOException exception) {
            problems.add(where + ": the value is no JSON (was " + value + ")");
            return;
        }
        check(problems, where, new ItemComponentDTO(null, key, node));
        if (componentRules.isManaged(key)) {
            problems.add(where + ": the component has its own storage and can't be set as a component");
        }
        if (componentRules.isUnknownCustom(key)) {
            problems.add(where + ": the component is not a known component of its namespace");
        }
    }

    private static void fail(List<String> problems) {
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Seed data violates API validation:\n  " + String.join("\n  ", problems));
        }
    }

    private <T> void check(List<String> problems, String where, T dto) {
        for (ConstraintViolation<T> violation : validator.validate(dto)) {
            problems.add(where + ": " + violation.getPropertyPath() + " " + violation.getMessage()
                    + " (was " + violation.getInvalidValue() + ")");
        }
    }

    private static String where(String type, AbstractEntity entity) {
        return type + " " + entity.getProject().getKey() + "/" + entity.getKey();
    }
}
