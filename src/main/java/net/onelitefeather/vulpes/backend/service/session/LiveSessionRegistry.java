package net.onelitefeather.vulpes.backend.service.session;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The live sessions of assets, held in memory.
 *
 * <p>At most one session exists per asset. A session expires {@value #TTL_SECONDS} seconds after its last
 * claim; the clients refresh it every 20 seconds. The registry is correct for one backend instance only: with
 * several replicas each one would hold its own sessions, so a shared store is needed first.
 *
 * <p>Every decision compares against the instant of the injected {@link Clock}, so expiry can be tested
 * without waiting. Each operation runs atomically per asset through {@link ConcurrentMap#compute}.
 */
@Singleton
public class LiveSessionRegistry {

    /** Seconds a session stays active after its last claim. */
    static final long TTL_SECONDS = 60;

    private static final Duration TTL = Duration.ofSeconds(TTL_SECONDS);

    private final Clock clock;
    private final ConcurrentMap<UUID, LiveSessionResponseDTO> sessions = new ConcurrentHashMap<>();

    /**
     * Creates the registry.
     *
     * @param clock the source of the current instant
     */
    @Inject
    public LiveSessionRegistry(Clock clock) {
        this.clock = clock;
    }

    /**
     * Returns the active session of an asset, if there is one.
     *
     * @param assetId the asset
     * @return the session, or empty when none is active or the session has expired
     */
    public Optional<LiveSessionResponseDTO> active(UUID assetId) {
        Instant now = clock.instant();
        return Optional.ofNullable(sessions.get(assetId)).filter(session -> isActive(session, now));
    }

    /**
     * Creates the session of an asset, or refreshes it when the token is the one of the active session.
     *
     * @param assetId the asset
     * @param token   the join token the client holds
     * @param host    the name of the host
     * @return the session and whether this claim created it
     * @throws ApiException {@code RESOURCE_CONFLICT} if another token holds an active session of the asset
     */
    public Claim claim(UUID assetId, String token, String host) {
        Instant now = clock.instant();
        AtomicBoolean created = new AtomicBoolean();
        LiveSessionResponseDTO session = sessions.compute(assetId, (id, existing) -> {
            if (existing != null && isActive(existing, now)) {
                if (!existing.token().equals(token)) {
                    throw ApiException.conflict("A live session of this asset is already active.");
                }
                return new LiveSessionResponseDTO(token, host, existing.startedAt(), now.plus(TTL));
            }
            created.set(true);
            return new LiveSessionResponseDTO(token, host, now, now.plus(TTL));
        });
        return new Claim(session, created.get());
    }

    /**
     * Ends the active session of an asset when the token matches it.
     *
     * @param assetId the asset
     * @param token   the join token the client holds
     * @return the session that was removed, or empty when no session was active
     * @throws ApiException {@code RESOURCE_CONFLICT} if another token holds an active session of the asset
     */
    public Optional<LiveSessionResponseDTO> release(UUID assetId, String token) {
        Instant now = clock.instant();
        AtomicReference<LiveSessionResponseDTO> removed = new AtomicReference<>();
        sessions.compute(assetId, (id, existing) -> {
            if (existing == null || !isActive(existing, now)) {
                return null;
            }
            if (!existing.token().equals(token)) {
                throw ApiException.conflict("A live session of this asset is already active.");
            }
            removed.set(existing);
            return null;
        });
        return Optional.ofNullable(removed.get());
    }

    /**
     * Removes every session that has expired and returns them, so the caller can announce the expiry.
     *
     * @return the expired sessions with the asset they belong to
     */
    public List<Expired> expire() {
        Instant now = clock.instant();
        List<Expired> expired = new ArrayList<>();
        for (UUID assetId : List.copyOf(sessions.keySet())) {
            sessions.computeIfPresent(assetId, (id, session) -> {
                if (isActive(session, now)) {
                    return session;
                }
                expired.add(new Expired(id, session));
                return null;
            });
        }
        return List.copyOf(expired);
    }

    private static boolean isActive(LiveSessionResponseDTO session, Instant now) {
        return now.isBefore(session.expiresAt());
    }

    /**
     * The outcome of a claim.
     *
     * @param session the session after the claim
     * @param created whether the claim created the session, as opposed to refreshing it
     */
    public record Claim(LiveSessionResponseDTO session, boolean created) {
    }

    /**
     * A session that expired, with the asset it belonged to.
     *
     * @param assetId the asset
     * @param session the expired session
     */
    public record Expired(UUID assetId, LiveSessionResponseDTO session) {
    }
}
