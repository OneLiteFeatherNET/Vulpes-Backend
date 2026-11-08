## Context

The generator needs one consistent state of a project. The backend reads entities from `vulpes-model` through Micronaut Data repositories on PostgreSQL. Read paths today have no transaction, so each repository call runs on its own. The child rows (item components, lore and enchantments, font chars, sound sources, dimension attributes and timelines, asset revisions) are only reachable per parent, and asset links have no repository yet. The list DTOs drop data: `ItemModelDTO` returns empty lore and enchantments, `FontModelDTO` has no chars, `SoundModelDTO` has no `replace` flag.

Two unmerged drafts touch the same area. Branch `feat/snapshot-events` holds `add-runtime-snapshot` and `add-snapshot-events`: `GET /project/{projectId}/snapshot` serves the Minestom runtime a resolved view of items and attributes, with a revision and server-sent change events. Branch `docs/propose-snapshot-format-1` proposes to turn that endpoint into snapshot format 1. Neither is on `main` or open as a pull request.

## Goals / Non-Goals

**Goals:**
- One request that returns every content type of a project from one consistent database snapshot.
- A body that is identical for identical content, so a revision over it is meaningful.
- A fixed number of queries per export, independent of the number of rows.

**Non-Goals:**
- Snapshot format 1, asset bytes, pagination, change events, authentication (see proposal).

## Decisions

### A separate endpoint next to the runtime snapshot
`GET /project/{projectId}/content` is a new route and does not reuse `/snapshot`. The runtime snapshot resolves content for Minestom (material derived from a component, `stelaris:*` components stripped, lore merged into components); the generator needs the stored model unchanged and maps it itself. One body cannot serve both without bending one of them. The runtime branch is far behind `main` and has no owner; building on it would block this change on its rebase and review. The proven parts are taken over as ideas, not as code: a revision over a canonical JSON form, and the `If-None-Match` parsing that accepts `*`, weak tags and lists.

Alternative considered: format 1 delivered by the backend (`add-snapshot-format-1`). Rejected for this change, because it ties the backend to the plugin's contract and moves namespace, text-component and asset-path mapping into the backend. The generator already owns that mapping. The two drafts stay untouched.

### Own response records, existing field names
The export has its own records in `domain/content`. Their field names are the names of the existing per-type DTOs, so a client that knows the regular endpoints knows the export. They differ in three ways: children are nested (`components`, `lore`, `enchantments` on an item, `chars` on a font, `sources` on a sound, `attributes` and `timelines` on a dimension, `revisions` and `links` on an asset), fields the list DTOs drop are present (`replace` on a sound), and `creationDate`, `modificationDate` and the redundant `projectId` on every element are left out. Timestamps change on a save without a content change and would change the revision for nothing. Ids stay, because references (`parentId`, `textureAssetId`, `iconAssetId`, `sourceAssetId`, link targets) point at them.

JSON stored as text (item component values, advancement title and description, pack description) is delivered as a JSON value, as the existing endpoints do.

Reusing the existing DTOs was rejected: they are shaped for the editor, carry timestamps, and fixing their empty lists would change existing responses.

### One read-only transaction with a repeatable-read snapshot
The export service method runs in one transaction with `readOnly = true` and isolation `REPEATABLE_READ` (`io.micronaut.transaction.annotation.Transactional`; the services otherwise use `jakarta.transaction.Transactional`, which has neither attribute). On PostgreSQL the default `READ COMMITTED` gives every statement its own snapshot, so a transaction alone would not make the export atomic. Under `REPEATABLE READ` all statements see the state at the first read. A read-only transaction never fails with a serialization error.

### Fixed set of project-wide queries
Each kind is loaded by one query per table, filtered by project id through the parent (`c.item.project.id = :projectId`, as `ItemModelComponentRepository` already does), and grouped in memory by parent id. That is about fifteen queries per export, whatever the project size. Fetch joins over two bags of the same parent are avoided (`MultipleBagFetchException` on items and dimensions). The new queries live in backend repositories; `vulpes-model` is not changed. Element collections (expression variables, function parameters, pack features) are loaded with their owner.

`AssetServiceImpl.getRevisions` is not used: it creates missing revision rows and therefore writes. An asset without revision rows is exported with an empty `revisions` list.

### Deterministic order
Every list has a total order. The sort key is the natural key, with the id as the final tie-break:

| List | Order |
|---|---|
| items, attributes, fonts, sounds, advancements, dimensions | `key`, then `id` |
| expression functions | `name`, then `id` |
| expressions | `uiName`, then `id` |
| assets | `keySpace`, `key`, then `id` |
| packs | `packType` |
| item components | `componentKey` |
| item lore, font chars | `orderIndex`, then `id` |
| item enchantments | `name`, then `id` |
| sound sources | `name`, then `id` |
| dimension attributes | `attributeKey`, then `id` |
| dimension timelines | `timelineKey`, then `id` |
| asset revisions | `number` |
| asset links | `targetType`, `targetId`, `role` |
| expression variables, function parameters | stored order (`@OrderColumn`) |
| pack `enabledFeatures` | lexicographic |

Sound sources have no stored position. Ordering them by name is stable but is not the order in which they were added; a weighted pool does not depend on order. Strings are compared by Java's natural `String` order, not by database collation, so the order does not depend on the database locale.

### Revision over the canonical content
The revision is the lowercase hex SHA-256 of the canonical JSON of the body without the `revision` field: object keys sorted, no insignificant whitespace, UTF-8, also inside nested JSON values. It is built from the serialized tree of the response records, so it covers exactly what is delivered. The revision is opaque: clients compare it for equality and do not recompute it. A backend upgrade that changes number formatting or adds a field may change the revision of unchanged content; for the generator that only means one more build.

The response carries `ETag: "<revision>"` (strong). `If-None-Match` with `*`, the quoted revision, `W/"<revision>"` or a list containing one of them is answered with `304 Not Modified` and the `ETag`, without a body. The export is still read before the comparison, so a `304` saves bandwidth, not database work.

## Risks / Trade-offs

- [Large projects make large responses] → No pagination by decision. Content projects are in the low thousands of rows; the response is built in memory once per request. If this grows, a streaming writer can replace the in-memory body without changing the contract.
- [`REPEATABLE_READ` must reach the JDBC connection] → An integration test checks the isolation level inside the export transaction.
- [New fields change the revision] → Accepted, see revision decision. Field additions are additive for the generator.
- [Overlap with the unmerged snapshot drafts] → The paths are distinct. If the runtime snapshot is revived, it can derive its view from this export later.

## Migration Plan

None. No schema change, no change to existing responses. Rollback removes the route.

## Open Questions

None.
