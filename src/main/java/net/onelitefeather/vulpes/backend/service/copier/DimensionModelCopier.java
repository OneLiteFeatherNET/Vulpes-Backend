package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.dimension.DimensionAttributeEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTimelineEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionAttributeRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionTimelineRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionTypeRepository;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionRelation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Copies a {@link DimensionTypeEntity} within the same project or into another one, optionally
 * including its environment attributes and timeline references.
 *
 * <p>Qualified {@code @Named("dimension")} — see {@link AttributeModelCopier}'s Javadoc for why.
 */
@Singleton
@Named("dimension")
public class DimensionModelCopier extends AbstractRelationalModelCopier<DimensionTypeEntity, DimensionRelation> implements EntityCopier<DimensionTypeEntity, DimensionRelation> {

    private final DimensionAttributeRepository dimensionAttributeRepository;
    private final DimensionTimelineRepository dimensionTimelineRepository;

    @Inject
    public DimensionModelCopier(
            DimensionTypeRepository dimensionTypeRepository,
            DimensionAttributeRepository dimensionAttributeRepository,
            DimensionTimelineRepository dimensionTimelineRepository,
            ProjectRepository projectRepository
    ) {
        super(dimensionTypeRepository, projectRepository, dimensionTypeRepository::existsByProjectIdAndKey, "Dimension");
        this.dimensionAttributeRepository = dimensionAttributeRepository;
        this.dimensionTimelineRepository = dimensionTimelineRepository;
    }

    /**
     * Widens {@link AbstractRelationalModelCopier#copy} to {@code public} and wraps it in a real
     * transaction boundary. See the class Javadoc on {@link AbstractRelationalModelCopier} for
     * why {@code @Transactional} has to be declared here, on the concrete class, rather than on
     * the inherited method itself.
     */
    @Override
    @Transactional
    public DimensionTypeEntity copy(
            UUID sourceProjectId,
            UUID sourceId,
            @Nullable UUID targetProjectId,
            @Nullable String targetKey,
            @Nullable String targetName,
            Set<DimensionRelation> relations
    ) {
        return super.copy(sourceProjectId, sourceId, targetProjectId, targetKey, targetName, relations);
    }

    @Override
    protected DimensionTypeEntity copyRoot(DimensionTypeEntity source, ProjectEntity targetProject, String targetKey, @Nullable String targetName) {
        String resolvedName = (targetName != null && !targetName.isBlank()) ? targetName : source.getUiName();
        return new DimensionTypeEntity(
                null,
                resolvedName,
                targetKey,
                source.isHasFixedTime(),
                source.isHasSkylight(),
                source.isHasCeiling(),
                source.isHasEnderDragonFight(),
                source.getCoordinateScale(),
                source.getMinY(),
                source.getHeight(),
                source.getLogicalHeight(),
                source.getInfiniburn(),
                source.getAmbientLight(),
                source.getMonsterSpawnLightLevel(),
                source.getMonsterSpawnBlockLightLimit(),
                source.getSkybox(),
                source.getCardinalLight(),
                source.getDefaultClock(),
                List.of(),
                List.of(),
                targetProject
        );
    }

    @Override
    protected void copyRelation(DimensionRelation relation, DimensionTypeEntity source, DimensionTypeEntity target) {
        switch (relation) {
            case ATTRIBUTES -> copyAttributes(source, target);
            case TIMELINES -> copyTimelines(source, target);
        }
    }

    private void copyAttributes(DimensionTypeEntity source, DimensionTypeEntity target) {
        List<DimensionAttributeEntity> copies = new ArrayList<>();
        for (DimensionAttributeEntity attribute : dimensionAttributeRepository.findAttributesByDimensionTypeId(source.getId(), Pageable.unpaged()).getContent()) {
            DimensionAttributeEntity copy = new DimensionAttributeEntity(null, attribute.getAttributeKey(), attribute.getOperator(), attribute.getAttributeValue());
            copy.setDimensionType(target);
            copies.add(copy);
        }
        dimensionAttributeRepository.saveAll(copies);
    }

    private void copyTimelines(DimensionTypeEntity source, DimensionTypeEntity target) {
        List<DimensionTimelineEntity> copies = new ArrayList<>();
        for (DimensionTimelineEntity timeline : dimensionTimelineRepository.findTimelinesByDimensionTypeId(source.getId(), Pageable.unpaged()).getContent()) {
            DimensionTimelineEntity copy = new DimensionTimelineEntity(null, timeline.getTimelineKey());
            copy.setDimensionType(target);
            copies.add(copy);
        }
        dimensionTimelineRepository.saveAll(copies);
    }
}
