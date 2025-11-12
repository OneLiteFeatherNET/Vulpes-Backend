package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing items.
 */
public interface ItemService {

    /**
     * Creates a new item.
     *
     * @param itemModelDTO the item data to create
     * @return the created item response
     */
    ItemModelResponseDTO.ItemModelDTO createItem(ItemModelDTO itemModelDTO);

    /**
     * Updates an existing item.
     *
     * @param itemModelDTO the item data to update
     * @return the updated item response or an error response if the item doesn't exist
     */
    ItemModelResponseDTO updateItem(ItemModelDTO itemModelDTO);

    /**
     * Deletes an item by its ID.
     *
     * @param id the ID of the item to delete
     * @return the deleted item response or an error response if the item doesn't exist
     */
    ItemModelResponseDTO deleteItem(UUID id);

    /**
     * Deletes all items.
     *
     * @return an empty list
     */
    List<ItemModelResponseDTO> deleteAllItems();

    /**
     * Gets all items with pagination.
     *
     * @param pageable pagination information
     * @return a page of items
     */
    Page<ItemModelResponseDTO.ItemModelDTO> getAllItems(Pageable pageable);

    /**
     * Finds an item by its ID.
     *
     * @param id the ID of the item to find
     * @return an optional containing the item if found, or empty if not found
     */
    Optional<ItemEntity> findItemById(UUID id);

    /**
     * Gets the enchantments of an item by its ID.
     *
     * @param id the ID of the item
     * @param pageable pagination information
     * @return a map of enchantment names to levels
     */
    Page<ItemEnchantmentResponseDTO> findEnchantmentsById(UUID id, Pageable pageable);

    /**
     * Gets the flags of an item by its ID.
     *
     * @param id the ID of the item
     * @param pageable pagination information
     * @return a list of flags
     */
    List<String> findFlagsById(UUID id, Pageable pageable);

    /**
     * Gets the lore of an item by its ID.
     *
     * @param id the ID of the item
     * @param pageable pagination information
     * @return a list of lore lines
     */
    List<String> findLoreById(UUID id, Pageable pageable);

    /**
     * Updates the flags of an item by its ID.
     * @param id the ID of the item to update the flags of
     * @param flags the new flags to set
     * @return the updated flags
     */
    List<String> updateFlagsById(UUID id, List<String> flags);

    /**
     * Updates the enchantments of an item by its ID.
     * @param id the ID of the item to update the enchantments of
     * @param enchantment the enchantments to update
     * @return the updated enchantments
     */
    ItemEnchantmentResponseDTO updateEnchantmentById(UUID id, ItemEnchantmentDTO enchantment);

    /**
     * Updates the lore of an item by its ID.
     * @param id the ID of the item to update the lore of
     * @param lore the new lore to set
     * @return the updated lore
     */
    List<String> updateLoreById(UUID id, List<String> lore);
}