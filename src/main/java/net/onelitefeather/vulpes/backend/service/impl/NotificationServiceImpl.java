package net.onelitefeather.vulpes.backend.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.repository.NotificationRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.NotificationService;

import java.util.UUID;

/**
 * Implementation of the {@link NotificationService} interface.
 */
@Singleton
public class NotificationServiceImpl
        extends AbstractCrudService<NotificationEntity, UUID, NotificationModelDTO, NotificationModelResponseDTO.NotificationModelDTO>
        implements NotificationService {

    @Inject
    public NotificationServiceImpl(NotificationRepository notificationRepository, ProjectRepository projectRepository) {
        super(
                notificationRepository,
                projectRepository,
                NotificationModelDTO::toNotificationModel,
                NotificationModelResponseDTO.NotificationModelDTO::createDTO,
                entity -> entity.getProject().getId(),
                notificationRepository::findByProjectId,
                NotificationModelDTO::id,
                "Notification"
        );
    }
}