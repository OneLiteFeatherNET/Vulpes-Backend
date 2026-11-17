package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.api.model.blockstate.BlockStateEntity;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateDTO;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateResponseDTO;

import java.util.UUID;

/**
 * Service interface for managing blockstates.
 */
public interface BlockStateService extends CrudService<BlockStateEntity, UUID, BlockStateDTO, BlockStateResponseDTO> {

    /**
     * Maps a blockstate to the response, with its rules read from their JSON column.
     *
     * @param entity the blockstate
     * @return the response DTO
     */
    BlockStateResponseDTO toResponse(BlockStateEntity entity);
}
