# Proposal

Ships as `feat(asset): upload textures and blockbench models to projects`.

## Why

A Vulpes project names the textures and models of its items, fonts and advancements as resource locations, for example the `minecraft:item_model` component, `texturePath` or `background`. The files behind these names live outside Vulpes. Nobody can build the resource pack of a project from Vulpes alone, and designers have to hand PNG and model files around by other means.

Designers work in Blockbench. Its `.bbmodel` files carry the model and its textures in one file. If Vulpes keeps these files with the project, the resource pack can later be generated from the project at any time, and a Blockbench plugin can open and save models directly against the backend.

## What Changes

- Projects get assets: textures (`.png`), Minecraft models (`.json`) and Blockbench models (`.bbmodel`), under `/project/{projectId}/asset`. Assets can be uploaded, listed (optionally by kind), read, downloaded, replaced and deleted, one by one or all of a project.
- Every asset has a key, a lowercase resource location without file extension such as `vulpes:item/ruby`. The key is the place of the file in the pack.
- Keys are unique per project and key space. Textures form one key space, models and Blockbench models the other, as `textures/item/ruby.png` and `models/item/ruby.json` sit side by side in a pack.
- Textures embedded in a Blockbench model are stored as textures of their own. They are replaced together with the model and deleted with it.
- Uploads are checked by extension and content. Broken files, unsupported types, invalid keys and taken keys are rejected with a problem response, and nothing is stored.
- Downloads carry an ETag (SHA-1 of the content) and answer `304` on a matching `If-None-Match`. The browser may read the `ETag` header through CORS.
- Assets stand alone as pack content. They are not linked to items, fonts or advancements, those models keep naming resource locations as today, and nothing checks that an asset exists for a name. Missing files are left to the later pack generation to report.
- Uploads are limited to 8 MB.

## Capabilities

### New Capabilities
- `project-assets`: Textures and models of a project as resource pack content, keyed by resource location, independent of the other project models.

### Modified Capabilities
<!-- None: openspec/specs has no capabilities yet. -->

## Impact

- **Code**: new asset domain, persistence, storage, service and controller in `Vulpes-Backend`. Item, font and advancement code stays untouched. `ApiException` gains field-level violations for `VALIDATION_FAILED`.
- **API**: new routes under `/project/{projectId}/asset`. CORS additionally exposes `ETag`.
- **Dependencies**: Micronaut Object Storage (core and local), managed by the Micronaut platform BOM.
- **Data**: a new `project_assets` table. Deployed environments run with `hbm2ddl.auto: none`, so the table needs a migration (see design).
- **Operations**: files are stored on the local file system under `VULPES_ASSET_PATH`. A deployment needs a persistent volume at that path (Flux, another repository).
- **Out of scope**: resource pack generation; writing files only after the database commit and removing the files of a deleted project (separate `fix(asset)` change); optimistic locking on replace (`add-asset-replace-precondition`); authentication.
