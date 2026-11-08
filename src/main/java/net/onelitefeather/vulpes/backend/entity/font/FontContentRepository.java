package net.onelitefeather.vulpes.backend.entity.font;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;

import java.util.List;
import java.util.UUID;

/**
 * Reads every font of a project and the chars of those fonts for the complete project export.
 */
@Repository
public interface FontContentRepository extends JpaRepository<FontEntity, UUID> {

    /**
     * Returns all fonts of the project with their texture asset loaded, without paging.
     *
     * @param projectId the project of the fonts
     * @return the fonts
     */
    @Query("SELECT f FROM fonts f LEFT JOIN FETCH f.textureAsset WHERE f.project.id = :projectId")
    List<FontEntity> findFontsByProjectId(UUID projectId);

    /**
     * Returns the chars of all fonts of the project.
     *
     * @param projectId the project of the fonts
     * @return the chars, with their font
     */
    @Query("SELECT s FROM font_string s WHERE s.font.project.id = :projectId")
    List<FontStringEntity> findCharsByProjectId(UUID projectId);
}
