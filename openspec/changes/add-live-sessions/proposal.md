Ships as `feat(session): register live edit sessions and push asset events`

## Why

Designers co-edit a Vulpes model live. Blockbench's native Edit Sessions connect the participants peer to peer, but two things are missing on the Vulpes side. Nothing records that a model is being edited right now, so a designer cannot see that a session exists or join it. And nothing tells the other participants when someone saves: the second saver still holds the ETag of the version before the first save, and the replacement fails with a false `412`, although nothing conflicts in substance.

This change gives the backend the two small pieces it needs for that: a record of which asset has a live session and the token to join it, and a stream of asset events that tells every participant the new ETag after a save.

This change depends on `add-asset-revisions`, which introduces the revisions whose `saved` events carry the revision number, and on the asset endpoints of `add-project-assets`.

## What Changes

- `GET`, `PUT` and `DELETE /project/{projectId}/asset/{id}/live-session` read, start or refresh, and end the live session of an asset. A session holds a join token and a host and expires 60 seconds after its last `PUT`.
- A second, different token cannot start a session while one is active; it is refused with `409`.
- `GET /project/{projectId}/asset/{id}/events` streams asset events as `text/event-stream`:
  - `saved` after every committed create, replacement or restore of the asset, with the new `ETag`, the SHA-1 digest and the revision number;
  - `session` when a session starts, ends, or expires.
  - A comment line every 15 seconds keeps the connection open.
- The session registry is in memory. It is correct for one backend replica only.

## Capabilities

### New Capabilities

- `live-sessions`: the live-session record of an asset and the event stream that announces saves and session changes.

### Modified Capabilities

None.

## Non-goals

- The signaling server. The PeerJS sidecar that exchanges the connection offers is a separate deployment and not part of the backend.
- Authentication and authorization of the live-session endpoints and the event stream. They are open like the rest of the API until the authentication change lands.
- A shared store for the registry and the event fan-out. A deployment with more than one replica needs one; this change does not add it.
- Replaying events a client missed while disconnected. A client that reconnects reads the asset to catch up.
- Changing the CORS configuration. Local testing uses an additional configuration file.

## Impact

- **Code**: a session registry with an injected clock, a session service and controller, an asset event hub with after-commit publishing of `saved` events, an expiry sweep, and one `Clock` bean. `AssetPersistence` publishes a content-saved event inside its transactions.
- **API**: three routes for the live session and one event stream under `/project/{projectId}/asset/{id}`. Existing responses are unchanged.
- **Data**: none. The registry is not persisted; a restart ends all live sessions.
- **Operations**: a deployment with more than one replica must not serve live sessions until a shared store exists.
