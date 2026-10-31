package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetRevisionEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.asset.AssetContent;
import net.onelitefeather.vulpes.backend.domain.asset.AssetResponseDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetRevisionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetUploadDTO;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRepository;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRevisionRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.AssetService;
import net.onelitefeather.vulpes.backend.storage.AssetStorage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Implementation of the {@link AssetService} interface.
 *
 * <p>Writes are delegated to {@link AssetPersistence}, which runs them in transactions. Responses are
 * read back from the database afterwards, so the audit timestamps are always filled in.
 */
@Singleton
@NullMarked
public class AssetServiceImpl extends AbstractCrudService<ProjectAssetEntity, UUID, AssetUploadDTO, AssetResponseDTO>
        implements AssetService {

    private static final String ASSET = "Asset";
    private static final String REVISION = "Asset revision";

    private final ProjectAssetRepository assetRepository;
    private final ProjectAssetRevisionRepository revisionRepository;
    private final AssetPersistence persistence;
    private final AssetStorage storage;

    /**
     * Constructs a new AssetServiceImpl.
     *
     * @param assetRepository    the repository of assets
     * @param revisionRepository the repository of asset revisions
     * @param projectRepository  the repository of projects
     * @param persistence        the transactional writes
     * @param storage            the object storage holding the bytes
     */
    @Inject
    public AssetServiceImpl(
            ProjectAssetRepository assetRepository,
            ProjectAssetRevisionRepository revisionRepository,
            ProjectRepository projectRepository,
            AssetPersistence persistence,
            AssetStorage storage) {
        super(
                assetRepository,
                projectRepository,
                // Unused: create and update are overridden, because an upload may create several assets.
                null,
                asset -> AssetResponseMapper.toDto(asset, derivedIdsOf(assetRepository, asset)),
                asset -> asset.getProject().getId(),
                assetRepository::findByProjectId,
                AssetUploadDTO::id,
                ASSET);
        this.assetRepository = assetRepository;
        this.revisionRepository = revisionRepository;
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
        return replaceContent(projectId, upload.id(), upload, null);
    }

    @Override
    public AssetResponseDTO replaceContent(UUID projectId, UUID id, AssetUploadDTO upload, @Nullable String ifMatch) {
        return getAsset(projectId, persistence.replaceContent(projectId, id, upload, ifMatch));
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
        return AssetResponseMapper.toDto(asset, derivedIdsOf(assetRepository, asset));
    }

    @Override
    public Page<AssetResponseDTO> getAll(UUID projectId, @Nullable AssetKind kind, Pageable pageable) {
        Page<ProjectAssetEntity> page = kind == null
                ? assetRepository.findByProjectId(projectId, pageable)
                : assetRepository.findByProjectIdAndKind(projectId, kind, pageable);
        return page.map(asset -> AssetResponseMapper.toDto(asset, derivedIdsOf(assetRepository, asset)));
    }

    @Override
    public AssetContent getContent(UUID projectId, UUID id) {
        AssetResponseDTO asset = getAsset(projectId, id);
        // The newest revision is the current content. An asset without revisions still has its legacy bytes.
        String key = revisionRepository
                .findFirstByAssetIdOrderByNumberDesc(id)
                .map(ProjectAssetRevisionEntity::getStorageKey)
                .orElseGet(() -> AssetStorage.keyFor(projectId, id));
        return new AssetContent(storage.require(key), asset.contentType(), asset.sha1());
    }

    @Override
    public Page<AssetRevisionResponseDTO> getRevisions(UUID projectId, UUID id, Pageable pageable) {
        persistence.ensureRevisions(projectId, id);
        int newest = revisionRepository
                .findFirstByAssetIdOrderByNumberDesc(id)
                .map(ProjectAssetRevisionEntity::getNumber)
                .orElse(0);
        return revisionRepository
                .findByAssetIdOrderByNumberDesc(id, pageable)
                .map(revision -> AssetResponseMapper.toRevisionDto(revision, revision.getNumber() == newest));
    }

    @Override
    public AssetContent getRevisionContent(UUID projectId, UUID id, UUID revisionId) {
        AssetResponseDTO asset = getAsset(projectId, id);
        ProjectAssetRevisionEntity revision = revisionRepository
                .findByIdAndAssetId(revisionId, id)
                .orElseThrow(() -> ApiException.notFound(REVISION));
        return new AssetContent(storage.require(revision.getStorageKey()), asset.contentType(), revision.getSha1());
    }

    @Override
    public AssetResponseDTO restoreRevision(
            UUID projectId, UUID id, UUID revisionId, @Nullable String ifMatch, @Nullable String comment) {
        return getAsset(projectId, persistence.restoreRevision(projectId, id, revisionId, ifMatch, comment));
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
