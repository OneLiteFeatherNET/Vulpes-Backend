package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.asset.AssetContent;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.backend.domain.asset.AssetResponseDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetUploadDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.AssetService;
import net.onelitefeather.vulpes.backend.storage.AssetStorage;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link AssetService} interface.
 *
 * <p>Writes are delegated to {@link AssetPersistence}, which runs them in transactions. Responses are
 * read back from the database afterwards, so the audit timestamps are always filled in.
 */
@Singleton
public class AssetServiceImpl
        extends AbstractCrudService<ProjectAssetEntity, UUID, AssetUploadDTO, AssetResponseDTO>
        implements AssetService {

    private static final String ASSET = "Asset";

    private final ProjectAssetRepository assetRepository;
    private final AssetPersistence persistence;
    private final AssetStorage storage;

    /**
     * Constructs a new AssetServiceImpl.
     *
     * @param assetRepository   the repository of assets
     * @param projectRepository the repository of projects
     * @param persistence       the transactional writes
     * @param storage           the object storage holding the bytes
     */
    @Inject
    public AssetServiceImpl(
            ProjectAssetRepository assetRepository,
            ProjectRepository projectRepository,
            AssetPersistence persistence,
            AssetStorage storage
    ) {
        super(
                assetRepository,
                projectRepository,
                // Unused: create and update are overridden, because an upload may create several assets.
                null,
                asset -> AssetResponseDTO.createDTO(asset, derivedIdsOf(assetRepository, asset)),
                asset -> asset.getProject().getId(),
                assetRepository::findByProjectId,
                AssetUploadDTO::id,
                ASSET
        );
        this.assetRepository = assetRepository;
        this.persistence = persistence;
        this.storage = storage;
    }

    @Override
    public AssetResponseDTO create(UUID projectId, AssetUploadDTO upload) {
        return getAsset(projectId, persistence.create(projectId, upload));
    }

    @Override
    public AssetResponseDTO update(UUID projectId, AssetUploadDTO upload) {
        if (upload.id() == null) {
            throw ApiException.invalidRequest("An id is required to update an asset.");
        }
        return replaceContent(projectId, upload.id(), upload);
    }

    @Override
    public AssetResponseDTO replaceContent(UUID projectId, UUID id, AssetUploadDTO upload) {
        return getAsset(projectId, persistence.replaceContent(projectId, id, upload));
    }

    @Override
    public AssetResponseDTO delete(UUID projectId, UUID id) {
        AssetResponseDTO removed = getAsset(projectId, id);
        persistence.delete(projectId, id);
        return removed;
    }

    @Override
    public void deleteAll(UUID projectId) {
        persistence.deleteAll(projectId);
    }

    @Override
    public AssetResponseDTO getAsset(UUID projectId, UUID id) {
        ProjectAssetEntity asset = assetRepository.findById(id).orElseThrow(() -> ApiException.notFound(ASSET));
        if (!projectId.equals(asset.getProject().getId())) {
            throw ApiException.notOwnedByProject(ASSET, id, projectId);
        }
        return AssetResponseDTO.createDTO(asset, derivedIdsOf(assetRepository, asset));
    }

    @Override
    public Page<AssetResponseDTO> getAll(UUID projectId, AssetKind kind, Pageable pageable) {
        Page<ProjectAssetEntity> page = kind == null
                ? assetRepository.findByProjectId(projectId, pageable)
                : assetRepository.findByProjectIdAndKind(projectId, kind, pageable);
        return page.map(asset -> AssetResponseDTO.createDTO(asset, derivedIdsOf(assetRepository, asset)));
    }

    @Override
    public AssetContent getContent(UUID projectId, UUID id) {
        AssetResponseDTO asset = getAsset(projectId, id);
        String key = AssetStorage.keyFor(projectId, id);
        byte[] content = storage.get(key).orElseThrow(() -> new ApiException(
                ErrorCode.INTERNAL_ERROR,
                "Internal server error",
                "Asset " + id + " has no stored content under " + key
        ));
        return new AssetContent(content, asset.contentType(), asset.sha1());
    }

    private static List<UUID> derivedIdsOf(ProjectAssetRepository repository, ProjectAssetEntity asset) {
        if (asset.getKind() != AssetKind.BBMODEL) {
            return List.of();
        }
        return repository.findBySourceAssetId(asset.getId()).stream()
                .map(ProjectAssetEntity::getId)
                .toList();
    }
}
