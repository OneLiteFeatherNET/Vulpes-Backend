package net.onelitefeather.vulpes.backend.domain.copy;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;

import java.util.UUID;

@Schema(description = "Request DTO to copy an entity, optionally into another project under a new key")
@Introspected
@Serdeable
public record CopyDTO(
        @Schema(description = "Project to copy the entity into. Defaults to the source project when omitted.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable UUID targetProjectId,
        @Schema(description = "Key to give the copy. Defaults to the source entity's own key when omitted.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable String targetKey,
        @Schema(description = "Display name to give the copy. Defaults to the source entity's own name when omitted.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable String targetName
) implements CopyRequest {
}
