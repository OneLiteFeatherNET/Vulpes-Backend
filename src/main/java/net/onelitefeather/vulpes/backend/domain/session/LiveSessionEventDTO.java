package net.onelitefeather.vulpes.backend.domain.session;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The data of one {@code session} event on the event stream of an asset.
 *
 * @param state {@code started} when a session was created, {@code ended} when it was removed or expired
 * @param token the join token of the session
 * @param host  the name of the host
 */
@Schema(name = "LiveSessionEventDTO", description = "A change of the live session of an asset.")
@Serdeable
public record LiveSessionEventDTO(
        @Schema(description = "Either started or ended", allowableValues = {"started", "ended"}) String state,
        @Schema(description = "Join token of the session") String token,
        @Schema(description = "Name of the host") String host
) {

    /**
     * Creates the event of a session that started.
     *
     * @param token the join token
     * @param host  the name of the host
     * @return the event
     */
    public static LiveSessionEventDTO started(String token, String host) {
        return new LiveSessionEventDTO("started", token, host);
    }

    /**
     * Creates the event of a session that ended.
     *
     * @param token the join token
     * @param host  the name of the host
     * @return the event
     */
    public static LiveSessionEventDTO ended(String token, String host) {
        return new LiveSessionEventDTO("ended", token, host);
    }
}
