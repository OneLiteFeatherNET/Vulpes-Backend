## ADDED Requirements

### Requirement: Export the complete content of a project
The system SHALL answer `GET /project/{projectId}/content` with `200 OK` and one JSON document that contains `revision`, `project` and the lists `items`, `attributes`, `fonts`, `sounds`, `expressionFunctions`, `expressions`, `assets`, `advancements`, `dimensions` and `packs`. Every list SHALL contain all elements of that type that belong to the project and no element of another project. A type without elements SHALL be an empty list, never missing or `null`.

#### Scenario: Project with every content type
- **WHEN** a client exports a project that has at least one element of every type
- **THEN** the system answers `200 OK` and every list contains exactly the elements of that project

#### Scenario: Empty project
- **WHEN** a client exports a project without content
- **THEN** the system answers `200 OK` with `project` and every list empty

#### Scenario: Other projects stay out
- **WHEN** two projects have content and a client exports the first
- **THEN** no element of the second project appears in the body

#### Scenario: Unknown project
- **WHEN** a client exports a project id that does not exist
- **THEN** the system answers `404 RESOURCE_NOT_FOUND`

### Requirement: Nest child elements under their parent
Each item SHALL carry its `components` (stored value as a JSON value), `lore` and `enchantments`. Each font SHALL carry its `chars`. Each sound SHALL carry its `sources` and its own `replace` flag. Each dimension SHALL carry its `attributes` and `timelines`. Each asset SHALL carry its `revisions` and `links`. Children SHALL carry all their stored content fields.

#### Scenario: Item children are present
- **WHEN** an item has two components, two lore lines and one enchantment
- **THEN** the exported item carries all of them, and each component value is a JSON value, not a string

#### Scenario: Font chars and sound sources are present
- **WHEN** a font has chars and a sound has sources
- **THEN** the exported font carries its chars and the exported sound carries its sources and its `replace` flag

### Requirement: Export asset metadata without file content
Each asset SHALL carry `key`, `displayName`, `kind`, `keySpace`, `fileName`, `contentType`, `size`, `sha1`, `width`, `height` and `sourceAssetId`, its `revisions` with `number`, `sha1` and `size`, and its `links` with `targetType`, `targetId` and `role`. The export SHALL NOT carry file content.

#### Scenario: Asset metadata only
- **WHEN** a project has a texture asset
- **THEN** the exported asset carries its metadata and SHA-1 and no file bytes

#### Scenario: Asset without revision rows
- **WHEN** an asset has no stored revisions
- **THEN** the exported asset carries an empty `revisions` list and the export writes nothing

### Requirement: Leave out audit timestamps
The export SHALL NOT contain `creationDate` or `modificationDate` on any element. Ids SHALL be present on every element that has one.

#### Scenario: Save without a content change
- **WHEN** a client saves an item with unchanged values
- **THEN** a later export has the same body and the same revision as before

### Requirement: Read the export from one consistent state
The system SHALL read the whole export in one read-only database transaction with isolation `REPEATABLE READ`, so that all parts of the body reflect the same committed state.

#### Scenario: Export transaction
- **WHEN** the export reads the database
- **THEN** all its queries run in one read-only transaction whose isolation level is `repeatable read`

### Requirement: Order the top-level lists deterministically
The top-level lists SHALL be ordered by content only: items, attributes, fonts, sounds, advancements and dimensions by `key`; expression functions by `name`; expressions by `uiName`; assets by `keySpace` then `key`; packs by `packType`. Ties SHALL be broken by `id`. Strings SHALL be compared independent of the database collation.

#### Scenario: Items by key
- **WHEN** a project has items with the keys `b`, `a` and `c`
- **THEN** the export lists them as `a`, `b`, `c`

### Requirement: Order the nested lists deterministically
Nested lists SHALL be ordered by content only: item components by `componentKey`; lore and font chars by `orderIndex`; enchantments and sound sources by `name`; dimension attributes by `attributeKey`; dimension timelines by `timelineKey`; asset revisions by `number`; asset links by `targetType`, `targetId` then `role`; pack `enabledFeatures` lexicographically. Ties SHALL be broken by `id`. Expression variables and function parameters SHALL keep their stored order.

#### Scenario: Lore by position
- **WHEN** an item has lore lines stored with `orderIndex` 2, 0 and 1
- **THEN** the export lists them by `orderIndex` 0, 1, 2

#### Scenario: Insertion order does not matter
- **WHEN** two projects receive the same content in different insertion orders
- **THEN** their exports list the elements in the same order

#### Scenario: Repeated export is identical
- **WHEN** a client exports the same unchanged project twice
- **THEN** both bodies are byte-identical

### Requirement: Identify the exported state by a revision
The body SHALL carry `revision`, the lowercase hex SHA-256 of the canonical JSON of the body without `revision` (object keys sorted at every depth, no insignificant whitespace, UTF-8). The response SHALL carry the header `ETag` with the quoted revision. Any change of exported content SHALL change the revision.

#### Scenario: ETag equals the revision
- **WHEN** a client exports a project
- **THEN** the `ETag` header is the body's `revision` in double quotes

#### Scenario: Content change changes the revision
- **WHEN** a lore line of an item changes between two exports
- **THEN** the second export has a different revision

### Requirement: Answer conditional exports
When the `If-None-Match` header of an export request is `*`, the quoted current revision, the weak form `W/"<revision>"`, or a comma-separated list containing one of them, the system SHALL answer `304 Not Modified` with the `ETag` header and no body. Otherwise it SHALL answer `200 OK` with the body.

#### Scenario: Matching If-None-Match
- **WHEN** a client sends the `ETag` of its last export as `If-None-Match` and nothing changed
- **THEN** the system answers `304 Not Modified` with the same `ETag` and no body

#### Scenario: Stale If-None-Match
- **WHEN** a client sends an old `ETag` after the content changed
- **THEN** the system answers `200 OK` with the new body and the new `ETag`
