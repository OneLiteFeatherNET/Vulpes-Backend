package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkEntity;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkRole;
import net.onelitefeather.vulpes.api.model.asset.AssetTargetType;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.SoundFileSourceRepository;
import net.onelitefeather.vulpes.api.repository.SoundRepository;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import net.onelitefeather.vulpes.backend.entity.asset.AssetLinkRepository;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRepository;
import net.onelitefeather.vulpes.backend.entity.sound.SoundContentRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.SoundService;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link SoundService} interface.
 *
 * <p>A source that plays an uploaded sound is linked to that sound asset through an {@link AssetLinkEntity}
 * with target type {@link AssetTargetType#SOUND_SOURCE} and role {@link AssetLinkRole#SOUND}. The link keeps
 * the asset from being deleted. The target id is no foreign key, so every path that deletes sources deletes
 * their links first.
 */
@Singleton
public class SoundServiceImpl
        extends AbstractCrudService<SoundEventEntity, UUID, SoundEventDTO, SoundResponseDTO.SoundModelDTO>
        implements SoundService {

    private static final String SOUND_EVENT = "Sound event";
    private static final String SOUND_SOURCE = "Sound source";
    private static final String ASSET = "Asset";
    private static final String ASSET_ID = "assetId";

    /** The sound asset is missing, of another kind, or does not fit the source. */
    static final String ASSET_INVALID = "SOUND_SOURCE_ASSET_INVALID";

    /** The source is not of type {@code file}, or its name is not the key of the sound asset. */
    static final String ASSET_MISMATCH = "SOUND_SOURCE_ASSET_MISMATCH";

    private final SoundFileSourceRepository soundFileSourceRepository;
    private final SoundContentRepository soundContentRepository;
    private final ProjectAssetRepository assetRepository;
    private final AssetLinkRepository linkRepository;

    /**
     * Constructs a new SoundServiceImpl with the specified SoundRepository and SoundFileSourceRepository.
     *
     * @param soundRepository           the repository to manage sound events
     * @param soundFileSourceRepository the repository to manage sound file sources
     * @param soundContentRepository    the repository to read all sources of a project
     * @param assetRepository           the repository of project assets
     * @param linkRepository            the repository of links between sources and sound assets
     * @param projectRepository         the repository to manage projects
     */
    @Inject
    public SoundServiceImpl(
            SoundRepository soundRepository,
            SoundFileSourceRepository soundFileSourceRepository,
            SoundContentRepository soundContentRepository,
            ProjectAssetRepository assetRepository,
            AssetLinkRepository linkRepository,
            ProjectRepository projectRepository) {
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
        this.soundContentRepository = soundContentRepository;
        this.assetRepository = assetRepository;
        this.linkRepository = linkRepository;
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

    /**
     * Returns the sound asset a source request names, checked against the source.
     *
     * @param soundEvent the sound event the source belongs to
     * @param sourceDTO  the request
     * @return the sound asset, {@code null} if the request names none
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the asset does not exist in the project of the
     *                      sound event, {@code VALIDATION_FAILED} if it is no sound or does not fit the source
     */
    private @Nullable ProjectAssetEntity requirePlayableSound(SoundEventEntity soundEvent, SoundFileSourceDTO sourceDTO) {
        UUID assetId = sourceDTO.assetId();
        if (assetId == null) {
            return null;
        }
        ProjectAssetEntity asset = assetRepository.findById(assetId).orElseThrow(() -> ApiException.notFound(ASSET));
        UUID projectId = soundEvent.getProject().getId();
        if (!asset.getProject().getId().equals(projectId)) {
            throw ApiException.notOwnedByProject(ASSET, assetId, projectId);
        }
        if (asset.getKind() != AssetKind.SOUND) {
            throw assetViolation(ASSET_INVALID, "must be a sound asset");
        }
        if (!SoundFileSourceDTO.TYPE_FILE.equals(sourceDTO.type()) || !asset.getKey().equals(sourceDTO.name())) {
            throw assetViolation(ASSET_MISMATCH, "requires type 'file' and the asset key '" + asset.getKey() + "' as name");
        }
        return asset;
    }

    private static ApiException assetViolation(String code, String message) {
        return ApiException.validationFailed(List.of(new ProblemDetail.Violation(ASSET_ID, code, message)));
    }

    /**
     * Makes the source play the given sound: the existing link is pointed to it, created, or removed when no
     * sound is given.
     *
     * @return the id of the linked sound, {@code null} if the source plays none
     */
    private @Nullable UUID linkSound(UUID sourceId, @Nullable ProjectAssetEntity sound) {
        Optional<AssetLinkEntity> existing =
                linkRepository.findByTargetTypeAndTargetIdAndRole(AssetTargetType.SOUND_SOURCE, sourceId, AssetLinkRole.SOUND);
        if (sound == null) {
            existing.ifPresent(linkRepository::delete);
            return null;
        }
        AssetLinkEntity link = existing.orElseGet(AssetLinkEntity::new);
        link.setAsset(sound);
        link.setTargetType(AssetTargetType.SOUND_SOURCE);
        link.setTargetId(sourceId);
        link.setRole(AssetLinkRole.SOUND);
        if (existing.isPresent()) {
            linkRepository.update(link);
        } else {
            linkRepository.save(link);
        }
        return sound.getId();
    }

    /**
     * Deletes the links of the given sources, which releases the sounds they play.
     */
    private void unlinkSources(Collection<UUID> sourceIds) {
        if (!sourceIds.isEmpty()) {
            linkRepository.deleteTargetLinks(AssetTargetType.SOUND_SOURCE, sourceIds);
        }
    }

    private List<UUID> sourceIdsOf(UUID soundEventId) {
        return soundFileSourceRepository.findSoundFileSourcesBySoundEvent(soundEventId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(SoundFileSource::getId)
                .toList();
    }

    @Override
    public Page<SoundResponseDTO.SoundFileSourceDTO> getSoundSourcesById(UUID id, Pageable pageable) {
        Page<SoundFileSource> sources = this.soundFileSourceRepository.findSoundFileSourcesBySoundEvent(id, pageable);
        List<UUID> sourceIds = sources.getContent().stream().map(SoundFileSource::getId).toList();
        Map<UUID, UUID> sounds = new HashMap<>();
        if (!sourceIds.isEmpty()) {
            for (AssetLinkEntity link :
                    linkRepository.findTargetLinks(AssetTargetType.SOUND_SOURCE, sourceIds, AssetLinkRole.SOUND)) {
                sounds.put(link.getTargetId(), link.getAsset().getId());
            }
        }
        return sources.map(source -> SoundResponseDTO.SoundFileSourceDTO.createDTO(source, sounds.get(source.getId())));
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO createAndLinkSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
        if (sourceDTO == null) {
            throw ApiException.invalidRequest("A sound source body is required.");
        }
        var soundEvent = requireSoundEvent(soundEventId);
        ProjectAssetEntity sound = requirePlayableSound(soundEvent, sourceDTO);
        var sourceEntity = sourceDTO.toEntity();
        sourceEntity.setSoundEvent(soundEvent);
        var savedSource = soundFileSourceRepository.save(sourceEntity);
        return SoundResponseDTO.SoundFileSourceDTO.createDTO(savedSource, linkSound(savedSource.getId(), sound));
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO updateLinkedSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
        if (sourceDTO == null || sourceDTO.id() == null) {
            throw ApiException.invalidRequest("An id is required to update a sound source.");
        }
        var soundEvent = requireSoundEvent(soundEventId);
        requireLinkedSource(soundEventId, sourceDTO.id());
        ProjectAssetEntity sound = requirePlayableSound(soundEvent, sourceDTO);
        var sourceEntity = sourceDTO.toEntity();
        sourceEntity.setSoundEvent(soundEvent);
        var updatedSource = soundFileSourceRepository.update(sourceEntity);
        return SoundResponseDTO.SoundFileSourceDTO.createDTO(updatedSource, linkSound(updatedSource.getId(), sound));
    }

    @Override
    @Transactional
    public SoundResponseDTO.SoundFileSourceDTO deleteLinkedSource(UUID soundEventId, UUID sourceID) {
        if (sourceID == null) {
            throw ApiException.invalidRequest("A sound source id is required.");
        }
        requireSoundEvent(soundEventId);
        var existingSource = requireLinkedSource(soundEventId, sourceID);
        unlinkSources(List.of(sourceID));
        soundFileSourceRepository.deleteById(sourceID);
        return existingSource;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The links of the sources of the sound event are deleted with it.
     */
    @Override
    @Transactional
    public SoundResponseDTO.SoundModelDTO delete(UUID projectId, UUID id) {
        if (findById(projectId, id).isPresent()) {
            unlinkSources(sourceIdsOf(id));
        }
        return super.delete(projectId, id);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The links of all sources of the project are deleted with them.
     */
    @Override
    @Transactional
    public void deleteAll(UUID projectId) {
        unlinkSources(soundContentRepository.findSourcesByProjectId(projectId).stream()
                .map(SoundFileSource::getId)
                .filter(Objects::nonNull)
                .toList());
        super.deleteAll(projectId);
    }
}
