package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.SoundFileSourceRepository;
import net.onelitefeather.vulpes.api.repository.SoundRepository;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.SoundService;

import java.util.UUID;

/**
 * Implementation of the {@link SoundService} interface.
 */
@Singleton
public class SoundServiceImpl
        extends AbstractCrudService<SoundEventEntity, UUID, SoundEventDTO, SoundResponseDTO.SoundModelDTO>
        implements SoundService {

    private static final String SOUND_EVENT = "Sound event";
    private static final String SOUND_SOURCE = "Sound source";
    private final SoundFileSourceRepository soundFileSourceRepository;

    /**
     * Constructs a new SoundServiceImpl with the specified SoundRepository and SoundFileSourceRepository.
     *
     * @param soundRepository           the repository to manage sound events
     * @param soundFileSourceRepository the repository to manage sound file sources
     * @param projectRepository         the repository to manage projects
     */
    @Inject
    public SoundServiceImpl(SoundRepository soundRepository, SoundFileSourceRepository soundFileSourceRepository, ProjectRepository projectRepository) {
        super(
                soundRepository,
                projectRepository,
                SoundEventDTO::toEntity,
                SoundResponseDTO.SoundModelDTO::createDTO,
                entity -> entity.getProject().getId(),
                soundRepository::findByProjectId,
                SoundEventDTO::id,
                SOUND_EVENT
        );
        this.soundFileSourceRepository = soundFileSourceRepository;
    }

    /**
     * Loads the sound event a source request addressed.
     *
     * @param soundEventId the sound event identifier
     * @return the sound event
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if no sound event has that identifier
     */
    private SoundEventEntity requireSoundEvent(UUID soundEventId) {
        return this.repository.findById(soundEventId).orElseThrow(() -> ApiException.notFound(SOUND_EVENT));
    }

    /**
     * Verifies that a source is linked to the given sound event.
     *
     * @param soundEventId the sound event the request addressed
     * @param sourceId     the source identifier
     * @return the source as it is currently stored
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the source is not linked to that event
     */
    private SoundResponseDTO.SoundFileSourceDTO requireLinkedSource(UUID soundEventId, UUID sourceId) {
        return getSoundSourcesById(soundEventId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(source -> sourceId.equals(source.id()))
                .findFirst()
                .orElseThrow(() -> ApiException.notOwnedBy(SOUND_SOURCE, sourceId, "sound event", soundEventId));
    }

    @Override
    public Page<SoundResponseDTO.SoundFileSourceDTO> getSoundSourcesById(UUID id, Pageable pageable) {
        return this.soundFileSourceRepository.findSoundFileSourcesBySoundEvent(id, pageable)
                .map(SoundResponseDTO.SoundFileSourceDTO::createDTO);
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO createAndLinkSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
        if (sourceDTO == null) {
            throw ApiException.invalidRequest("A sound source body is required.");
        }
        var soundEvent = requireSoundEvent(soundEventId);
        var sourceEntity = sourceDTO.toEntity();
        sourceEntity.setSoundEvent(soundEvent);
        var savedSource = soundFileSourceRepository.save(sourceEntity);
        return SoundResponseDTO.SoundFileSourceDTO.createDTO(savedSource);
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO updateLinkedSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
        if (sourceDTO == null || sourceDTO.id() == null) {
            throw ApiException.invalidRequest("An id is required to update a sound source.");
        }
        var soundEvent = requireSoundEvent(soundEventId);
        requireLinkedSource(soundEventId, sourceDTO.id());
        var sourceEntity = sourceDTO.toEntity();
        sourceEntity.setSoundEvent(soundEvent);
        var updatedSource = soundFileSourceRepository.update(sourceEntity);
        return SoundResponseDTO.SoundFileSourceDTO.createDTO(updatedSource);
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO deleteLinkedSource(UUID soundEventId, UUID sourceID) {
        if (sourceID == null) {
            throw ApiException.invalidRequest("A sound source id is required.");
        }
        requireSoundEvent(soundEventId);
        var existingSource = requireLinkedSource(soundEventId, sourceID);
        soundFileSourceRepository.deleteById(sourceID);
        return existingSource;
    }
}
