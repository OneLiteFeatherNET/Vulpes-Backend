package net.onelitefeather.vulpes.backend.entity.advancement;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;

import java.util.List;
import java.util.UUID;

/**
 * Reads every advancement of a project for the complete project export.
 */
@Repository
public interface AdvancementContentRepository extends JpaRepository<AdvancementEntity, UUID> {

    /**
     * Returns all advancements of the project, without paging.
     *
     * @param projectId the project of the advancements
     * @return the advancements
     */
    @Query("SELECT a FROM advancements a WHERE a.project.id = :projectId")
    List<AdvancementEntity> findAdvancementsByProjectId(UUID projectId);
}
