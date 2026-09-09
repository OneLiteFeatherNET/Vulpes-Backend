package net.onelitefeather.vulpes.backend.domain.dimension;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTimelineEntity;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema()
@Introspected
@Serdeable
public record DimensionTimelineDTO(
        @Schema(description = "ID of the timeline reference", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Registry key of the referenced timeline", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String timelineKey
) {

    public DimensionTimelineEntity toEntity() {
        DimensionTimelineEntity entity = new DimensionTimelineEntity();
        entity.setId(this.id);
        entity.setTimelineKey(this.timelineKey);
        return entity;
    }
}
