package net.onelitefeather.vulpes.backend.entity.sound;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;

import java.util.List;
import java.util.UUID;

/**
 * Reads every sound event of a project and its sound sources for the complete project export.
 */
@Repository
public interface SoundContentRepository extends JpaRepository<SoundEventEntity, UUID> {

    /**
     * Returns all sound events of the project, without paging.
     *
     * @param projectId the project of the sounds
     * @return the sound events
     */
    @Query("SELECT s FROM sounds s WHERE s.project.id = :projectId")
    List<SoundEventEntity> findSoundsByProjectId(UUID projectId);

    /**
     * Returns the sound sources of all sound events of the project.
     *
     * @param projectId the project of the sounds
     * @return the sound sources, with their sound event
     */
    @Query("SELECT s FROM sound_data s WHERE s.soundEvent.project.id = :projectId")
    List<SoundFileSource> findSourcesByProjectId(UUID projectId);
}
