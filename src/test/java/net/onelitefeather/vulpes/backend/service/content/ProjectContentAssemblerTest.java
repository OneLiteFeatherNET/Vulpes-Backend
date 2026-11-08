package net.onelitefeather.vulpes.backend.service.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.asset.AssetKeySpace;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkEntity;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkRole;
import net.onelitefeather.vulpes.api.model.asset.AssetTargetType;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetRevisionEntity;
import net.onelitefeather.vulpes.api.model.dimension.AttributeOperator;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.DimensionAttributeEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTimelineEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.api.model.expression.ExpressionEntity;
import net.onelitefeather.vulpes.api.model.expression.ExpressionFunctionEntity;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariable;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.api.model.item.ItemComponentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.api.model.pack.PackMetaEntity;
import net.onelitefeather.vulpes.api.model.pack.PackType;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.AssetDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.FontDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.ItemDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.SoundDTO;
import org.junit.jupiter.api.Test;

class ProjectContentAssemblerTest {

    private final ProjectContentAssembler assembler = new ProjectContentAssembler(JsonMapper.createDefault());

    @Test
    void componentsAreNestedUnderTheirItem() {
        Rows rows = new Rows();
        ItemEntity sword = rows.item(1, "sword");
        ItemEntity shield = rows.item(2, "shield");
        rows.itemComponents.add(component(10, "minecraft:max_stack_size", "{\"value\":1}", sword));
        rows.itemComponents.add(component(11, "minecraft:unbreakable", "{}", shield));

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("minecraft:max_stack_size"), componentKeys(itemNamed(content, "sword")),
                "the components of the sword item must be nested under the sword");
        assertEquals(List.of("minecraft:unbreakable"), componentKeys(itemNamed(content, "shield")),
                "the components of the shield item must be nested under the shield");
    }

    @Test
    void loreLinesAreNestedUnderTheirItem() {
        Rows rows = new Rows();
        ItemEntity sword = rows.item(1, "sword");
        ItemEntity shield = rows.item(2, "shield");
        rows.itemLore.add(lore(10, "Sharp", 0, sword));
        rows.itemLore.add(lore(11, "Heavy", 0, shield));

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("Sharp"), itemNamed(content, "sword").lore().stream().map(ProjectContentDTO.LoreDTO::text).toList(),
                "the lore of the sword item must be nested under the sword");
    }

    @Test
    void enchantmentsAreNestedUnderTheirItem() {
        Rows rows = new Rows();
        ItemEntity sword = rows.item(1, "sword");
        rows.itemEnchantments.add(enchantment(10, "sharpness", (short) 5, sword));

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("sharpness"),
                itemNamed(content, "sword").enchantments().stream().map(ProjectContentDTO.EnchantmentDTO::name).toList(),
                "the enchantments of the item must be nested under it");
    }

    @Test
    void fontCharsAreNestedUnderTheirFont() {
        Rows rows = new Rows();
        FontEntity icons = rows.font(1, "icons");
        rows.fontChars.add(fontChar(10, "", 0, icons));

        ProjectContentDTO content = assembler.assemble(rows.build());

        FontDTO font = fontNamed(content, "icons");
        assertEquals(List.of(""), font.chars().stream().map(ProjectContentDTO.FontCharDTO::line).toList(),
                "the chars of the font must be nested under the font");
    }

    @Test
    void soundSourcesAreNestedUnderTheirSound() {
        Rows rows = new Rows();
        SoundEventEntity click = rows.sound(1, "click");
        rows.soundSources.add(source(10, "click_a", click));

        ProjectContentDTO content = assembler.assemble(rows.build());

        SoundDTO sound = soundNamed(content, "click");
        assertEquals(List.of("click_a"), sound.sources().stream().map(ProjectContentDTO.SoundSourceDTO::name).toList(),
                "the sources of the sound must be nested under the sound");
    }

    @Test
    void dimensionAttributesAndTimelinesAreNestedUnderTheirDimension() {
        Rows rows = new Rows();
        DimensionTypeEntity overworld = rows.dimension(1, "overworld");
        DimensionTypeEntity nether = rows.dimension(2, "nether");
        rows.dimensionAttributes.add(dimensionAttribute(10, overworld));
        rows.dimensionTimelines.add(timeline(11, "day", overworld));

        ProjectContentDTO content = assembler.assemble(rows.build());

        ProjectContentDTO.DimensionDTO dimension = dimensionNamed(content, "overworld");
        assertEquals(1, dimension.attributes().size(), "the attributes must be nested under their dimension");
        assertEquals(List.of("day"), dimension.timelines().stream().map(ProjectContentDTO.DimensionTimelineDTO::timelineKey).toList(),
                "the timelines must be nested under their dimension");
        assertTrue(dimensionNamed(content, "nether").attributes().isEmpty(),
                "a dimension without attributes must not receive the attributes of another dimension");
    }

    @Test
    void assetRevisionsAndLinksAreNestedUnderTheirAsset() {
        Rows rows = new Rows();
        ProjectAssetEntity texture = rows.asset(1, AssetKeySpace.TEXTURE, "stone");
        rows.assetRevisions.add(revision(10, 1, texture));
        rows.assetLinks.add(link(20, AssetTargetType.ITEM, 3L, AssetLinkRole.TEXTURE, texture));

        ProjectContentDTO content = assembler.assemble(rows.build());

        AssetDTO asset = assetNamed(content, "stone");
        assertEquals(List.of(1), asset.revisions().stream().map(ProjectContentDTO.AssetRevisionDTO::number).toList(),
                "the revisions must be nested under their asset");
        assertEquals(List.of(AssetTargetType.ITEM), asset.links().stream().map(ProjectContentDTO.AssetLinkDTO::targetType).toList(),
                "the links must be nested under their asset");
    }

    @Test
    void itemsAreOrderedByKey() {
        Rows rows = new Rows();
        rows.item(1, "b");
        rows.item(2, "a");
        rows.item(3, "c");

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("a", "b", "c"), content.items().stream().map(ItemDTO::key).toList(),
                "items must be ordered by key");
    }

    @Test
    void equalItemKeysAreOrderedById() {
        Rows rows = new Rows();
        rows.item(2, "same");
        rows.item(1, "same");

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of(id(1), id(2)), content.items().stream().map(ItemDTO::id).toList(),
                "items with the same key must be ordered by id");
    }

    @Test
    void expressionsAreOrderedByUiName() {
        Rows rows = new Rows();
        rows.expression(1, "Zeta");
        rows.expression(2, "Alpha");

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("Alpha", "Zeta"), content.expressions().stream().map(ExpressionDTO::uiName).toList(),
                "expressions must be ordered by their UI name");
    }

    @Test
    void assetsAreOrderedByKeySpaceThenKey() {
        Rows rows = new Rows();
        rows.asset(1, AssetKeySpace.TEXTURE, "b");
        rows.asset(2, AssetKeySpace.TEXTURE, "a");
        rows.asset(3, AssetKeySpace.MODEL, "z");

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("MODEL/z", "TEXTURE/a", "TEXTURE/b"),
                content.assets().stream().map(asset -> asset.keySpace() + "/" + asset.key()).toList(),
                "assets must be ordered by key space first and by key second");
    }

    @Test
    void loreLinesAreOrderedByOrderIndex() {
        Rows rows = new Rows();
        ItemEntity sword = rows.item(1, "sword");
        rows.itemLore.add(lore(10, "second", 2, sword));
        rows.itemLore.add(lore(11, "zero", 0, sword));
        rows.itemLore.add(lore(12, "first", 1, sword));

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("zero", "first", "second"),
                itemNamed(content, "sword").lore().stream().map(ProjectContentDTO.LoreDTO::text).toList(),
                "lore lines must be ordered by their order index");
    }

    @Test
    void componentsAreOrderedByComponentKey() {
        Rows rows = new Rows();
        ItemEntity sword = rows.item(1, "sword");
        rows.itemComponents.add(component(10, "minecraft:unbreakable", "{}", sword));
        rows.itemComponents.add(component(11, "minecraft:max_stack_size", "{}", sword));

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("minecraft:max_stack_size", "minecraft:unbreakable"), componentKeys(itemNamed(content, "sword")),
                "components must be ordered by component key");
    }

    @Test
    void enchantmentsAreOrderedByName() {
        Rows rows = new Rows();
        ItemEntity sword = rows.item(1, "sword");
        rows.itemEnchantments.add(enchantment(10, "unbreaking", (short) 3, sword));
        rows.itemEnchantments.add(enchantment(11, "sharpness", (short) 5, sword));

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("sharpness", "unbreaking"),
                itemNamed(content, "sword").enchantments().stream().map(ProjectContentDTO.EnchantmentDTO::name).toList(),
                "enchantments must be ordered by name");
    }

    @Test
    void soundSourcesAreOrderedByName() {
        Rows rows = new Rows();
        SoundEventEntity click = rows.sound(1, "click");
        rows.soundSources.add(source(10, "zeta", click));
        rows.soundSources.add(source(11, "alpha", click));

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertEquals(List.of("alpha", "zeta"),
                soundNamed(content, "click").sources().stream().map(ProjectContentDTO.SoundSourceDTO::name).toList(),
                "sound sources must be ordered by name");
    }

    @Test
    void insertionOrderDoesNotChangeTheResult() {
        Rows forward = sampleRows(false);
        Rows backward = sampleRows(true);

        ProjectContentDTO first = assembler.assemble(forward.build());
        ProjectContentDTO second = assembler.assemble(backward.build());

        assertEquals(first, second, "rows loaded in a different order must produce the same content");
    }

    @Test
    void componentValueIsDeliveredAsJsonObjectNotAsString() {
        Rows rows = new Rows();
        ItemEntity sword = rows.item(1, "sword");
        rows.itemComponents.add(component(10, "minecraft:max_stack_size", "{\"value\":64}", sword));

        ProjectContentDTO content = assembler.assemble(rows.build());

        JsonNode value = componentNamed(content, "sword", "minecraft:max_stack_size").value();
        assertTrue(value.isObject(), "the stored component value must be exported as a JSON object");
        assertFalse(value.isString(), "the stored component value must not be exported as a JSON string");
    }

    @Test
    void emptyInputGivesEmptyListsNotNull() {
        ProjectContentDTO content = assembler.assemble(new Rows().build());

        assertNotNull(content.items(), "items must be an empty list, not null");
        assertTrue(content.items().isEmpty(), "items must be empty for a project without items");
        assertNotNull(content.assets(), "assets must be an empty list, not null");
        assertTrue(content.assets().isEmpty(), "assets must be empty for a project without assets");
        assertNotNull(content.packs(), "packs must be an empty list, not null");
        assertTrue(content.packs().isEmpty(), "packs must be empty for a project without packs");
    }

    @Test
    void assetWithoutRevisionsHasEmptyRevisionList() {
        Rows rows = new Rows();
        rows.asset(1, AssetKeySpace.TEXTURE, "stone");

        ProjectContentDTO content = assembler.assemble(rows.build());

        assertNotNull(assetNamed(content, "stone").revisions(), "revisions must be an empty list, not null");
        assertTrue(assetNamed(content, "stone").revisions().isEmpty(),
                "an asset without revision rows must export no revisions");
    }

    @Test
    void assembledContentHasNoRevision() {
        ProjectContentDTO content = assembler.assemble(new Rows().build());

        assertNull(content.revision(), "the assembler must leave the revision to the caller");
    }

    @Test
    void packFeaturesAreSortedAndFormatRangeIsKept() {
        Rows rows = new Rows();
        rows.packs.add(new PackMetaEntity(id(1), rows.project, PackType.RESOURCE, "{\"text\":\"pack\"}",
                1, 0, 2, 3, Set.of("zeta", "alpha")));

        ProjectContentDTO content = assembler.assemble(rows.build());

        ProjectContentDTO.PackDTO pack = content.packs().get(0);
        assertEquals(List.of("alpha", "zeta"), pack.enabledFeatures(), "enabled features must be sorted");
        assertEquals(Integer.valueOf(3), pack.maxFormatMinor(), "the maximum minor format must be kept");
    }

    private static Rows sampleRows(boolean reversed) {
        Rows rows = new Rows();
        ItemEntity sword = rows.item(1, "sword");
        ItemEntity shield = rows.item(2, "shield");
        rows.itemComponents.add(component(10, "minecraft:unbreakable", "{}", sword));
        rows.itemComponents.add(component(11, "minecraft:max_stack_size", "{\"value\":1}", sword));
        rows.itemLore.add(lore(12, "Sharp", 0, sword));
        rows.itemLore.add(lore(13, "Heavy", 1, shield));
        rows.itemEnchantments.add(enchantment(14, "unbreaking", (short) 3, sword));
        rows.itemEnchantments.add(enchantment(15, "sharpness", (short) 5, sword));
        rows.asset(3, AssetKeySpace.TEXTURE, "b");
        rows.asset(4, AssetKeySpace.TEXTURE, "a");
        if (reversed) {
            Collections.reverse(rows.items);
            Collections.reverse(rows.itemComponents);
            Collections.reverse(rows.itemLore);
            Collections.reverse(rows.itemEnchantments);
            Collections.reverse(rows.assets);
        }
        return rows;
    }

    private static List<String> componentKeys(ItemDTO item) {
        return item.components().stream().map(ProjectContentDTO.ComponentDTO::componentKey).toList();
    }

    private static ItemDTO itemNamed(ProjectContentDTO content, String key) {
        return content.items().stream().filter(item -> item.key().equals(key)).findFirst()
                .orElseThrow(() -> new AssertionError("no item with key " + key));
    }

    private static ProjectContentDTO.ComponentDTO componentNamed(ProjectContentDTO content, String item, String key) {
        return itemNamed(content, item).components().stream().filter(c -> c.componentKey().equals(key)).findFirst()
                .orElseThrow(() -> new AssertionError("no component " + key + " on item " + item));
    }

    private static FontDTO fontNamed(ProjectContentDTO content, String key) {
        return content.fonts().stream().filter(font -> font.key().equals(key)).findFirst()
                .orElseThrow(() -> new AssertionError("no font with key " + key));
    }

    private static SoundDTO soundNamed(ProjectContentDTO content, String key) {
        return content.sounds().stream().filter(sound -> sound.key().equals(key)).findFirst()
                .orElseThrow(() -> new AssertionError("no sound with key " + key));
    }

    private static ProjectContentDTO.DimensionDTO dimensionNamed(ProjectContentDTO content, String key) {
        return content.dimensions().stream().filter(dimension -> dimension.key().equals(key)).findFirst()
                .orElseThrow(() -> new AssertionError("no dimension with key " + key));
    }

    private static AssetDTO assetNamed(ProjectContentDTO content, String key) {
        return content.assets().stream().filter(asset -> asset.key().equals(key)).findFirst()
                .orElseThrow(() -> new AssertionError("no asset with key " + key));
    }

    private static UUID id(long number) {
        return new UUID(0L, number);
    }

    private static ItemComponentEntity component(long id, String key, String value, ItemEntity item) {
        ItemComponentEntity component = new ItemComponentEntity(id(id), key, value);
        component.setItem(item);
        return component;
    }

    private static ItemLoreEntity lore(long id, String text, int orderIndex, ItemEntity item) {
        ItemLoreEntity line = new ItemLoreEntity(id(id), text, orderIndex);
        line.setItem(item);
        return line;
    }

    private static ItemEnchantmentEntity enchantment(long id, String name, short level, ItemEntity item) {
        ItemEnchantmentEntity enchantment = new ItemEnchantmentEntity(id(id), name, level, false);
        enchantment.setItem(item);
        return enchantment;
    }

    private static FontStringEntity fontChar(long id, String line, int orderIndex, FontEntity font) {
        FontStringEntity entry = new FontStringEntity(id(id), line, orderIndex);
        entry.setFont(font);
        return entry;
    }

    private static SoundFileSource source(long id, String name, SoundEventEntity sound) {
        SoundFileSource source = new SoundFileSource(id(id), name, 1f, 1f, 1, true, 16, false, "type_file");
        source.setSoundEvent(sound);
        return source;
    }

    private static DimensionAttributeEntity dimensionAttribute(long id, DimensionTypeEntity dimension) {
        DimensionAttributeEntity attribute = new DimensionAttributeEntity();
        attribute.setId(id(id));
        attribute.setAttributeKey(EnvironmentAttributeKey.values()[0]);
        attribute.setOperator(AttributeOperator.OVERRIDE);
        attribute.setAttributeValue("1");
        attribute.setDimensionType(dimension);
        return attribute;
    }

    private static DimensionTimelineEntity timeline(long id, String key, DimensionTypeEntity dimension) {
        DimensionTimelineEntity timeline = new DimensionTimelineEntity(id(id), key);
        timeline.setDimensionType(dimension);
        return timeline;
    }

    private static ProjectAssetRevisionEntity revision(long id, int number, ProjectAssetEntity asset) {
        ProjectAssetRevisionEntity revision = new ProjectAssetRevisionEntity();
        revision.setId(id(id));
        revision.setNumber(number);
        revision.setSha1("0".repeat(40));
        revision.setSize(10L);
        revision.setAsset(asset);
        return revision;
    }

    private static AssetLinkEntity link(long id, AssetTargetType type, long targetId, AssetLinkRole role,
                                        ProjectAssetEntity asset) {
        AssetLinkEntity link = new AssetLinkEntity();
        link.setId(id(id));
        link.setTargetType(type);
        link.setTargetId(id(targetId));
        link.setRole(role);
        link.setAsset(asset);
        return link;
    }

    /**
     * The rows of one project, built in memory. Every test creates its own instance.
     */
    private static final class Rows {
        final ProjectEntity project = new ProjectEntity(id(100), "Project", "project", null, null, null, false);
        final List<ItemEntity> items = new ArrayList<>();
        final List<ItemComponentEntity> itemComponents = new ArrayList<>();
        final List<ItemLoreEntity> itemLore = new ArrayList<>();
        final List<ItemEnchantmentEntity> itemEnchantments = new ArrayList<>();
        final List<AttributeEntity> attributes = new ArrayList<>();
        final List<FontEntity> fonts = new ArrayList<>();
        final List<FontStringEntity> fontChars = new ArrayList<>();
        final List<SoundEventEntity> sounds = new ArrayList<>();
        final List<SoundFileSource> soundSources = new ArrayList<>();
        final List<ExpressionFunctionEntity> expressionFunctions = new ArrayList<>();
        final List<ExpressionEntity> expressions = new ArrayList<>();
        final List<ProjectAssetEntity> assets = new ArrayList<>();
        final List<ProjectAssetRevisionEntity> assetRevisions = new ArrayList<>();
        final List<AssetLinkEntity> assetLinks = new ArrayList<>();
        final List<AdvancementEntity> advancements = new ArrayList<>();
        final List<DimensionTypeEntity> dimensions = new ArrayList<>();
        final List<DimensionAttributeEntity> dimensionAttributes = new ArrayList<>();
        final List<DimensionTimelineEntity> dimensionTimelines = new ArrayList<>();
        final List<PackMetaEntity> packs = new ArrayList<>();

        ItemEntity item(long id, String key) {
            ItemEntity item = new ItemEntity();
            item.setId(id(id));
            item.setKey(key);
            item.setUiName(key);
            item.setProject(project);
            items.add(item);
            return item;
        }

        FontEntity font(long id, String key) {
            FontEntity font = new FontEntity();
            font.setId(id(id));
            font.setKey(key);
            font.setUiName(key);
            font.setProject(project);
            fonts.add(font);
            return font;
        }

        SoundEventEntity sound(long id, String key) {
            SoundEventEntity sound = new SoundEventEntity();
            sound.setId(id(id));
            sound.setKey(key);
            sound.setUiName(key);
            sound.setKeyName(key);
            sound.setProject(project);
            sounds.add(sound);
            return sound;
        }

        DimensionTypeEntity dimension(long id, String key) {
            DimensionTypeEntity dimension = new DimensionTypeEntity();
            dimension.setId(id(id));
            dimension.setKey(key);
            dimension.setUiName(key);
            dimension.setSkybox(Skybox.NONE);
            dimension.setCardinalLight(CardinalLight.DEFAULT);
            dimension.setProject(project);
            dimensions.add(dimension);
            return dimension;
        }

        ExpressionEntity expression(long id, String uiName) {
            ExpressionEntity expression = new ExpressionEntity(id(id), uiName, "1 + 1", null,
                    List.<ExpressionVariable>of(), project);
            expressions.add(expression);
            return expression;
        }

        ProjectAssetEntity asset(long id, AssetKeySpace keySpace, String key) {
            ProjectAssetEntity asset = new ProjectAssetEntity();
            asset.setId(id(id));
            asset.setProject(project);
            asset.setKey(key);
            asset.setDisplayName(key);
            asset.setKeySpace(keySpace);
            asset.setKind(keySpace == AssetKeySpace.MODEL ? AssetKind.MODEL : AssetKind.TEXTURE);
            asset.setFileName(key + ".png");
            asset.setContentType(asset.getKind().contentType());
            asset.setSize(10L);
            asset.setSha1("0".repeat(40));
            assets.add(asset);
            return asset;
        }

        ProjectContentRows build() {
            return new ProjectContentRows(project, items, itemComponents, itemLore, itemEnchantments, attributes, fonts,
                    fontChars, sounds, soundSources, expressionFunctions, expressions, assets, assetRevisions, assetLinks,
                    advancements, dimensions, dimensionAttributes, dimensionTimelines, packs);
        }
    }
}
