# Design

## Context

Vulpes stores projects and their models in Postgres via `vulpes-model`. Nothing stores binary files yet. A prototype of this feature exists as draft PR #238 (`themeinerlp/poc/project-assets`). It also links assets to items, fonts and advancements and keeps keys unique regardless of kind. Both are dropped here before the feature reaches `main`, so the code PR is the prototype minus the links plus key spaces. Deployed environments run `hbm2ddl.auto: none`; `add-flyway-migrations` proposes Flyway with `validate`.

## Goals / Non-Goals

**Goals:**
- Upload, list, download, replace and delete textures, models and Blockbench models per project.
- Keys as pack resource locations, unique per key space.
- Extract textures embedded in Blockbench models.

**Non-Goals:**
- Resource pack generation.
- Any coupling to items, fonts or advancements (no links, no reference validation, no usages lookup, no delete guard). A read-only usages endpoint may follow if the UI needs it.
- Storage consistency (files written after commit, files removed with the project): separate `fix(asset)` change.
- Optimistic locking on replace: `add-asset-replace-precondition`.
- Authentication.

## Decisions

### Bytes in Micronaut Object Storage, metadata in Postgres
`ProjectAssetEntity` keeps project, key, key space, kind, file name, media type, size, SHA-1, width, height and, for extracted textures, the source model. The bytes go to Micronaut Object Storage under `<projectId>/<assetId>`, backed by the local file system (`VULPES_ASSET_PATH`, default `./data/assets`). Object Storage keeps a later switch to S3 a configuration change. Alternative considered: `bytea` in Postgres. Rejected, because it bloats the shared database and its backups.

### Key spaces as a derived column
`keySpace` (`TEXTURE` | `MODEL`) is derived from `AssetKind` on every store. The unique constraint `uk_project_asset_key_space` covers `(project_id, key_space, key)`, so concurrent uploads still conflict in the database. Alternative considered: `kind` in the constraint. Rejected, because a `MODEL` and a `BBMODEL` could then share a key.

### Kind from extension, checked by content
The extension decides the kind, and the content must match it: PNG signature and decode for textures, JSON object for models. Media types sent by clients are not trusted.

### Extracted textures belong to their model
An extracted texture stores the id of its source model. Replacing the model deletes its old textures and extracts the new ones; deleting it deletes them. All derived keys are checked before anything is stored.

### Errors use the existing problem format
Field errors use `VALIDATION_FAILED` with violations (`ASSET_UNSUPPORTED_TYPE`, `ASSET_INVALID_CONTENT`, `ASSET_KEY_INVALID`), taken keys `RESOURCE_CONFLICT`. `ApiException` gains a list of violations and a `validationFailed` factory.

### Schema through a migration
The code PR adds the `project_assets` table as a Flyway migration if `add-flyway-migrations` is on `main` by then, as the next `V<n>`. Otherwise the table has to be created by the rollout, because deployed environments do not run `hbm2ddl update`. Preferred order: Flyway first.

## Risks / Trade-offs

- [Files are written inside the database transaction; a rollback can leave orphan files, a failed commit after delete can lose bytes] → Accepted for the first version; fixed in the follow-up `fix(asset)` change.
- [Deleting a project leaves its files on disk] → Same follow-up.
- [Local file system storage needs a persistent volume per deployment, and scales to one replica] → Volume in the Flux overlay; S3 is a configuration change later.
- [A model may name a key without an asset] → Intended; pack generation reports missing files.

## Migration Plan

New table only, no changes to existing data. With Flyway on `main`: a new `V<n>__project_assets.sql`. Without it: create the table in dev and prod before the release. Rollback: drop the table and the asset directory.

## Open Questions

- Does `add-flyway-migrations` land before this change? It decides how the table gets into deployed environments.
