package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
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
        ItemModelResponseDTO response;
        Page<ItemModelResponseDTO.ItemModelDTO> page;
        Optional<ItemEntity> findByIdResponse = Optional.empty();

        @Override
        public ItemModelResponseDTO.ItemModelDTO create(ItemModelDTO dto) {
            return (ItemModelResponseDTO.ItemModelDTO) response;
        }

        @Override
        public ItemModelResponseDTO update(ItemModelDTO dto) {
            return response;
        }

        @Override
        public ItemModelResponseDTO delete(UUID id) {
            return response;
        }

        @Override
        public List<ItemModelResponseDTO> deleteAll() {
            return List.of();
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
        public ItemModelResponseDTO create(UUID projectId, ItemModelDTO dto) {
            return response;
        }

        @Override
        public ItemModelResponseDTO update(UUID projectId, ItemModelDTO dto) {
            return response;
        }

        @Override
        public ItemModelResponseDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public List<ItemModelResponseDTO> deleteAll(UUID projectId) {
            return List.of();
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
        public Page<ItemFlagResponseDTO> findFlagsById(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public ItemFlagResponseDTO createFlagById(UUID id, ItemFlagDTO itemFlagDTO) {
            return null;
        }

        @Override
        public ItemFlagResponseDTO deleteFlagById(UUID id, UUID flagId) {
            return null;
        }

        @Override
        public List<ItemFlagResponseDTO> deleteAllFlagsById(UUID id) {
            return List.of();
        }

        @Override
        public ItemFlagResponseDTO updateFlagById(UUID id, ItemFlagDTO flag) {
            return null;
        }

        @Override
        public Page<ItemEnchantmentResponseDTO> findEnchantmentsById(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public ItemEnchantmentResponseDTO updateEnchantmentById(UUID id, ItemEnchantmentDTO enchantment) {
            return null;
        }

        @Override
        public ItemEnchantmentResponseDTO createEnchantmentById(UUID id, ItemEnchantmentDTO enchantment) {
            return null;
        }

        @Override
        public ItemEnchantmentResponseDTO deleteEnchantmentById(UUID id, UUID enchantment) {
            return null;
        }

        @Override
        public List<ItemEnchantmentResponseDTO> deleteAllEnchantmentsById(UUID id) {
            return List.of();
        }

        @Override
        public Page<ItemLoreResponseDTO> findLoreById(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public ItemLoreResponseDTO updateLoreById(UUID id, ItemLoreDTO loreDto) {
            return null;
        }

        @Override
        public ItemLoreResponseDTO createLoreById(UUID id, ItemLoreDTO loreDto) {
            return null;
        }

        @Override
        public ItemLoreResponseDTO deleteLoreById(UUID id, UUID loreId) {
            return null;
        }

        @Override
        public ItemLoreResponseDTO reorderLoreById(UUID id, UUID entryId, int newIndex) {
            return null;
        }

        @Override
        public List<ItemLoreResponseDTO> deleteAllLoreById(UUID id) {
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

        HttpResponse<ItemModelResponseDTO> resp = controller.add(projectId, sampleDTO(null));

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(ItemModelResponseDTO.ItemModelDTO.class, resp.body());
    }

    @Test
    void add_unknownProject_returns404() {
        StubItemService stub = new StubItemService();
        stub.response = new ItemModelResponseDTO.ItemModelErrorDTO("Project not found");
        ItemController controller = new ItemController(stub);

        HttpResponse<ItemModelResponseDTO> resp = controller.add(UUID.randomUUID(), sampleDTO(null));

        assertEquals(404, resp.getStatus().getCode());
    }

    @Test
    void getById_crossProject_returns404() {
        StubItemService stub = new StubItemService();
        stub.findByIdResponse = Optional.empty();
        ItemController controller = new ItemController(stub);

        HttpResponse<ItemModelResponseDTO> resp = controller.getById(UUID.randomUUID(), UUID.randomUUID());

        assertEquals(404, resp.getStatus().getCode());
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
