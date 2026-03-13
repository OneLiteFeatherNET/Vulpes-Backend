package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing fonts.
 */
public interface FontService {

    /**
     * Creates a new font.
     *
     * @param fontModelDTO the font data to create
     * @return the created font response
     */
    FontModelResponseDTO.FontModelDTO createFont(FontModelDTO fontModelDTO);

    /**
     * Updates an existing font.
     *
     * @param fontModelDTO the font data to update
     * @return the updated font response or an error response if the font doesn't exist
     */
    FontModelResponseDTO updateFont(FontModelDTO fontModelDTO);

    /**
     * Deletes a font by its ID.
     *
     * @param id the ID of the font to delete
     * @return the deleted font response or an error response if the font doesn't exist
     */
    FontModelResponseDTO deleteFont(UUID id);

    /**
     * Deletes all fonts.
     *
     * @return an empty list
     */
    List<FontModelResponseDTO> deleteAllFonts();

    /**
     * Gets all fonts with pagination.
     *
     * @param pageable pagination information
     * @return a page of fonts
     */
    Page<FontModelResponseDTO.FontModelDTO> getAllFonts(Pageable pageable);

    /**
     * Finds a font by its ID.
     *
     * @param id the ID of the font to find
     * @return an optional containing the font if found, or empty if not found
     */
    Optional<FontEntity> findFontById(UUID id);

    /**
     * Gets the characters of a font by its ID.
     *
     * @param id the ID of the font
     * @param pageable pagination information
     * @return a list of characters
     */
    Page<FontStringResponseDTO> findCharsByFontId(UUID id, Pageable pageable);

    /**
     * Updates the character of a font by its ID.
     * @param id the ID of the font
     * @param charModel the new character to set
     * @return the updated character
     */
    FontStringResponseDTO updateCharByFontId(UUID id, FontStringDTO charModel);

    /**
     * Create the character of a font by its ID.
     * @param id the ID of the font
     * @param charModel the new character to set
     * @return the updated character
     */
    FontStringResponseDTO createCharByFontId(UUID id, FontStringDTO charModel);

    /**
     * Deletes the character of a font by its ID.
     * @param fontId the ID of the font
     * @param charId the id of the character to delete
     * @return the deleted character
     */
    FontStringResponseDTO deleteCharByFontId(UUID fontId, UUID charId);


    /**
     * Deletes all characters of a font by its ID.
     * @param fontId the ID of the font
     * @return the list of deleted characters
     */
    List<FontStringResponseDTO> deleteAllCharByFontId(UUID fontId);
}