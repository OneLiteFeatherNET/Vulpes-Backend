package net.onelitefeather.vulpes.backend.seed;

import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.model.dimension.AttributeOperator;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.DimensionAttributeEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTimelineEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemFlagEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;
import net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates and persists seed aggregates, and counts what it wrote.
 *
 * <p>Each aggregate is checked by {@link SeedValidator} before any of it is saved. Parents are then
 * saved first with empty child collections, and each child is saved through its own repository.
 * That works the same for every aggregate, whether or not the model cascades the collection.
 */
public final class SeedWriter {

    private final SeedRepositories repositories;
    private final SeedValidator validator;

    final List<ProjectEntity> projects = new ArrayList<>();
    final List<ItemEntity> items = new ArrayList<>();
    final List<FontEntity> fonts = new ArrayList<>();
    final List<SoundEventEntity> sounds = new ArrayList<>();
    final List<DimensionTypeEntity> dimensions = new ArrayList<>();
    final List<AttributeEntity> attributes = new ArrayList<>();
    final List<NotificationEntity> notifications = new ArrayList<>();

    public SeedWriter(SeedRepositories repositories, SeedValidator validator) {
        this.repositories = repositories;
        this.validator = validator;
    }

    public ProjectEntity project(String key, String displayName, String description, boolean labor,
                                 String projectUrl, String docuUrl) {
        ProjectEntity project = new ProjectEntity(null, displayName, key, projectUrl, docuUrl, description, labor);
        validator.project(project);
        projects.add(repositories.projects().save(project));
        return project;
    }

    public ItemBuilder item(ProjectEntity project, String key) {
        return new ItemBuilder(project, key);
    }

    public FontBuilder font(ProjectEntity project, String key) {
        return new FontBuilder(project, key);
    }

    public SoundBuilder sound(ProjectEntity project, String key) {
        return new SoundBuilder(project, key);
    }

    public DimensionBuilder dimension(ProjectEntity project, String key) {
        return new DimensionBuilder(project, key);
    }

    public AttributeEntity attribute(ProjectEntity project, String key, String uiName,
                                     double defaultValue, double maximumValue) {
        AttributeEntity attribute = new AttributeEntity(null, uiName, key, defaultValue, maximumValue, project);
        validator.attribute(attribute);
        attributes.add(repositories.attributes().save(attribute));
        return attribute;
    }

    public NotificationEntity notification(ProjectEntity project, String key, String uiName, String title,
                                           String material, String frameType, String comment) {
        NotificationEntity notification = new NotificationEntity();
        notification.setProject(project);
        notification.setKey(key);
        notification.setUiName(uiName);
        notification.setTitle(title);
        notification.setMaterial(material);
        notification.setFrameType(frameType);
        notification.setComment(comment);
        validator.notification(notification);
        notifications.add(repositories.notifications().save(notification));
        return notification;
    }

    public final class ItemBuilder {

        private final ItemEntity item = new ItemEntity();
        private final List<ItemEnchantmentEntity> enchantments = new ArrayList<>();
        private final List<ItemLoreEntity> lore = new ArrayList<>();
        private final List<ItemFlagEntity> flags = new ArrayList<>();

        private ItemBuilder(ProjectEntity project, String key) {
            item.setProject(project);
            item.setKey(key);
            item.setUiName(key);
            item.setDisplayName(key);
            item.setMaterial("minecraft:stone");
            item.setGroupName("misc");
            item.setAmount(1);
        }

        public ItemBuilder uiName(String uiName) {
            item.setUiName(uiName);
            return this;
        }

        public ItemBuilder displayName(String displayName) {
            item.setDisplayName(displayName);
            return this;
        }

        public ItemBuilder material(String material) {
            item.setMaterial(material);
            return this;
        }

        public ItemBuilder group(String groupName) {
            item.setGroupName(groupName);
            return this;
        }

        public ItemBuilder customModelData(int customModelData) {
            item.setCustomModelData(customModelData);
            return this;
        }

        public ItemBuilder amount(int amount) {
            item.setAmount(amount);
            return this;
        }

        public ItemBuilder comment(String comment) {
            item.setComment(comment);
            return this;
        }

        public ItemBuilder enchant(String name, int level) {
            return enchant(name, level, false);
        }

        public ItemBuilder enchant(String name, int level, boolean unsafe) {
            enchantments.add(new ItemEnchantmentEntity(null, name, (short) level, unsafe));
            return this;
        }

        public ItemBuilder enchant(MinecraftCatalog.Enchantment enchantment, int level) {
            return enchant(enchantment.key(), level, level > enchantment.maxLevel());
        }

        public ItemBuilder lore(String... lines) {
            for (String line : lines) {
                lore.add(new ItemLoreEntity(null, line, lore.size()));
            }
            return this;
        }

        public ItemBuilder flags(String... names) {
            for (String name : names) {
                flags.add(new ItemFlagEntity(null, name));
            }
            return this;
        }

        public ItemEntity save() {
            item.setEnchantments(enchantments);
            item.setLore(lore);
            item.setFlags(flags);
            validator.item(item);

            item.setEnchantments(new ArrayList<>());
            item.setLore(new ArrayList<>());
            item.setFlags(new ArrayList<>());
            repositories.items().save(item);

            enchantments.forEach(enchantment -> enchantment.setItem(item));
            lore.forEach(line -> line.setItem(item));
            flags.forEach(flag -> flag.setItem(item));
            repositories.itemEnchantments().saveAll(enchantments);
            repositories.itemLore().saveAll(lore);
            repositories.itemFlags().saveAll(flags);

            item.setEnchantments(enchantments);
            item.setLore(lore);
            item.setFlags(flags);
            items.add(item);
            return item;
        }
    }

    public final class FontBuilder {

        private final FontEntity font = new FontEntity();
        private final List<FontStringEntity> chars = new ArrayList<>();

        private FontBuilder(ProjectEntity project, String key) {
            font.setProject(project);
            font.setKey(key);
            font.setUiName(key);
            font.setProvider("bitmap");
            font.setMapper("font");
            font.setAscent(7);
            font.setHeight(8);
        }

        public FontBuilder uiName(String uiName) {
            font.setUiName(uiName);
            return this;
        }

        public FontBuilder provider(String provider) {
            font.setProvider(provider);
            return this;
        }

        public FontBuilder texture(String texturePath) {
            font.setTexturePath(texturePath);
            return this;
        }

        public FontBuilder metrics(int ascent, int height) {
            font.setAscent(ascent);
            font.setHeight(height);
            return this;
        }

        public FontBuilder comment(String comment) {
            font.setComment(comment);
            return this;
        }

        public FontBuilder chars(String... lines) {
            for (String line : lines) {
                chars.add(new FontStringEntity(null, line, this.chars.size()));
            }
            return this;
        }

        public FontEntity save() {
            font.setChars(chars);
            validator.font(font);

            font.setChars(new ArrayList<>());
            repositories.fonts().save(font);
            chars.forEach(line -> line.setFont(font));
            repositories.fontChars().saveAll(chars);
            font.setChars(chars);
            fonts.add(font);
            return font;
        }
    }

    public final class SoundBuilder {

        private final ProjectEntity project;
        private final String key;
        private String uiName;
        private String keyName;
        private boolean replace;
        private String subTitle;
        private final List<SoundFileSource> sources = new ArrayList<>();

        private SoundBuilder(ProjectEntity project, String key) {
            this.project = project;
            this.key = key;
            this.uiName = key;
            this.keyName = key;
        }

        public SoundBuilder uiName(String uiName) {
            this.uiName = uiName;
            return this;
        }

        public SoundBuilder keyName(String keyName) {
            this.keyName = keyName;
            return this;
        }

        public SoundBuilder replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public SoundBuilder subTitle(String subTitle) {
            this.subTitle = subTitle;
            return this;
        }

        /**
         * Adds a plain file source with vanilla defaults (volume 1, pitch 1, weight 1, distance 16).
         */
        public SoundBuilder file(String name) {
            return source(name, 1.0f, 1.0f, 1, false, 16, false, "file");
        }

        public SoundBuilder source(String name, float volume, float pitch, int weight, boolean stream,
                                   int attenuationDistance, boolean preload, String type) {
            sources.add(new SoundFileSource(null, name, volume, pitch, weight, stream, attenuationDistance,
                    preload, type));
            return this;
        }

        public SoundEventEntity save() {
            SoundEventEntity sound = new SoundEventEntity(null, uiName, key, keyName, replace, subTitle,
                    sources, project);
            validator.sound(sound);

            sound.setSoundData(new ArrayList<>());
            repositories.sounds().save(sound);
            sources.forEach(source -> source.setSoundEvent(sound));
            repositories.soundSources().saveAll(sources);
            sound.setSoundData(sources);
            sounds.add(sound);
            return sound;
        }
    }

    public final class DimensionBuilder {

        private final DimensionTypeEntity dimension = new DimensionTypeEntity();
        private final List<DimensionAttributeEntity> attributes = new ArrayList<>();
        private final List<DimensionTimelineEntity> timelines = new ArrayList<>();

        /**
         * Starts from vanilla overworld values.
         */
        private DimensionBuilder(ProjectEntity project, String key) {
            dimension.setProject(project);
            dimension.setKey(key);
            dimension.setUiName(key);
            dimension.setHasSkylight(true);
            dimension.setCoordinateScale(1.0);
            dimension.setMinY(-64);
            dimension.setHeight(384);
            dimension.setLogicalHeight(384);
            dimension.setInfiniburn(MinecraftCatalog.INFINIBURN_OVERWORLD);
            dimension.setAmbientLight(0.0f);
            dimension.setMonsterSpawnLightLevel("constant:0");
            dimension.setMonsterSpawnBlockLightLimit(0);
            dimension.setSkybox(Skybox.OVERWORLD);
            dimension.setCardinalLight(CardinalLight.DEFAULT);
            dimension.setDefaultClock(MinecraftCatalog.CLOCK_OVERWORLD);
        }

        public DimensionBuilder uiName(String uiName) {
            dimension.setUiName(uiName);
            return this;
        }

        public DimensionBuilder flags(boolean fixedTime, boolean skylight, boolean ceiling, boolean dragonFight) {
            dimension.setHasFixedTime(fixedTime);
            dimension.setHasSkylight(skylight);
            dimension.setHasCeiling(ceiling);
            dimension.setHasEnderDragonFight(dragonFight);
            return this;
        }

        public DimensionBuilder coordinateScale(double coordinateScale) {
            dimension.setCoordinateScale(coordinateScale);
            return this;
        }

        /**
         * Sets {@code minY} and {@code height}; the logical height follows the height.
         */
        public DimensionBuilder bounds(int minY, int height) {
            dimension.setMinY(minY);
            dimension.setHeight(height);
            dimension.setLogicalHeight(height);
            return this;
        }

        public DimensionBuilder logicalHeight(int logicalHeight) {
            dimension.setLogicalHeight(logicalHeight);
            return this;
        }

        public DimensionBuilder infiniburn(String infiniburn) {
            dimension.setInfiniburn(infiniburn);
            return this;
        }

        public DimensionBuilder ambientLight(float ambientLight) {
            dimension.setAmbientLight(ambientLight);
            return this;
        }

        public DimensionBuilder monsterSpawn(String lightLevel, int blockLightLimit) {
            dimension.setMonsterSpawnLightLevel(lightLevel);
            dimension.setMonsterSpawnBlockLightLimit(blockLightLimit);
            return this;
        }

        public DimensionBuilder sky(Skybox skybox, CardinalLight cardinalLight) {
            dimension.setSkybox(skybox);
            dimension.setCardinalLight(cardinalLight);
            return this;
        }

        public DimensionBuilder defaultClock(String defaultClock) {
            dimension.setDefaultClock(defaultClock);
            return this;
        }

        public DimensionBuilder attribute(EnvironmentAttributeKey key, AttributeOperator operator, String value) {
            attributes.add(new DimensionAttributeEntity(null, key, operator, value));
            return this;
        }

        public DimensionBuilder attribute(EnvironmentAttributeKey key, String value) {
            return attribute(key, AttributeOperator.OVERRIDE, value);
        }

        public DimensionBuilder timelines(String... keys) {
            for (String key : keys) {
                timelines.add(new DimensionTimelineEntity(null, key));
            }
            return this;
        }

        public DimensionTypeEntity save() {
            dimension.setAttributes(attributes);
            dimension.setTimelines(timelines);
            validator.dimension(dimension);

            dimension.setAttributes(new ArrayList<>());
            dimension.setTimelines(new ArrayList<>());
            repositories.dimensions().save(dimension);
            attributes.forEach(attribute -> attribute.setDimensionType(dimension));
            timelines.forEach(timeline -> timeline.setDimensionType(dimension));
            repositories.dimensionAttributes().saveAll(attributes);
            repositories.dimensionTimelines().saveAll(timelines);
            dimension.setAttributes(attributes);
            dimension.setTimelines(timelines);
            dimensions.add(dimension);
            return dimension;
        }
    }
}
