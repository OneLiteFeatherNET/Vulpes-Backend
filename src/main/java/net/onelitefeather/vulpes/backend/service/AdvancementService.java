package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelResponseDTO;

import java.util.UUID;

/**
 * Service interface for managing advancements.
 */
public interface AdvancementService extends CrudService<AdvancementEntity, UUID, AdvancementModelDTO, AdvancementModelResponseDTO.AdvancementModelDTO> {
}
