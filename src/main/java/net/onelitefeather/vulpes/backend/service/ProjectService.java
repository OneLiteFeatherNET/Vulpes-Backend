package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing projects.
 */
public interface ProjectService extends CrudService<ProjectEntity, UUID, ProjectModelDTO, ProjectModelResponseDTO.ProjectModelDTO> {

    /**
     * Finds a project by its unique key.
     *
     * @param key the key of the project to find
     * @return an optional containing the project if found, or empty if not found
     */
    Optional<ProjectEntity> findProjectByKey(String key);
}

