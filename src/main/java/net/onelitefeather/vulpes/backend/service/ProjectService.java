package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing projects.
 */
public interface ProjectService {

    /**
     * Creates a new project.
     *
     * @param projectModelDTO the project data to create
     * @return the created project response
     */
    ProjectModelResponseDTO.ProjectModelDTO createProject(ProjectModelDTO projectModelDTO);

    /**
     * Updates an existing project.
     *
     * @param projectModelDTO the project data to update
     * @return the updated project response or an error response if the project doesn't exist
     */
    ProjectModelResponseDTO updateProject(ProjectModelDTO projectModelDTO);

    /**
     * Deletes a project by its ID.
     *
     * @param id the ID of the project to delete
     * @return the deleted project response or an error response if the project doesn't exist
     */
    ProjectModelResponseDTO deleteProject(UUID id);

    /**
     * Deletes all projects.
     *
     * @return an empty list
     */
    List<ProjectModelResponseDTO> deleteAllProjects();

    /**
     * Gets all projects with pagination.
     *
     * @param pageable pagination information
     * @return a page of projects
     */
    Page<ProjectModelResponseDTO.ProjectModelDTO> getAllProjects(Pageable pageable);

    /**
     * Finds a project by its ID.
     *
     * @param id the ID of the project to find
     * @return an optional containing the project if found, or empty if not found
     */
    Optional<ProjectEntity> findProjectById(UUID id);

    /**
     * Finds a project by its unique key.
     *
     * @param key the key of the project to find
     * @return an optional containing the project if found, or empty if not found
     */
    Optional<ProjectEntity> findProjectByKey(String key);
}
