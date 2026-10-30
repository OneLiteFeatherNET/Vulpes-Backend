package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.Default;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing items.
 */
public interface ItemService extends CrudService<ItemEntity, UUID, ItemModelDTO, ItemModelResponseDTO.ItemModelDTO> {

    /**
     * Gets the enchantments of an item by its ID.
     *
     * @param id       the ID of the item
     * @param pageable pagination information
     * @return a map of enchantment names to levels
     */
    Page<ItemEnchantmentResponseDTO.ItemEnchantmentDTO> findEnchantmentsById(@NotNull UUID id, @NotNull Pageable pageable);

    /**
     * Updates the enchantments of an item by its ID.
     *
     * @param id          the ID of the item to update the enchantments of
     * @param enchantment the enchantments to update
     * @return the updated enchantments
     */
    @Validated(groups = {Default.class, ValidationGroup.Update.class})
    ItemEnchantmentResponseDTO.ItemEnchantmentDTO updateEnchantmentById(@NotNull UUID id, @NotNull @Valid ItemEnchantmentDTO enchantment);

    /**
     * Creates the enchantments of an item by its ID.
     *
     * @param id          the ID of the item to update the enchantments of
     * @param enchantment the enchantments to create
     * @return the created enchantment
     */
    @Validated(groups = {Default.class, ValidationGroup.Create.class})
    ItemEnchantmentResponseDTO.ItemEnchantmentDTO createEnchantmentById(@NotNull UUID id, @NotNull @Valid ItemEnchantmentDTO enchantment);

    /**
     * Delete the enchantment of an item by its ID.
     *
     * @param id          the ID of the item to update the enchantments of
     * @param enchantment the enchantment to delete
     * @return the deleted enchantment
     */
    ItemEnchantmentResponseDTO.ItemEnchantmentDTO deleteEnchantmentById(@NotNull UUID id, @NotNull UUID enchantment);

    /**
     * Delete the enchantments of an item by its ID.
     *
     * @param id the ID of the item to update the enchantments of
     * @return the deleted enchantment
     */
    List<ItemEnchantmentResponseDTO.ItemEnchantmentDTO> deleteAllEnchantmentsById(@NotNull UUID id);

    /**
     * Gets the lore of an item by its ID.
     *
     * @param id       the ID of the item
     * @param pageable pagination information
     * @return a list of lore lines
     */
    Page<ItemLoreResponseDTO.ItemLoreDTO> findLoreById(@NotNull UUID id, @NotNull Pageable pageable);

    /**
     * Updates the lore of an item by its ID.
     *
     * @param id      the ID of the item to update the lore of
     * @param loreDto the lore to update
     * @return the updated lore
     */
    @Validated(groups = {Default.class, ValidationGroup.Update.class})
    ItemLoreResponseDTO.ItemLoreDTO updateLoreById(@NotNull UUID id, @NotNull @Valid ItemLoreDTO loreDto);

    /**
     * Creates the lore of an item by its ID.
     *
     * @param id      the ID of the item to update the lore of item
     * @param loreDto the lore to create
     * @return the created lore
     */
    @Validated(groups = {Default.class, ValidationGroup.Create.class})
    ItemLoreResponseDTO.ItemLoreDTO createLoreById(@NotNull UUID id, @NotNull @Valid ItemLoreDTO loreDto);

    /**
     * Delete the enchantment of an item by its ID.
     *
     * @param id     the ID of the item to update the enchantments of
     * @param loreId the enchantment to delete
     * @return the deleted enchantment
     */
    ItemLoreResponseDTO.ItemLoreDTO deleteLoreById(@NotNull UUID id, @NotNull UUID loreId);

    /**
     * Reorders a lore entry of an item by its ID.
     *
     * @param id       the ID of the item the lore entry belongs to
     * @param entryId  the ID of the lore entry to move
     * @param newIndex the new index of the entry within the lore list
     * @return the moved lore entry, or an error if the item or entry could not be found
     */
    ItemLoreResponseDTO.ItemLoreDTO reorderLoreById(@NotNull UUID id, @NotNull UUID entryId, @PositiveOrZero int newIndex);

    /**
     * Delete the lore of an item by its ID.
     *
     * @param id the ID of the item to update the lore of
     * @return the deleted lore
     */
    List<ItemLoreResponseDTO.ItemLoreDTO> deleteAllLoreById(@NotNull UUID id);
}