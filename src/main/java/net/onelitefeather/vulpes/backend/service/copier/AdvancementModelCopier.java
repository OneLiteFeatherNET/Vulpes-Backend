package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.AdvancementRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;

import java.util.Set;
import java.util.UUID;

/**
 * Copies an {@link AdvancementEntity} within the same project or into another one.
 *
 * <p>Only the advancement itself is copied, not its children. A copy within the same project keeps
 * the parent of the source, a copy into another project becomes a root, since the parent belongs
 * to the source project.
 *
 * <p>Qualified {@code @Named("advancement")} — see {@link AttributeModelCopier}'s Javadoc for why.
 */
@Singleton
@Named("advancement")
public class AdvancementModelCopier extends AbstractModelCopier<AdvancementEntity> implements EntityCopier<AdvancementEntity, Void> {

    @Inject
    public AdvancementModelCopier(AdvancementRepository advancementRepository, ProjectRepository projectRepository) {
        super(advancementRepository, projectRepository, advancementRepository::existsByProjectIdAndKey, "Advancement");
    }

    /**
     * Advancements have no relations to copy; {@code relations} is always empty and ignored.
     */
    @Override
    public AdvancementEntity copy(
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
    protected AdvancementEntity copyRoot(AdvancementEntity source, ProjectEntity targetProject, String targetKey, @Nullable String targetName) {
        String resolvedName = (targetName != null && !targetName.isBlank()) ? targetName : source.getUiName();
        boolean sameProject = source.getProject().getId().equals(targetProject.getId());
        AdvancementEntity copy = new AdvancementEntity(
                null,
                resolvedName,
                targetKey,
                source.getComment(),
                source.getMaterial(),
                source.getFrameType(),
                source.getTitle(),
                source.getDescription(),
                sameProject ? source.getParent() : null,
                targetProject
        );
        copy.setBackground(source.getBackground());
        copy.setX(source.getX());
        copy.setY(source.getY());
        copy.setShowToast(source.isShowToast());
        copy.setAnnounceToChat(source.isAnnounceToChat());
        copy.setHidden(source.isHidden());
        return copy;
    }
}
