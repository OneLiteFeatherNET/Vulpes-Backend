package net.onelitefeather.vulpes.backend.service.content;

import java.util.UUID;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO;

/**
 * Exports the complete content of a project from one consistent database state.
 */
public interface ProjectContentService {

    /**
     * Exports every content type of the project.
     *
     * @param projectId the id of the project
     * @return the content of the project, with a {@code null} revision
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException if the project does not exist
     */
    ProjectContentDTO export(UUID projectId);
}
