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
import java.util.UUID;

/**
 * Service interface for managing items.
 */
public interface ItemService extends CrudService<ItemEntity, UUID, ItemModelDTO, ItemModelResponseDTO.ItemModelDTO> {

    /**
     * Gets the flags of an item by its ID.
     *
     * @param id       the ID of the item
     * @param pageable pagination information
     * @return a list of flags
     */
    Page<ItemFlagResponseDTO.ItemFlagDTO> findFlagsById(UUID id, Pageable pageable);

    /**
     * Creates the flag of an item by its ID.
     *
     * @param id          the ID of the item to update the flag of
     * @param itemFlagDTO the flag to create
     * @return the created flag
     */
    ItemFlagResponseDTO.ItemFlagDTO createFlagById(UUID id, ItemFlagDTO itemFlagDTO);

    /**
     * Delete the flag of an item by its ID.
     *
     * @param id     the ID of the item to update the flag of
     * @param flagId the flag to delete
     * @return the deleted flag
     */
    ItemFlagResponseDTO.ItemFlagDTO deleteFlagById(UUID id, UUID flagId);

    /**
     * Delete the flags of an item by its ID.
     *
     * @param id the ID of the item to update the flags of
     * @return the deleted flags
     */
    List<ItemFlagResponseDTO.ItemFlagDTO> deleteAllFlagsById(UUID id);

    /**
     * Updates the flag of an item by its ID.
     *
     * @param id   the ID of the item to update the flag of
     * @param flag the new flag to set
     * @return the updated flag
     */
    ItemFlagResponseDTO.ItemFlagDTO updateFlagById(UUID id, ItemFlagDTO flag);

    /**
     * Gets the enchantments of an item by its ID.
     *
     * @param id       the ID of the item
     * @param pageable pagination information
     * @return a map of enchantment names to levels
     */
    Page<ItemEnchantmentResponseDTO.ItemEnchantmentDTO> findEnchantmentsById(UUID id, Pageable pageable);

    /**
     * Updates the enchantments of an item by its ID.
     *
     * @param id          the ID of the item to update the enchantments of
     * @param enchantment the enchantments to update
     * @return the updated enchantments
     */
    ItemEnchantmentResponseDTO.ItemEnchantmentDTO updateEnchantmentById(UUID id, ItemEnchantmentDTO enchantment);

    /**
     * Creates the enchantments of an item by its ID.
     *
     * @param id          the ID of the item to update the enchantments of
     * @param enchantment the enchantments to create
     * @return the created enchantment
     */
    ItemEnchantmentResponseDTO.ItemEnchantmentDTO createEnchantmentById(UUID id, ItemEnchantmentDTO enchantment);

    /**
     * Delete the enchantment of an item by its ID.
     *
     * @param id          the ID of the item to update the enchantments of
     * @param enchantment the enchantment to delete
     * @return the deleted enchantment
     */
    ItemEnchantmentResponseDTO.ItemEnchantmentDTO deleteEnchantmentById(UUID id, UUID enchantment);

    /**
     * Delete the enchantments of an item by its ID.
     *
     * @param id the ID of the item to update the enchantments of
     * @return the deleted enchantment
     */
    List<ItemEnchantmentResponseDTO.ItemEnchantmentDTO> deleteAllEnchantmentsById(UUID id);

    /**
     * Gets the lore of an item by its ID.
     *
     * @param id       the ID of the item
     * @param pageable pagination information
     * @return a list of lore lines
     */
    Page<ItemLoreResponseDTO.ItemLoreDTO> findLoreById(UUID id, Pageable pageable);

    /**
     * Updates the lore of an item by its ID.
     *
     * @param id      the ID of the item to update the lore of
     * @param loreDto the lore to update
     * @return the updated lore
     */
    ItemLoreResponseDTO.ItemLoreDTO updateLoreById(UUID id, ItemLoreDTO loreDto);

    /**
     * Creates the lore of an item by its ID.
     *
     * @param id      the ID of the item to update the lore of item
     * @param loreDto the lore to create
     * @return the created lore
     */
    ItemLoreResponseDTO.ItemLoreDTO createLoreById(UUID id, ItemLoreDTO loreDto);

    /**
     * Delete the enchantment of an item by its ID.
     *
     * @param id     the ID of the item to update the enchantments of
     * @param loreId the enchantment to delete
     * @return the deleted enchantment
     */
    ItemLoreResponseDTO.ItemLoreDTO deleteLoreById(UUID id, UUID loreId);

    /**
     * Reorders a lore entry of an item by its ID.
     *
     * @param id       the ID of the item the lore entry belongs to
     * @param entryId  the ID of the lore entry to move
     * @param newIndex the new index of the entry within the lore list
     * @return the moved lore entry, or an error if the item or entry could not be found
     */
    ItemLoreResponseDTO.ItemLoreDTO reorderLoreById(UUID id, UUID entryId, int newIndex);

    /**
     * Delete the lore of an item by its ID.
     *
     * @param id the ID of the item to update the lore of
     * @return the deleted lore
     */
    List<ItemLoreResponseDTO.ItemLoreDTO> deleteAllLoreById(UUID id);
}