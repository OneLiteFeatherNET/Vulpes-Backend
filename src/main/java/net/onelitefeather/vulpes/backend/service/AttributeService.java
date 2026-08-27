package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;

import java.util.UUID;

/**
 * Service interface for managing attributes.
 */
public interface AttributeService extends CrudService<AttributeEntity, UUID, AttributeModelDTO, AttributeModelResponseDTO.AttributeModelDTO> {
}