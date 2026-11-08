package net.onelitefeather.vulpes.backend.entity.item;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.item.ItemComponentEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Reads the components of all items of a project which decide the model of an item.
 */
@Repository
public interface ItemModelComponentRepository extends JpaRepository<ItemComponentEntity, UUID> {

    /**
     * Returns the components with one of the keys of all items in the project.
     *
     * @param projectId the project of the items
     * @param keys      the component keys to read
     * @return the components, with their item
     */
    @Query("SELECT c FROM item_components c WHERE c.item.project.id = :projectId AND c.componentKey IN (:keys)")
    List<ItemComponentEntity> findByProjectIdAndComponentKeys(UUID projectId, Collection<String> keys);
}
