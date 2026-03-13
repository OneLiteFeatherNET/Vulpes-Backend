package net.onelitefeather.vulpes.backend.domain.font;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PositiveOrZero;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema(
        requiredProperties = {
                "id",
                "line",
                "orderIndex"
        }
)
@Introspected
@Serdeable
public record FontStringDTO(
        @Schema(description = "ID of the font string", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = Update.class)
        UUID id,
        @NotNull(groups = {Create.class, Update.class})
        @Schema(description = "Content of the line", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String line,
        @Schema(description = "The internal order index", requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero int orderIndex
) {

    public FontStringEntity toEntity() {
        FontStringEntity entity = new FontStringEntity();
        entity.setId(this.id);
        entity.setLine(this.line);
        entity.setOrderIndex(this.orderIndex);
        return entity;
    }

}
