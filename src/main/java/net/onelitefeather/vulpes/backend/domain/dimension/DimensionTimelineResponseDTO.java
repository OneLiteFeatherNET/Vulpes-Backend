package net.onelitefeather.vulpes.backend.domain.dimension;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTimelineEntity;

import java.util.UUID;

@Schema(description = "Response DTO for Dimension Timeline Model")
@Serdeable
public interface DimensionTimelineResponseDTO {

    /**
     * Represents a response DTO for a dimension timeline reference.
     *
     * @param id          the unique identifier of the timeline reference
     * @param timelineKey the registry key of the referenced timeline
     */
    @Schema(
            name = "ResponseDimensionTimelineDTO",
            description = "Dimension Timeline DTO"
    )
    @Serdeable
    record DimensionTimelineDTO(
            @Schema(description = "Timeline reference ID") UUID id,
            @Schema(description = "Timeline registry key") String timelineKey
    ) implements DimensionTimelineResponseDTO {

        public static DimensionTimelineDTO createDTO(DimensionTimelineEntity entity) {
            return new DimensionTimelineDTO(entity.getId(), entity.getTimelineKey());
        }
    }
}
