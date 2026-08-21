package net.onelitefeather.vulpes.backend.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.repository.AttributeRepository;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.AttributeService;

import java.util.UUID;

/**
 * Implementation of the {@link AttributeService} interface.
 */
@Singleton
public class AttributeServiceImpl
        extends AbstractCrudService<AttributeEntity, UUID, AttributeModelDTO, AttributeModelResponseDTO, AttributeModelResponseDTO.AttributeModelDTO>
        implements AttributeService {

    @Inject
    public AttributeServiceImpl(AttributeRepository attributeRepository) {
        super(
                attributeRepository,
                AttributeModelDTO::toAttributeModel,
                AttributeModelResponseDTO.AttributeModelDTO::create,
                AttributeModelDTO::id,
                AttributeModelResponseDTO.AttributeModelErrorDTO::new,
                "Attribute"
        );
    }
}