# Spec Delta

## ADDED Requirements

### Requirement: Start or refresh the live session of an asset
The system SHALL keep at most one live session per asset. A `PUT /project/{projectId}/asset/{id}/live-session` with a body `{ "token": string, "host": string }` SHALL create the session when none is active, or refresh its expiry when the token matches the active session. The response SHALL be `200 OK` with `token`, `host`, `startedAt` and `expiresAt` in ISO-8601 form. A session SHALL expire 60 seconds after its last `PUT`.

#### Scenario: Start a session
- **WHEN** a client sends a valid `PUT` for an asset without an active session
- **THEN** the system answers `200 OK` with the token and host, a `startedAt` of now and an `expiresAt` 60 seconds later

#### Scenario: Refresh keeps the start
- **WHEN** a client sends a `PUT` with the token of the active session 20 seconds after its start
- **THEN** the system answers `200 OK`, keeps `startedAt` and moves `expiresAt` to 60 seconds after the new request

### Requirement: Validate the live-session request
The token of a live-session `PUT` SHALL be exactly 16 characters of `a` to `z` and `0` to `9`, and the host SHALL be 1 to 64 characters. A request that breaks either rule SHALL be answered with `400 VALIDATION_FAILED` naming the rejected field, and SHALL store nothing.

#### Scenario: Malformed token
- **WHEN** a client sends a `PUT` whose token is not 16 characters of `a` to `z` and `0` to `9`
- **THEN** the system answers `400 VALIDATION_FAILED` with a violation on field `token` and stores nothing

### Requirement: Refuse a second live session
While a session of an asset is active, the system SHALL refuse a `PUT` with a different token with `409 RESOURCE_CONFLICT`, and SHALL NOT change the active session. A session whose expiry has passed SHALL NOT count as active.

#### Scenario: Another token while active
- **WHEN** a client sends a `PUT` with another token while the session of the asset is active
- **THEN** the system answers `409 RESOURCE_CONFLICT` and the active session is unchanged

#### Scenario: Another token after expiry
- **WHEN** a client sends a `PUT` with another token after the previous session expired
- **THEN** the system answers `200 OK` with the new session

### Requirement: Read the live session of an asset
The system SHALL answer `GET /project/{projectId}/asset/{id}/live-session` with `200 OK` and the active session, or with `404 RESOURCE_NOT_FOUND` when no session is active or the asset does not belong to the project.

#### Scenario: No active session
- **WHEN** a client reads the live session of an asset without an active session
- **THEN** the system answers `404 RESOURCE_NOT_FOUND`

#### Scenario: Expired session
- **WHEN** a client reads the live session of an asset whose session expired 1 second ago
- **THEN** the system answers `404 RESOURCE_NOT_FOUND`

### Requirement: End a live session
The system SHALL answer `DELETE /project/{projectId}/asset/{id}/live-session?token=<token>` with `204 No Content` when the token matches the active session or no session is active. When another token is active, the system SHALL answer `409 RESOURCE_CONFLICT` and keep the session.

#### Scenario: End with the matching token
- **WHEN** a client deletes the session with the token of the active session
- **THEN** the system answers `204 No Content` and a subsequent read answers `404 RESOURCE_NOT_FOUND`

#### Scenario: End with another token
- **WHEN** a client deletes the session with a token that is not the active one
- **THEN** the system answers `409 RESOURCE_CONFLICT` and the active session is unchanged

### Requirement: Stream the events of an asset
The system SHALL answer `GET /project/{projectId}/asset/{id}/events` with `text/event-stream`. The stream SHALL carry only the events of that asset. The stream SHALL answer `404 RESOURCE_NOT_FOUND` for an asset that does not belong to the project, before it starts.

#### Scenario: Events of another asset are not delivered
- **WHEN** a stream of asset A is open and asset B is saved
- **THEN** the stream carries no event for asset B

#### Scenario: Stream of an unknown asset
- **WHEN** a client opens the event stream of an asset that does not exist
- **THEN** the system answers `404 RESOURCE_NOT_FOUND` and starts no stream

### Requirement: Announce committed saves with the new ETag
The system SHALL send a `saved` event after every committed create, replacement or restore of an asset. The data SHALL carry `etag` as the quoted SHA-1 digest of the new content, `sha1` as the digest, and `revision` as the number of the new revision. The system SHALL NOT send the event for a write that rolls back.

#### Scenario: Replacement emits the new ETag
- **WHEN** a client replaces the content of an asset with an open event stream
- **THEN** the stream carries a `saved` event whose `etag` is the quoted SHA-1 digest of the new bytes and whose `revision` is the new revision number

#### Scenario: Rolled-back save
- **WHEN** a write of an asset fails and its transaction rolls back
- **THEN** the stream carries no `saved` event for that write

### Requirement: Announce session changes
The system SHALL send a `session` event with `state` `started` when a session is created, `ended` when a session is removed by `DELETE` or expires, and the `token` and `host` of the session. A session that replaces an expired one SHALL be announced with `started` only; the `started` event implies the earlier session ended.

#### Scenario: Start and end are announced
- **WHEN** a client starts a session and then deletes it, while a stream of the asset is open
- **THEN** the stream carries a `session` event with state `started`, followed by one with state `ended`, both with the token of the session

#### Scenario: Expiry is announced
- **WHEN** a session expires while a stream of the asset is open
- **THEN** the stream carries a `session` event with state `ended` within the next expiry sweep

### Requirement: Keep the event stream open
The stream SHALL begin with a comment line, and SHALL carry a comment line at least every 15 seconds while it is open, so that idle connections are not closed by intermediaries.

#### Scenario: Stream opened
- **WHEN** a client opens the event stream of an existing asset
- **THEN** the first line of the stream is a comment

### Requirement: Keep the registry in one backend instance
The live-session registry and the event fan-out SHALL be held in memory of the backend instance that serves the request. A deployment with more than one replica SHALL NOT be used for live sessions until a shared store exists.

#### Scenario: Registry state after restart
- **WHEN** the backend restarts
- **THEN** no live session is active and clients read `404` until a new session starts
