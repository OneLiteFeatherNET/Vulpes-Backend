package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.ProjectService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link ProjectService} interface.
 */
@Singleton
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Inject
    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectModelResponseDTO.ProjectModelDTO createProject(ProjectModelDTO projectModelDTO) {
        ProjectEntity projectModel = projectModelDTO.toProjectEntity();
        ProjectEntity savedProjectModel = projectRepository.save(projectModel);
        return ProjectModelResponseDTO.ProjectModelDTO.createDTO(savedProjectModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectModelResponseDTO updateProject(ProjectModelDTO projectModelDTO) {
        Optional<ProjectEntity> existingModel = projectRepository.findById(projectModelDTO.id());
        if (existingModel.isEmpty()) {
            return new ProjectModelResponseDTO.ProjectModelErrorDTO("Project not found");
        }
        ProjectEntity projectModel = projectModelDTO.toProjectEntity();
        projectModel = projectRepository.update(projectModel);
        return ProjectModelResponseDTO.ProjectModelDTO.createDTO(projectModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectModelResponseDTO deleteProject(UUID id) {
        Optional<ProjectEntity> model = projectRepository.findById(id);
        if (model.isPresent()) {
            projectRepository.deleteById(id);
            return ProjectModelResponseDTO.ProjectModelDTO.createDTO(model.get());
        }
        return new ProjectModelResponseDTO.ProjectModelErrorDTO("Project not found");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProjectModelResponseDTO> deleteAllProjects() {
        projectRepository.deleteAll();
        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ProjectModelResponseDTO.ProjectModelDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(ProjectModelResponseDTO.ProjectModelDTO::createDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProjectEntity> findProjectById(UUID id) {
        return projectRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProjectEntity> findProjectByKey(String key) {
        return projectRepository.findByKey(key);
    }
}
