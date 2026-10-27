# Design

## Context

- `AssetPersistence.replaceContent` overwrites the bytes under `<projectId>/<assetId>` and updates the asset row (`sha1`, `size`, `fileName`, `width`, `height`). The asset row already describes the current content, so the current revision needs no separate pointer.
- `AssetStorage` is Micronaut Object Storage. In the local implementation (`micronaut-object-storage-local` 3.0.0) a key is a file path under `VULPES_ASSET_PATH`, and a write creates the parent directories with `Files.createDirectories`. A file and a directory cannot share a path, so a key `a/b` and a key `a/b/c` cannot coexist there. S3 has no such restriction.
- Deployed environments run `hbm2ddl.auto: none`; `add-flyway-migrations` proposes Flyway with `validate`.

## Goals / Non-Goals

**Goals:**
- Every content write leaves a revision that can be listed, downloaded and restored.
- Revision bytes are immutable: a write never overwrites the bytes of an earlier revision.
- Existing assets gain a history without a data job.

**Non-Goals:**
- Pruning, diffing, or a web UI for revisions.
- Authentication or the identity of the author.
- Changing the replacement precondition (`add-asset-replace-precondition`); restore only reuses it.

## Decisions

### D1. Current revision is the newest one, no pointer column
The newest revision (highest `number`) is the current one. Restore appends a revision, so the newest revision always holds the current bytes. The asset row keeps `sha1`, `size`, `fileName`, `contentType`, `width` and `height` in sync, as replacement does today, so the asset response and the content `ETag` need no extra query. Alternative considered: a `current_revision_id` on the asset. Rejected, because it adds a foreign key cycle and an extra update on every write, and the newest-revision rule already gives the same answer.

### D2. Revision table
`project_asset_revision`: `id` (UUID, `IdentifiableEntity`), `asset_id` (foreign key, `ON DELETE CASCADE`), `number` (int, from 1), `sha1`, `size_bytes`, `storage_key`, `comment` (nullable, 500), and `creation_date` from `IdentifiableEntity`. The unique constraint `uk_project_asset_revision_number` covers `(asset_id, number)`; the row lock of D5 serializes writers, the constraint is the last line of defence. Revisions have no update path.

### D3. Storage keys
- New revisions: `<projectId>/revisions/<assetId>/<number>`, the key of revision `n` of the asset.
- Revision 1 of an asset created before this change keeps the legacy key `<projectId>/<assetId>`. Nothing is moved or copied.

The contract proposed `<projectId>/<assetId>/<revisionId>`, with two changes. The `revisions/` segment is needed because the local store cannot hold the legacy file `<projectId>/<assetId>` and a directory below it at the same time. The revision number replaces the revision id because the key must be known before the row is inserted: a save flushes the insert, and the key column is not nullable. `(asset, number)` is unique and numbers are never reused, so the key stays immutable. Each revision row stores its key, so no code derives a key from a revision of another asset.

### D4. Lazy backfill
`ensureRevisions(asset)` runs in its own transaction under the asset row lock. When the asset has no revision rows, it inserts revision 1 with the asset's `sha1` and `size`, the legacy key, and no comment. It runs on the first revision listing and at the start of the first replacement or restore. Content reads of an asset without rows serve the legacy key directly, so a read never writes. If the legacy bytes are missing, the revision list still works, and the content of that revision answers `INTERNAL_ERROR` as the content endpoint does today.

### D5. Transaction and storage order
Each write runs in one transaction with the asset row locked (`findByIdForUpdate`, as `add-asset-replace-precondition`). The order is: check the precondition, insert the revision row, write the bytes to the new key, update the asset row, commit. A rollback leaves an orphan object under a key no row refers to; it never overwrites live bytes, which is the improvement over the overwrite of today. Deletes still remove bytes before the commit, the known issue of `add-project-assets`, fixed in its follow-up `fix(asset)` change.

### D6. Restore
Restore reads the bytes of the chosen revision, validates them with `AssetInspector` against the asset's kind and file name, and runs the same write path as replacement: revision `n+1`, asset row update, and for a Blockbench model removal and re-extraction of the derived textures. The comment is `Restored revision <number>` unless the request carries `comment`. The precondition is the same as on replacement: an absent `If-Match` restores unconditionally, `*` and the exact strong ETag of the current content match, anything else answers `412` with the current ETag. The response is the asset (`200`) with its new `ETag`.

### D7. Ownership and errors
A revision that belongs to another asset, or an asset of another project, answers `404 RESOURCE_NOT_FOUND` through the existing `notOwnedBy` path. A comment longer than 500 characters answers `400 VALIDATION_FAILED` on field `comment` with the violation `ASSET_COMMENT_TOO_LONG`.

### D8. Delete
Deleting an asset deletes the bytes of each of its revisions and the legacy key, then the revision rows (also cascaded by the database), then the asset. Derived textures are removed the same way first.

## Risks / Trade-offs

- [Orphan objects after a rolled-back write] → Accepted; they are unreachable and reclaimed by the follow-up `fix(asset)` change.
- [Legacy bytes are read by the backfill path only when the revision is first used] → Acceptable: the legacy key stays valid until the asset is deleted.
- [Revision history grows without bound] → Accepted as a non-goal; assets are small (8 MB limit per upload).
- [The key scheme differs from the first draft of the contract] → Documented in D3; no client sees storage keys.

## Migration Plan

- New table only; existing assets gain revision 1 lazily, no startup job.
- With Flyway on `main`: a new `V<n>__project_asset_revision.sql` creating the table, its foreign key with `ON DELETE CASCADE`, and the unique constraint.
- Without Flyway: the table is created by `hbm2ddl update` in development; deployed environments need the DDL in the rollout, as `add-project-assets` does.
- Rollback: drop the table; new revision bytes under `revisions/` can be removed with the asset directory.

## Open Questions

- Does `add-flyway-migrations` land before this change? It decides how the table reaches deployed environments.
