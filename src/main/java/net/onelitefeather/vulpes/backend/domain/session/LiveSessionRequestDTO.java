package net.onelitefeather.vulpes.backend.domain.session;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The request body that starts or refreshes the live session of an asset.
 *
 * @param token the join token of the session, 16 characters of {@code a} to {@code z} and {@code 0} to {@code 9}
 * @param host  the name the host shows to the other participants, 1 to 64 characters
 */
@Schema(name = "RequestLiveSessionDTO", description = "Starts or refreshes the live session of an asset.")
@Serdeable
public record LiveSessionRequestDTO(
        @Schema(description = "Join token of the session", example = "k3j4h5g6f7d8s9a0")
        @NotNull
        @Pattern(regexp = "[a-z0-9]{16}")
        String token,

        @Schema(description = "Name the host shows to the other participants", example = "designer-a")
        @NotNull
        @Size(min = 1, max = 64)
        String host
) {
}
