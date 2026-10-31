package net.onelitefeather.vulpes.backend.domain.asset;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The data of one {@code saved} event on the event stream of an asset. It names the content that a committed
 * write stored, so a client can send the new ETag as {@code If-Match} on its next save.
 *
 * @param etag     the quoted SHA-1 digest of the new content, as the ETag header carries it
 * @param sha1     the SHA-1 digest of the new content in lowercase hex
 * @param revision the number of the revision the write created
 */
@Schema(name = "AssetSavedEventDTO", description = "A committed save of a project asset.")
@Serdeable
public record AssetSavedEventDTO(
        @Schema(
                description = "Quoted SHA-1 digest of the new content",
                example = "\"2fd4e1c67a2d28fced849ee1bb76e7391b93eb12\"")
        String etag,

        @Schema(description = "SHA-1 digest of the new content, lowercase hex")
        String sha1,

        @Schema(description = "Number of the revision the save created", example = "2")
        int revision) {}
