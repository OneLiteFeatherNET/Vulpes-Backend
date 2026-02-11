package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemEnchantmentRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemFlagRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemLoreRepository;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.ItemService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the ItemService interface.
 */
@Singleton
public class ItemServiceImpl implements ItemService {

    private static final String GENERIC_ERROR = "Item not found";
    private final ItemRepository itemRepository;
    private final ItemEnchantmentRepository itemEnchantmentRepository;
    private final ItemLoreRepository itemLoreRepository;
    private final ItemFlagRepository itemFlagRepository;

    @Inject
    public ItemServiceImpl(ItemRepository itemRepository,
                           ItemEnchantmentRepository itemEnchantmentRepository,
                           ItemLoreRepository itemLoreRepository,
                           ItemFlagRepository itemFlagRepository) {
        this.itemRepository = itemRepository;
        this.itemEnchantmentRepository = itemEnchantmentRepository;
        this.itemLoreRepository = itemLoreRepository;
        this.itemFlagRepository = itemFlagRepository;
    }

    @Override
    public ItemModelResponseDTO.ItemModelDTO createItem(ItemModelDTO itemModelDTO) {
        ItemEntity itemModel = itemModelDTO.toItemEntity();
        ItemEntity savedItemModel = itemRepository.save(itemModel);
        return ItemModelResponseDTO.ItemModelDTO.createDTO(savedItemModel);
    }

    @Override
    public ItemModelResponseDTO updateItem(ItemModelDTO itemModelDTO) {
        Optional<ItemEntity> existingItem = itemRepository.findById(itemModelDTO.id());
        if (existingItem.isEmpty()) {
            return new ItemModelResponseDTO.ItemModelErrorDTO("Item not found");
        }
        ItemEntity itemModel = itemModelDTO.toItemEntity();
        ItemEntity updatedItemModel = itemRepository.update(itemModel);
        return ItemModelResponseDTO.ItemModelDTO.createDTO(updatedItemModel);
    }

    @Override
    public ItemModelResponseDTO deleteItem(UUID id) {
        Optional<ItemEntity> model = itemRepository.findById(id);
        if (model.isPresent()) {
            itemRepository.deleteById(id);
            return ItemModelResponseDTO.ItemModelDTO.createDTO(model.get());
        }
        return new ItemModelResponseDTO.ItemModelErrorDTO("Item not found");
    }

    @Override
    public List<ItemModelResponseDTO> deleteAllItems() {
        itemRepository.deleteAll();
        return List.of();
    }

    @Override
    public Page<ItemModelResponseDTO.ItemModelDTO> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable).map(ItemModelResponseDTO.ItemModelDTO::createDTO);
    }

    @Override
    public Optional<ItemEntity> findItemById(UUID id) {
        return itemRepository.findById(id);
    }

    @Override
    public Page<ItemEnchantmentResponseDTO> findEnchantmentsById(UUID id, Pageable pageable) {
        return this.itemEnchantmentRepository.findEnchantmentsById(id, pageable).map(ItemEnchantmentResponseDTO.ItemEnchantmentDTO::createDTO);
    }

    @Override
    public Page<ItemFlagResponseDTO> findFlagsById(UUID id, Pageable pageable) {
        return this.itemFlagRepository.findFlagsById(id, pageable).map(ItemFlagResponseDTO.ItemFlagDTO::createDTO);
    }

    @Override
    public ItemFlagResponseDTO createFlagById(UUID id, ItemFlagDTO itemFlagDTO) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return new ItemFlagResponseDTO.ItemFlagErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        var entity = itemFlagDTO.toEntity();
        entity.setItem(item);
        var saved = this.itemFlagRepository.save(entity);
        return ItemFlagResponseDTO.ItemFlagDTO.createDTO(saved);
    }

    @Override
    public ItemFlagResponseDTO deleteFlagById(UUID id, UUID flagId) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return new ItemFlagResponseDTO.ItemFlagErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        var entity = this.itemFlagRepository.findById(flagId);
        if (entity.isEmpty()) {
            return new ItemFlagResponseDTO.ItemFlagErrorDTO(GENERIC_ERROR);
        }
        var resolvedEntity = entity.get();
        if (!resolvedEntity.getItem().getId().equals(item.getId())) {
            return new ItemFlagResponseDTO.ItemFlagErrorDTO(GENERIC_ERROR);
        }
        this.itemFlagRepository.deleteById(resolvedEntity.getId());
        return ItemFlagResponseDTO.ItemFlagDTO.createDTO(resolvedEntity);
    }

    @Override
    public List<ItemFlagResponseDTO> deleteAllFlagsById(UUID id) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return List.of();
        }
        var item = byId.get();
        var flags = this.itemFlagRepository.findAll().stream().filter(e -> e.getItem().getId().equals(item.getId())).toList();
        this.itemFlagRepository.deleteAll(flags);
        return flags.stream()
                .map(ItemFlagResponseDTO.ItemFlagDTO::createDTO)
                .toList();
    }

    @Override
    public Page<ItemLoreResponseDTO> findLoreById(UUID id, Pageable pageable) {
        return this.itemLoreRepository.findLoreById(id, pageable).map(ItemLoreResponseDTO.ItemLoreDTO::createDTO);
    }

    @Override
    public ItemLoreResponseDTO updateLoreById(UUID id, ItemLoreDTO lore) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        var entity = lore.toEntity();
        entity.setItem(item);
        var saved = this.itemLoreRepository.update(entity);
        return ItemLoreResponseDTO.ItemLoreDTO.createDTO(saved);
    }

    @Override
    public ItemLoreResponseDTO createLoreById(UUID id, ItemLoreDTO loreDto) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        var entity = loreDto.toEntity();
        entity.setItem(item);

        // Set orderIndex to the current count (= next available index)
        long count = this.itemLoreRepository.countByItemId(item.getId());
        entity.setOrderIndex((int) (count));

        var saved = this.itemLoreRepository.save(entity);
        return ItemLoreResponseDTO.ItemLoreDTO.createDTO(saved);
    }

    @Override
    public ItemLoreResponseDTO deleteLoreById(UUID id, UUID loreId) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        var entity = this.itemLoreRepository.findById(loreId);
        if (entity.isEmpty()) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }
        var resolvedEntity = entity.get();
        if (!resolvedEntity.getItem().getId().equals(item.getId())) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }
        this.itemLoreRepository.deleteById(resolvedEntity.getId());
        return ItemLoreResponseDTO.ItemLoreDTO.createDTO(resolvedEntity);
    }

    @Override
    public ItemLoreResponseDTO reorderLoreById(UUID fontId, UUID entryId, int newIndex) {
        Optional<ItemEntity> byId = this.itemRepository.findById(fontId);

        if (byId.isEmpty()) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }

        ItemEntity item = byId.get();
        List<ItemLoreEntity> loreLines =
                this.itemLoreRepository.findLoreById(item.getId(), Pageable.unpaged()).getContent();

        // 1. Find and remove the entry to move
        ItemLoreEntity entryToMove = loreLines.stream()
                .filter(l -> l.getId().equals(entryId))
                .findFirst()
                .orElse(null);

        if (entryToMove == null) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }

        loreLines.remove(entryToMove);

        // 2. Clamp newIndex to valid bounds
        if (newIndex < 0) newIndex = 0;
        if (newIndex > loreLines.size()) newIndex = loreLines.size();

        // 3. Re-insert at the target position
        loreLines.add(newIndex, entryToMove);

        // 4. Reassign orderIndex on all entries
        for (int i = 0; i < loreLines.size(); i++) {
            loreLines.get(i).setOrderIndex(i);
        }

        // 5. Persist all updated entries
        this.itemLoreRepository.updateAll(loreLines);

        // 6. Return success (or 204 No Content via controller)
        return new ItemLoreResponseDTO.ItemLoreReorderDTO();
    }

    @Override
    public List<ItemLoreResponseDTO> deleteAllLoreById(UUID id) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return List.of();
        }
        var item = byId.get();
        var lores = this.itemLoreRepository.findAll().stream().filter(e -> e.getItem().getId().equals(item.getId())).toList();
        this.itemLoreRepository.deleteAll(lores);
        return lores.stream()
                .map(ItemLoreResponseDTO.ItemLoreDTO::createDTO)
                .toList();
    }

    @Override
    public ItemEnchantmentResponseDTO updateEnchantmentById(UUID id, ItemEnchantmentDTO enchantment) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return new ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        ItemEnchantmentEntity entity = enchantment.toEntity();
        entity.setItem(item);
        var saved = this.itemEnchantmentRepository.update(entity);
        return ItemEnchantmentResponseDTO.ItemEnchantmentDTO.createDTO(saved);
    }

    @Override
    public ItemEnchantmentResponseDTO createEnchantmentById(UUID id, ItemEnchantmentDTO enchantment) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return new ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        ItemEnchantmentEntity entity = enchantment.toEntity();
        entity.setItem(item);
        var saved = this.itemEnchantmentRepository.save(entity);
        return ItemEnchantmentResponseDTO.ItemEnchantmentDTO.createDTO(saved);
    }

    @Override
    public ItemEnchantmentResponseDTO deleteEnchantmentById(UUID id, UUID enchantment) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return new ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        var entity = this.itemEnchantmentRepository.findById(enchantment);
        if (entity.isEmpty()) {
            return new ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO(GENERIC_ERROR);
        }
        var resolvedEntity = entity.get();
        if (!resolvedEntity.getItem().getId().equals(item.getId())) {
            return new ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO(GENERIC_ERROR);
        }
        this.itemEnchantmentRepository.deleteById(resolvedEntity.getId());
        return ItemEnchantmentResponseDTO.ItemEnchantmentDTO.createDTO(resolvedEntity);
    }

    @Override
    public List<ItemEnchantmentResponseDTO> deleteAllEnchantmentsById(UUID id) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return List.of();
        }
        var item = byId.get();
        var enchantments = this.itemEnchantmentRepository.findAll().stream().filter(e -> e.getItem().getId().equals(item.getId())).toList();
        this.itemEnchantmentRepository.deleteAll(enchantments);
        return enchantments.stream()
                .map(ItemEnchantmentResponseDTO.ItemEnchantmentDTO::createDTO)
                .toList();
    }

    @Override
    public ItemFlagResponseDTO updateFlagById(UUID id, ItemFlagDTO flag) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return new ItemFlagResponseDTO.ItemFlagErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        var entity = flag.toEntity();
        entity.setItem(item);
        var saved = this.itemFlagRepository.update(entity);
        return ItemFlagResponseDTO.ItemFlagDTO.createDTO(saved);
    }
}