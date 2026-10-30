package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

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
    Page<FontStringResponseDTO.FontStringDTO> findCharsByFontId(@NotNull UUID id, @NotNull Pageable pageable);

    /**
     * Updates the character of a font by its ID.
     *
     * @param id        the ID of the font
     * @param charModel the new character to set
     * @return the updated character
     */
    @Validated(groups = {Default.class, ValidationGroup.Update.class})
    FontStringResponseDTO.FontStringDTO updateCharByFontId(@NotNull UUID id, @NotNull @Valid FontStringDTO charModel);

    /**
     * Creates the character of a font by its ID.
     *
     * @param id        the ID of the font
     * @param charModel the new character to set
     * @return the updated character
     */
    @Validated(groups = {Default.class, ValidationGroup.Create.class})
    FontStringResponseDTO.FontStringDTO createCharByFontId(@NotNull UUID id, @NotNull @Valid FontStringDTO charModel);

    /**
     * Deletes the character of a font by its ID.
     *
     * @param fontId the ID of the font
     * @param charId the ID of the character to delete
     * @return the deleted character
     */
    FontStringResponseDTO.FontStringDTO deleteCharByFontId(@NotNull UUID fontId, @NotNull UUID charId);

    /**
     * Deletes all characters of a font by its ID.
     *
     * @param fontId the ID of the font
     * @return the list of deleted characters
     */
    List<FontStringResponseDTO.FontStringDTO> deleteAllCharByFontId(@NotNull UUID fontId);
}