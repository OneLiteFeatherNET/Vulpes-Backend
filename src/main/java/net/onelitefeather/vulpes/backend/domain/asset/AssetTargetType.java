package net.onelitefeather.vulpes.backend.domain.asset;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The kind of database model an asset can be linked to.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Schema(name = "AssetTargetType", description = "Kind of model an asset is linked to.")
@Serdeable
public enum AssetTargetType {
    ITEM,
    FONT,
    ADVANCEMENT
}
