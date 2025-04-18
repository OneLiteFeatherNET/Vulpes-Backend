package net.onelitefeather.vulpes.backend.domain.font;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.theevilreaper.vulpes.api.model.FontModel;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.List;
import java.util.UUID;

@Serdeable
public sealed interface FontModelResponseDTO {

    @Schema(description = "Font model data")
    @Serdeable
    record FontModelDTO(@Schema(description = "ID of the mode", requiredMode = Schema.RequiredMode.NOT_REQUIRED) UUID id,
                        @Schema(description = "Model Name for the ui", requiredMode = Schema.RequiredMode.REQUIRED) String modelName,
                        @Schema(description = "Name in the UI", requiredMode = Schema.RequiredMode.REQUIRED) String name,
                        @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) String description,
                        @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) String type,
                        @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) int ascent,
                        @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) int height,
                        @Schema(description = "Example description", requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<String> chars,
                        @Schema(description = "Example description", requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<Double> shift) implements FontModelResponseDTO {
        public static FontModelDTO createDTO(FontModel fontModel) {
            return new FontModelDTO(
                    fontModel.getId(),
                    fontModel.getModelName(),
                    fontModel.getName(),
                    fontModel.getDescription(),
                    fontModel.getType(),
                    fontModel.getAscent(),
                    fontModel.getHeight(),
                    fontModel.getChars(),
                    fontModel.getShift()
            );
        }
    }

    @Schema(description = "Error message")
    @Serdeable
    record FontModelErrorDTO(@Schema(description = "Error message") String errorMessage) implements FontModelResponseDTO, ErrorResponse {
    }
}
