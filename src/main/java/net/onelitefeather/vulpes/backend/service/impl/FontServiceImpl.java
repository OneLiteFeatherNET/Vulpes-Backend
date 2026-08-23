package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.repository.FontRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.font.FontStringRepository;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;
import net.onelitefeather.vulpes.backend.service.FontService;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link FontService} interface.
 */
@Singleton
public class FontServiceImpl
        extends AbstractCrudService<FontEntity, UUID, FontModelDTO, FontModelResponseDTO, FontModelResponseDTO.FontModelDTO>
        implements FontService {

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
                FontModelResponseDTO.FontModelErrorDTO::new,
                "Font"
        );
        this.fontStringRepository = fontStringRepository;
    }

    @Override
    public Page<FontStringResponseDTO> findCharsByFontId(UUID id, Pageable pageable) {
        return this.fontStringRepository.findCharsByFontId(id, pageable).map(FontStringResponseDTO.FontStringDTO::createDTO);
    }

    @Transactional
    @Override
    public FontStringResponseDTO updateCharByFontId(UUID id, FontStringDTO charModel) {
        var byId = this.repository.findById(id);
        if (byId.isEmpty()) {
            return new FontStringResponseDTO.FontStringErrorDTO("Font not found");
        }
        var fontEntity = byId.get();
        var charEntity = charModel.toEntity();
        charEntity.setFont(fontEntity);
        var updatedChar = this.fontStringRepository.update(charEntity);
        return FontStringResponseDTO.FontStringDTO.createDTO(updatedChar);
    }

    @Transactional
    @Override
    public FontStringResponseDTO createCharByFontId(UUID id, FontStringDTO charModel) {
        var byId = this.repository.findById(id);
        if (byId.isEmpty()) {
            return new FontStringResponseDTO.FontStringErrorDTO("Font not found");
        }
        var fontEntity = byId.get();
        var charEntity = charModel.toEntity();
        charEntity.setFont(fontEntity);
        var savedChar = this.fontStringRepository.save(charEntity);
        return FontStringResponseDTO.FontStringDTO.createDTO(savedChar);
    }

    @Override
    public FontStringResponseDTO deleteCharByFontId(UUID fontId, UUID charId) {
        var byId = this.repository.findById(fontId);
        if (byId.isEmpty()) {
            return new FontStringResponseDTO.FontStringErrorDTO("Font not found");
        }
        var charById = this.fontStringRepository.findById(charId);
        if (charById.isEmpty()) {
            return new FontStringResponseDTO.FontStringErrorDTO("Font character not found");
        }
        var fontEntity = byId.get();
        var charEntity = charById.get();
        if (!fontEntity.getId().equals(charEntity.getFont().getId())) {
            return new FontStringResponseDTO.FontStringErrorDTO("Font character not found");
        }
        this.fontStringRepository.deleteById(charId);
        return FontStringResponseDTO.FontStringDTO.createDTO(charEntity);
    }

    @Override
    public List<FontStringResponseDTO> deleteAllCharByFontId(UUID fontId) {
        var byId = this.repository.findById(fontId);
        if (byId.isEmpty()) {
            return List.of();
        }
        var fontEntity = byId.get();
        var charEntities = this.fontStringRepository.findAll()
                .stream()
                .filter(charEntity -> charEntity.getFont().getId().equals(fontEntity.getId())).toList();
        this.fontStringRepository.deleteAll(charEntities);
        return charEntities.stream()
                .map(FontStringResponseDTO.FontStringDTO::createDTO)
                .toList();
    }
}