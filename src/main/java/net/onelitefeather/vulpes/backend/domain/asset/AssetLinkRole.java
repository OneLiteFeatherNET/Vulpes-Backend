package net.onelitefeather.vulpes.backend.domain.asset;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * How a linked asset is used by its target. A {@link #TEXTURE} link requires a texture asset, a
 * {@link #MODEL} link requires a model or a Blockbench model.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Schema(name = "AssetLinkRole", description = "How a linked asset is used by its target.")
@Serdeable
public enum AssetLinkRole {
    TEXTURE,
    MODEL
}
