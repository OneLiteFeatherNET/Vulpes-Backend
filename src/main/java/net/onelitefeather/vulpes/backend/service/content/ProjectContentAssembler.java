package net.onelitefeather.vulpes.backend.service.content;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkEntity;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetRevisionEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionAttributeEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTimelineEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.expression.ExpressionEntity;
import net.onelitefeather.vulpes.api.model.expression.ExpressionFunctionEntity;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariable;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.api.model.item.ItemComponentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.api.model.pack.PackMetaEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.AdvancementDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.AssetDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.AssetLinkDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.AssetRevisionDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.AttributeDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.ComponentDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.DimensionAttributeDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.DimensionDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.DimensionTimelineDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.EnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.ExpressionFunctionDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.ExpressionVariableDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.FontCharDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.FontDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.ItemDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.LoreDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.PackDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.ProjectDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.SoundDTO;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO.SoundSourceDTO;

/**
 * Turns the loaded rows of one project into the content export, without touching the database.
 *
 * <p>Children are grouped under their parent id. Every list is ordered by content with the id as the final
 * tie-break, so the result does not depend on the order in which the rows were loaded. Strings are
 * compared with Java's natural {@link String} order, never with a database collation. Enum values are
 * compared by their names. JSON text stored in the database is parsed into JSON values.
 */
public final class ProjectContentAssembler {

    private static final Comparator<String> TEXT = Comparator.nullsLast(Comparator.naturalOrder());
    private static final Comparator<UUID> IDS = Comparator.nullsLast(Comparator.naturalOrder());

    private static final Comparator<ItemEntity> ITEM_ORDER = byText(ItemEntity::getKey, ItemEntity::getId);
    private static final Comparator<ItemComponentEntity> COMPONENT_ORDER =
            byText(ItemComponentEntity::getComponentKey, ItemComponentEntity::getId);
    private static final Comparator<ItemLoreEntity> LORE_ORDER =
            Comparator.comparingInt(ItemLoreEntity::getOrderIndex).thenComparing(ItemLoreEntity::getId, IDS);
    private static final Comparator<ItemEnchantmentEntity> ENCHANTMENT_ORDER =
            byText(ItemEnchantmentEntity::getName, ItemEnchantmentEntity::getId);
    private static final Comparator<AttributeEntity> ATTRIBUTE_ORDER = byText(AttributeEntity::getKey, AttributeEntity::getId);
    private static final Comparator<FontEntity> FONT_ORDER = byText(FontEntity::getKey, FontEntity::getId);
    private static final Comparator<FontStringEntity> FONT_CHAR_ORDER =
            Comparator.comparingInt(FontStringEntity::getOrderIndex).thenComparing(FontStringEntity::getId, IDS);
    private static final Comparator<SoundEventEntity> SOUND_ORDER = byText(SoundEventEntity::getKey, SoundEventEntity::getId);
    private static final Comparator<SoundFileSource> SOUND_SOURCE_ORDER =
            byText(SoundFileSource::getName, SoundFileSource::getId);
    private static final Comparator<ExpressionFunctionEntity> FUNCTION_ORDER =
            byText(ExpressionFunctionEntity::getName, ExpressionFunctionEntity::getId);
    private static final Comparator<ExpressionEntity> EXPRESSION_ORDER =
            byText(ExpressionEntity::getUiName, ExpressionEntity::getId);
    private static final Comparator<ProjectAssetEntity> ASSET_ORDER =
            Comparator.<ProjectAssetEntity, String>comparing(asset -> name(asset.getKeySpace()), TEXT)
                    .thenComparing(ProjectAssetEntity::getKey, TEXT)
                    .thenComparing(ProjectAssetEntity::getId, IDS);
    private static final Comparator<ProjectAssetRevisionEntity> REVISION_ORDER =
            Comparator.comparingInt(ProjectAssetRevisionEntity::getNumber).thenComparing(ProjectAssetRevisionEntity::getId, IDS);
    private static final Comparator<AssetLinkEntity> LINK_ORDER =
            Comparator.<AssetLinkEntity, String>comparing(link -> name(link.getTargetType()), TEXT)
                    .thenComparing(AssetLinkEntity::getTargetId, IDS)
                    .thenComparing(link -> name(link.getRole()), TEXT)
                    .thenComparing(AssetLinkEntity::getId, IDS);
    private static final Comparator<AdvancementEntity> ADVANCEMENT_ORDER =
            byText(AdvancementEntity::getKey, AdvancementEntity::getId);
    private static final Comparator<DimensionTypeEntity> DIMENSION_ORDER =
            byText(DimensionTypeEntity::getKey, DimensionTypeEntity::getId);
    private static final Comparator<DimensionAttributeEntity> DIMENSION_ATTRIBUTE_ORDER =
            Comparator.<DimensionAttributeEntity, String>comparing(attribute -> name(attribute.getAttributeKey()), TEXT)
                    .thenComparing(DimensionAttributeEntity::getId, IDS);
    private static final Comparator<DimensionTimelineEntity> DIMENSION_TIMELINE_ORDER =
            byText(DimensionTimelineEntity::getTimelineKey, DimensionTimelineEntity::getId);
    private static final Comparator<PackMetaEntity> PACK_ORDER =
            Comparator.<PackMetaEntity, String>comparing(pack -> name(pack.getPackType()), TEXT)
                    .thenComparing(PackMetaEntity::getId, IDS);

    private final JsonMapper jsonMapper;

    /**
     * Creates an assembler that parses stored JSON text with the given mapper.
     *
     * @param jsonMapper the mapper used to read JSON text stored in the database
     */
    public ProjectContentAssembler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Assembles the content of one project from its loaded rows.
     *
     * @param rows the rows of the project, in any order
     * @return the content with a {@code null} revision
     */
    public ProjectContentDTO assemble(ProjectContentRows rows) {
        Map<UUID, List<ItemComponentEntity>> components = group(rows.itemComponents(),
                component -> component.getItem().getId(), COMPONENT_ORDER);
        Map<UUID, List<ItemLoreEntity>> lore = group(rows.itemLore(), line -> line.getItem().getId(), LORE_ORDER);
        Map<UUID, List<ItemEnchantmentEntity>> enchantments = group(rows.itemEnchantments(),
                enchantment -> enchantment.getItem().getId(), ENCHANTMENT_ORDER);
        Map<UUID, List<FontStringEntity>> fontChars = group(rows.fontChars(), ch -> ch.getFont().getId(), FONT_CHAR_ORDER);
        Map<UUID, List<SoundFileSource>> soundSources = group(rows.soundSources(),
                source -> source.getSoundEvent().getId(), SOUND_SOURCE_ORDER);
        Map<UUID, List<DimensionAttributeEntity>> dimensionAttributes = group(rows.dimensionAttributes(),
                attribute -> attribute.getDimensionType().getId(), DIMENSION_ATTRIBUTE_ORDER);
        Map<UUID, List<DimensionTimelineEntity>> dimensionTimelines = group(rows.dimensionTimelines(),
                timeline -> timeline.getDimensionType().getId(), DIMENSION_TIMELINE_ORDER);
        Map<UUID, List<ProjectAssetRevisionEntity>> revisions = group(rows.assetRevisions(),
                revision -> revision.getAsset().getId(), REVISION_ORDER);
        Map<UUID, List<AssetLinkEntity>> links = group(rows.assetLinks(), link -> link.getAsset().getId(), LINK_ORDER);

        return new ProjectContentDTO(
                null,
                projectDTO(rows.project()),
                sorted(rows.items(), ITEM_ORDER).stream().map(item -> new ItemDTO(
                        item.getId(),
                        item.getUiName(),
                        item.getKey(),
                        item.getComment(),
                        item.getGroupName(),
                        children(components, item.getId(), component -> new ComponentDTO(
                                component.getId(),
                                component.getComponentKey(),
                                componentValue(component.getComponentValue()))),
                        children(lore, item.getId(), line -> new LoreDTO(line.getId(), line.getText(), line.getOrderIndex())),
                        children(enchantments, item.getId(), enchantment -> new EnchantmentDTO(
                                enchantment.getId(),
                                enchantment.getName(),
                                enchantment.getLevel(),
                                enchantment.isUnsafe()))
                )).toList(),
                sorted(rows.attributes(), ATTRIBUTE_ORDER).stream().map(attribute -> new AttributeDTO(
                        attribute.getId(),
                        attribute.getUiName(),
                        attribute.getKey(),
                        attribute.getDefaultValue(),
                        attribute.getMinimumValue(),
                        attribute.getMaximumValue(),
                        attribute.isSynced(),
                        attribute.getTranslationKey(),
                        attribute.getComment()
                )).toList(),
                sorted(rows.fonts(), FONT_ORDER).stream().map(font -> new FontDTO(
                        font.getId(),
                        font.getUiName(),
                        font.getKey(),
                        font.getProvider(),
                        font.getMapper(),
                        font.getTexturePath(),
                        font.getTextureAsset() == null ? null : font.getTextureAsset().getId(),
                        font.getComment(),
                        font.getHeight(),
                        font.getAscent(),
                        children(fontChars, font.getId(), ch -> new FontCharDTO(ch.getId(), ch.getLine(), ch.getOrderIndex()))
                )).toList(),
                sorted(rows.sounds(), SOUND_ORDER).stream().map(sound -> new SoundDTO(
                        sound.getId(),
                        sound.getUiName(),
                        sound.getKey(),
                        sound.getKeyName(),
                        sound.isReplace(),
                        sound.getSubTitle(),
                        sound.getComment(),
                        children(soundSources, sound.getId(), source -> new SoundSourceDTO(
                                source.getId(),
                                source.getName(),
                                source.getVolume(),
                                source.getPitch(),
                                source.getWeight(),
                                source.isStreamable(),
                                source.getAttenuationDistance(),
                                source.isPreloadable(),
                                source.getType())))
                ).toList(),
                sorted(rows.expressionFunctions(), FUNCTION_ORDER).stream().map(function -> new ExpressionFunctionDTO(
                        function.getId(),
                        function.getUiName(),
                        function.getName(),
                        function.getKind(),
                        function.getReturnType(),
                        function.getBody(),
                        function.getComment(),
                        variables(function.getParameters())
                )).toList(),
                sorted(rows.expressions(), EXPRESSION_ORDER).stream().map(expression -> new ExpressionDTO(
                        expression.getId(),
                        expression.getUiName(),
                        expression.getExpression(),
                        expression.getComment(),
                        variables(expression.getVariables())
                )).toList(),
                sorted(rows.assets(), ASSET_ORDER).stream().map(asset -> new AssetDTO(
                        asset.getId(),
                        asset.getKey(),
                        asset.getDisplayName(),
                        asset.getKind(),
                        asset.getKeySpace(),
                        asset.getFileName(),
                        asset.getContentType(),
                        asset.getSize(),
                        asset.getSha1(),
                        asset.getWidth(),
                        asset.getHeight(),
                        asset.getChannels(),
                        asset.getSampleRate(),
                        asset.getDurationMs(),
                        asset.getSourceAsset() == null ? null : asset.getSourceAsset().getId(),
                        children(revisions, asset.getId(), revision -> new AssetRevisionDTO(
                                revision.getId(),
                                revision.getNumber(),
                                revision.getSha1(),
                                revision.getSize(),
                                revision.getComment())),
                        children(links, asset.getId(), link -> new AssetLinkDTO(
                                link.getId(),
                                link.getTargetType(),
                                link.getTargetId(),
                                link.getRole()))
                )).toList(),
                sorted(rows.advancements(), ADVANCEMENT_ORDER).stream().map(advancement -> new AdvancementDTO(
                        advancement.getId(),
                        advancement.getUiName(),
                        advancement.getKey(),
                        advancement.getComment(),
                        advancement.getMaterial(),
                        advancement.getFrameType(),
                        json(advancement.getTitle()),
                        json(advancement.getDescription()),
                        advancement.getBackground(),
                        advancement.getParent() == null ? null : advancement.getParent().getId(),
                        advancement.getX(),
                        advancement.getY(),
                        advancement.isShowToast(),
                        advancement.isAnnounceToChat(),
                        advancement.isHidden()
                )).toList(),
                sorted(rows.dimensions(), DIMENSION_ORDER).stream().map(dimension -> new DimensionDTO(
                        dimension.getId(),
                        dimension.getUiName(),
                        dimension.getKey(),
                        dimension.isHasFixedTime(),
                        dimension.isHasSkylight(),
                        dimension.isHasCeiling(),
                        dimension.isHasEnderDragonFight(),
                        dimension.getCoordinateScale(),
                        dimension.getMinY(),
                        dimension.getHeight(),
                        dimension.getLogicalHeight(),
                        dimension.getInfiniburn(),
                        dimension.getAmbientLight(),
                        dimension.getMonsterSpawnLightLevel(),
                        dimension.getMonsterSpawnBlockLightLimit(),
                        dimension.getSkybox(),
                        dimension.getCardinalLight(),
                        dimension.getDefaultClock(),
                        children(dimensionAttributes, dimension.getId(), attribute -> new DimensionAttributeDTO(
                                attribute.getId(),
                                attribute.getAttributeKey(),
                                attribute.getOperator(),
                                attribute.getAttributeValue())),
                        children(dimensionTimelines, dimension.getId(), timeline -> new DimensionTimelineDTO(
                                timeline.getId(),
                                timeline.getTimelineKey()))
                )).toList(),
                sorted(rows.packs(), PACK_ORDER).stream().map(pack -> new PackDTO(
                        pack.getId(),
                        pack.getPackType(),
                        json(pack.getDescription()),
                        pack.getMinFormatMajor(),
                        pack.getMinFormatMinor(),
                        pack.getMaxFormatMajor(),
                        pack.getMaxFormatMinor(),
                        pack.getEnabledFeatures().stream().sorted().toList(),
                        pack.getIconAsset() == null ? null : pack.getIconAsset().getId()
                )).toList()
        );
    }

    private static ProjectDTO projectDTO(ProjectEntity project) {
        return new ProjectDTO(
                project.getId(),
                project.getDisplayName(),
                project.getKey(),
                project.getProjectUrl(),
                project.getDocuUrl(),
                project.getDescription(),
                project.isLabor()
        );
    }

    private static <T> Comparator<T> byText(Function<T, String> text, Function<T, UUID> id) {
        return Comparator.comparing(text, TEXT).thenComparing(id, IDS);
    }

    private static String name(@Nullable Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static <T> List<T> sorted(List<T> rows, Comparator<T> order) {
        return rows.stream().sorted(order).toList();
    }

    /**
     * Groups the rows by their parent id, each group in the given order.
     */
    private static <T, P> Map<P, List<T>> group(List<T> rows, Function<T, P> parentId, Comparator<T> order) {
        Map<P, List<T>> grouped = new HashMap<>();
        for (T row : sorted(rows, order)) {
            grouped.computeIfAbsent(parentId.apply(row), key -> new ArrayList<>()).add(row);
        }
        return grouped;
    }

    private static <T, R> List<R> children(Map<UUID, List<T>> grouped, UUID parentId, Function<T, R> map) {
        return grouped.getOrDefault(parentId, List.of()).stream().map(map).toList();
    }

    private static List<ExpressionVariableDTO> variables(List<ExpressionVariable> variables) {
        return variables.stream()
                .map(variable -> new ExpressionVariableDTO(variable.getName(), variable.getType(), variable.getTestValue()))
                .toList();
    }

    /**
     * A component without a stored value is exported as an empty object, as the item endpoints do.
     */
    private JsonNode componentValue(@Nullable String value) {
        return value == null ? JsonNode.createObjectNode(Map.of()) : json(value);
    }

    @Nullable
    private JsonNode json(@Nullable String text) {
        if (text == null) {
            return null;
        }
        try {
            return jsonMapper.readValue(text, JsonNode.class);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read a stored JSON value of the project", exception);
        }
    }
}
