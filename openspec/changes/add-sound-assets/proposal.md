Ships as `feat(sound): upload sound files as project assets and link them to sound sources`

## Why

A project can describe sound events and their sources, but the `.ogg` files those sources play cannot be uploaded. A designer has to place them in the resource pack by hand, and nothing tells them that a source names a file that does not exist, or that deleting a file breaks a sound.

Textures and models are already project assets with storage, revisions and live sessions. Sounds should be the same kind of asset, and a sound source that plays an uploaded file should be linked to it, so the file cannot be deleted while it is in use.

This change depends on `add-project-assets` and `add-asset-revisions`, and on a release of `vulpes-model` that adds the new enum values (see Impact).

## What Changes

- `.ogg` uploads to `POST /project/{projectId}/asset` become assets of the new kind `SOUND`, stored and served as `audio/ogg`.
- A sound SHALL be Ogg Vorbis with exactly one channel. Ogg Opus, other codecs and broken files are refused with `ASSET_INVALID_CONTENT`; stereo or multi-channel Vorbis is refused with the new violation `ASSET_SOUND_NOT_MONO`. Channel count, sample rate and duration are stored with the asset.
- Sounds form a key space of their own: the key `vulpes:ambient/wind` can exist once as a texture, once as a model and once as a sound.
- A sound source (`SoundFileSourceDTO`) gets an optional `assetId`. When it is set, the source is of type `file`, the asset must be a sound of the same project, and the source `name` must equal the asset key. The backend stores an `AssetLinkEntity` (target `SOUND_SOURCE`, role `SOUND`) between the source and the asset. Without `assetId`, a source keeps working as today, for example to name a vanilla sound file.
- Deleting a sound asset that a source is linked to is refused with `409 ASSET_IN_USE`. Deleting a source or its sound event removes its link.
- Copying a sound event keeps the links of its sources within the same project. A copy into another project drops `assetId`; the sources keep their names.
- The project content export lists the sound links with the other asset links.
- The upload limit becomes per kind: sounds up to 32 MB, all other kinds stay at 8 MB.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `project-assets`: adds the sound kind and its key space, and replaces the rule that assets are independent of other models with an exception for sound sources. The capability is introduced by `add-project-assets`.

## Non-goals

- Transcoding (for example WAV or MP3 to Ogg Vorbis) or down-mixing stereo to mono.
- Links between assets and items, fonts or advancements; those stay independent as before.
- Generating `sounds.json` or the resource pack.
- Audio preview, waveforms or loudness checks.

## Impact

- **`vulpes-model`** (released as 3.10.0): `AssetKind.SOUND("audio/ogg")`, `AssetKeySpace.SOUND` with `AssetKeySpace.of(SOUND) == SOUND`, `AssetTargetType.SOUND_SOURCE`, `AssetLinkRole.SOUND`, and nullable `channels`, `sample_rate` and `duration_ms` columns on `ProjectAssetEntity`. Revisions keep only digest and size, as for textures; a restore reads the audio values from the bytes again.
- **Code**: `AssetInspector` (Ogg Vorbis parser), `AssetViolationCode` (`ASSET_SOUND_NOT_MONO`), `AssetPersistence` (size limit per kind, delete check), `AssetLinkRepository` (write and lookup methods), `SoundFileSourceDTO` and `SoundResponseDTO` (`assetId`), `SoundServiceImpl` (link on create and update, unlink on delete), `SoundModelCopier`, `ProjectContentAssembler`, `ErrorCode` (`ASSET_IN_USE`).
- **API**: new kind `SOUND` in asset responses; optional `assetId` on sound source requests and responses; asset delete may answer `409`. Existing requests keep working unchanged.
- **Configuration**: `micronaut.server.multipart.max-file-size` rises to 32 MB, the request limit with it; the 8 MB limit for the other kinds moves into `AssetPersistence`.
- **Data**: new enum values in `project_asset.kind`, `project_asset.key_space`, `asset_link.target_type` and `asset_link.role`, and three nullable columns. See design for the check constraints.
