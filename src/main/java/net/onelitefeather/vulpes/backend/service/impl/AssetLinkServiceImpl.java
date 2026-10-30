package net.onelitefeather.vulpes.backend.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.repository.AdvancementRepository;
import net.onelitefeather.vulpes.api.repository.FontRepository;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.asset.AssetKind;
import net.onelitefeather.vulpes.backend.domain.asset.AssetLinkDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetLinkRequestDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetLinkRole;
import net.onelitefeather.vulpes.backend.domain.asset.AssetTargetType;
import net.onelitefeather.vulpes.backend.domain.asset.AssetViolationCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.entity.asset.AssetLinkEntity;
import net.onelitefeather.vulpes.backend.entity.asset.AssetLinkRepository;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.AssetLinkService;
import net.onelitefeather.vulpes.backend.service.AssetService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Implementation of the {@link AssetLinkService} interface.
 *
 * <p>A target counts as existing only if it belongs to the project in the request path. Otherwise the
 * answer is the same as for a missing target.
 */
@Singleton
public class AssetLinkServiceImpl implements AssetLinkService {

    private static final String ASSET = "Asset";
    private static final String ASSET_LINK = "Asset link";

    private final ProjectAssetRepository assetRepository;
    private final AssetLinkRepository linkRepository;
    private final ProjectRepository projectRepository;
    private final ItemRepository itemRepository;
    private final FontRepository fontRepository;
    private final AdvancementRepository advancementRepository;
    private final AssetService assetService;

    /**
     * Constructs a new AssetLinkServiceImpl.
     *
     * @param assetRepository        the repository of assets
     * @param linkRepository         the repository of asset links
     * @param projectRepository      the repository of projects
     * @param itemRepository         the repository of items, the target of {@code ITEM} links
     * @param fontRepository         the repository of fonts, the target of {@code FONT} links
     * @param advancementRepository  the repository of advancements, the target of {@code ADVANCEMENT} links
     * @param assetService           the service that builds asset responses
     */
    @Inject
    public AssetLinkServiceImpl(
            ProjectAssetRepository assetRepository,
            AssetLinkRepository linkRepository,
            ProjectRepository projectRepository,
            ItemRepository itemRepository,
            FontRepository fontRepository,
            AdvancementRepository advancementRepository,
            AssetService assetService
    ) {
        this.assetRepository = assetRepository;
        this.linkRepository = linkRepository;
        this.projectRepository = projectRepository;
        this.itemRepository = itemRepository;
        this.fontRepository = fontRepository;
        this.advancementRepository = advancementRepository;
        this.assetService = assetService;
    }

    @Override
    @Transactional
    public AssetLinkDTO link(UUID projectId, UUID assetId, AssetLinkRequestDTO request) {
        requireProject(projectId);
        ProjectAssetEntity asset = requireOwnedAsset(projectId, assetId);
        requireRoleFits(asset.getKind(), request.role());
        requireTarget(projectId, request.targetType(), request.targetId());
        if (linkRepository.existsByAssetIdAndTargetTypeAndTargetId(assetId, request.targetType(), request.targetId())) {
            throw ApiException.conflict("The asset is already linked to this " + request.targetType().name().toLowerCase(Locale.ROOT) + ".");
        }
        AssetLinkEntity link = new AssetLinkEntity();
        link.setAsset(asset);
        link.setTargetType(request.targetType());
        link.setTargetId(request.targetId());
        link.setRole(request.role());
        AssetLinkEntity saved = linkRepository.save(link);
        return AssetLinkDTO.createDTO(saved, assetService.getAsset(projectId, assetId));
    }

    @Override
    public List<AssetLinkDTO> getLinks(UUID projectId, AssetTargetType targetType, UUID targetId) {
        requireProject(projectId);
        requireTarget(projectId, targetType, targetId);
        return linkRepository.findByTargetTypeAndTargetId(targetType, targetId).stream()
                .filter(ownedBy(projectId))
                .map(link -> AssetLinkDTO.createDTO(
                        link, assetService.getAsset(projectId, link.getAsset().getId())))
                .toList();
    }

    @Override
    @Transactional
    public void unlink(UUID projectId, UUID linkId) {
        requireProject(projectId);
        AssetLinkEntity link = linkRepository.findById(linkId)
                .filter(ownedBy(projectId))
                .orElseThrow(() -> ApiException.notFound(ASSET_LINK));
        linkRepository.deleteById(link.getId());
    }

    /**
     * A texture links with role TEXTURE, a model or a Blockbench model with role MODEL.
     */
    private static void requireRoleFits(AssetKind kind, AssetLinkRole role) {
        boolean fits = switch (kind) {
            case TEXTURE -> role == AssetLinkRole.TEXTURE;
            case MODEL, BBMODEL -> role == AssetLinkRole.MODEL;
        };
        if (!fits) {
            String message = kind == AssetKind.TEXTURE
                    ? "must be TEXTURE for a texture asset"
                    : "must be MODEL for a model or Blockbench model asset";
            throw ApiException.validationFailed(List.of(new ProblemDetail.Violation(
                    "role", AssetViolationCode.ROLE_INVALID, message)));
        }
    }

    private void requireTarget(UUID projectId, AssetTargetType targetType, UUID targetId) {
        boolean inProject = switch (targetType) {
            case ITEM -> itemRepository.findById(targetId)
                    .map(item -> belongsTo(projectId, item))
                    .orElse(false);
            case FONT -> fontRepository.findById(targetId)
                    .map(font -> belongsTo(projectId, font))
                    .orElse(false);
            case ADVANCEMENT -> advancementRepository.findById(targetId)
                    .map(advancement -> belongsTo(projectId, advancement))
                    .orElse(false);
        };
        if (!inProject) {
            throw ApiException.notFound(targetName(targetType));
        }
    }

    private static boolean belongsTo(UUID projectId, ItemEntity item) {
        return projectId.equals(item.getProject().getId());
    }

    private static boolean belongsTo(UUID projectId, FontEntity font) {
        return projectId.equals(font.getProject().getId());
    }

    private static boolean belongsTo(UUID projectId, AdvancementEntity advancement) {
        return projectId.equals(advancement.getProject().getId());
    }

    private static Predicate<AssetLinkEntity> ownedBy(UUID projectId) {
        return link -> projectId.equals(link.getAsset().getProject().getId());
    }

    private static String targetName(AssetTargetType targetType) {
        return switch (targetType) {
            case ITEM -> "Item";
            case FONT -> "Font";
            case ADVANCEMENT -> "Advancement";
        };
    }

    private void requireProject(UUID projectId) {
        projectRepository.findById(projectId).orElseThrow(ApiException::projectNotFound);
    }

    private ProjectAssetEntity requireOwnedAsset(UUID projectId, UUID assetId) {
        ProjectAssetEntity asset = assetRepository.findById(assetId)
                .orElseThrow(() -> ApiException.notFound(ASSET));
        if (!projectId.equals(asset.getProject().getId())) {
            throw ApiException.notOwnedByProject(ASSET, assetId, projectId);
        }
        return asset;
    }
}
