package net.onelitefeather.vulpes.backend.service.item;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import net.onelitefeather.vulpes.backend.domain.item.ItemComponentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemComponentResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Manages the data components of an item.
 * <p>
 * Everything about the item stack is stored here, the vanilla components as well as the ones Stelaris adds. The
 * {@link ItemComponentRules} decide which keys are rejected and which components every item has; those can't be
 * removed or renamed.
 * </p>
 */
public interface ItemComponentService {

    /**
     * Returns a page of the components of an item.
     *
     * @param itemId   the id of the item
     * @param pageable the page to return
     * @return the components on the page
     */
    Page<ItemComponentResponseDTO.ItemComponentDTO> findComponents(UUID itemId, Pageable pageable);

    /**
     * Adds a component to an item.
     *
     * @param itemId    the id of the item
     * @param component the component to add
     * @return the stored component
     */
    ItemComponentResponseDTO.ItemComponentDTO createComponent(UUID itemId, ItemComponentDTO component);

    /**
     * Updates a component of an item.
     *
     * @param itemId    the id of the item
     * @param component the component with its id
     * @return the stored component
     */
    ItemComponentResponseDTO.ItemComponentDTO updateComponent(UUID itemId, ItemComponentDTO component);

    /**
     * Removes a component from an item.
     *
     * @param itemId      the id of the item
     * @param componentId the id of the component entry
     * @return the removed component
     */
    ItemComponentResponseDTO.ItemComponentDTO deleteComponent(UUID itemId, UUID componentId);

    /**
     * Removes all components from an item.
     *
     * @param itemId the id of the item
     * @return the removed components
     */
    List<ItemComponentResponseDTO.ItemComponentDTO> deleteAllComponents(UUID itemId);
}
