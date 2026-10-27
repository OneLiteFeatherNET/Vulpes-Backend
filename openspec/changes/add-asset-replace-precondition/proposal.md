Ships as `feat(asset): reject stale asset replacements with if-match`

## Why

Project assets are edited outside the web UI as well. The Blockbench desktop plugin opens a `.bbmodel` asset, lets a designer work on it for a while, and saves it back over the same asset. Today the content endpoint is last-writer-wins: if a colleague saved a newer version in between, the older save silently replaces it and nothing tells either designer that work was lost.

Clients need a way to say "replace this only if it is still the version I started from". The content endpoint already sends a strong ETag (the SHA-1 of the stored bytes), so the natural answer is the HTTP `If-Match` precondition. It has to be opt-in, because the web UI (Stelaris) replaces content without remembering an ETag and must keep working unchanged.

This change depends on the OpenSpec change `add-project-assets`, which introduces the asset endpoints this change extends.

## What Changes

- `POST /project/{projectId}/asset/{id}/content` accepts an optional `If-Match` header. When present and it does not match the current ETag of the asset, the request is refused with `412 Precondition Failed` and nothing is changed. The response carries the current ETag, so the client can fetch, merge and retry.
- `If-Match: *` matches any existing asset. Weak (`W/`) and malformed values never match.
- Without `If-Match` the behaviour is exactly as today.
- Two replacements that send the same `If-Match` cannot both succeed: the comparison and the write happen under a row lock in one transaction.
- The upload (`POST /project/{projectId}/asset`) and replace responses send the new `ETag` header, so a client learns the new version without a second request.
- The refusal uses the existing error mechanism with one new error code, `ASSET_PRECONDITION_FAILED`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `project-assets`: adds requirements for optimistic concurrency on content replacement and for the ETag on write responses. The capability is introduced by `add-project-assets`; this change adds to it.

## Non-goals

- Authentication and per-user identity of writers. This comes later as one shared change together with the OIDC work of Stelaris; the Blockbench plugin does not rely on it here.
- A separate API for the Blockbench plugin. It uses the existing asset endpoints.
- Making `If-Match` mandatory. Clients that do not send it keep last-writer-wins.
- Preconditions on upload, delete, or link endpoints.

## Impact

- **Code**: `AssetController`, `AssetService` and `AssetServiceImpl`, `AssetPersistence` (replace path only), `ProjectAssetRepository` (one locking finder), `ErrorCode`, `ApiException`, `ApiExceptionHandler`.
- **API**: one new `412` response on the replace endpoint, one new error code, `ETag` on upload and replace responses. No new paths, no CORS or security changes.
- **Data**: no schema change.
- **Clients**: the generated Dart client gains one enum value; existing callers are unaffected.
