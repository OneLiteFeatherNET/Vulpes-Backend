# Spec Delta

## Purpose

Keeps the uploaded textures and models of a project as the content of its resource pack. The asset key is the resource location under which the file is placed in the pack.

## ADDED Requirements

### Requirement: Upload assets by kind
The system SHALL accept uploads to `POST /project/{projectId}/asset` with a file and a key, and SHALL derive the kind from the file extension: `.png` is a texture, `.json` a model, `.bbmodel` a Blockbench model. A texture SHALL be a decodable PNG, and its width and height SHALL be stored. A model and a Blockbench model SHALL be a JSON object. Each asset SHALL store its SHA-1 digest and size.

#### Scenario: Upload a texture
- **WHEN** a valid PNG `ruby.png` is uploaded with key `vulpes:item/ruby`
- **THEN** the system responds with 201 and returns the asset with kind `TEXTURE`, width, height, size and SHA-1

#### Scenario: Unsupported file type
- **WHEN** a file `ruby.gif` is uploaded
- **THEN** the system responds with 400 `VALIDATION_FAILED` and a violation `ASSET_UNSUPPORTED_TYPE` on field `file`, and stores nothing

#### Scenario: Broken content
- **WHEN** a file `ruby.png` that is not a PNG, or a `.json` that is not a JSON object, is uploaded
- **THEN** the system responds with 400 `VALIDATION_FAILED` and a violation `ASSET_INVALID_CONTENT` on field `file`, and stores nothing

#### Scenario: Unknown project
- **WHEN** an asset is uploaded to a project that does not exist
- **THEN** the system responds with 404 `PROJECT_NOT_FOUND`

#### Scenario: File too large
- **WHEN** a file larger than 8 MB is uploaded
- **THEN** the system responds with 413 as a problem response (`application/problem+json`) and stores nothing

### Requirement: Asset keys are resource locations
An asset key SHALL be a lowercase resource location `namespace:path` that matches `[a-z0-9_.-]+:[a-z0-9_./-]+` and whose last path segment has no file extension.

#### Scenario: Invalid key
- **WHEN** an asset is uploaded with key `Vulpes:Item/Ruby.png`
- **THEN** the system responds with 400 `VALIDATION_FAILED` and a violation `ASSET_KEY_INVALID` on field `key`

### Requirement: Asset keys are unique per key space
The system SHALL keep asset keys unique per project within a key space. Textures form one key space. Models and Blockbench models form the other. The same key MAY exist once in each key space.

#### Scenario: Texture and model share a key
- **WHEN** a project has a model asset `vulpes:item/ruby` and a texture `vulpes:item/ruby` is uploaded
- **THEN** the upload succeeds with 201

#### Scenario: Duplicate key in the same key space
- **WHEN** a project has a texture `vulpes:item/ruby` and another texture with that key is uploaded
- **THEN** the system responds with 409 `RESOURCE_CONFLICT` and stores nothing

#### Scenario: Model and Blockbench model share a key space
- **WHEN** a project has a model asset `vulpes:item/ruby` and a Blockbench model with that key is uploaded
- **THEN** the system responds with 409 `RESOURCE_CONFLICT` and stores nothing

### Requirement: Blockbench models bring their textures
For a Blockbench model, the system SHALL store every embedded PNG texture as a texture asset of its own. Its key SHALL keep the namespace and folder of the model key and use the sanitized texture name, so model `vulpes:item/ruby` with texture `Ruby Side.png` yields `vulpes:item/ruby_side`. If any derived key is taken, or two textures derive the same key, the whole upload SHALL be rejected.

#### Scenario: Model with a texture of the same name
- **WHEN** a Blockbench model with key `vulpes:item/ruby` and an embedded texture named `ruby` is uploaded
- **THEN** the upload succeeds and stores the model `vulpes:item/ruby` and the texture `vulpes:item/ruby`

#### Scenario: Extracted texture key is taken
- **WHEN** a project has a texture `vulpes:item/ruby` and a Blockbench model `vulpes:item/gem` with an embedded texture `ruby` is uploaded
- **THEN** the system responds with 409 `RESOURCE_CONFLICT` and stores neither the model nor any texture

### Requirement: Read and download assets
The system SHALL return a single asset, and a page of the assets of a project, optionally filtered by kind. `GET /project/{projectId}/asset/{id}/content` SHALL return the stored bytes with their media type and an ETag of the SHA-1 digest, and SHALL answer 304 when `If-None-Match` matches. Browsers SHALL be able to read the `ETag` header through CORS.

#### Scenario: Cached download
- **WHEN** a client downloads an asset with `If-None-Match` set to its current ETag
- **THEN** the system responds with 304 and no body

#### Scenario: Asset of another project
- **WHEN** a client requests an asset id under a project it does not belong to
- **THEN** the system responds with 404 `RESOURCE_NOT_FOUND`

### Requirement: Replace and delete assets
`POST /project/{projectId}/asset/{id}/content` SHALL replace the bytes of an asset with a file of the same kind. Replacing a Blockbench model SHALL extract its textures again. Deleting an asset SHALL remove its stored bytes, and for a Blockbench model also its extracted textures. Deleting all assets of a project SHALL be possible in one request.

#### Scenario: Replace with another kind
- **WHEN** a texture is replaced with a `.json` file
- **THEN** the system responds with 400 `VALIDATION_FAILED` on field `file` and keeps the old content

#### Scenario: Delete a Blockbench model
- **WHEN** a Blockbench model with two extracted textures is deleted
- **THEN** the model and both textures are gone, including their stored bytes

### Requirement: Assets are independent of other project models
Assets SHALL NOT be linked to items, fonts or advancements. Saving those models SHALL NOT check whether an asset with a named key exists, and deleting an asset SHALL NOT be restricted by such names.

#### Scenario: Item names a model that has no asset
- **WHEN** an item component `minecraft:item_model` with value `"vulpes:item/ruby"` is saved in a project without assets
- **THEN** the component is saved as before

#### Scenario: Delete an asset whose key an item names
- **WHEN** an item has `minecraft:item_model` = `"vulpes:item/ruby"` and the model asset `vulpes:item/ruby` is deleted
- **THEN** the asset is deleted and the item keeps its component unchanged
