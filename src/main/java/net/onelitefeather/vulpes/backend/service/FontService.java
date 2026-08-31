package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing fonts and font characters.
 */
public interface FontService extends CrudService<FontEntity, UUID, FontModelDTO, FontModelResponseDTO.FontModelDTO> {

    /**
     * Gets the characters of a font by its ID.
     *
     * @param id       the ID of the font
     * @param pageable pagination information
     * @return a list of characters
     */
    Page<FontStringResponseDTO.FontStringDTO> findCharsByFontId(UUID id, Pageable pageable);

    /**
     * Updates the character of a font by its ID.
     *
     * @param id        the ID of the font
     * @param charModel the new character to set
     * @return the updated character
     */
    FontStringResponseDTO.FontStringDTO updateCharByFontId(UUID id, FontStringDTO charModel);

    /**
     * Creates the character of a font by its ID.
     *
     * @param id        the ID of the font
     * @param charModel the new character to set
     * @return the updated character
     */
    FontStringResponseDTO.FontStringDTO createCharByFontId(UUID id, FontStringDTO charModel);

    /**
     * Deletes the character of a font by its ID.
     *
     * @param fontId the ID of the font
     * @param charId the ID of the character to delete
     * @return the deleted character
     */
    FontStringResponseDTO.FontStringDTO deleteCharByFontId(UUID fontId, UUID charId);

    /**
     * Deletes all characters of a font by its ID.
     *
     * @param fontId the ID of the font
     * @return the list of deleted characters
     */
    List<FontStringResponseDTO.FontStringDTO> deleteAllCharByFontId(UUID fontId);
}