package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.repository.SoundFileSourceRepository;
import net.onelitefeather.vulpes.api.repository.SoundRepository;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import net.onelitefeather.vulpes.backend.service.SoundService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the SoundService interface.
 */
@Singleton
public class SoundServiceImpl implements SoundService {

    private static final String GENERIC_ERROR = "Sound event not found";
    private final SoundRepository soundRepository;
    private final SoundFileSourceRepository soundFileSourceRepository;

    /**
     * Constructs a new SoundServiceImpl with the specified SoundRepository.
     *
     * @param soundRepository the repository to manage sound events
     * @param soundFileSourceRepository the repository to manage sound file sources
     */
    @Inject
    public SoundServiceImpl(SoundRepository soundRepository, SoundFileSourceRepository soundFileSourceRepository) {
        this.soundRepository = soundRepository;
        this.soundFileSourceRepository = soundFileSourceRepository;
    }

    @Override
    public SoundResponseDTO createSoundEvent(SoundEventDTO soundEventDTO) {
        SoundEventEntity event = soundEventDTO.toEntity();
        if (event.getId() != null) {
            return new SoundResponseDTO.SoundErrorDTO("New sound event cannot have an id");
        } else {
            event = soundRepository.save(event);
        }
        return SoundResponseDTO.SoundModelDTO.createDTO(event);
    }

    @Override
    public SoundResponseDTO updateSoundEvent(SoundEventDTO soundEventDTO) {
        Optional<SoundEventEntity> existingModel = soundRepository.findById(soundEventDTO.getId());
        if (existingModel.isEmpty()) {
            return new SoundResponseDTO.SoundErrorDTO(GENERIC_ERROR);
        }
        SoundEventEntity soundModel = soundEventDTO.toEntity();
        soundModel = soundRepository.update(soundModel);
        return SoundResponseDTO.SoundModelDTO.createDTO(soundModel);
    }

    @Override
    public SoundResponseDTO deleteSoundEvent(UUID id) {
        Optional<SoundEventEntity> model = soundRepository.findById(id);
        if (model.isPresent()) {
            soundRepository.deleteById(id);
            return SoundResponseDTO.SoundModelDTO.createDTO(model.get());
        }
        return new SoundResponseDTO.SoundErrorDTO(GENERIC_ERROR);
    }

    @Override
    public List<SoundResponseDTO> deleteAllSoundEvents() {
        soundRepository.deleteAll();
        return List.of();
    }

    @Override
    public List<SoundResponseDTO> getAllSoundEvents() {
        List<SoundEventEntity> list = soundRepository.findAll();

        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream()
                .map(SoundResponseDTO.SoundModelDTO::createDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SoundEventEntity> findSoundEventById(UUID id) {
        return soundRepository.findById(id);
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
        Optional<SoundEventEntity> soundEventOpt = soundRepository.findById(soundEventId);
        if (soundEventOpt.isEmpty()) {
            throw new IllegalArgumentException("Sound event not found");
        }
        var sourceEntity = sourceDTO.toEntity();
        sourceEntity.setSoundEvent(soundEventOpt.get());
        var savedSource = soundFileSourceRepository.save(sourceEntity);
        return SoundResponseDTO.SoundFileSourceDTO.createDTO(savedSource);
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO updateLinkedSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
        if (soundEventId == null || sourceDTO == null || sourceDTO.getId() == null) {
            throw new IllegalArgumentException("SoundEventId and SourceDTO and SourceDTO.Id must not be null");
        }
        Optional<SoundEventEntity> soundEventOpt = soundRepository.findById(soundEventId);
        if (soundEventOpt.isEmpty()) {
            throw new IllegalArgumentException("Sound event not found");
        }
        Optional<SoundResponseDTO.SoundFileSourceDTO> existingSourceOpt = this.getSoundSourcesById(soundEventId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(SoundResponseDTO.SoundFileSourceDTO.class::isInstance)
                .filter(Objects::nonNull)
                .map(SoundResponseDTO.SoundFileSourceDTO.class::cast)
                .filter(s -> s.id().equals(sourceDTO.getId()))
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
    public SoundResponseDTO.SoundFileSourceDTO deleteLinkedSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
        if (soundEventId == null || sourceDTO == null || sourceDTO.getId() == null) {
            throw new IllegalArgumentException("SoundEventId and SourceDTO and SourceDTO.Id must not be null");
        }

        Optional<SoundEventEntity> soundEventOpt = soundRepository.findById(soundEventId);
        if (soundEventOpt.isEmpty()) {
            throw new IllegalArgumentException("Sound event not found");
        }
        Optional<SoundResponseDTO.SoundFileSourceDTO> existingSourceOpt = this.getSoundSourcesById(soundEventId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(SoundResponseDTO.SoundFileSourceDTO.class::isInstance)
                .filter(Objects::nonNull)
                .map(SoundResponseDTO.SoundFileSourceDTO.class::cast)
                .filter(s -> s.id().equals(sourceDTO.getId()))
                .findFirst();
        if (existingSourceOpt.isEmpty()) {
            throw new IllegalArgumentException("Sound source not found for the given sound event");
        }
        soundFileSourceRepository.deleteById(sourceDTO.getId());
        return existingSourceOpt.get();
    }
}