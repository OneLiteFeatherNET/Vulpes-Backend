package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.NotificationRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;

import java.util.Set;
import java.util.UUID;

/**
 * Copies a {@link NotificationEntity} within the same project or into another one.
 *
 * <p>Qualified {@code @Named("notification")} — see {@link AttributeModelCopier}'s Javadoc for why.
 */
@Singleton
@Named("notification")
public class NotificationModelCopier extends AbstractModelCopier<NotificationEntity> implements EntityCopier<NotificationEntity, Void> {

    @Inject
    public NotificationModelCopier(NotificationRepository notificationRepository, ProjectRepository projectRepository) {
        super(notificationRepository, projectRepository, notificationRepository::existsByProjectIdAndKey, "Notification");
    }

    /**
     * Notifications have no relations to copy; {@code relations} is always empty and ignored.
     */
    @Override
    public NotificationEntity copy(
            UUID sourceProjectId,
            UUID sourceId,
            @Nullable UUID targetProjectId,
            @Nullable String targetKey,
            @Nullable String targetName,
            Set<Void> relations
    ) {
        return super.copy(sourceProjectId, sourceId, targetProjectId, targetKey, targetName);
    }

    @Override
    protected NotificationEntity copyRoot(NotificationEntity source, ProjectEntity targetProject, String targetKey, @Nullable String targetName) {
        String resolvedName = (targetName != null && !targetName.isBlank()) ? targetName : source.getUiName();
        return new NotificationEntity(
                null,
                resolvedName,
                targetKey,
                source.getComment(),
                source.getMaterial(),
                source.getFrameType(),
                source.getTitle(),
                targetProject
        );
    }
}
