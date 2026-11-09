# Spec Delta

## ADDED Requirements

### Requirement: Sound assets are mono Ogg Vorbis
The system SHALL accept a `.ogg` upload as an asset of kind `SOUND`, stored and served as `audio/ogg`, only if it is an Ogg Vorbis stream with exactly one channel. It SHALL store the channel count and sample rate of the sound, and its duration in milliseconds when it can be read.

#### Scenario: Upload a mono sound
- **WHEN** a mono Ogg Vorbis file `wind.ogg` is uploaded with key `vulpes:ambient/wind`
- **THEN** the system responds with 201 and returns the asset with kind `SOUND`, one channel, its sample rate, duration, size and SHA-1

#### Scenario: Stereo sound
- **WHEN** a stereo Ogg Vorbis file `wind.ogg` is uploaded
- **THEN** the system responds with 400 `VALIDATION_FAILED` and a violation `ASSET_SOUND_NOT_MONO` on field `file`, and stores nothing

#### Scenario: Ogg Opus
- **WHEN** an Ogg Opus file `wind.ogg` is uploaded
- **THEN** the system responds with 400 `VALIDATION_FAILED` and a violation `ASSET_INVALID_CONTENT` on field `file`, and stores nothing

#### Scenario: Not an Ogg file
- **WHEN** a PNG renamed to `wind.ogg`, or a truncated Ogg file, is uploaded
- **THEN** the system responds with 400 `VALIDATION_FAILED` and a violation `ASSET_INVALID_CONTENT` on field `file`, and stores nothing

#### Scenario: Replace a sound with a stereo file
- **WHEN** the content of a sound asset is replaced with a stereo Ogg Vorbis file
- **THEN** the system responds with 400 `VALIDATION_FAILED` with `ASSET_SOUND_NOT_MONO` on field `file`, keeps the old content and creates no revision

### Requirement: Upload size depends on the kind
The system SHALL accept sound files up to 32 MB and files of every other kind up to 8 MB. A larger file SHALL be refused with 413 as a problem response (`application/problem+json`), and nothing SHALL be stored.

#### Scenario: Large sound
- **WHEN** a valid mono Ogg Vorbis file of 20 MB is uploaded
- **THEN** the system responds with 201

#### Scenario: Large texture
- **WHEN** a valid PNG of 20 MB is uploaded
- **THEN** the system responds with 413 as a problem response and stores nothing

### Requirement: A sound source can play an uploaded sound
A sound source SHALL accept an optional `assetId`. When it is set, the asset SHALL be a `SOUND` asset of the project of the sound event, the source SHALL be of type `file`, and its `name` SHALL equal the asset key. The system SHALL then keep exactly one link between the source and the asset, and SHALL return `assetId` with the source. Without `assetId` the source SHALL be saved as before and SHALL have no link.

#### Scenario: Link a source to a sound
- **WHEN** a source of type `file` with name `vulpes:ambient/wind` and the `assetId` of the sound `vulpes:ambient/wind` is created
- **THEN** the source is saved, linked to the asset, and returned with that `assetId`

#### Scenario: Name does not match the asset
- **WHEN** a source with name `vulpes:ambient/rain` and the `assetId` of the sound `vulpes:ambient/wind` is saved
- **THEN** the system responds with 400 `VALIDATION_FAILED` and a violation `SOUND_SOURCE_ASSET_MISMATCH` on field `assetId`, and saves nothing

#### Scenario: Asset is not a sound
- **WHEN** a source is saved with the `assetId` of a texture
- **THEN** the system responds with 400 `VALIDATION_FAILED` and a violation `SOUND_SOURCE_ASSET_INVALID` on field `assetId`

#### Scenario: Asset of another project
- **WHEN** a source is saved with the `assetId` of a sound of another project
- **THEN** the system responds with 404 `RESOURCE_NOT_FOUND`

#### Scenario: Vanilla sound without asset
- **WHEN** a source of type `file` with name `minecraft:ambient/cave/cave1` and no `assetId` is saved
- **THEN** the source is saved as before and has no link

#### Scenario: Remove the link on update
- **WHEN** a linked source is updated without `assetId`
- **THEN** the source has no link, and the asset can be deleted

### Requirement: A sound in use cannot be deleted
The system SHALL refuse to delete a sound asset that a sound source is linked to with `409 ASSET_IN_USE`. Deleting a sound source or a sound event SHALL delete the links of its sources. Deleting all assets of a project SHALL delete their links first.

#### Scenario: Delete a linked sound
- **WHEN** a sound asset is deleted while a source is linked to it
- **THEN** the system responds with 409 `ASSET_IN_USE`, and the asset, its bytes and the link remain

#### Scenario: Delete the source first
- **WHEN** the only source linked to a sound is deleted, and then the sound is deleted
- **THEN** both deletes succeed

#### Scenario: Delete the sound event
- **WHEN** a sound event whose sources are linked to sounds is deleted
- **THEN** the links are gone and the sounds can be deleted

### Requirement: Copying a sound event keeps links within the project
Copying a sound event with its sources into the same project SHALL link each copied source to the asset of its original. Copying it into another project SHALL copy the sources without links.

#### Scenario: Copy within the project
- **WHEN** a sound event with a linked source is copied into the same project
- **THEN** the copied source is linked to the same asset

#### Scenario: Copy into another project
- **WHEN** a sound event with a linked source is copied into another project
- **THEN** the copied source keeps its name and type and has no `assetId`

## MODIFIED Requirements

### Requirement: Asset keys are unique per key space
The system SHALL keep asset keys unique per project within a key space. Textures form one key space. Models and Blockbench models form the second. Sounds form the third. The same key MAY exist once in each key space.

#### Scenario: Texture and model share a key
- **WHEN** a project has a model asset `vulpes:item/ruby` and a texture `vulpes:item/ruby` is uploaded
- **THEN** the upload succeeds with 201

#### Scenario: Sound and texture share a key
- **WHEN** a project has a texture `vulpes:ambient/wind` and a sound `vulpes:ambient/wind` is uploaded
- **THEN** the upload succeeds with 201

#### Scenario: Duplicate key in the same key space
- **WHEN** a project has a texture `vulpes:item/ruby` and another texture with that key is uploaded
- **THEN** the system responds with 409 `RESOURCE_CONFLICT` and stores nothing

#### Scenario: Model and Blockbench model share a key space
- **WHEN** a project has a model asset `vulpes:item/ruby` and a Blockbench model with that key is uploaded
- **THEN** the system responds with 409 `RESOURCE_CONFLICT` and stores nothing

### Requirement: Assets are independent of other project models
Assets SHALL NOT be linked to items, fonts or advancements. Saving those models SHALL NOT check whether an asset with a named key exists, and deleting an asset SHALL NOT be restricted by such names. Sound sources are the only exception, through an explicit `assetId` (see `A sound source can play an uploaded sound`).

#### Scenario: Item names a model that has no asset
- **WHEN** an item component `minecraft:item_model` with value `"vulpes:item/ruby"` is saved in a project without assets
- **THEN** the component is saved as before

#### Scenario: Delete an asset whose key an item names
- **WHEN** an item has `minecraft:item_model` = `"vulpes:item/ruby"` and the model asset `vulpes:item/ruby` is deleted
- **THEN** the asset is deleted and the item keeps its component unchanged
