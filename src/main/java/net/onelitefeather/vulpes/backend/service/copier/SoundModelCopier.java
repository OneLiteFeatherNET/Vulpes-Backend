package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkEntity;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkRole;
import net.onelitefeather.vulpes.api.model.asset.AssetTargetType;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.SoundFileSourceRepository;
import net.onelitefeather.vulpes.api.repository.SoundRepository;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.sound.SoundRelation;
import net.onelitefeather.vulpes.backend.entity.asset.AssetLinkRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Copies a {@link SoundEventEntity} within the same project or into another one, optionally
 * including its sound file sources.
 *
 * <p>Qualified {@code @Named("sound")} — see {@link AttributeModelCopier}'s Javadoc for why.
 */
@Singleton
@Named("sound")
public class SoundModelCopier extends AbstractRelationalModelCopier<SoundEventEntity, SoundRelation> implements EntityCopier<SoundEventEntity, SoundRelation> {

    private final SoundFileSourceRepository soundFileSourceRepository;
    private final AssetLinkRepository assetLinkRepository;

    @Inject
    public SoundModelCopier(
            SoundRepository soundRepository,
            SoundFileSourceRepository soundFileSourceRepository,
            AssetLinkRepository assetLinkRepository,
            ProjectRepository projectRepository
    ) {
        super(soundRepository, projectRepository, soundRepository::existsByProjectIdAndKey, "Sound event");
        this.soundFileSourceRepository = soundFileSourceRepository;
        this.assetLinkRepository = assetLinkRepository;
    }

    /**
     * Widens {@link AbstractRelationalModelCopier#copy} to {@code public} and wraps it in a real
     * transaction boundary. See the class Javadoc on {@link AbstractRelationalModelCopier} for
     * why {@code @Transactional} has to be declared here, on the concrete class, rather than on
     * the inherited method itself.
     */
    @Override
    @Transactional
    public SoundEventEntity copy(
            UUID sourceProjectId,
            UUID sourceId,
            @Nullable UUID targetProjectId,
            @Nullable String targetKey,
            @Nullable String targetName,
            Set<SoundRelation> relations
    ) {
        return super.copy(sourceProjectId, sourceId, targetProjectId, targetKey, targetName, relations);
    }

    @Override
    protected SoundEventEntity copyRoot(SoundEventEntity source, ProjectEntity targetProject, String targetKey, @Nullable String targetName) {
        String resolvedName = (targetName != null && !targetName.isBlank()) ? targetName : source.getUiName();
        return new SoundEventEntity(
                null,
                resolvedName,
                targetKey,
                source.getKeyName(),
                // SoundEventEntity exposes no getter for the "replace" flag (see the field in
                // net.onelitefeather.vulpes.api.model.sound.SoundEventEntity); every other spot
                // in this codebase that builds one from an existing entity, e.g. SoundEventDTO#toEntity,
                // has the same gap and passes false for the same reason.
                false,
                source.getSubTitle(),
                source.getComment(),
                List.of(),
                targetProject
        );
    }

    @Override
    protected void copyRelation(SoundRelation relation, SoundEventEntity source, SoundEventEntity target) {
        if (relation == SoundRelation.SOURCES) {
            copySources(source, target);
        }
    }

    /**
     * Copies the sources of a sound event. Within the same project a copied source plays the same sound asset
     * as its original; in another project that asset does not exist, so the copy keeps only its name.
     */
    private void copySources(SoundEventEntity source, SoundEventEntity target) {
        List<SoundFileSource> originals = soundFileSourceRepository
                .findSoundFileSourcesBySoundEvent(source.getId(), Pageable.unpaged())
                .getContent();
        boolean sameProject = source.getProject().getId().equals(target.getProject().getId());
        Map<UUID, AssetLinkEntity> links = sameProject ? soundLinks(originals) : Map.of();
        List<SoundFileSource> copies = new ArrayList<>();
        List<AssetLinkEntity> copiedLinks = new ArrayList<>();
        for (SoundFileSource fileSource : originals) {
            SoundFileSource copy = new SoundFileSource(
                    null,
                    fileSource.getName(),
                    fileSource.getVolume(),
                    fileSource.getPitch(),
                    fileSource.getWeight(),
                    fileSource.isStreamable(),
                    fileSource.getAttenuationDistance(),
                    fileSource.isPreloadable(),
                    fileSource.getType()
            );
            copy.setSoundEvent(target);
            copies.add(copy);
        }
        List<SoundFileSource> saved = soundFileSourceRepository.saveAll(copies);
        for (int index = 0; index < originals.size(); index++) {
            AssetLinkEntity link = links.get(originals.get(index).getId());
            if (link != null) {
                copiedLinks.add(soundLink(link, saved.get(index).getId()));
            }
        }
        assetLinkRepository.saveAll(copiedLinks);
    }

    private Map<UUID, AssetLinkEntity> soundLinks(List<SoundFileSource> sources) {
        if (sources.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = sources.stream().map(SoundFileSource::getId).toList();
        Map<UUID, AssetLinkEntity> links = new HashMap<>();
        for (AssetLinkEntity link :
                assetLinkRepository.findTargetLinks(AssetTargetType.SOUND_SOURCE, ids, AssetLinkRole.SOUND)) {
            links.put(link.getTargetId(), link);
        }
        return links;
    }

    private static AssetLinkEntity soundLink(AssetLinkEntity original, UUID sourceId) {
        AssetLinkEntity link = new AssetLinkEntity();
        link.setAsset(original.getAsset());
        link.setTargetType(AssetTargetType.SOUND_SOURCE);
        link.setTargetId(sourceId);
        link.setRole(AssetLinkRole.SOUND);
        return link;
    }
}
