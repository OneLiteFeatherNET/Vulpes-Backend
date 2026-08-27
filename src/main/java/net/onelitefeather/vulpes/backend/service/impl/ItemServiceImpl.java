package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemFlagEntity;
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
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.ItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link ItemService} interface.
 *
 * <p>The sub-resources of an item &mdash; lore, flags, enchantments &mdash; all follow the same
 * shape: resolve the item, resolve the child, verify the child hangs off that item. A child owned by
 * a different item is answered as "not found" so the endpoint cannot be used to enumerate ids across
 * items; the log records what actually happened.
 */
@Singleton
public class ItemServiceImpl
        extends AbstractCrudService<ItemEntity, UUID, ItemModelDTO, ItemModelResponseDTO.ItemModelDTO>
        implements ItemService {

    private static final String ITEM = "Item";
    private static final String LORE_ENTRY = "Lore entry";
    private static final String FLAG = "Flag";
    private static final String ENCHANTMENT = "Enchantment";

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
                ITEM
        );
        this.itemEnchantmentRepository = itemEnchantmentRepository;
        this.itemLoreRepository = itemLoreRepository;
        this.itemFlagRepository = itemFlagRepository;
    }

    /**
     * Loads the item a sub-resource request addressed.
     *
     * @param id the item identifier
     * @return the item
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if no item has that identifier
     */
    private ItemEntity requireItem(UUID id) {
        return this.repository.findById(id).orElseThrow(() -> ApiException.notFound(ITEM));
    }

    @Override
    public Page<ItemEnchantmentResponseDTO.ItemEnchantmentDTO> findEnchantmentsById(UUID id, Pageable pageable) {
        return this.itemEnchantmentRepository.findEnchantmentsById(id, pageable)
                .map(ItemEnchantmentResponseDTO.ItemEnchantmentDTO::createDTO);
    }

    @Override
    public Page<ItemFlagResponseDTO.ItemFlagDTO> findFlagsById(UUID id, Pageable pageable) {
        return this.itemFlagRepository.findFlagsById(id, pageable).map(ItemFlagResponseDTO.ItemFlagDTO::createDTO);
    }

    @Override
    public ItemFlagResponseDTO.ItemFlagDTO createFlagById(UUID id, ItemFlagDTO itemFlagDTO) {
        var item = requireItem(id);
        var entity = itemFlagDTO.toEntity();
        entity.setItem(item);
        var saved = this.itemFlagRepository.save(entity);
        return ItemFlagResponseDTO.ItemFlagDTO.createDTO(saved);
    }

    @Override
    public ItemFlagResponseDTO.ItemFlagDTO deleteFlagById(UUID id, UUID flagId) {
        var item = requireItem(id);
        var flag = this.itemFlagRepository.findById(flagId).orElseThrow(() -> ApiException.notFound(FLAG));
        if (!flag.getItem().getId().equals(item.getId())) {
            throw ApiException.notOwnedBy(FLAG, flagId, "item", id);
        }
        this.itemFlagRepository.deleteById(flag.getId());
        return ItemFlagResponseDTO.ItemFlagDTO.createDTO(flag);
    }

    @Override
    public List<ItemFlagResponseDTO.ItemFlagDTO> deleteAllFlagsById(UUID id) {
        var item = requireItem(id);
        List<ItemFlagEntity> flags = this.itemFlagRepository.findFlagsById(item.getId(), Pageable.unpaged()).getContent();
        this.itemFlagRepository.deleteAll(flags);
        return flags.stream()
                .map(ItemFlagResponseDTO.ItemFlagDTO::createDTO)
                .toList();
    }

    @Override
    public ItemFlagResponseDTO.ItemFlagDTO updateFlagById(UUID id, ItemFlagDTO flag) {
        var item = requireItem(id);
        var entity = flag.toEntity();
        entity.setItem(item);
        var saved = this.itemFlagRepository.update(entity);
        return ItemFlagResponseDTO.ItemFlagDTO.createDTO(saved);
    }

    @Override
    public Page<ItemLoreResponseDTO.ItemLoreDTO> findLoreById(UUID id, Pageable pageable) {
        return this.itemLoreRepository.findLoreById(id, pageable).map(ItemLoreResponseDTO.ItemLoreDTO::createDTO);
    }

    @Override
    @Transactional
    public ItemLoreResponseDTO.ItemLoreDTO updateLoreById(UUID id, ItemLoreDTO lore) {
        var item = requireItem(id);
        if (lore.id() == null) {
            throw ApiException.invalidRequest("An id is required to update a lore entry.");
        }
        var entity = this.itemLoreRepository.findById(lore.id())
                .orElseThrow(() -> ApiException.notFound(LORE_ENTRY));
        if (!entity.getItem().getId().equals(item.getId())) {
            throw ApiException.notOwnedBy(LORE_ENTRY, lore.id(), "item", id);
        }
        entity.setText(lore.text());
        var saved = this.itemLoreRepository.update(entity);
        return ItemLoreResponseDTO.ItemLoreDTO.createDTO(saved);
    }

    @Override
    @Transactional
    public ItemLoreResponseDTO.ItemLoreDTO createLoreById(UUID id, ItemLoreDTO loreDto) {
        var item = requireItem(id);
        var entity = loreDto.toEntity();
        entity.setItem(item);
        int nextIndex = (int) this.itemLoreRepository.findLoreById(item.getId(), Pageable.unpaged()).getTotalSize();
        entity.setOrderIndex(nextIndex);
        var saved = this.itemLoreRepository.save(entity);
        return ItemLoreResponseDTO.ItemLoreDTO.createDTO(saved);
    }

    @Override
    public ItemLoreResponseDTO.ItemLoreDTO deleteLoreById(UUID id, UUID loreId) {
        var item = requireItem(id);
        var entity = this.itemLoreRepository.findById(loreId).orElseThrow(() -> ApiException.notFound(LORE_ENTRY));
        if (!entity.getItem().getId().equals(item.getId())) {
            throw ApiException.notOwnedBy(LORE_ENTRY, loreId, "item", id);
        }
        this.itemLoreRepository.deleteById(entity.getId());
        return ItemLoreResponseDTO.ItemLoreDTO.createDTO(entity);
    }

    @Override
    @Transactional
    public ItemLoreResponseDTO.ItemLoreDTO reorderLoreById(UUID id, UUID entryId, int newIndex) {
        var item = requireItem(id);
        List<ItemLoreEntity> loreLines = new ArrayList<>(
                this.itemLoreRepository.findLoreById(item.getId(), Pageable.unpaged()).getContent());

        var entryToMove = loreLines.stream()
                .filter(entry -> entry.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound(LORE_ENTRY));

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
    public List<ItemLoreResponseDTO.ItemLoreDTO> deleteAllLoreById(UUID id) {
        var item = requireItem(id);
        List<ItemLoreEntity> lores = this.itemLoreRepository.findLoreById(item.getId(), Pageable.unpaged()).getContent();
        this.itemLoreRepository.deleteAll(lores);
        return lores.stream()
                .map(ItemLoreResponseDTO.ItemLoreDTO::createDTO)
                .toList();
    }

    @Override
    public ItemEnchantmentResponseDTO.ItemEnchantmentDTO updateEnchantmentById(UUID id, ItemEnchantmentDTO enchantment) {
        var item = requireItem(id);
        ItemEnchantmentEntity entity = enchantment.toEntity();
        entity.setItem(item);
        var saved = this.itemEnchantmentRepository.update(entity);
        return ItemEnchantmentResponseDTO.ItemEnchantmentDTO.createDTO(saved);
    }

    @Override
    public ItemEnchantmentResponseDTO.ItemEnchantmentDTO createEnchantmentById(UUID id, ItemEnchantmentDTO enchantment) {
        var item = requireItem(id);
        ItemEnchantmentEntity entity = enchantment.toEntity();
        entity.setItem(item);
        var saved = this.itemEnchantmentRepository.save(entity);
        return ItemEnchantmentResponseDTO.ItemEnchantmentDTO.createDTO(saved);
    }

    @Override
    public ItemEnchantmentResponseDTO.ItemEnchantmentDTO deleteEnchantmentById(UUID id, UUID enchantment) {
        var item = requireItem(id);
        var entity = this.itemEnchantmentRepository.findById(enchantment)
                .orElseThrow(() -> ApiException.notFound(ENCHANTMENT));
        if (!entity.getItem().getId().equals(item.getId())) {
            throw ApiException.notOwnedBy(ENCHANTMENT, enchantment, "item", id);
        }
        this.itemEnchantmentRepository.deleteById(entity.getId());
        return ItemEnchantmentResponseDTO.ItemEnchantmentDTO.createDTO(entity);
    }

    @Override
    public List<ItemEnchantmentResponseDTO.ItemEnchantmentDTO> deleteAllEnchantmentsById(UUID id) {
        var item = requireItem(id);
        List<ItemEnchantmentEntity> enchantments =
                this.itemEnchantmentRepository.findEnchantmentsById(item.getId(), Pageable.unpaged()).getContent();
        this.itemEnchantmentRepository.deleteAll(enchantments);
        return enchantments.stream()
                .map(ItemEnchantmentResponseDTO.ItemEnchantmentDTO::createDTO)
                .toList();
    }
}
