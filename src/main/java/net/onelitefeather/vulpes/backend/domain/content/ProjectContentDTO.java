package net.onelitefeather.vulpes.backend.domain.content;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.api.model.asset.AssetKeySpace;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkRole;
import net.onelitefeather.vulpes.api.model.asset.AssetTargetType;
import net.onelitefeather.vulpes.api.model.dimension.AttributeOperator;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.api.model.expression.ExpressionFunctionKind;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariableType;
import net.onelitefeather.vulpes.api.model.pack.PackType;

/**
 * The complete content of one project, as served by the project content export.
 *
 * <p>Every list contains all elements of its type that belong to the project, ordered by content and
 * never {@code null}. The records carry the field names of the regular per-type responses, with
 * children nested under their parent. Audit timestamps and the project id of each element are left out.
 * JSON stored as text is delivered as a JSON value.
 *
 * <p>The export has no revision when it is assembled. {@link #withRevision(String)} returns a copy that
 * carries the revision computed over this body.
 *
 * @param revision   the revision of this body, or {@code null} while the body is not yet identified
 * @param project    the project the content belongs to
 * @param items      the items of the project
 * @param attributes the attributes of the project
 * @param fonts      the fonts of the project
 * @param sounds     the sound events of the project
 * @param expressionFunctions the expression functions of the project
 * @param expressions the expressions of the project
 * @param assets     the assets of the project, without file content
 * @param advancements the advancements of the project
 * @param dimensions the dimension types of the project
 * @param packs      the pack metadata of the project
 */
@Serdeable
public record ProjectContentDTO(
        String revision,
        ProjectDTO project,
        List<ItemDTO> items,
        List<AttributeDTO> attributes,
        List<FontDTO> fonts,
        List<SoundDTO> sounds,
        List<ExpressionFunctionDTO> expressionFunctions,
        List<ExpressionDTO> expressions,
        List<AssetDTO> assets,
        List<AdvancementDTO> advancements,
        List<DimensionDTO> dimensions,
        List<PackDTO> packs
) {

    /**
     * Returns a copy of this body that carries the given revision.
     *
     * @param revision the revision computed over this body
     * @return the copy with the revision set
     */
    public ProjectContentDTO withRevision(String revision) {
        return new ProjectContentDTO(revision, project, items, attributes, fonts, sounds, expressionFunctions,
                expressions, assets, advancements, dimensions, packs);
    }

    /**
     * The project the content belongs to.
     *
     * @param id          the id of the project
     * @param displayName the display name of the project
     * @param key         the key of the project
     * @param projectUrl  the URL of the project, or {@code null}
     * @param docuUrl     the URL of the documentation, or {@code null}
     * @param description the description of the project, or {@code null}
     * @param labor       whether the project is a labor project
     */
    @Serdeable
    public record ProjectDTO(
            UUID id,
            String displayName,
            String key,
            String projectUrl,
            String docuUrl,
            String description,
            boolean labor
    ) {
    }

    /**
     * An item with its components, lore lines and enchantments.
     *
     * @param id           the id of the item
     * @param uiName       the name shown in the UI
     * @param key          the key of the item
     * @param comment      the comment on the item, or {@code null}
     * @param groupName    the group of the item, or {@code null}
     * @param components   the data components of the item, ordered by component key
     * @param lore         the lore lines of the item, ordered by position
     * @param enchantments the enchantments of the item, ordered by name
     */
    @Serdeable
    public record ItemDTO(
            UUID id,
            String uiName,
            String key,
            String comment,
            String groupName,
            List<ComponentDTO> components,
            List<LoreDTO> lore,
            List<EnchantmentDTO> enchantments
    ) {
    }

    /**
     * A data component of an item.
     *
     * @param id           the id of the component entry
     * @param componentKey the key of the data component
     * @param value        the value of the component as a JSON value
     */
    @Serdeable
    public record ComponentDTO(UUID id, String componentKey, JsonNode value) {
    }

    /**
     * A lore line of an item.
     *
     * @param id         the id of the lore line
     * @param text       the text of the line
     * @param orderIndex the position of the line in the tooltip
     */
    @Serdeable
    public record LoreDTO(UUID id, String text, int orderIndex) {
    }

    /**
     * An enchantment of an item.
     *
     * @param id     the id of the enchantment
     * @param name   the name of the enchantment
     * @param level  the level of the enchantment
     * @param unsafe whether the enchantment is applied unsafely
     */
    @Serdeable
    public record EnchantmentDTO(UUID id, String name, short level, boolean unsafe) {
    }

    /**
     * An attribute of the project.
     *
     * @param id              the id of the attribute
     * @param uiName          the name shown in the UI
     * @param key             the key of the attribute
     * @param defaultValue    the default value
     * @param minimumValue    the minimum value
     * @param maximumValue    the maximum value
     * @param synced          whether the attribute is synced to the client
     * @param translationKey  the translation key, or {@code null}
     * @param comment         the comment on the attribute, or {@code null}
     */
    @Serdeable
    public record AttributeDTO(
            UUID id,
            String uiName,
            String key,
            double defaultValue,
            double minimumValue,
            double maximumValue,
            boolean synced,
            String translationKey,
            String comment
    ) {
    }

    /**
     * A font of the project with its chars.
     *
     * @param id            the id of the font
     * @param uiName        the name shown in the UI
     * @param key           the key of the font
     * @param provider      the provider of the font
     * @param mapper        the mapper of the font
     * @param texturePath   the texture path of the font
     * @param textureAssetId the id of the texture asset, or {@code null}
     * @param comment       the comment on the font, or {@code null}
     * @param height        the height of the font
     * @param ascent        the ascent of the font
     * @param chars         the chars of the font, ordered by position
     */
    @Serdeable
    public record FontDTO(
            UUID id,
            String uiName,
            String key,
            String provider,
            String mapper,
            String texturePath,
            UUID textureAssetId,
            String comment,
            int height,
            int ascent,
            List<FontCharDTO> chars
    ) {
    }

    /**
     * A char line of a font.
     *
     * @param id         the id of the char line
     * @param line       the characters of the line
     * @param orderIndex the position of the line in the font
     */
    @Serdeable
    public record FontCharDTO(UUID id, String line, int orderIndex) {
    }

    /**
     * A sound event of the project with its sources.
     *
     * @param id       the id of the sound event
     * @param uiName   the name shown in the UI
     * @param key      the key of the sound event
     * @param keyName  the sound name in the game
     * @param replace  whether the sound replaces the vanilla sound
     * @param subTitle the subtitle, or {@code null}
     * @param comment  the comment on the sound event, or {@code null}
     * @param sources  the sound sources, ordered by name
     */
    @Serdeable
    public record SoundDTO(
            UUID id,
            String uiName,
            String key,
            String keyName,
            boolean replace,
            String subTitle,
            String comment,
            List<SoundSourceDTO> sources
    ) {
    }

    /**
     * A sound file of a sound event.
     *
     * @param id                  the id of the sound source
     * @param name                the name of the sound file
     * @param volume              the volume
     * @param pitch               the pitch
     * @param weight              the weight in the sound pool
     * @param stream              whether the sound is streamed
     * @param attenuationDistance the attenuation distance
     * @param preload             whether the sound is preloaded
     * @param type                the type of the sound source
     */
    @Serdeable
    public record SoundSourceDTO(
            UUID id,
            String name,
            float volume,
            float pitch,
            int weight,
            boolean stream,
            int attenuationDistance,
            boolean preload,
            String type
    ) {
    }

    /**
     * An expression function of the project.
     *
     * @param id         the id of the function
     * @param uiName     the name shown in the UI
     * @param name       the name of the function
     * @param kind       the kind of the function
     * @param returnType the return type
     * @param body       the body of the function, or {@code null}
     * @param comment    the comment on the function, or {@code null}
     * @param parameters the parameters, in their declared order
     */
    @Serdeable
    public record ExpressionFunctionDTO(
            UUID id,
            String uiName,
            String name,
            ExpressionFunctionKind kind,
            ExpressionVariableType returnType,
            String body,
            String comment,
            List<ExpressionVariableDTO> parameters
    ) {
    }

    /**
     * An expression of the project.
     *
     * @param id         the id of the expression
     * @param uiName     the name shown in the UI
     * @param expression the expression text
     * @param comment    the comment on the expression, or {@code null}
     * @param variables  the variables, in their declared order
     */
    @Serdeable
    public record ExpressionDTO(
            UUID id,
            String uiName,
            String expression,
            String comment,
            List<ExpressionVariableDTO> variables
    ) {
    }

    /**
     * A variable of an expression or a parameter of an expression function.
     *
     * @param name      the name of the variable
     * @param type      the type of the variable
     * @param testValue the test value, or {@code null}
     */
    @Serdeable
    public record ExpressionVariableDTO(String name, ExpressionVariableType type, String testValue) {
    }

    /**
     * An asset of the project with its revisions and links. The file content is not part of the export.
     *
     * @param id            the id of the asset
     * @param key           the key of the asset
     * @param displayName   the display name of the asset
     * @param kind          the kind of the asset
     * @param keySpace      the key space of the asset
     * @param fileName      the file name of the asset
     * @param contentType   the content type of the asset
     * @param size          the size of the asset in bytes
     * @param sha1          the SHA-1 of the asset content
     * @param width         the width of a texture, or {@code null}
     * @param height        the height of a texture, or {@code null}
     * @param sourceAssetId the id of the asset this asset was derived from, or {@code null}
     * @param revisions     the revisions of the asset, ordered by number
     * @param links         the links of the asset, ordered by target type, target id and role
     */
    @Serdeable
    public record AssetDTO(
            UUID id,
            String key,
            String displayName,
            AssetKind kind,
            AssetKeySpace keySpace,
            String fileName,
            String contentType,
            long size,
            String sha1,
            Integer width,
            Integer height,
            UUID sourceAssetId,
            List<AssetRevisionDTO> revisions,
            List<AssetLinkDTO> links
    ) {
    }

    /**
     * A stored revision of an asset.
     *
     * @param id      the id of the revision row
     * @param number  the revision number
     * @param sha1    the SHA-1 of the revision content
     * @param size    the size of the revision content in bytes
     * @param comment the comment on the revision, or {@code null}
     */
    @Serdeable
    public record AssetRevisionDTO(UUID id, int number, String sha1, long size, String comment) {
    }

    /**
     * A link from an asset to the element that uses it.
     *
     * @param id         the id of the link
     * @param targetType the type of the linked element
     * @param targetId   the id of the linked element
     * @param role       the role the asset plays for the element
     */
    @Serdeable
    public record AssetLinkDTO(UUID id, AssetTargetType targetType, UUID targetId, AssetLinkRole role) {
    }

    /**
     * An advancement of the project.
     *
     * @param id            the id of the advancement
     * @param uiName        the name shown in the UI
     * @param key           the key of the advancement
     * @param comment       the comment on the advancement, or {@code null}
     * @param material      the material of the icon
     * @param frameType     the frame type
     * @param title         the title as a JSON text component, or {@code null}
     * @param description   the description as a JSON text component, or {@code null}
     * @param background    the background, or {@code null}
     * @param parentId      the id of the parent advancement, or {@code null} for a root
     * @param x             the horizontal position
     * @param y             the vertical position
     * @param showToast     whether a toast is shown
     * @param announceToChat whether the advancement is announced in chat
     * @param hidden        whether the advancement is hidden
     */
    @Serdeable
    public record AdvancementDTO(
            UUID id,
            String uiName,
            String key,
            String comment,
            String material,
            AdvancementFrameType frameType,
            JsonNode title,
            JsonNode description,
            String background,
            UUID parentId,
            float x,
            float y,
            boolean showToast,
            boolean announceToChat,
            boolean hidden
    ) {
    }

    /**
     * A dimension type of the project with its attributes and timelines.
     *
     * @param id                         the id of the dimension type
     * @param uiName                     the name shown in the UI
     * @param key                        the key of the dimension type
     * @param hasFixedTime               whether the time is fixed
     * @param hasSkylight                whether the dimension has skylight
     * @param hasCeiling                 whether the dimension has a ceiling
     * @param hasEnderDragonFight        whether the dimension has the ender dragon fight
     * @param coordinateScale            the coordinate scale
     * @param minY                       the minimum Y coordinate
     * @param height                     the height
     * @param logicalHeight              the logical height
     * @param infiniburn                 the infiniburn tag
     * @param ambientLight               the ambient light
     * @param monsterSpawnLightLevel     the monster spawn light level
     * @param monsterSpawnBlockLightLimit the monster spawn block light limit
     * @param skybox                     the skybox
     * @param cardinalLight              the cardinal light
     * @param defaultClock               the default clock, or {@code null}
     * @param attributes                 the environment attributes, ordered by attribute key
     * @param timelines                  the timelines, ordered by timeline key
     */
    @Serdeable
    public record DimensionDTO(
            UUID id,
            String uiName,
            String key,
            boolean hasFixedTime,
            boolean hasSkylight,
            boolean hasCeiling,
            boolean hasEnderDragonFight,
            double coordinateScale,
            int minY,
            int height,
            int logicalHeight,
            String infiniburn,
            float ambientLight,
            String monsterSpawnLightLevel,
            int monsterSpawnBlockLightLimit,
            Skybox skybox,
            CardinalLight cardinalLight,
            String defaultClock,
            List<DimensionAttributeDTO> attributes,
            List<DimensionTimelineDTO> timelines
    ) {
    }

    /**
     * An environment attribute of a dimension type.
     *
     * @param id             the id of the attribute entry
     * @param attributeKey   the environment attribute key
     * @param operator       the operator
     * @param attributeValue the value
     */
    @Serdeable
    public record DimensionAttributeDTO(
            UUID id,
            EnvironmentAttributeKey attributeKey,
            AttributeOperator operator,
            String attributeValue
    ) {
    }

    /**
     * A timeline of a dimension type.
     *
     * @param id          the id of the timeline entry
     * @param timelineKey the key of the timeline
     */
    @Serdeable
    public record DimensionTimelineDTO(UUID id, String timelineKey) {
    }

    /**
     * The metadata of one pack of the project.
     *
     * @param id              the id of the pack metadata
     * @param packType        the pack type
     * @param description     the description as a JSON text component
     * @param minFormatMajor  the minimum format, major part
     * @param minFormatMinor  the minimum format, minor part
     * @param maxFormatMajor  the maximum format, major part
     * @param maxFormatMinor  the maximum format, minor part, or {@code null}
     * @param enabledFeatures the enabled features, sorted
     * @param iconAssetId     the id of the icon asset, or {@code null}
     */
    @Serdeable
    public record PackDTO(
            UUID id,
            PackType packType,
            JsonNode description,
            int minFormatMajor,
            int minFormatMinor,
            int maxFormatMajor,
            Integer maxFormatMinor,
            List<String> enabledFeatures,
            UUID iconAssetId
    ) {
    }
}
