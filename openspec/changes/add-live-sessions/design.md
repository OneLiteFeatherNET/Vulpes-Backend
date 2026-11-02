# Design

## Context

- `AssetPersistence` runs every content write in one transaction (`create`, `replaceContent`, `restoreRevision`). The revision of a write is known inside that transaction; the commit happens when the method returns.
- The snapshot event stream (`feat(snapshot): push snapshot change events over sse`) already uses Micronaut's `Event` and a per-subscriber unicast buffer, and publishes after commit with `TransactionalEventListener`. This change follows that approach.
- Micronaut's `TransactionalEventListener` is itself meta-annotated as an application event listener. Putting a plain `@EventListener` on the same method may dispatch the event twice, once before the commit. The listener therefore carries only `@TransactionalEventListener(AFTER_COMMIT)`.
- Validation failures reach the client as `VALIDATION_FAILED` with the offending fields in `errors`, through bean validation on the request body.

## Goals / Non-Goals

**Goals:**
- A client can tell whether an asset is being edited live and obtain the token to join.
- Every participant learns the new ETag of an asset after a committed save, without polling.
- Expiry is decided by an injected clock, so it can be tested without waiting.

**Non-Goals:**
- Signaling, authentication, multi-replica operation (see the proposal).
- Persisting sessions or replaying missed events.

## Decisions

### D1. Registry in memory, one entry per asset
`LiveSessionRegistry` holds at most one session per asset id in a `ConcurrentHashMap`. Each entry carries the token, the host, the start instant and the expiry instant. The registry takes a `java.time.Clock`; a session is active while the current instant is before its expiry. A session expires 60 seconds after its last `PUT`; the TTL is a constant, not configuration, because the clients refresh every 20 seconds against it.

Alternative considered: a database table. Rejected for this change: a session lasts a minute, a restart ending all sessions is acceptable, and a table adds a migration without a benefit for a single replica.

### D2. Start, refresh, conflict and release
- `PUT` with no active session creates it, with `startedAt` set to now.
- `PUT` with the token of the active session refreshes its expiry and keeps `startedAt`.
- `PUT` with another token while the session is active answers `409 RESOURCE_CONFLICT`. An expired session counts as absent, so a new token replaces it.
- `DELETE` with the token of the active session removes it. `DELETE` with no active session answers `204`. `DELETE` with another token while a session is active answers `409`.

The check and the write run inside `ConcurrentHashMap.compute`, so two `PUT`s with different tokens cannot both create a session.

### D3. Expiry events
A scheduled sweep (every 5 seconds) removes expired sessions and publishes a `session` event with `state: "ended"` for each. Between two sweeps an expired session is already invisible to `GET` and `PUT`. A `started` event for a new token implies that the previous session ended, so a replacement of an expired session needs no separate `ended` event even when the sweep did not run first.

### D4. After-commit `saved` events
`AssetPersistence` publishes an `AssetContentSaved` event (asset id, SHA-1, revision number) as the last step inside `create`, `replaceContent` and `restoreRevision`. `AssetEventHub` listens with `@TransactionalEventListener(AFTER_COMMIT)`, so the event is dispatched only after the commit, and not at all when the transaction rolls back. The `saved` payload carries `etag` as the quoted SHA-1, the `sha1`, and the `revision` number.

### D5. Fan-out per subscriber
`AssetEventHub` keeps one subscriber record per open stream: its asset id and a unicast sink with an unbounded buffer, as the snapshot stream does. Publishing is synchronized and only passes events to the subscribers of the same asset. A slow client therefore never loses an event; the buffer grows only for that client. Subscription happens when the stream is subscribed, and the subscriber is removed when the stream ends.

### D6. Stream opening and keep-alive
The stream opens with a comment line. The comment is written after the subscriber is registered, so the moment a client reads it, every later event of the asset reaches the stream. Keep-alive comments follow every 15 seconds. The stream answers `404` for an asset that does not exist or belongs to another project, through the existing asset lookup, before the stream starts.

### D7. Where the code lives
Controllers depend on the service interfaces and the event types only. The mapping from events to SSE frames stays in the controller, because the frames are a transport concern; the mapping from the registry to the response bodies happens in `service.impl`. `Clock` is provided as one singleton bean with `Clock.systemUTC()`.

## Risks / Trade-offs

- [Two replicas each hold their own registry; a client may see a session on one and none on the other] → Documented as a single-replica limitation; a deployment with more than one replica needs a shared store first.
- [Events are not replayed] → A client that reconnects reads the asset and its ETag; a `saved` event missed during a disconnect is not recovered.
- [A restart ends every live session] → Accepted; clients see `GET` answer `404` and can start a new session.
- [The sweep runs every 5 seconds, so `ended` for an expired session can arrive up to 5 seconds late] → Accepted; `GET` and `PUT` already treat the session as ended at its expiry.

## Migration Plan

- No database change and no startup job.
- Deploy with one replica; the registry starts empty.
- Rollback: remove the routes; no data needs cleaning up.

## Open Questions

- Whether the deployment will need a shared store for the registry once the authentication change lands and more than one replica is planned.
