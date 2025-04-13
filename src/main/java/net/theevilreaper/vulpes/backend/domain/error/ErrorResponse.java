package net.theevilreaper.vulpes.backend.domain.error;

import io.swagger.v3.oas.annotations.media.Schema;

public interface ErrorResponse {

    @Schema(description = "Error message")
    String errorMessage();

    @Schema(description = "Error message")
    record ErrorResponseDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements ErrorResponse {
    }
}
