package net.onelitefeather.vulpes.backend.domain.blockstate;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A blockstate as the backend answers it.
 *
 * @param id               the id of the blockstate
 * @param uiName           the name shown in the user interface
 * @param key              the full key of the block, e.g. {@code minecraft:pink_petals}
 * @param comment          a comment of the user
 * @param rules            the rules in the order they are written to the file
 * @param projectId        the id of the project the blockstate belongs to
 * @param creationDate     the point in time at which the blockstate was created
 * @param modificationDate the point in time at which the blockstate was last modified
 */
@Schema(name = "ResponseBlockStateDTO", description = "Blockstate data")
@JsonInclude(JsonInclude.Include.ALWAYS)
@Serdeable
public record BlockStateResponseDTO(
        @Schema(description = "UUID of the blockstate") UUID id,
        @Schema(description = "The name for the ui") String uiName,
        @Schema(description = "Full key of the block, e.g. minecraft:pink_petals") String key,
        @Schema(description = "Internal description of the blockstate") String comment,
        @Schema(description = "Rules in the order they are written to the file") List<BlockStateRuleDTO> rules,
        @Schema(description = "ID of the project this blockstate belongs to") UUID projectId,
        @Schema(description = "The point in time at which the blockstate was created") Instant creationDate,
        @Schema(description = "The point in time at which the blockstate was last modified") Instant modificationDate
) {
}
