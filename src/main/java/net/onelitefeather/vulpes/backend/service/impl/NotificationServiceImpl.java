package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.repository.NotificationRepository;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.NotificationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the NotificationService interface.
 */
@Singleton
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Inject
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationModelResponseDTO.NotificationModelDTO createNotification(NotificationModelDTO notificationModelDTO) {
        NotificationEntity notificationModel = notificationModelDTO.toNotificationModel();
        NotificationEntity savedNotificationModel = notificationRepository.save(notificationModel);
        return NotificationModelResponseDTO.NotificationModelDTO.createDTO(savedNotificationModel);
    }

    @Override
    public NotificationModelResponseDTO updateNotification(NotificationModelDTO notificationModelDTO) {
        Optional<NotificationEntity> existingModel = notificationRepository.findById(notificationModelDTO.id());
        if (existingModel.isEmpty()) {
            return new NotificationModelResponseDTO.NotificationModelErrorDTO("Notification not found");
        }
        NotificationEntity notificationModel = notificationModelDTO.toNotificationModel();
        notificationModel = notificationRepository.update(notificationModel);
        return NotificationModelResponseDTO.NotificationModelDTO.createDTO(notificationModel);
    }

    @Override
    public NotificationModelResponseDTO deleteNotification(UUID id) {
        Optional<NotificationEntity> model = notificationRepository.findById(id);
        if (model.isPresent()) {
            notificationRepository.deleteById(id);
            return NotificationModelResponseDTO.NotificationModelDTO.createDTO(model.get());
        }
        return new NotificationModelResponseDTO.NotificationModelErrorDTO("Notification not found");
    }

    @Override
    public List<NotificationModelResponseDTO> deleteAllNotifications() {
        notificationRepository.deleteAll();
        return List.of();
    }

    @Override
    public Page<NotificationModelResponseDTO.NotificationModelDTO> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(NotificationModelResponseDTO.NotificationModelDTO::createDTO);
    }

    @Override
    public Optional<NotificationEntity> findNotificationById(UUID id) {
        return notificationRepository.findById(id);
    }
}