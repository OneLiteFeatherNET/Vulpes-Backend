package net.onelitefeather.vulpes.backend.domain.item;

/**
 * The relations of an {@link net.onelitefeather.vulpes.api.model.ItemEntity} that can be
 * selectively copied alongside its own fields.
 */
public enum ItemRelation {
    LORE,
    ENCHANTMENTS,
    /**
     * All data components. The required ones, like the material, are copied in any case.
     */
    COMPONENTS
}
