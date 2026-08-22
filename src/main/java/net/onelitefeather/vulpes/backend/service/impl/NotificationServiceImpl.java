package net.onelitefeather.vulpes.backend.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.repository.NotificationRepository;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.NotificationService;

import java.util.UUID;

/**
 * Implementation of the {@link NotificationService} interface.
 */
@Singleton
public class NotificationServiceImpl
        extends AbstractCrudService<NotificationEntity, UUID, NotificationModelDTO, NotificationModelResponseDTO, NotificationModelResponseDTO.NotificationModelDTO>
        implements NotificationService {

    @Inject
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        super(
                notificationRepository,
                NotificationModelDTO::toNotificationModel,
                NotificationModelResponseDTO.NotificationModelDTO::createDTO,
                NotificationModelDTO::id,
                NotificationModelResponseDTO.NotificationModelErrorDTO::new,
                "Notification"
        );
    }
}