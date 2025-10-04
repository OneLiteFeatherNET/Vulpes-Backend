package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.ItemService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the ItemService interface.
 */
@Singleton
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Inject
    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemModelResponseDTO.ItemModelDTO createItem(ItemModelDTO itemModelDTO) {
        ItemEntity itemModel = itemModelDTO.toItemEntity();
        ItemEntity savedItemModel = itemRepository.save(itemModel);
        return ItemModelResponseDTO.ItemModelDTO.createDTO(savedItemModel);
    }

    @Override
    public ItemModelResponseDTO updateItem(ItemModelDTO itemModelDTO) {
        Optional<ItemEntity> existingItem = itemRepository.findById(itemModelDTO.getId());
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
    public Map<String, Short> findEnchantmentsById(UUID id,Pageable pageable) {
        return itemRepository.findEnchantmentsById(id, pageable);
    }

    @Override
    public List<String> findFlagsById(UUID id, Pageable pageable) {
        return itemRepository.findFlagsById(id, pageable);
    }

    @Override
    public List<String> findLoreById(UUID id, Pageable pageable) {
        return itemRepository.findLoreById(id, pageable);
    }

    @Override
    public List<String> updateLoreById(UUID id, List<String> lore) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return List.of();
        }
        var item = byId.get();
        item.setLore(lore);
        var updated = this.itemRepository.update(item);
        return updated.getLore();
    }

    @Override
    public Map<String, Short> updateEnchantmentsById(UUID id, Map<String, Short> enchantments) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return Map.of();
        }
        var item = byId.get();
        item.setEnchantments(enchantments);
        var updated = this.itemRepository.update(item);
        return updated.getEnchantments();
    }

    @Override
    public List<String> updateFlagsById(UUID id, List<String> flags) {
        var byId = this.itemRepository.findById(id);
        if (byId.isEmpty()) {
            return List.of();
        }
        var item = byId.get();
        item.setFlags(flags);
        var updated = this.itemRepository.update(item);
        return updated.getFlags();
    }
}