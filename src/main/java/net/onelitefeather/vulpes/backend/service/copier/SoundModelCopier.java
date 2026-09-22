package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.SoundFileSourceRepository;
import net.onelitefeather.vulpes.api.repository.SoundRepository;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.sound.SoundRelation;

import java.util.ArrayList;
import java.util.List;
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

    @Inject
    public SoundModelCopier(
            SoundRepository soundRepository,
            SoundFileSourceRepository soundFileSourceRepository,
            ProjectRepository projectRepository
    ) {
        super(soundRepository, projectRepository, soundRepository::existsByProjectIdAndKey, "Sound event");
        this.soundFileSourceRepository = soundFileSourceRepository;
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

    private void copySources(SoundEventEntity source, SoundEventEntity target) {
        List<SoundFileSource> copies = new ArrayList<>();
        for (SoundFileSource fileSource : soundFileSourceRepository.findSoundFileSourcesBySoundEvent(source.getId(), Pageable.unpaged()).getContent()) {
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
        soundFileSourceRepository.saveAll(copies);
    }
}
