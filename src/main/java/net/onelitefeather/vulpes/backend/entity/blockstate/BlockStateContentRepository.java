package net.onelitefeather.vulpes.backend.entity.blockstate;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.blockstate.BlockStateEntity;

import java.util.List;
import java.util.UUID;

/**
 * Reads the blockstates of a project for the content export.
 */
@Repository
public interface BlockStateContentRepository extends JpaRepository<BlockStateEntity, UUID> {

    /**
     * Returns all blockstates of the project, without paging.
     *
     * @param projectId the project of the blockstates
     * @return the blockstates
     */
    @Query("SELECT b FROM BlockStateEntity b WHERE b.project.id = :projectId")
    List<BlockStateEntity> findBlockStatesByProjectId(UUID projectId);
}
