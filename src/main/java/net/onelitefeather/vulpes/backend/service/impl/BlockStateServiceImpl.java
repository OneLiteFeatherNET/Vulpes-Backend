package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.core.type.Argument;
import io.micronaut.json.JsonMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.blockstate.BlockStateEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.blockstate.BlockStateRepository;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateDTO;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateResponseDTO;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateRuleDTO;
import net.onelitefeather.vulpes.backend.service.BlockStateService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link BlockStateService} interface.
 * <p>
 * The entity keeps the rules as a JSON array in one column, they are always read and written as a whole. This
 * service turns them into {@link BlockStateRuleDTO} records and back.
 * </p>
 */
@Singleton
public class BlockStateServiceImpl
        extends AbstractCrudService<BlockStateEntity, UUID, BlockStateDTO, BlockStateResponseDTO>
        implements BlockStateService {

    private static final Argument<List<BlockStateRuleDTO>> RULES = Argument.listOf(BlockStateRuleDTO.class);

    private final JsonMapper jsonMapper;

    @Inject
    public BlockStateServiceImpl(
            BlockStateRepository blockStateRepository,
            ProjectRepository projectRepository,
            JsonMapper jsonMapper
    ) {
        super(
                blockStateRepository,
                projectRepository,
                (dto, project) -> toEntity(jsonMapper, dto, project),
                entity -> toResponse(jsonMapper, entity),
                entity -> entity.getProject().getId(),
                blockStateRepository::findByProjectId,
                BlockStateDTO::id,
                "Blockstate"
        );
        this.jsonMapper = jsonMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockStateResponseDTO toResponse(BlockStateEntity entity) {
        return toResponse(jsonMapper, entity);
    }

    private static BlockStateEntity toEntity(JsonMapper jsonMapper, BlockStateDTO dto, ProjectEntity project) {
        try {
            String rules = jsonMapper.writeValueAsString(RULES, dto.rules());
            return new BlockStateEntity(dto.id(), project, dto.uiName(), dto.key(), dto.comment(), rules);
        } catch (IOException exception) {
            throw new UncheckedIOException("The rules of a blockstate could not be written", exception);
        }
    }

    private static BlockStateResponseDTO toResponse(JsonMapper jsonMapper, BlockStateEntity entity) {
        try {
            List<BlockStateRuleDTO> rules = jsonMapper.readValue(entity.getRules(), RULES);
            return new BlockStateResponseDTO(
                    entity.getId(),
                    entity.getUiName(),
                    entity.getKey(),
                    entity.getComment(),
                    rules,
                    entity.getProject().getId(),
                    entity.getCreationDate(),
                    entity.getModificationDate()
            );
        } catch (IOException exception) {
            throw new UncheckedIOException("The rules of blockstate " + entity.getId() + " could not be read", exception);
        }
    }
}
