package net.onelitefeather.vulpes.backend.entity.item;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemComponentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;

import java.util.List;
import java.util.UUID;

/**
 * Reads every item of a project and the children of those items, for the complete project export.
 *
 * <p>Each query returns all rows of one project without paging. Load the items before their children,
 * so that the item references of the children resolve to the already loaded items.
 */
@Repository
public interface ItemContentRepository extends JpaRepository<ItemEntity, UUID> {

    /**
     * Returns all items of the project.
     *
     * @param projectId the project of the items
     * @return the items
     */
    @Query("SELECT i FROM items i WHERE i.project.id = :projectId")
    List<ItemEntity> findItemsByProjectId(UUID projectId);

    /**
     * Returns the components of all items of the project.
     *
     * @param projectId the project of the items
     * @return the components, with their item
     */
    @Query("SELECT c FROM item_components c WHERE c.item.project.id = :projectId")
    List<ItemComponentEntity> findComponentsByProjectId(UUID projectId);

    /**
     * Returns the lore lines of all items of the project.
     *
     * @param projectId the project of the items
     * @return the lore lines, with their item
     */
    @Query("SELECT l FROM item_lore l WHERE l.item.project.id = :projectId")
    List<ItemLoreEntity> findLoreByProjectId(UUID projectId);

    /**
     * Returns the enchantments of all items of the project.
     *
     * @param projectId the project of the items
     * @return the enchantments, with their item
     */
    @Query("SELECT e FROM item_enchantments e WHERE e.item.project.id = :projectId")
    List<ItemEnchantmentEntity> findEnchantmentsByProjectId(UUID projectId);
}
