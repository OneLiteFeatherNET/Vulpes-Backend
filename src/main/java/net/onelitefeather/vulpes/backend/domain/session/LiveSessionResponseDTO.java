package net.onelitefeather.vulpes.backend.domain.session;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * The live session of an asset as the API returns it.
 *
 * @param token     the join token of the session
 * @param host      the name of the host
 * @param startedAt the point in time at which the session started
 * @param expiresAt the point in time at which the session expires unless it is refreshed
 */
@Schema(name = "ResponseLiveSessionDTO", description = "The live edit session of an asset.")
@Serdeable
public record LiveSessionResponseDTO(
        @Schema(description = "Join token of the session") String token,
        @Schema(description = "Name of the host") String host,
        @Schema(description = "When the session started") Instant startedAt,
        @Schema(description = "When the session expires unless it is refreshed") Instant expiresAt
) {
}
