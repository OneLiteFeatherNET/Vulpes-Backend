package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.repository.FontRepository;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
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

    @Inject
    public FontServiceImpl(FontRepository fontRepository) {
        this.fontRepository = fontRepository;
    }

    @Override
    public FontModelResponseDTO.FontModelDTO createFont(FontModelDTO fontModelDTO) {
        FontEntity fontModel = fontModelDTO.toFontModel();
        FontEntity savedFontModel = fontRepository.save(fontModel);
        return FontModelResponseDTO.FontModelDTO.createDTOWithChars(savedFontModel);
    }

    @Override
    public FontModelResponseDTO updateFont(FontModelDTO fontModelDTO) {
        Optional<FontEntity> modelOptional = fontRepository.findById(fontModelDTO.getId());
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
    public List<String> findCharsByFontId(UUID id, Pageable pageable) {
        return fontRepository.findCharsByFontId(id, pageable);
    }

    @Override
    public List<String> updateCharsByFontId(UUID id, List<String> chars) {
        var byId = this.fontRepository.findById(id);
        if (byId.isEmpty()) {
            return List.of();
        }
        var font = byId.get();
        font.setChars(chars);
        var updated = this.fontRepository.update(font);
        return updated.getChars();
    }
}