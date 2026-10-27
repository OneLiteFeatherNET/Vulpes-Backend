Ships as `feat(asset): keep a revision for every asset content write`

## Why

A designer who saves a Blockbench model over its asset today removes the previous version for good. If the new version is wrong, the only way back is a copy the designer kept elsewhere, and nothing says who changed what or when. The `add-asset-replace-precondition` change stops two saves from silently overwriting each other, but it does not keep the older version either.

Designers ask for a history per asset: every save is kept, they can see the earlier versions with a note on each, and they can make an earlier version current again without losing the versions in between.

This change depends on `add-project-assets`, which introduces the asset endpoints, and on `add-asset-replace-precondition`, whose `If-Match` rule the restore endpoint reuses.

## What Changes

- Every content write creates a revision. Creating an asset makes revision 1; each replacement makes the next number. An optional `comment` (up to 500 characters) on upload and replacement is stored with the revision.
- `GET /project/{projectId}/asset/{id}/revision` lists the revisions of an asset, newest first, as a page. Each entry tells whether it is the current one.
- `GET /project/{projectId}/asset/{id}/revision/{revisionId}/content` returns the bytes of one revision with its own `ETag`.
- `POST /project/{projectId}/asset/{id}/revision/{revisionId}/restore` makes an earlier revision current by writing its bytes as a new revision. It accepts the same optional `If-Match` as the replacement and answers `412` the same way.
- Assets created before this change have no revision rows. Their first revision is created when their revisions are listed or their content is first replaced; that revision keeps the bytes the asset already has.
- Deleting an asset deletes its revisions and their bytes.
- Revisions are kept forever. Pruning is a non-goal.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `project-assets`: adds requirements for the revision history of an asset. The capability is introduced by `add-project-assets`; this change adds to it.

## Non-goals

- Pruning or capping the number of revisions.
- Diffing revisions, or showing them in the web UI.
- A history of the textures extracted from a Blockbench model. Restoring a model re-extracts its textures as a replacement does; the textures are assets of their own and keep their own revisions.
- Authentication and per-user identity of the author of a revision.
- Making `If-Match` mandatory.

## Impact

- **Code**: new entity and repository for revisions, `AssetPersistence` (create, replace, restore and delete write revisions), `AssetService` and `AssetServiceImpl`, `AssetController` (three routes, one optional form field), `AssetUploadDTO` (carries the comment), `AssetViolationCode` (comment too long).
- **API**: three new routes under `/project/{projectId}/asset/{id}/revision`; an optional `comment` form field on upload and replacement. Existing responses are unchanged; the asset response keeps its fields.
- **Data**: a new `project_asset_revision` table. Deployed environments run `hbm2ddl.auto: none`, so it needs a migration (see design).
- **Storage**: revision bytes live under a new key scheme; the bytes of existing assets are not moved.
