package net.onelitefeather.vulpes.backend.domain.font;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Serdeable
public sealed interface FontModelResponseDTO {

    /**
     * Represents a response DTO for font models that includes characters.
     *
     * @param chars a list of characters in the font model
     * @param id    the ID of the font model
     */
    @Schema(name = "ResponseFontModelCharsDTO", description = "Font model data with characters")
    @Serdeable
    record FontModelCharsResponseDTO(
            @Schema(description = "ID of the font model", requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
            @Schema(description = "List of characters in the font model", requiredMode = Schema.RequiredMode.REQUIRED) List<String> chars
    ) implements FontModelResponseDTO {

        /**
         * Creates a new instance of FontModelCharsResponseDTO.
         *
         * @param chars a list of characters in the font model
         * @param id    the ID of the font model
         * @return a new FontModelCharsResponseDTO instance
         */
        public static @NotNull FontModelCharsResponseDTO createDTO(@NotNull UUID id, @NotNull List<String> chars) {
            return new FontModelCharsResponseDTO(id, chars);
        }
    }

    /**
     * Represents a response DTO for font models without characters.
     *
     * @param id           the ID of the font model
     * @param uiName       the name to display in the UI
     * @param variableName the name used for variable generation
     * @param provider     the provider of the font
     * @param mapper       the mapper for the font
     * @param texturePath  the path to the texture of the font
     * @param comment      an example comment for the font model
     * @param ascent       the ascent value of the font model
     * @param height       the height of the font model
     * @param chars        an optional list of characters in the font model
     */
    @Schema(name = "ResponseFontModelDTO", description = "Font model data")
    @Serdeable
    record FontModelDTO(
            @Schema(description = "The id of the model", requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
            @Schema(description = "Model Name for the UI", requiredMode = Schema.RequiredMode.REQUIRED) String uiName,
            @Schema(description = "Variable name for the generation", requiredMode = Schema.RequiredMode.REQUIRED) String variableName,
            @Schema(description = "Example comment", requiredMode = Schema.RequiredMode.REQUIRED) String provider,
            @Schema(description = "Example comment", requiredMode = Schema.RequiredMode.REQUIRED) String mapper,
            @Schema(description = "Example comment", requiredMode = Schema.RequiredMode.REQUIRED) String texturePath,
            @Schema(description = "Example comment", requiredMode = Schema.RequiredMode.REQUIRED) String comment,
            @Schema(description = "Example comment", requiredMode = Schema.RequiredMode.REQUIRED) int ascent,
            @Schema(description = "Example comment", requiredMode = Schema.RequiredMode.REQUIRED) int height,
            @Schema(description = "Example comment", requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<String> chars
    ) implements FontModelResponseDTO {

        /**
         * Converts a {@link FontEntity} to a {@link FontModelDTO}.
         *
         * @param fontModel the entity to convert
         * @return a new dto instance
         */
        public static FontModelDTO createDTO(FontEntity fontModel) {
            return new FontModelDTO(
                    fontModel.getId(),
                    fontModel.getUiName(),
                    fontModel.getVariableName(),
                    fontModel.getProvider(),
                    fontModel.getMapper(),
                    fontModel.getTexturePath(),
                    fontModel.getComment(),
                    fontModel.getAscent(),
                    fontModel.getHeight(),
                    Collections.emptyList()
            );
        }

        /**
         * Creates a {@link FontModelDTO} with characters from a {@link FontEntity}.
         *
         * @param fontModel the entity to convert
         * @return a new dto instance with characters
         */
        public static FontModelDTO createDTOWithChars(FontEntity fontModel) {
            return new FontModelDTO(
                    fontModel.getId(),
                    fontModel.getUiName(),
                    fontModel.getVariableName(),
                    fontModel.getProvider(),
                    fontModel.getMapper(),
                    fontModel.getTexturePath(),
                    fontModel.getComment(),
                    fontModel.getAscent(),
                    fontModel.getHeight(),
                    fontModel.getChars()
            );
        }
    }

    /**
     * Represents an error response for font models.
     *
     * @param errorMessage the error message describing the issue
     */
    @Schema(name = "ResponseFontModelErrorDTO", description = "Error message")
    @Serdeable
    record FontModelErrorDTO(
            @Schema(description = "Error message") String errorMessage) implements FontModelResponseDTO, ErrorResponse {
    }
}
