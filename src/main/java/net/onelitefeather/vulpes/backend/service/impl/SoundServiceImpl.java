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
import net.onelitefeather.vulpes.backend.service.SoundService;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link SoundService} interface.
 */
@Singleton
public class SoundServiceImpl
        extends AbstractCrudService<SoundEventEntity, UUID, SoundEventDTO, SoundResponseDTO, SoundResponseDTO.SoundModelDTO>
        implements SoundService {

    private static final String GENERIC_ERROR = "Sound event not found";
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
                SoundResponseDTO.SoundErrorDTO::new,
                "Sound event"
        );
        this.soundFileSourceRepository = soundFileSourceRepository;
    }

    @Override
    public Page<SoundResponseDTO> getSoundSourcesById(UUID id, Pageable pageable) {
        return this.soundFileSourceRepository.findSoundFileSourcesBySoundEvent(id, pageable).map(SoundResponseDTO.SoundFileSourceDTO::createDTO);
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO createAndLinkSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
        if (soundEventId == null || sourceDTO == null) {
            throw new IllegalArgumentException("SoundEventId and SourceDTO must not be null");
        }
        Optional<SoundEventEntity> soundEventOpt = this.repository.findById(soundEventId);
        if (soundEventOpt.isEmpty()) {
            throw new IllegalArgumentException(GENERIC_ERROR);
        }
        var sourceEntity = sourceDTO.toEntity();
        sourceEntity.setSoundEvent(soundEventOpt.get());
        var savedSource = soundFileSourceRepository.save(sourceEntity);
        return SoundResponseDTO.SoundFileSourceDTO.createDTO(savedSource);
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO updateLinkedSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
        if (soundEventId == null || sourceDTO == null || sourceDTO.id() == null) {
            throw new IllegalArgumentException("SoundEventId and SourceDTO and SourceDTO.Id must not be null");
        }
        Optional<SoundEventEntity> soundEventOpt = this.repository.findById(soundEventId);
        if (soundEventOpt.isEmpty()) {
            throw new IllegalArgumentException(GENERIC_ERROR);
        }
        Optional<SoundResponseDTO.SoundFileSourceDTO> existingSourceOpt = this.getSoundSourcesById(soundEventId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(SoundResponseDTO.SoundFileSourceDTO.class::isInstance)
                .map(SoundResponseDTO.SoundFileSourceDTO.class::cast)
                .filter(s -> s.id().equals(sourceDTO.id()))
                .findFirst();
        if (existingSourceOpt.isEmpty()) {
            throw new IllegalArgumentException("Sound source not found for the given sound event");
        }
        var sourceEntity = sourceDTO.toEntity();
        sourceEntity.setSoundEvent(soundEventOpt.get());
        var updatedSource = soundFileSourceRepository.update(sourceEntity);
        return SoundResponseDTO.SoundFileSourceDTO.createDTO(updatedSource);
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO deleteLinkedSource(UUID soundEventId, UUID sourceID) {
        if (soundEventId == null || sourceID == null) {
            throw new IllegalArgumentException("SoundEventId and SourceDTO and SourceDTO.Id must not be null");
        }

        Optional<SoundEventEntity> soundEventOpt = this.repository.findById(soundEventId);
        if (soundEventOpt.isEmpty()) {
            throw new IllegalArgumentException(GENERIC_ERROR);
        }
        Optional<SoundResponseDTO.SoundFileSourceDTO> existingSourceOpt = this.getSoundSourcesById(soundEventId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(SoundResponseDTO.SoundFileSourceDTO.class::isInstance)
                .map(SoundResponseDTO.SoundFileSourceDTO.class::cast)
                .filter(s -> s.id().equals(sourceID))
                .findFirst();
        if (existingSourceOpt.isEmpty()) {
            throw new IllegalArgumentException("Sound source not found for the given sound event");
        }
        soundFileSourceRepository.deleteById(sourceID);
        return existingSourceOpt.get();
    }
}