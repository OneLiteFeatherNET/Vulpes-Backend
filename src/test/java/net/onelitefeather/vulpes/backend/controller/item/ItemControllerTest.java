package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ItemController (project-scoped)")
class ItemControllerTest {

    private static class StubItemService implements ItemService {
        ItemModelResponseDTO.ItemModelDTO response;
        Page<ItemModelResponseDTO.ItemModelDTO> page;
        Optional<ItemEntity> findByIdResponse = Optional.empty();

        @Override
        public ItemModelResponseDTO.ItemModelDTO create(ItemModelDTO dto) {
            return (ItemModelResponseDTO.ItemModelDTO) response;
        }

        @Override
        public ItemModelResponseDTO.ItemModelDTO update(ItemModelDTO dto) {
            return response;
        }

        @Override
        public ItemModelResponseDTO.ItemModelDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<ItemModelResponseDTO.ItemModelDTO> getAll(Pageable pageable) {
            return page;
        }

        @Override
        public Optional<ItemEntity> findById(UUID id) {
            return findByIdResponse;
        }

        @Override
        public ItemModelResponseDTO.ItemModelDTO create(UUID projectId, ItemModelDTO dto) {
            return response;
        }

        @Override
        public ItemModelResponseDTO.ItemModelDTO update(UUID projectId, ItemModelDTO dto) {
            return response;
        }

        @Override
        public ItemModelResponseDTO.ItemModelDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<ItemModelResponseDTO.ItemModelDTO> getAll(UUID projectId, Pageable pageable) {
            return page;
        }

        @Override
        public Optional<ItemEntity> findById(UUID projectId, UUID id) {
            return findByIdResponse;
        }

        @Override
        public Page<ItemFlagResponseDTO.ItemFlagDTO> findFlagsById(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public ItemFlagResponseDTO.ItemFlagDTO createFlagById(UUID id, ItemFlagDTO itemFlagDTO) {
            return null;
        }

        @Override
        public ItemFlagResponseDTO.ItemFlagDTO deleteFlagById(UUID id, UUID flagId) {
            return null;
        }

        @Override
        public List<ItemFlagResponseDTO.ItemFlagDTO> deleteAllFlagsById(UUID id) {
            return List.of();
        }

        @Override
        public ItemFlagResponseDTO.ItemFlagDTO updateFlagById(UUID id, ItemFlagDTO flag) {
            return null;
        }

        @Override
        public Page<ItemEnchantmentResponseDTO.ItemEnchantmentDTO> findEnchantmentsById(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public ItemEnchantmentResponseDTO.ItemEnchantmentDTO updateEnchantmentById(UUID id, ItemEnchantmentDTO enchantment) {
            return null;
        }

        @Override
        public ItemEnchantmentResponseDTO.ItemEnchantmentDTO createEnchantmentById(UUID id, ItemEnchantmentDTO enchantment) {
            return null;
        }

        @Override
        public ItemEnchantmentResponseDTO.ItemEnchantmentDTO deleteEnchantmentById(UUID id, UUID enchantment) {
            return null;
        }

        @Override
        public List<ItemEnchantmentResponseDTO.ItemEnchantmentDTO> deleteAllEnchantmentsById(UUID id) {
            return List.of();
        }

        @Override
        public Page<ItemLoreResponseDTO.ItemLoreDTO> findLoreById(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public ItemLoreResponseDTO.ItemLoreDTO updateLoreById(UUID id, ItemLoreDTO loreDto) {
            return null;
        }

        @Override
        public ItemLoreResponseDTO.ItemLoreDTO createLoreById(UUID id, ItemLoreDTO loreDto) {
            return null;
        }

        @Override
        public ItemLoreResponseDTO.ItemLoreDTO deleteLoreById(UUID id, UUID loreId) {
            return null;
        }

        @Override
        public ItemLoreResponseDTO.ItemLoreDTO reorderLoreById(UUID id, UUID entryId, int newIndex) {
            return null;
        }

        @Override
        public List<ItemLoreResponseDTO.ItemLoreDTO> deleteAllLoreById(UUID id) {
            return List.of();
        }
    }

    private static ItemModelDTO sampleDTO(UUID id) {
        return new ItemModelDTO(id, "UI", "var", "comment", "display", "STONE", "group", 0, 1);
    }

    private static ItemModelResponseDTO.ItemModelDTO sampleResponse(UUID id, UUID projectId) {
        return new ItemModelResponseDTO.ItemModelDTO(
                id, "UI", "var", "comment", "display", "STONE", "group", 0, 1,
                Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), projectId
        );
    }

    @Test
    void add_success_returnsOk() {
        StubItemService stub = new StubItemService();
        UUID projectId = UUID.randomUUID();
        stub.response = sampleResponse(UUID.randomUUID(), projectId);
        ItemController controller = new ItemController(stub);

        HttpResponse<ItemModelResponseDTO.ItemModelDTO> resp = controller.add(projectId, sampleDTO(null));

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(ItemModelResponseDTO.ItemModelDTO.class, resp.body());
    }

    @Test
    @DisplayName("add() lets a PROJECT_NOT_FOUND from the service reach the exception handler")
    void add_unknownProject_propagates() {
        StubItemService stub = new StubItemService() {
            @Override
            public ItemModelResponseDTO.ItemModelDTO create(UUID projectId, ItemModelDTO dto) {
                throw ApiException.projectNotFound();
            }
        };
        ItemController controller = new ItemController(stub);
        ItemModelDTO dto = sampleDTO(null);
        UUID projectId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.add(projectId, dto));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("getById() raises RESOURCE_NOT_FOUND when the entity belongs to another project")
    void getById_crossProject_raisesNotFound() {
        StubItemService stub = new StubItemService();
        stub.findByIdResponse = Optional.empty();
        ItemController controller = new ItemController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.getById(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    void getAll_returnsScopedPage() {
        StubItemService stub = new StubItemService();
        UUID projectId = UUID.randomUUID();
        stub.page = Page.of(List.of(sampleResponse(UUID.randomUUID(), projectId)), Pageable.from(0, 10), 1L);
        ItemController controller = new ItemController(stub);

        HttpResponse<Page<ItemModelResponseDTO.ItemModelDTO>> resp = controller.getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }
}
