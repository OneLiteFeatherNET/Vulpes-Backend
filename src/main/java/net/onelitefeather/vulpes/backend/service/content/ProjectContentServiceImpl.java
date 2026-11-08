package net.onelitefeather.vulpes.backend.service.content;

import io.micronaut.json.JsonMapper;
import io.micronaut.transaction.TransactionDefinition;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO;
import net.onelitefeather.vulpes.backend.entity.advancement.AdvancementContentRepository;
import net.onelitefeather.vulpes.backend.entity.asset.AssetContentRepository;
import net.onelitefeather.vulpes.backend.entity.asset.AssetLinkRepository;
import net.onelitefeather.vulpes.backend.entity.attribute.AttributeContentRepository;
import net.onelitefeather.vulpes.backend.entity.dimension.DimensionContentRepository;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionContentRepository;
import net.onelitefeather.vulpes.backend.entity.font.FontContentRepository;
import net.onelitefeather.vulpes.backend.entity.item.ItemContentRepository;
import net.onelitefeather.vulpes.backend.entity.pack.PackMetaContentRepository;
import net.onelitefeather.vulpes.backend.entity.sound.SoundContentRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;

/**
 * Loads the content of a project with a fixed number of queries inside one read-only transaction.
 *
 * <p>Each table is read once for the whole project. The parents are loaded before their children, so the
 * back-references of the children resolve to the loaded parents.
 */
@Singleton
public class ProjectContentServiceImpl implements ProjectContentService {

    private final ProjectRepository projectRepository;
    private final ItemContentRepository itemRepository;
    private final AttributeContentRepository attributeRepository;
    private final FontContentRepository fontRepository;
    private final SoundContentRepository soundRepository;
    private final ExpressionContentRepository expressionRepository;
    private final AssetContentRepository assetRepository;
    private final AssetLinkRepository assetLinkRepository;
    private final AdvancementContentRepository advancementRepository;
    private final DimensionContentRepository dimensionRepository;
    private final PackMetaContentRepository packRepository;
    private final ProjectContentAssembler assembler;

    @Inject
    public ProjectContentServiceImpl(
            ProjectRepository projectRepository,
            ItemContentRepository itemRepository,
            AttributeContentRepository attributeRepository,
            FontContentRepository fontRepository,
            SoundContentRepository soundRepository,
            ExpressionContentRepository expressionRepository,
            AssetContentRepository assetRepository,
            AssetLinkRepository assetLinkRepository,
            AdvancementContentRepository advancementRepository,
            DimensionContentRepository dimensionRepository,
            PackMetaContentRepository packRepository,
            JsonMapper jsonMapper
    ) {
        this.projectRepository = projectRepository;
        this.itemRepository = itemRepository;
        this.attributeRepository = attributeRepository;
        this.fontRepository = fontRepository;
        this.soundRepository = soundRepository;
        this.expressionRepository = expressionRepository;
        this.assetRepository = assetRepository;
        this.assetLinkRepository = assetLinkRepository;
        this.advancementRepository = advancementRepository;
        this.dimensionRepository = dimensionRepository;
        this.packRepository = packRepository;
        this.assembler = new ProjectContentAssembler(jsonMapper);
    }

    /**
     * Runs all reads of the export in one read-only transaction with repeatable-read isolation, so every
     * table reflects the same committed state. The annotation sits on this concrete method, because the
     * AOP proxy intercepts only there.
     */
    @Override
    @io.micronaut.transaction.annotation.Transactional(
            readOnly = true,
            isolation = TransactionDefinition.Isolation.REPEATABLE_READ
    )
    public ProjectContentDTO export(UUID projectId) {
        ProjectEntity project = projectRepository.findById(projectId).orElseThrow(ApiException::projectNotFound);
        return assembler.assemble(loadRows(project));
    }

    private ProjectContentRows loadRows(ProjectEntity project) {
        UUID projectId = project.getId();
        var items = itemRepository.findItemsByProjectId(projectId);
        var attributes = attributeRepository.findAttributesByProjectId(projectId);
        var fonts = fontRepository.findFontsByProjectId(projectId);
        var sounds = soundRepository.findSoundsByProjectId(projectId);
        var expressionFunctions = expressionRepository.findFunctionsByProjectId(projectId);
        var expressions = expressionRepository.findExpressionsByProjectId(projectId);
        var assets = assetRepository.findAssetsByProjectId(projectId);
        var advancements = advancementRepository.findAdvancementsByProjectId(projectId);
        var dimensions = dimensionRepository.findDimensionsByProjectId(projectId);
        var packs = packRepository.findPacksByProjectId(projectId);
        var itemComponents = itemRepository.findComponentsByProjectId(projectId);
        var itemLore = itemRepository.findLoreByProjectId(projectId);
        var itemEnchantments = itemRepository.findEnchantmentsByProjectId(projectId);
        var fontChars = fontRepository.findCharsByProjectId(projectId);
        var soundSources = soundRepository.findSourcesByProjectId(projectId);
        var dimensionAttributes = dimensionRepository.findAttributesByProjectId(projectId);
        var dimensionTimelines = dimensionRepository.findTimelinesByProjectId(projectId);
        var assetRevisions = assetRepository.findRevisionsByProjectId(projectId);
        var assetLinks = assetLinkRepository.findLinksByProjectId(projectId);
        return new ProjectContentRows(
                project,
                items,
                itemComponents,
                itemLore,
                itemEnchantments,
                attributes,
                fonts,
                fontChars,
                sounds,
                soundSources,
                expressionFunctions,
                expressions,
                assets,
                assetRevisions,
                assetLinks,
                advancements,
                dimensions,
                dimensionAttributes,
                dimensionTimelines,
                packs
        );
    }
}
