package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.AttributeRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;

import java.util.Set;
import java.util.UUID;

/**
 * Copies an {@link AttributeEntity} within the same project or into another one.
 *
 * <p>Qualified {@code @Named("attribute")}: with three beans implementing {@link EntityCopier}
 * for different entity/relation type parameters, Micronaut's generic-type bean resolution alone
 * proved unreliable once {@link ItemModelCopier}'s {@code @Transactional} AOP proxy is in play
 * (its reified generics get lost on the proxy, making it a false candidate for every injection
 * point). An explicit qualifier sidesteps that entirely.
 */
@Singleton
@Named("attribute")
public class AttributeModelCopier extends AbstractModelCopier<AttributeEntity> implements EntityCopier<AttributeEntity, Void> {

    @Inject
    public AttributeModelCopier(AttributeRepository attributeRepository, ProjectRepository projectRepository) {
        super(attributeRepository, projectRepository, attributeRepository::existsByProjectIdAndKey, "Attribute");
    }

    /**
     * Attributes have no relations to copy; {@code relations} is always empty and ignored.
     */
    @Override
    public AttributeEntity copy(
            UUID sourceProjectId,
            UUID sourceId,
            @Nullable UUID targetProjectId,
            @Nullable String targetKey,
            Set<Void> relations
    ) {
        return super.copy(sourceProjectId, sourceId, targetProjectId, targetKey);
    }

    @Override
    protected AttributeEntity copyRoot(AttributeEntity source, ProjectEntity targetProject, String targetKey) {
        return new AttributeEntity(
                null,
                source.getUiName(),
                targetKey,
                source.getDefaultValue(),
                source.getMaximumValue(),
                targetProject
        );
    }
}
