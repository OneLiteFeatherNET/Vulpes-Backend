package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.api.repository.FontRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.font.FontStringRepository;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.FontService;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link FontService} interface.
 */
@Singleton
public class FontServiceImpl
        extends AbstractCrudService<FontEntity, UUID, FontModelDTO, FontModelResponseDTO.FontModelDTO>
        implements FontService {

    private static final String FONT = "Font";
    private static final String FONT_CHARACTER = "Font character";

    private final FontStringRepository fontStringRepository;

    @Inject
    public FontServiceImpl(FontRepository fontRepository, FontStringRepository fontStringRepository, ProjectRepository projectRepository) {
        super(
                fontRepository,
                projectRepository,
                FontModelDTO::toFontModel,
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
        var charEntity = charModel.toEntity();
        charEntity.setFont(fontEntity);
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
