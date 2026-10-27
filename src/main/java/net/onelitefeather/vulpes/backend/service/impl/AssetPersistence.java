package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.asset.AssetKeySpace;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetRevisionEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.asset.AssetDisplayName;
import net.onelitefeather.vulpes.backend.domain.asset.AssetInspector;
import net.onelitefeather.vulpes.backend.domain.asset.AssetKey;
import net.onelitefeather.vulpes.backend.domain.asset.AssetUploadDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetViolationCode;
import net.onelitefeather.vulpes.backend.domain.asset.BbModelTextureExtractor;
import net.onelitefeather.vulpes.backend.domain.asset.BbModelTextureExtractor.ExtractedTexture;
import net.onelitefeather.vulpes.backend.domain.asset.Sha1Hex;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRepository;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRevisionRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.storage.AssetStorage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
@NullMarked
public class AssetPersistence {

    private static final String ASSET = "Asset";
    private static final String REVISION = "Asset revision";

    private final ProjectAssetRepository assetRepository;
    private final ProjectAssetRevisionRepository revisionRepository;
    private final ProjectRepository projectRepository;
    private final AssetStorage storage;

    /**
     * Constructs a new AssetPersistence.
     *
     * @param assetRepository    the repository of assets
     * @param revisionRepository the repository of asset revisions
     * @param projectRepository  the repository of projects
     * @param storage            the object storage holding the bytes
     */
    @Inject
    public AssetPersistence(
            ProjectAssetRepository assetRepository,
            ProjectAssetRevisionRepository revisionRepository,
            ProjectRepository projectRepository,
            AssetStorage storage) {
        this.assetRepository = assetRepository;
        this.revisionRepository = revisionRepository;
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
        String comment = requireValidComment(upload.comment());
        ProjectEntity project = requireProject(projectId);
        String key = AssetKey.requireValid(upload.key());
        String displayName = AssetDisplayName.requireValid(upload.displayName());
        AssetInspector.Inspection inspection = AssetInspector.inspect(upload.fileName(), upload.content());
        List<ExtractedTexture> textures = inspection.kind() == AssetKind.BBMODEL
                ? BbModelTextureExtractor.extract(key, upload.content())
                : List.of();
        if (assetRepository.existsByProjectIdAndKeySpaceAndKey(projectId, AssetKeySpace.of(inspection.kind()), key)) {
            throw ApiException.conflict("An asset with the key '" + key + "' already exists.");
        }
        requireTextureKeysAvailable(projectId, textures, Set.of());

        ProjectAssetEntity asset = store(
                project,
                key,
                displayName,
                upload.fileName(),
                inspection.kind(),
                inspection.width(),
                inspection.height(),
                upload.content(),
                null,
                comment);
        for (ExtractedTexture texture : textures) {
            store(
                    project,
                    texture.key(),
                    texture.displayName(),
                    texture.fileName(),
                    AssetKind.TEXTURE,
                    texture.width(),
                    texture.height(),
                    texture.content(),
                    asset,
                    null);
        }
        return asset.getId();
    }

    /**
     * Replaces the bytes of an asset with a file of the same kind. The new content becomes the next
     * revision. For a Blockbench model the embedded textures are extracted again.
     *
     * @param projectId the project
     * @param id        the asset
     * @param upload    the new file and its optional comment
     * @param ifMatch   the If-Match value the client holds, or {@code null} to replace unconditionally
     * @return the id of the asset
     * @throws ApiException {@code VALIDATION_FAILED} on field {@code file} for a bad or different kind, or on
     *                      field {@code comment} if it is too long, {@code RESOURCE_NOT_FOUND} if the asset is
     *                      not in the project, {@code ASSET_PRECONDITION_FAILED} if {@code ifMatch} does not
     *                      match the stored content
     */
    @Transactional
    public UUID replaceContent(UUID projectId, UUID id, AssetUploadDTO upload, @Nullable String ifMatch) {
        String comment = requireValidComment(upload.comment());
        // The row lock lasts until commit, so the precondition check and the write below cannot interleave
        // with another replacement of this asset. The lock must be taken before the check.
        assetRepository.findByIdForUpdate(id);
        ProjectAssetEntity asset = requireOwnedAsset(projectId, id);
        if (ifMatch != null && !matchesIfMatch(ifMatch, asset.getSha1())) {
            throw ApiException.assetPreconditionFailed("\"" + asset.getSha1() + "\"");
        }
        AssetInspector.Inspection inspection = AssetInspector.inspect(upload.fileName(), upload.content());
        if (inspection.kind() != asset.getKind()) {
            throw ApiException.validationFailed(List.of(new ProblemDetail.Violation(
                    "file",
                    AssetViolationCode.UNSUPPORTED_TYPE,
                    "must be a " + asset.getKind() + " file, the same kind as the stored asset")));
        }
        writeRevision(asset, upload.fileName(), inspection, upload.content(), comment);
        return id;
    }

    /**
     * Makes an earlier revision the current content again. The bytes of the chosen revision are written as a
     * new revision, so no revision is changed or removed.
     *
     * @param projectId  the project
     * @param id         the asset
     * @param revisionId the revision to restore
     * @param ifMatch    the If-Match value the client holds, or {@code null} to restore unconditionally
     * @param comment    the comment of the new revision, {@code null} for {@code Restored revision <number>}
     * @return the id of the asset
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the asset or the revision is not in the project,
     *                      {@code ASSET_PRECONDITION_FAILED} if {@code ifMatch} does not match the stored content
     */
    @Transactional
    public UUID restoreRevision(
            UUID projectId, UUID id, UUID revisionId, @Nullable String ifMatch, @Nullable String comment) {
        String requestedComment = requireValidComment(comment);
        assetRepository.findByIdForUpdate(id);
        ProjectAssetEntity asset = requireOwnedAsset(projectId, id);
        if (ifMatch != null && !matchesIfMatch(ifMatch, asset.getSha1())) {
            throw ApiException.assetPreconditionFailed("\"" + asset.getSha1() + "\"");
        }
        ProjectAssetRevisionEntity source = revisionRepository
                .findByIdAndAssetId(revisionId, id)
                .orElseThrow(() -> ApiException.notFound(REVISION));
        byte[] content = storage.require(source.getStorageKey());
        AssetInspector.Inspection inspection = AssetInspector.inspect(asset.getFileName(), content);
        String restoreComment = requestedComment != null ? requestedComment : "Restored revision " + source.getNumber();
        writeRevision(asset, asset.getFileName(), inspection, content, restoreComment);
        return id;
    }

    /**
     * Gives an asset that was created before revisions existed its revision 1, if it has none yet. The
     * revision points to the bytes the asset already has. Nothing is written to storage.
     *
     * @param projectId the project
     * @param id        the asset
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the asset is not in the project
     */
    @Transactional
    public void ensureRevisions(UUID projectId, UUID id) {
        assetRepository.findByIdForUpdate(id);
        ensureLegacyRevision(requireOwnedAsset(projectId, id));
    }

    /**
     * Deletes an asset, the textures extracted from it, and their bytes.
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
        List<ProjectAssetEntity> roots =
                assetRepository.findByProjectId(projectId, Pageable.unpaged()).getContent().stream()
                        .filter(asset -> asset.getSourceAsset() == null)
                        .toList();
        for (ProjectAssetEntity root : roots) {
            delete(projectId, root.getId());
        }
    }

    /**
     * Creates an asset row with its first revision. The bytes are written under the key of the revision.
     */
    private ProjectAssetEntity store(
            ProjectEntity project,
            String key,
            String displayName,
            String fileName,
            AssetKind kind,
            @Nullable Integer width,
            @Nullable Integer height,
            byte[] content,
            @Nullable ProjectAssetEntity sourceAsset,
            @Nullable String comment) {
        ProjectAssetEntity asset = new ProjectAssetEntity();
        asset.setProject(project);
        asset.setKey(key);
        asset.setDisplayName(displayName);
        asset.setKind(kind);
        asset.setKeySpace(AssetKeySpace.of(kind));
        asset.setFileName(fileName);
        asset.setContentType(kind.contentType());
        asset.setSize(content.length);
        asset.setSha1(Sha1Hex.of(content));
        asset.setWidth(width);
        asset.setHeight(height);
        asset.setSourceAsset(sourceAsset);
        ProjectAssetEntity saved = assetRepository.save(asset);
        addRevision(saved, 1, content, comment);
        return saved;
    }

    /**
     * Writes content as the next revision of an asset and makes it the current content. A Blockbench model
     * replaces its extracted textures. Runs under the row lock of the asset.
     */
    private void writeRevision(
            ProjectAssetEntity asset,
            String fileName,
            AssetInspector.Inspection inspection,
            byte[] content,
            @Nullable String comment) {
        UUID projectId = asset.getProject().getId();
        ensureLegacyRevision(asset);
        int number = revisionRepository
                .findFirstByAssetIdOrderByNumberDesc(asset.getId())
                .map(revision -> revision.getNumber() + 1)
                .orElse(1);
        List<ProjectAssetEntity> oldTextures = assetRepository.findBySourceAssetId(asset.getId());
        Set<String> replaceable = new HashSet<>();
        oldTextures.forEach(texture -> replaceable.add(texture.getKey()));
        List<ExtractedTexture> textures = inspection.kind() == AssetKind.BBMODEL
                ? BbModelTextureExtractor.extract(asset.getKey(), content)
                : List.of();
        requireTextureKeysAvailable(projectId, textures, replaceable);

        for (ProjectAssetEntity oldTexture : oldTextures) {
            removeAsset(projectId, oldTexture);
        }
        // The old textures must be gone before rows with the same keys are inserted.
        assetRepository.flush();

        ProjectEntity project = asset.getProject();
        for (ExtractedTexture texture : textures) {
            store(
                    project,
                    texture.key(),
                    texture.displayName(),
                    texture.fileName(),
                    AssetKind.TEXTURE,
                    texture.width(),
                    texture.height(),
                    texture.content(),
                    asset,
                    null);
        }
        asset.setFileName(fileName);
        asset.setContentType(inspection.kind().contentType());
        asset.setSize(content.length);
        asset.setSha1(Sha1Hex.of(content));
        asset.setWidth(inspection.width());
        asset.setHeight(inspection.height());
        assetRepository.update(asset);
        addRevision(asset, number, content, comment);
    }

    /**
     * Adds a revision row and writes its bytes under the key of the revision. The key is set before the row is
     * saved, because the save flushes the insert and the key column is not nullable.
     */
    private ProjectAssetRevisionEntity addRevision(
            ProjectAssetEntity asset, int number, byte[] content, @Nullable String comment) {
        String key = AssetStorage.revisionKeyFor(asset.getProject().getId(), asset.getId(), number);
        ProjectAssetRevisionEntity revision = new ProjectAssetRevisionEntity();
        revision.setAsset(asset);
        revision.setNumber(number);
        revision.setSha1(Sha1Hex.of(content));
        revision.setSize(content.length);
        revision.setStorageKey(key);
        revision.setComment(comment);
        ProjectAssetRevisionEntity saved = revisionRepository.save(revision);
        storage.put(key, content, asset.getKind().contentType());
        return saved;
    }

    /**
     * Creates revision 1 of an asset that has no revision yet. The revision points to the legacy key, where
     * the bytes of an asset created before revisions are stored.
     */
    private void ensureLegacyRevision(ProjectAssetEntity asset) {
        if (revisionRepository.existsByAssetId(asset.getId())) {
            return;
        }
        ProjectAssetRevisionEntity legacy = new ProjectAssetRevisionEntity();
        legacy.setAsset(asset);
        legacy.setNumber(1);
        legacy.setSha1(asset.getSha1());
        legacy.setSize(asset.getSize());
        legacy.setStorageKey(AssetStorage.keyFor(asset.getProject().getId(), asset.getId()));
        revisionRepository.save(legacy);
    }

    /**
     * Deletes one asset, its revisions and their bytes. Callers remove derived textures first. The legacy key
     * is removed too, for an asset that has no revision row yet; removing a missing key is a no-op.
     */
    private void removeAsset(UUID projectId, ProjectAssetEntity asset) {
        List<ProjectAssetRevisionEntity> revisions = revisionRepository.findByAssetId(asset.getId());
        for (ProjectAssetRevisionEntity revision : revisions) {
            storage.delete(revision.getStorageKey());
        }
        storage.delete(AssetStorage.keyFor(projectId, asset.getId()));
        revisionRepository.deleteAll(revisions);
        // deleteById runs as a bulk delete right away; the revision rows must be gone before it.
        revisionRepository.flush();
        assetRepository.deleteById(asset.getId());
    }

    /**
     * Rejects the upload if an extracted texture key is already taken as a texture, or repeated. The model
     * key is not checked here: a model and a texture may share a key. Nothing is written before this check
     * passes, so a rejected upload stores nothing.
     */
    private void requireTextureKeysAvailable(UUID projectId, List<ExtractedTexture> textures, Set<String> replaceable) {
        Set<String> seen = new HashSet<>();
        for (ExtractedTexture texture : textures) {
            boolean taken =
                    assetRepository.existsByProjectIdAndKeySpaceAndKey(projectId, AssetKeySpace.TEXTURE, texture.key())
                            && !replaceable.contains(texture.key());
            if (!seen.add(texture.key()) || taken) {
                throw ApiException.conflict(
                        "The texture key '" + texture.key() + "' extracted from the model is already in use.");
            }
        }
    }

    /**
     * Whether an If-Match value admits the stored content. Only {@code *} and the exact strong ETag match;
     * weak and malformed values never do.
     */
    private static boolean matchesIfMatch(String ifMatch, String sha1) {
        return ifMatch.equals("*") || ifMatch.equals("\"" + sha1 + "\"");
    }

    /**
     * Returns the comment as it is stored: {@code null} for a missing or blank comment.
     *
     * @throws ApiException {@code VALIDATION_FAILED} on field {@code comment} if it is too long
     */
    @Nullable
    private static String requireValidComment(@Nullable String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        if (comment.length() > ProjectAssetRevisionEntity.COMMENT_MAX_LENGTH) {
            throw ApiException.validationFailed(List.of(new ProblemDetail.Violation(
                    "comment",
                    AssetViolationCode.COMMENT_TOO_LONG,
                    "must be at most " + ProjectAssetRevisionEntity.COMMENT_MAX_LENGTH + " characters")));
        }
        return comment;
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
}
