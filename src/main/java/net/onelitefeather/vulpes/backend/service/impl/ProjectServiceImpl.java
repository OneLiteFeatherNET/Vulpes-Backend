package net.onelitefeather.vulpes.backend.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.ProjectService;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link ProjectService} interface.
 */
@Singleton
public class ProjectServiceImpl
        extends AbstractCrudService<ProjectEntity, UUID, ProjectModelDTO, ProjectModelResponseDTO.ProjectModelDTO>
        implements ProjectService {

    private final ProjectRepository projectRepository;

    @Inject
    public ProjectServiceImpl(ProjectRepository projectRepository) {
        super(
                projectRepository,
                ProjectModelDTO::toProjectEntity,
                ProjectModelResponseDTO.ProjectModelDTO::createDTO,
                ProjectModelDTO::id,
                "Project"
        );
        this.projectRepository = projectRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProjectEntity> findProjectByKey(String key) {
        return projectRepository.findAll()
                .stream()
                .filter(project -> key.equals(project.getKey()))
                .findFirst();
    }
}


