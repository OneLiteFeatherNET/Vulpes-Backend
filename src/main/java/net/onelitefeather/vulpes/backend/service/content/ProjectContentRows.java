package net.onelitefeather.vulpes.backend.service.content;

import java.util.List;
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
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.api.model.item.ItemComponentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.api.model.pack.PackMetaEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;

/**
 * The rows of one project as loaded from the database for the content export, one list per table.
 *
 * <p>The lists hold every row of the project and are not ordered or grouped; {@link ProjectContentAssembler}
 * does that.
 *
 * @param project                the project the rows belong to
 * @param items                  the items
 * @param itemComponents         the components of all items
 * @param itemLore               the lore lines of all items
 * @param itemEnchantments       the enchantments of all items
 * @param attributes             the attributes
 * @param fonts                  the fonts
 * @param fontChars              the chars of all fonts
 * @param sounds                 the sound events
 * @param soundSources           the sound sources of all sound events
 * @param expressionFunctions    the expression functions
 * @param expressions            the expressions
 * @param assets                 the assets
 * @param assetRevisions         the revision rows of all assets
 * @param assetLinks             the links of all assets
 * @param advancements           the advancements
 * @param dimensions             the dimension types
 * @param dimensionAttributes    the attributes of all dimension types
 * @param dimensionTimelines     the timelines of all dimension types
 * @param packs                  the pack metadata
 */
public record ProjectContentRows(
        ProjectEntity project,
        List<ItemEntity> items,
        List<ItemComponentEntity> itemComponents,
        List<ItemLoreEntity> itemLore,
        List<ItemEnchantmentEntity> itemEnchantments,
        List<AttributeEntity> attributes,
        List<FontEntity> fonts,
        List<FontStringEntity> fontChars,
        List<SoundEventEntity> sounds,
        List<SoundFileSource> soundSources,
        List<ExpressionFunctionEntity> expressionFunctions,
        List<ExpressionEntity> expressions,
        List<ProjectAssetEntity> assets,
        List<ProjectAssetRevisionEntity> assetRevisions,
        List<AssetLinkEntity> assetLinks,
        List<AdvancementEntity> advancements,
        List<DimensionTypeEntity> dimensions,
        List<DimensionAttributeEntity> dimensionAttributes,
        List<DimensionTimelineEntity> dimensionTimelines,
        List<PackMetaEntity> packs
) {
}
