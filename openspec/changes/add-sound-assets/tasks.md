# Tasks

## 0. Model release

- [x] 0.1 In `vulpes-model`: add `AssetKind.SOUND("audio/ogg")`, `AssetKeySpace.SOUND` (mapped in `AssetKeySpace.of`), `AssetTargetType.SOUND_SOURCE`, `AssetLinkRole.SOUND`, and nullable `channels`, `sampleRate`, `durationMs` on `ProjectAssetEntity`; released as 3.10.0
- [ ] 0.2 Bump `vulpes.model` in `settings.gradle.kts`

## 1. Failing tests first

- [ ] 1.1 Unit tests in `AssetInspectorTest` with fixtures: mono Vorbis accepted with channels, sample rate and duration; stereo Vorbis answers `ASSET_SOUND_NOT_MONO`; Opus, truncated Ogg and a PNG renamed to `.ogg` answer `ASSET_INVALID_CONTENT`
- [ ] 1.2 Integration tests for the asset endpoints: upload a sound (201, kind `SOUND`, `audio/ogg` on download); sound and texture share a key; 20 MB sound accepted, 20 MB PNG answers 413; replacing a sound with a stereo file keeps the content
- [ ] 1.3 Integration tests for the sound source endpoints: link on create; mismatch, wrong kind and other project refused; vanilla name without `assetId` unchanged; update without `assetId` removes the link
- [ ] 1.4 Integration tests for delete and copy: linked sound answers 409 `ASSET_IN_USE`; deleting the source, then the sound succeeds; deleting the sound event removes the links; delete-all of a project's assets succeeds with links; copy within the project keeps the link, into another project drops it
- [ ] 1.5 Extend the project content export test with a sound link

## 2. Implementation

- [ ] 2.1 Ogg Vorbis parsing and the `ogg` case in `AssetInspector`; `channels`, `sampleRate` and `durationMs` in `Inspection`; `ASSET_SOUND_NOT_MONO` in `AssetViolationCode`
- [ ] 2.2 Size limit per kind in `AssetPersistence`; raise the multipart limits in `application.yml`; store the new fields on the asset; add them to `AssetResponseDTO`
- [ ] 2.3 Write and lookup methods on `AssetLinkRepository` (by target, by asset and role, delete by targets)
- [ ] 2.4 `assetId` on `SoundFileSourceDTO` and `SoundResponseDTO.SoundFileSourceDTO`; link handling and the violation codes `SOUND_SOURCE_ASSET_MISMATCH` and `SOUND_SOURCE_ASSET_INVALID` in `SoundServiceImpl`; unlink on source and sound event delete
- [ ] 2.5 `ASSET_IN_USE` in `ErrorCode` and the delete check in `AssetPersistence`; delete links first in delete-all
- [ ] 2.6 Copy links in `SoundModelCopier` within the same project
- [ ] 2.7 `assetId` on sources in the project content export
- [ ] 2.8 OpenAPI annotations: kind `SOUND`, `assetId`, `409` on asset delete

## 3. Verification

- [ ] 3.1 Check the generated check constraints with `hbm2ddl update` on a database created before this change; write the DDL of the migration plan
- [ ] 3.2 Run the affected test classes with `./gradlew test --tests '<pattern>' --max-workers=2`; no full suite
- [ ] 3.3 Run `openspec validate add-sound-assets --strict`

## 4. Delivery

- [ ] 4.1 Open the code pull request under the title `feat(sound): upload sound files as project assets and link them to sound sources`
