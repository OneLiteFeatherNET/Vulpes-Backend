package net.onelitefeather.vulpes.backend.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.repository.AttributeRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.AttributeService;

import java.util.UUID;

/**
 * Implementation of the {@link AttributeService} interface.
 */
@Singleton
public class AttributeServiceImpl
        extends AbstractCrudService<AttributeEntity, UUID, AttributeModelDTO, AttributeModelResponseDTO.AttributeModelDTO>
        implements AttributeService {

    @Inject
    public AttributeServiceImpl(AttributeRepository attributeRepository, ProjectRepository projectRepository) {
        super(
                attributeRepository,
                projectRepository,
                AttributeModelDTO::toAttributeModel,
                AttributeModelResponseDTO.AttributeModelDTO::create,
                entity -> entity.getProject().getId(),
                attributeRepository::findByProjectId,
                AttributeModelDTO::id,
                "Attribute"
        );
    }
}