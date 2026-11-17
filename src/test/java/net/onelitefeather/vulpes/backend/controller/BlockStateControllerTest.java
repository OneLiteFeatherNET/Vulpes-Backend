package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.blockstate.BlockStateEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateDTO;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.BlockStateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit tests for BlockStateController (project-scoped)")
class BlockStateControllerTest {

    private static class StubBlockStateService implements BlockStateService {
        BlockStateResponseDTO response;
        Page<BlockStateResponseDTO> page;
        Optional<BlockStateEntity> findByIdResponse = Optional.empty();

        @Override
        public BlockStateResponseDTO toResponse(BlockStateEntity entity) {
            return new BlockStateResponseDTO(entity.getId(), entity.getUiName(), entity.getKey(), entity.getComment(),
                    List.of(), entity.getProject().getId(), null, null);
        }

        @Override
        public BlockStateResponseDTO create(BlockStateDTO dto) {
            return response;
        }

        @Override
        public BlockStateResponseDTO update(BlockStateDTO dto) {
            return response;
        }

        @Override
        public BlockStateResponseDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<BlockStateResponseDTO> getAll(Pageable pageable) {
            return page;
        }

        @Override
        public Optional<BlockStateEntity> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public BlockStateResponseDTO create(UUID projectId, BlockStateDTO dto) {
            return response;
        }

        @Override
        public BlockStateResponseDTO update(UUID projectId, BlockStateDTO dto) {
            return response;
        }

        @Override
        public BlockStateResponseDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<BlockStateResponseDTO> getAll(UUID projectId, Pageable pageable) {
            return page;
        }

        @Override
        public Optional<BlockStateEntity> findById(UUID projectId, UUID id) {
            return findByIdResponse;
        }
    }

    private static BlockStateDTO dto() {
        return new BlockStateDTO(null, "Petals", "minecraft:pink_petals", null, List.of());
    }

    private static BlockStateResponseDTO response(UUID projectId) {
        return new BlockStateResponseDTO(UUID.randomUUID(), "Petals", "minecraft:pink_petals", null, List.of(),
                projectId, Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("add() answers the created blockstate")
    void add_success_returnsOk() {
        StubBlockStateService stub = new StubBlockStateService();
        UUID projectId = UUID.randomUUID();
        stub.response = response(projectId);
        BlockStateController controller = new BlockStateController(stub);

        HttpResponse<BlockStateResponseDTO> resp = controller.add(projectId, dto());

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(stub.response, resp.body());
    }

    @Test
    @DisplayName("add() lets a PROJECT_NOT_FOUND from the service reach the exception handler")
    void add_unknownProject_propagates() {
        StubBlockStateService stub = new StubBlockStateService() {
            @Override
            public BlockStateResponseDTO create(UUID projectId, BlockStateDTO dto) {
                throw ApiException.projectNotFound();
            }
        };
        BlockStateController controller = new BlockStateController(stub);
        UUID projectId = UUID.randomUUID();
        BlockStateDTO dto = dto();

        ApiException exception = assertThrows(ApiException.class, () -> controller.add(projectId, dto));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("getById() returns the blockstate owned by the project")
    void getById_found_returnsOk() {
        StubBlockStateService stub = new StubBlockStateService();
        ProjectEntity project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        BlockStateEntity entity = new BlockStateEntity(UUID.randomUUID(), project, "Petals", "minecraft:pink_petals",
                null, "[]");
        stub.findByIdResponse = Optional.of(entity);
        BlockStateController controller = new BlockStateController(stub);

        HttpResponse<BlockStateResponseDTO> resp = controller.getById(project.getId(), entity.getId());

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(entity.getId(), resp.body().id());
    }

    @Test
    @DisplayName("getById() raises RESOURCE_NOT_FOUND when the entity belongs to another project")
    void getById_crossProject_raisesNotFound() {
        StubBlockStateService stub = new StubBlockStateService();
        BlockStateController controller = new BlockStateController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.getById(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("delete() lets a RESOURCE_NOT_FOUND from the service reach the exception handler")
    void delete_crossProject_propagates() {
        StubBlockStateService stub = new StubBlockStateService() {
            @Override
            public BlockStateResponseDTO delete(UUID projectId, UUID id) {
                throw ApiException.notFound("Blockstate");
            }
        };
        BlockStateController controller = new BlockStateController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.delete(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("getAll() answers the page of the project")
    void getAll_returnsScopedPage() {
        StubBlockStateService stub = new StubBlockStateService();
        UUID projectId = UUID.randomUUID();
        stub.page = Page.of(List.of(response(projectId)), Pageable.from(0, 10), 1L);
        BlockStateController controller = new BlockStateController(stub);

        HttpResponse<Page<BlockStateResponseDTO>> resp = controller.getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }
}
