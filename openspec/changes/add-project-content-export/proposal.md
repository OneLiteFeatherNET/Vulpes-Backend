Ships as `feat(project): export the complete content of a project in one request`

## Why

The vulpes-generator freezes the content of a project into a release snapshot. Today it assembles that snapshot from the regular endpoints: the project, the items and then per item their components and lore, attributes, fonts and per font their chars, sounds and per sound their sources, expression functions, expressions and asset metadata, every list read page by page. Those are dozens of requests, and each one sees whatever is committed at that moment. An edit that lands in between yields a snapshot that never existed in the editor.

The generator works around this by building everything twice and accepting it only when both runs are byte-identical, answering `409` otherwise. That doubles the load and still misses an edit that is made and reverted between the two runs. Some data is not reachable at all: the item list returns empty lore and enchantments, the font list has no chars, and the sound list drops the `replace` flag.

The backend owns the schema and the data, so it is the place that can read one consistent state. The generator stays a pure HTTP client.

## What Changes

- New `GET /project/{projectId}/content` returns the whole content of a project as one JSON document, read in one read-only database transaction with a consistent snapshot:
  - `project`;
  - `items`, each with its `components`, `lore` and `enchantments`;
  - `attributes`;
  - `fonts`, each with its `chars`;
  - `sounds`, each with its `sources`;
  - `expressionFunctions` and `expressions`;
  - `assets` with their metadata, current revision and links, but no file content;
  - `advancements`;
  - `dimensions`, each with its `attributes` and `timelines`;
  - `packs` (the pack metadata per pack type).
- The body uses the backend's own model and field names, the same names the existing per-type endpoints use, with every field the entity holds that matters for content. It leaves out audit timestamps.
- Every list is in a defined, stable order, so the same content always produces the same body.
- The body carries a `revision`: the SHA-256 of the canonical form of the content. The response sends it as a strong `ETag`; a request whose `If-None-Match` matches is answered with `304 Not Modified`.
- An unknown project is answered with `404 RESOURCE_NOT_FOUND`.

## Capabilities

### New Capabilities

- `project-content-export`: the complete, consistently read and deterministically ordered content of a project with its revision.

### Modified Capabilities

None.

## Non-goals

- Mapping to snapshot format 1 of the Vulpes Gradle plugin. Namespaces, foreign namespaces, lore as text components and asset paths stay in the generator, so the backend does not depend on the plugin contract.
- File content of assets. The generator downloads the bytes from the existing asset endpoints and checks them against the delivered SHA-1.
- Pagination or partial exports. Projects are small enough to be delivered whole.
- Push notifications on content changes, and any change to the unmerged `/project/{projectId}/snapshot` runtime endpoint of `add-runtime-snapshot`.
- Authentication. The endpoint is open like the rest of the API until the authentication change lands.
- Changes to existing endpoints, DTOs or write paths.

## Impact

- **API**: one new read-only route. Existing responses are unchanged.
- **Code**: a content export service with its own response records, new project-wide read queries for child rows (components, lore, enchantments, font chars, sound sources, dimension attributes and timelines, asset revisions and links), and a canonical JSON hasher.
- **Data**: none. No schema change.
- **Consumers**: the vulpes-generator replaces its many requests and its double build with this one request.
