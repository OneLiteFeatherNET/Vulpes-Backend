package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.asset.AssetInspector;
import net.onelitefeather.vulpes.backend.domain.asset.AssetDisplayName;
import net.onelitefeather.vulpes.backend.domain.asset.AssetKey;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkRole;
import net.onelitefeather.vulpes.api.model.asset.AssetTargetType;
import net.onelitefeather.vulpes.backend.domain.asset.AssetUploadDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetViolationCode;
import net.onelitefeather.vulpes.backend.domain.asset.BbModelTextureExtractor;
import net.onelitefeather.vulpes.backend.domain.asset.BbModelTextureExtractor.ExtractedTexture;
import net.onelitefeather.vulpes.backend.domain.asset.Sha1Hex;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkEntity;
import net.onelitefeather.vulpes.backend.entity.asset.AssetLinkRepository;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.storage.AssetStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The transactional writes of project assets: create, replace, and delete.
 *
 * <p>Every method runs in one transaction, and object storage is written inside it, so a failing
 * request leaves neither a row without bytes nor, in the common case, bytes without a row.
 *
 * <p>The writes live in their own bean and return ids. The entities are not mapped to responses
 * here: a transactional save does not fill in the audit timestamps on the instance, so the caller
 * reads the stored asset back after the commit.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Singleton
public class AssetPersistence {

    private static final String ASSET = "Asset";

    private final ProjectAssetRepository assetRepository;
    private final AssetLinkRepository linkRepository;
    private final ProjectRepository projectRepository;
    private final AssetStorage storage;

    /**
     * Constructs a new AssetPersistence.
     *
     * @param assetRepository   the repository of assets
     * @param linkRepository    the repository of asset links
     * @param projectRepository the repository of projects
     * @param storage           the object storage holding the bytes
     */
    @Inject
    public AssetPersistence(
            ProjectAssetRepository assetRepository,
            AssetLinkRepository linkRepository,
            ProjectRepository projectRepository,
            AssetStorage storage
    ) {
        this.assetRepository = assetRepository;
        this.linkRepository = linkRepository;
        this.projectRepository = projectRepository;
        this.storage = storage;
    }

    /**
     * Stores an upload. A Blockbench model also stores the textures embedded in it.
     *
     * @param projectId the project
     * @param upload    the key, the display name and the file
     * @return the id of the stored asset
     * @throws ApiException {@code VALIDATION_FAILED} for a bad key, display name or file, {@code RESOURCE_CONFLICT} if a
     *                      key is taken, {@code PROJECT_NOT_FOUND} if the project does not exist
     */
    @Transactional
    public UUID create(UUID projectId, AssetUploadDTO upload) {
        ProjectEntity project = requireProject(projectId);
        String key = AssetKey.requireValid(upload.key());
        String displayName = AssetDisplayName.requireValid(upload.displayName());
        AssetInspector.Inspection inspection = AssetInspector.inspect(upload.fileName(), upload.content());
        List<ExtractedTexture> textures = inspection.kind() == AssetKind.BBMODEL
                ? BbModelTextureExtractor.extract(key, upload.content())
                : List.of();
        if (assetRepository.existsByProjectIdAndKey(projectId, key)) {
            throw ApiException.conflict("An asset with the key '" + key + "' already exists.");
        }
        requireTextureKeysAvailable(projectId, key, textures, Set.of());

        ProjectAssetEntity asset = store(
                project, key, displayName, upload.fileName(), inspection.kind(),
                inspection.width(), inspection.height(), upload.content(), null
        );
        for (ExtractedTexture texture : textures) {
            store(project, texture.key(), texture.displayName(), texture.fileName(), AssetKind.TEXTURE,
                    texture.width(), texture.height(), texture.content(), asset);
        }
        return asset.getId();
    }

    /**
     * Replaces the bytes of an asset with a file of the same kind. For a Blockbench model the embedded
     * textures are extracted again. Links to old textures move to the new textures with the same key.
     *
     * @param projectId the project
     * @param id        the asset
     * @param upload    the new file
     * @return the id of the asset
     * @throws ApiException {@code VALIDATION_FAILED} on field {@code file} for a bad or different kind,
     *                      {@code RESOURCE_NOT_FOUND} if the asset is not in the project
     */
    @Transactional
    public UUID replaceContent(UUID projectId, UUID id, AssetUploadDTO upload) {
        ProjectAssetEntity asset = requireOwnedAsset(projectId, id);
        AssetInspector.Inspection inspection = AssetInspector.inspect(upload.fileName(), upload.content());
        if (inspection.kind() != asset.getKind()) {
            throw ApiException.validationFailed(List.of(new ProblemDetail.Violation(
                    "file",
                    AssetViolationCode.UNSUPPORTED_TYPE,
                    "must be a " + asset.getKind() + " file, the same kind as the stored asset"
            )));
        }
        List<ProjectAssetEntity> oldTextures = assetRepository.findBySourceAssetId(id);
        Set<String> replaceable = new HashSet<>();
        replaceable.add(asset.getKey());
        oldTextures.forEach(texture -> replaceable.add(texture.getKey()));
        List<ExtractedTexture> textures = inspection.kind() == AssetKind.BBMODEL
                ? BbModelTextureExtractor.extract(asset.getKey(), upload.content())
                : List.of();
        requireTextureKeysAvailable(projectId, asset.getKey(), textures, replaceable);

        List<CarriedLink> carried = new ArrayList<>();
        for (ProjectAssetEntity oldTexture : oldTextures) {
            for (AssetLinkEntity link : linkRepository.findByAssetId(oldTexture.getId())) {
                carried.add(new CarriedLink(
                        oldTexture.getKey(), link.getTargetType(), link.getTargetId(), link.getRole()));
            }
            removeAsset(projectId, oldTexture);
        }
        // The old textures must be gone before rows with the same keys are inserted.
        assetRepository.flush();

        ProjectEntity project = asset.getProject();
        Map<String, ProjectAssetEntity> newTextures = new HashMap<>();
        for (ExtractedTexture texture : textures) {
            newTextures.put(texture.key(), store(
                    project, texture.key(), texture.displayName(), texture.fileName(), AssetKind.TEXTURE,
                    texture.width(), texture.height(), texture.content(), asset
            ));
        }
        for (CarriedLink carry : carried) {
            ProjectAssetEntity target = newTextures.get(carry.assetKey());
            if (target == null) {
                continue;
            }
            AssetLinkEntity moved = new AssetLinkEntity();
            moved.setAsset(target);
            moved.setTargetType(carry.targetType());
            moved.setTargetId(carry.targetId());
            moved.setRole(carry.role());
            linkRepository.save(moved);
        }

        asset.setFileName(upload.fileName());
        asset.setContentType(inspection.kind().contentType());
        asset.setSize(upload.content().length);
        asset.setSha1(Sha1Hex.of(upload.content()));
        asset.setWidth(inspection.width());
        asset.setHeight(inspection.height());
        assetRepository.update(asset);
        storage.put(AssetStorage.keyFor(projectId, id), upload.content(), inspection.kind().contentType());
        return id;
    }

    /**
     * Deletes an asset with its links, the textures extracted from it, and their bytes.
     *
     * @param projectId the project
     * @param id        the asset
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the asset is not in the project
     */
    @Transactional
    public void delete(UUID projectId, UUID id) {
        ProjectAssetEntity asset = requireOwnedAsset(projectId, id);
        for (ProjectAssetEntity texture : assetRepository.findBySourceAssetId(id)) {
            removeAsset(projectId, texture);
        }
        removeAsset(projectId, asset);
    }

    /**
     * Deletes all assets of a project. Textures extracted from a model go with the model.
     *
     * @param projectId the project
     */
    @Transactional
    public void deleteAll(UUID projectId) {
        List<ProjectAssetEntity> roots = assetRepository.findByProjectId(projectId, Pageable.unpaged())
                .getContent().stream()
                .filter(asset -> asset.getSourceAsset() == null)
                .toList();
        for (ProjectAssetEntity root : roots) {
            delete(projectId, root.getId());
        }
    }

    /**
     * Creates an asset row and writes its bytes under the key derived from the generated id.
     */
    private ProjectAssetEntity store(
            ProjectEntity project,
            String key,
            String displayName,
            String fileName,
            AssetKind kind,
            Integer width,
            Integer height,
            byte[] content,
            ProjectAssetEntity sourceAsset
    ) {
        ProjectAssetEntity asset = new ProjectAssetEntity();
        asset.setProject(project);
        asset.setKey(key);
        asset.setDisplayName(displayName);
        asset.setKind(kind);
        asset.setFileName(fileName);
        asset.setContentType(kind.contentType());
        asset.setSize(content.length);
        asset.setSha1(Sha1Hex.of(content));
        asset.setWidth(width);
        asset.setHeight(height);
        asset.setSourceAsset(sourceAsset);
        ProjectAssetEntity saved = assetRepository.save(asset);
        storage.put(AssetStorage.keyFor(project.getId(), saved.getId()), content, kind.contentType());
        return saved;
    }

    /**
     * Deletes one asset with its links and its bytes. Callers remove derived textures first.
     */
    private void removeAsset(UUID projectId, ProjectAssetEntity asset) {
        linkRepository.deleteAll(linkRepository.findByAssetId(asset.getId()));
        // The asset row is deleted by a direct statement, so the link deletes must reach the database first.
        assetRepository.flush();
        storage.delete(AssetStorage.keyFor(projectId, asset.getId()));
        assetRepository.deleteById(asset.getId());
    }

    /**
     * Rejects the upload if the model key or an extracted texture key is already taken, or repeated.
     * Nothing is written before this check passes, so a rejected upload stores nothing.
     */
    private void requireTextureKeysAvailable(
            UUID projectId,
            String modelKey,
            List<ExtractedTexture> textures,
            Set<String> replaceable
    ) {
        Set<String> seen = new HashSet<>();
        seen.add(modelKey);
        for (ExtractedTexture texture : textures) {
            boolean taken = assetRepository.existsByProjectIdAndKey(projectId, texture.key())
                    && !replaceable.contains(texture.key());
            if (!seen.add(texture.key()) || taken) {
                throw ApiException.conflict(
                        "The texture key '" + texture.key() + "' extracted from the model is already in use.");
            }
        }
    }

    private ProjectEntity requireProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(ApiException::projectNotFound);
    }

    private ProjectAssetEntity requireOwnedAsset(UUID projectId, UUID id) {
        ProjectAssetEntity asset = assetRepository.findById(id).orElseThrow(() -> ApiException.notFound(ASSET));
        if (!projectId.equals(asset.getProject().getId())) {
            throw ApiException.notOwnedByProject(ASSET, id, projectId);
        }
        return asset;
    }

    /**
     * A link of a replaced texture, kept in memory until the new texture with the same key exists.
     */
    private record CarriedLink(String assetKey, AssetTargetType targetType, UUID targetId, AssetLinkRole role) {
    }
}
