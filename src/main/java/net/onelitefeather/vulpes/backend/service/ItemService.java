package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreResponseDTO;
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
     * Gets the flags of an item by its ID.
     *
     * @param id       the ID of the item
     * @param pageable pagination information
     * @return a list of flags
     */
    Page<ItemFlagResponseDTO> findFlagsById(UUID id, Pageable pageable);

    /**
     * Creates the flag of an item by its ID.
     *
     * @param id          the ID of the item to update the flag of
     * @param itemFlagDTO the flag to create
     * @return the created flag
     */
    ItemFlagResponseDTO createFlagById(UUID id, ItemFlagDTO itemFlagDTO);

    /**
     * Delete the flag of an item by its ID.
     *
     * @param id     the ID of the item to update the flag of
     * @param flagId the flag to delete
     * @return the deleted flag
     */
    ItemFlagResponseDTO deleteFlagById(UUID id, UUID flagId);

    /**
     * Delete the flags of an item by its ID.
     *
     * @param id the ID of the item to update the flags of
     * @return the deleted flags
     */
    List<ItemFlagResponseDTO> deleteAllFlagsById(UUID id);

    /**
     * Updates the flag of an item by its ID.
     *
     * @param id   the ID of the item to update the flag of
     * @param flag the new flag to set
     * @return the updated flag
     */
    ItemFlagResponseDTO updateFlagById(UUID id, ItemFlagDTO flag);

    /**
     * Gets the enchantments of an item by its ID.
     *
     * @param id       the ID of the item
     * @param pageable pagination information
     * @return a map of enchantment names to levels
     */
    Page<ItemEnchantmentResponseDTO> findEnchantmentsById(UUID id, Pageable pageable);

    /**
     * Updates the enchantments of an item by its ID.
     *
     * @param id          the ID of the item to update the enchantments of
     * @param enchantment the enchantments to update
     * @return the updated enchantments
     */
    ItemEnchantmentResponseDTO updateEnchantmentById(UUID id, ItemEnchantmentDTO enchantment);

    /**
     * Creates the enchantments of an item by its ID.
     *
     * @param id          the ID of the item to update the enchantments of
     * @param enchantment the enchantments to create
     * @return the created enchantment
     */
    ItemEnchantmentResponseDTO createEnchantmentById(UUID id, ItemEnchantmentDTO enchantment);

    /**
     * Delete the enchantment of an item by its ID.
     *
     * @param id          the ID of the item to update the enchantments of
     * @param enchantment the enchantment to delete
     * @return the deleted enchantment
     */
    ItemEnchantmentResponseDTO deleteEnchantmentById(UUID id, UUID enchantment);

    /**
     * Delete the enchantments of an item by its ID.
     *
     * @param id the ID of the item to update the enchantments of
     * @return the deleted enchantment
     */
    List<ItemEnchantmentResponseDTO> deleteAllEnchantmentsById(UUID id);

    /**
     * Gets the lore of an item by its ID.
     *
     * @param id       the ID of the item
     * @param pageable pagination information
     * @return a list of lore lines
     */
    Page<ItemLoreResponseDTO> findLoreById(UUID id, Pageable pageable);

    /**
     * Updates the lore of an item by its ID.
     *
     * @param id      the ID of the item to update the lore of
     * @param loreDto the lore to update
     * @return the updated lore
     */
    ItemLoreResponseDTO updateLoreById(UUID id, ItemLoreDTO loreDto);

    /**
     * Creates the lore of an item by its ID.
     *
     * @param id      the ID of the item to update the lore of item
     * @param loreDto the lore to create
     * @return the created lore
     */
    ItemLoreResponseDTO createLoreById(UUID id, ItemLoreDTO loreDto);

    /**
     * Delete the enchantment of an item by its ID.
     *
     * @param id     the ID of the item to update the enchantments of
     * @param loreId the enchantment to delete
     * @return the deleted enchantment
     */
    ItemLoreResponseDTO deleteLoreById(UUID id, UUID loreId);

    /**
     * Reorder the lore of an item by its ID.
     *
     * @param fontId   the ID of the font to reorder the entry of
     * @param entryId  the ID of the entry to reorder
     * @param newIndex the new index of the entry
     * @return the reordered entry
     */
    ItemLoreResponseDTO reorderLoreById(UUID fontId, UUID entryId, int newIndex);

    /**
     * Delete the lore of an item by its ID.
     *
     * @param id the ID of the item to update the lore of
     * @return the deleted lore
     */
    List<ItemLoreResponseDTO> deleteAllLoreById(UUID id);
}