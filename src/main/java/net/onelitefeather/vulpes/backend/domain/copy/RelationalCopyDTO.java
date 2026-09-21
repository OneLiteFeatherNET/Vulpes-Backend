package net.onelitefeather.vulpes.backend.domain.copy;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Request DTO to copy an entity, optionally into another project under a new key, with selected relations")
@Introspected
@Serdeable
public record RelationalCopyDTO<R>(
        @Schema(description = "Project to copy the entity into. Defaults to the source project when omitted.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable UUID targetProjectId,
        @Schema(description = "Key to give the copy. Defaults to the source entity's own key when omitted.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable String targetKey,
        @Schema(description = "Which relations to copy alongside the entity. Omitted or empty copies only the entity's own fields.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable Set<R> relations
) implements CopyRequest {

    /**
     * @return {@link #relations()}, or an empty set when the client sent none
     */
    public Set<R> relationsOrEmpty() {
        return relations != null ? relations : Set.of();
    }
}
