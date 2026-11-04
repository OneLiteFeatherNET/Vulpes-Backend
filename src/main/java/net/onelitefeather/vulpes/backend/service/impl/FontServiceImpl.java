package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.api.repository.FontRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.font.FontStringRepository;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.FontService;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link FontService} interface.
 *
 * <p>A font may name a texture asset by {@code textureAssetId}. It must be an uploaded texture of the
 * font's project: an asset of another project answers like a missing one, and a texture extracted
 * from a Blockbench model is refused, since replacing the model deletes it.
 */
@Singleton
public class FontServiceImpl
        extends AbstractCrudService<FontEntity, UUID, FontModelDTO, FontModelResponseDTO.FontModelDTO>
        implements FontService {

    private static final String FONT = "Font";
    private static final String FONT_CHARACTER = "Font character";
    static final String FIELD_TEXTURE_ASSET = "textureAssetId";
    static final String CODE_TEXTURE_NOT_FOUND = "FONT_TEXTURE_NOT_FOUND";
    static final String CODE_TEXTURE_NOT_A_TEXTURE = "FONT_TEXTURE_NOT_A_TEXTURE";
    static final String CODE_TEXTURE_EXTRACTED = "FONT_TEXTURE_EXTRACTED";

    private final FontStringRepository fontStringRepository;

    @Inject
    public FontServiceImpl(
            FontRepository fontRepository,
            FontStringRepository fontStringRepository,
            ProjectRepository projectRepository,
            ProjectAssetRepository assetRepository
    ) {
        super(
                fontRepository,
                projectRepository,
                (dto, project) -> dto.toFontModel(
                        project, requireTexture(assetRepository, project.getId(), dto.textureAssetId())),
                FontModelResponseDTO.FontModelDTO::createDTOWithChars,
                FontModelResponseDTO.FontModelDTO::createDTO,
                entity -> entity.getProject().getId(),
                fontRepository::findByProjectId,
                FontModelDTO::id,
                FONT
        );
        this.fontStringRepository = fontStringRepository;
    }

    /**
     * Loads the texture asset a font request names and checks that the font may use it.
     *
     * @param assetRepository to load the asset
     * @param projectId       the project of the font
     * @param assetId         the requested asset, or {@code null} for none
     * @return the asset, or {@code null} if none was requested
     * @throws ApiException {@code VALIDATION_FAILED} on {@code textureAssetId} if the asset does not
     *                      exist in the project, is no texture or was extracted from a Blockbench model
     */
    static ProjectAssetEntity requireTexture(ProjectAssetRepository assetRepository, UUID projectId, UUID assetId) {
        if (assetId == null) {
            return null;
        }
        ProjectAssetEntity asset = assetRepository.findById(assetId)
                .filter(found -> projectId.equals(found.getProject().getId()))
                .orElseThrow(() -> textureViolation(CODE_TEXTURE_NOT_FOUND, "must name an asset of the project"));
        if (asset.getKind() != AssetKind.TEXTURE) {
            throw textureViolation(CODE_TEXTURE_NOT_A_TEXTURE, "must name a texture asset");
        }
        if (asset.getSourceAsset() != null) {
            throw textureViolation(CODE_TEXTURE_EXTRACTED, "must not name a texture extracted from a Blockbench model");
        }
        return asset;
    }

    private static ApiException textureViolation(String code, String message) {
        return ApiException.validationFailed(List.of(new ProblemDetail.Violation(FIELD_TEXTURE_ASSET, code, message)));
    }

    /**
     * Loads the font a character request addressed.
     *
     * @param id the font identifier
     * @return the font
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if no font has that identifier
     */
    private FontEntity requireFont(UUID id) {
        return this.repository.findById(id).orElseThrow(() -> ApiException.notFound(FONT));
    }

    @Override
    public Page<FontStringResponseDTO.FontStringDTO> findCharsByFontId(UUID id, Pageable pageable) {
        return this.fontStringRepository.findCharsByFontId(id, pageable)
                .map(FontStringResponseDTO.FontStringDTO::createDTO);
    }

    @Transactional
    @Override
    public FontStringResponseDTO.FontStringDTO updateCharByFontId(UUID id, FontStringDTO charModel) {
        var fontEntity = requireFont(id);
        if (charModel.id() == null) {
            throw ApiException.invalidRequest("An id is required to update a font character.");
        }
        var charEntity = this.fontStringRepository.findById(charModel.id())
                .orElseThrow(() -> ApiException.notFound(FONT_CHARACTER));
        if (!fontEntity.getId().equals(charEntity.getFont().getId())) {
            throw ApiException.notOwnedBy(FONT_CHARACTER, charModel.id(), "font", id);
        }
        charEntity.setLine(charModel.line());
        charEntity.setOrderIndex(charModel.orderIndex());
        var updatedChar = this.fontStringRepository.update(charEntity);
        return FontStringResponseDTO.FontStringDTO.createDTO(updatedChar);
    }

    @Transactional
    @Override
    public FontStringResponseDTO.FontStringDTO createCharByFontId(UUID id, FontStringDTO charModel) {
        var fontEntity = requireFont(id);
        var charEntity = charModel.toEntity();
        charEntity.setFont(fontEntity);
        var savedChar = this.fontStringRepository.save(charEntity);
        return FontStringResponseDTO.FontStringDTO.createDTO(savedChar);
    }

    @Override
    public FontStringResponseDTO.FontStringDTO deleteCharByFontId(UUID fontId, UUID charId) {
        var fontEntity = requireFont(fontId);
        var charEntity = this.fontStringRepository.findById(charId)
                .orElseThrow(() -> ApiException.notFound(FONT_CHARACTER));
        if (!fontEntity.getId().equals(charEntity.getFont().getId())) {
            throw ApiException.notOwnedBy(FONT_CHARACTER, charId, "font", fontId);
        }
        this.fontStringRepository.deleteById(charId);
        return FontStringResponseDTO.FontStringDTO.createDTO(charEntity);
    }

    @Override
    public List<FontStringResponseDTO.FontStringDTO> deleteAllCharByFontId(UUID fontId) {
        var fontEntity = requireFont(fontId);
        List<FontStringEntity> charEntities =
                this.fontStringRepository.findCharsByFontId(fontEntity.getId(), Pageable.unpaged()).getContent();
        this.fontStringRepository.deleteAll(charEntities);
        return charEntities.stream()
                .map(FontStringResponseDTO.FontStringDTO::createDTO)
                .toList();
    }
}
