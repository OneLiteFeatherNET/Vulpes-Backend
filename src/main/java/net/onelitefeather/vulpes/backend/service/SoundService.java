package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;

import java.util.UUID;

/**
 * Service interface for managing sound events and sound sources.
 */
public interface SoundService extends CrudService<SoundEventEntity, UUID, SoundEventDTO, SoundResponseDTO.SoundModelDTO> {

    /**
     * Gets all sound file sources by an ID.
     *
     * @param id       the ID of the sound event
     * @param pageable pagination details
     * @return the sound event response with sources
     */
    Page<SoundResponseDTO.SoundFileSourceDTO> getSoundSourcesById(UUID id, Pageable pageable);

    /**
     * Creates a new sound file source and links it to a sound event.
     *
     * @param soundEventId the ID of the sound event to link the source to
     * @param sourceDTO    the source data to create
     * @return the created source response
     */
    SoundResponseDTO.SoundFileSourceDTO createAndLinkSource(UUID soundEventId, SoundFileSourceDTO sourceDTO);

    /**
     * Updates an existing sound file source linked to a sound event by ID.
     *
     * @param soundEventId the ID of the sound event
     * @param sourceDTO    the source data to update
     * @return the updated source response
     */
    SoundResponseDTO.SoundFileSourceDTO updateLinkedSource(UUID soundEventId, SoundFileSourceDTO sourceDTO);

    /**
     * Deletes an existing sound file source linked to a sound event by ID.
     *
     * @param soundEventId the ID of the sound event
     * @param sourceId     the ID of the source to delete
     * @return the deleted source response
     */
    SoundResponseDTO.SoundFileSourceDTO deleteLinkedSource(UUID soundEventId, UUID sourceId);
}