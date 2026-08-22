package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;

import java.util.UUID;

/**
 * Service interface for managing notifications.
 */
public interface NotificationService extends CrudService<NotificationEntity, UUID, NotificationModelDTO, NotificationModelResponseDTO, NotificationModelResponseDTO.NotificationModelDTO> {
}