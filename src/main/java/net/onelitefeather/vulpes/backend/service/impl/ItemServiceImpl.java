package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link ItemService} interface.
 */
@Singleton
public class ItemServiceImpl
        extends AbstractCrudService<ItemEntity, UUID, ItemModelDTO, ItemModelResponseDTO, ItemModelResponseDTO.ItemModelDTO>
        implements ItemService {

    private static final String GENERIC_ERROR = "Item not found";
    private final ItemEnchantmentRepository itemEnchantmentRepository;
    private final ItemLoreRepository itemLoreRepository;
    private final ItemFlagRepository itemFlagRepository;

    @Inject
    public ItemServiceImpl(ItemRepository itemRepository,
                           ItemEnchantmentRepository itemEnchantmentRepository,
                           ItemLoreRepository itemLoreRepository,
                           ItemFlagRepository itemFlagRepository,
                           ProjectRepository projectRepository) {
        super(
                itemRepository,
                projectRepository,
                ItemModelDTO::toItemEntity,
                ItemModelResponseDTO.ItemModelDTO::createDTO,
                entity -> entity.getProject().getId(),
                itemRepository::findByProjectId,
                ItemModelDTO::id,
                ItemModelResponseDTO.ItemModelErrorDTO::new,
                "Item"
        );
        this.itemEnchantmentRepository = itemEnchantmentRepository;
        this.itemLoreRepository = itemLoreRepository;
        this.itemFlagRepository = itemFlagRepository;
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
        var byId = this.repository.findById(id);
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
        var byId = this.repository.findById(id);
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
        var byId = this.repository.findById(id);
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
    @Transactional
    public ItemLoreResponseDTO updateLoreById(UUID id, ItemLoreDTO lore) {
        var byId = this.repository.findById(id);
        if (byId.isEmpty()) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        var existingLore = this.itemLoreRepository.findById(lore.id());
        if (existingLore.isEmpty() || !existingLore.get().getItem().getId().equals(item.getId())) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }
        var entity = existingLore.get();
        entity.setText(lore.text());
        var saved = this.itemLoreRepository.update(entity);
        return ItemLoreResponseDTO.ItemLoreDTO.createDTO(saved);
    }

    @Override
    @Transactional
    public ItemLoreResponseDTO createLoreById(UUID id, ItemLoreDTO loreDto) {
        var byId = this.repository.findById(id);
        if (byId.isEmpty()) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        var entity = loreDto.toEntity();
        entity.setItem(item);
        int nextIndex = (int) this.itemLoreRepository.findLoreById(item.getId(), Pageable.unpaged()).getTotalSize();
        entity.setOrderIndex(nextIndex);
        var saved = this.itemLoreRepository.save(entity);
        return ItemLoreResponseDTO.ItemLoreDTO.createDTO(saved);
    }

    @Override
    public ItemLoreResponseDTO deleteLoreById(UUID id, UUID loreId) {
        var byId = this.repository.findById(id);
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
    @Transactional
    public ItemLoreResponseDTO reorderLoreById(UUID id, UUID entryId, int newIndex) {
        var byId = this.repository.findById(id);
        if (byId.isEmpty()) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }
        var item = byId.get();
        List<ItemLoreEntity> loreLines = new ArrayList<>(
                this.itemLoreRepository.findLoreById(item.getId(), Pageable.unpaged()).getContent());

        var entryToMove = loreLines.stream()
                .filter(entry -> entry.getId().equals(entryId))
                .findFirst()
                .orElse(null);

        if (entryToMove == null) {
            return new ItemLoreResponseDTO.ItemLoreErrorDTO(GENERIC_ERROR);
        }

        loreLines.remove(entryToMove);
        int clampedIndex = Math.max(0, Math.min(newIndex, loreLines.size()));
        loreLines.add(clampedIndex, entryToMove);

        for (int i = 0; i < loreLines.size(); i++) {
            loreLines.get(i).setOrderIndex(i);
        }
        this.itemLoreRepository.updateAll(loreLines);

        return ItemLoreResponseDTO.ItemLoreDTO.createDTO(entryToMove);
    }

    @Override
    public List<ItemLoreResponseDTO> deleteAllLoreById(UUID id) {
        var byId = this.repository.findById(id);
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
        var byId = this.repository.findById(id);
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
        var byId = this.repository.findById(id);
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
        var byId = this.repository.findById(id);
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
        var byId = this.repository.findById(id);
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
        var byId = this.repository.findById(id);
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