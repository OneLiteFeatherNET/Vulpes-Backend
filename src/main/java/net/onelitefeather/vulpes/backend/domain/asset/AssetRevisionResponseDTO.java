package net.onelitefeather.vulpes.backend.domain.asset;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * The response body of one revision of a project asset.
 *
 * @param id           the unique identifier of the revision
 * @param number       the position of the revision in the history of the asset, starting at 1
 * @param sha1         the SHA-1 digest of the revision content in lowercase hex
 * @param size         the size of the revision content in bytes
 * @param comment      the comment the revision was written with, {@code null} if none
 * @param creationDate the point in time at which the revision was created
 * @param current      whether this is the current content of the asset
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Schema(name = "ResponseAssetRevisionDTO", description = "One revision of a project asset.")
@Serdeable
public record AssetRevisionResponseDTO(
        @Schema(description = "Id of the revision") UUID id,

        @Schema(description = "Position of the revision, starting at 1", example = "2")
        int number,

        @Schema(description = "SHA-1 digest of the revision content, lowercase hex")
        String sha1,

        @Schema(description = "Size of the revision content in bytes")
        long size,

        @Schema(description = "Comment of the revision") @Nullable
        String comment,

        @Schema(description = "The point in time at which the revision was created")
        Instant creationDate,

        @Schema(description = "Whether this is the current content of the asset")
        boolean current) {}
