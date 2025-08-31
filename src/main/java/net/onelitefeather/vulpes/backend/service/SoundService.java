package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.validation.Valid;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing sound events.
 */
public interface SoundService {

    /**
     * Creates a new sound event.
     *
     * @param soundEventDTO the sound event data to create
     * @return the created sound event response
     */
    SoundResponseDTO createSoundEvent(SoundEventDTO soundEventDTO);

    /**
     * Updates an existing sound event.
     *
     * @param soundEventDTO the sound event data to update
     * @return the updated sound event response or an error response if the sound event doesn't exist
     */
    SoundResponseDTO updateSoundEvent(SoundEventDTO soundEventDTO);

    /**
     * Deletes a sound event by its ID.
     *
     * @param id the ID of the sound event to delete
     * @return the deleted sound event response or an error response if the sound event doesn't exist
     */
    SoundResponseDTO deleteSoundEvent(UUID id);

    /**
     * Deletes all sound events.
     *
     * @return an empty list
     */
    List<SoundResponseDTO> deleteAllSoundEvents();

    /**
     * Gets all sound events.
     *
     * @return a list of all sound events
     */
    List<SoundResponseDTO> getAllSoundEvents();

    /**
     * Finds a sound event by its ID.
     *
     * @param id the ID of the sound event to find
     * @return an optional containing the sound event if found, or empty if not found
     */
    Optional<SoundEventEntity> findSoundEventById(UUID id);

    /**
     * Gets all sound file sources by an ID.
     *
     * @param id the ID of the sound event
     * @return the sound event response with sources
     */
    Page<SoundResponseDTO> getSoundSourcesById(UUID id, Pageable pageable);

    /**
     * Creates a new sound file source and links it to a sound event.
     * @param soundEventId the ID of the sound event to link the source to
     * @param sourceDTO the source data to create
     * @return the created source response
     */
    SoundResponseDTO createAndLinkSource(@Valid UUID soundEventId, SoundFileSourceDTO sourceDTO);

    /**
     * Updates an existing sound file source linked to a sound event by ID.
     * @param soundEventId the ID of the sound event
     * @param sourceDTO the source data to update
     * @return the updated source response
     */
    SoundResponseDTO updateLinkedSource(@Valid UUID soundEventId, SoundFileSourceDTO sourceDTO);

    /**
     * Deletes an existing sound file source linked to a sound event by ID.
     * @param soundEventId the ID of the sound event
     * @param sourceDTO the source data to delete
     * @return the deleted source response
     */
    SoundResponseDTO deleteLinkedSource(@Valid UUID soundEventId, SoundFileSourceDTO sourceDTO);
}