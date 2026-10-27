package net.onelitefeather.vulpes.backend.domain.asset;

/**
 * The namespace in which an asset key must be unique within a project. Textures form one key space,
 * Minecraft models and Blockbench models the other, so a texture and a model may share a key.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public enum AssetKeySpace {

    /** The key space of PNG textures. */
    TEXTURE,

    /** The key space of Minecraft models and Blockbench models. */
    MODEL;

    /**
     * Returns the key space an asset of the given kind lives in.
     *
     * @param kind the kind of the asset
     * @return the key space of the kind
     */
    public static AssetKeySpace of(AssetKind kind) {
        return switch (kind) {
            case TEXTURE -> TEXTURE;
            case MODEL, BBMODEL -> MODEL;
        };
    }
}
