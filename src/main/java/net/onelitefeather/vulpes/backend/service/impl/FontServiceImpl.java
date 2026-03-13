package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.repository.FontRepository;
import net.onelitefeather.vulpes.api.repository.font.FontStringRepository;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;
import net.onelitefeather.vulpes.backend.service.FontService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the FontService interface.
 */
@Singleton
public class FontServiceImpl implements FontService {

    private final FontRepository fontRepository;
    private final FontStringRepository fontStringRepository;

    @Inject
    public FontServiceImpl(FontRepository fontRepository, FontStringRepository fontStringRepository) {
        this.fontRepository = fontRepository;
        this.fontStringRepository = fontStringRepository;
    }

    @Override
    public FontModelResponseDTO.FontModelDTO createFont(FontModelDTO fontModelDTO) {
        FontEntity fontModel = fontModelDTO.toFontModel();
        FontEntity savedFontModel = fontRepository.save(fontModel);
        return FontModelResponseDTO.FontModelDTO.createDTOWithChars(savedFontModel);
    }

    @Override
    public FontModelResponseDTO updateFont(FontModelDTO fontModelDTO) {
        Optional<FontEntity> modelOptional = fontRepository.findById(fontModelDTO.id());
        if (modelOptional.isEmpty()) {
            return new FontModelResponseDTO.FontModelErrorDTO("Font not found");
        }
        FontEntity fontModel = fontModelDTO.toFontModel();
        var updatedFontModel = fontRepository.update(fontModel);
        return FontModelResponseDTO.FontModelDTO.createDTOWithChars(updatedFontModel);
    }

    @Override
    public FontModelResponseDTO deleteFont(UUID id) {
        Optional<FontEntity> model = fontRepository.findById(id);
        if (model.isPresent()) {
            fontRepository.deleteById(id);
            FontEntity fontModel = model.get();
            return FontModelResponseDTO.FontModelDTO.createDTO(fontModel);
        }
        return new FontModelResponseDTO.FontModelErrorDTO("Font not found");
    }

    @Override
    public List<FontModelResponseDTO> deleteAllFonts() {
        fontRepository.deleteAll();
        return List.of();
    }

    @Override
    public Page<FontModelResponseDTO.FontModelDTO> getAllFonts(Pageable pageable) {
        return fontRepository.findAll(pageable).map(FontModelResponseDTO.FontModelDTO::createDTO);
    }

    @Override
    public Optional<FontEntity> findFontById(UUID id) {
        return fontRepository.findById(id);
    }

    @Override
    public Page<FontStringResponseDTO> findCharsByFontId(UUID id, Pageable pageable) {
        return this.fontStringRepository.findCharsByFontId(id, pageable).map(FontStringResponseDTO.FontStringDTO::createDTO);
    }

    @Transactional
    @Override
    public FontStringResponseDTO updateCharByFontId(UUID id, FontStringDTO charModel) {
        var byId = this.fontRepository.findById(id);
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
        var byId = this.fontRepository.findById(id);
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
        var byId = this.fontRepository.findById(fontId);
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
        var byId = this.fontRepository.findById(fontId);
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