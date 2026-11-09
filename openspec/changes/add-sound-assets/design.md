# Design

## Context

- `AssetInspector.inspect` derives the kind from the extension and validates the content; `AssetPersistence` stores bytes through `AssetStorage` and writes asset and revision rows.
- `AssetLinkEntity` (`asset_link`) exists in `vulpes-model` with a unique constraint on `(asset_id, target_type, target_id)` and `ON DELETE CASCADE` on the asset. The backend only reads it today, for the project content export. `target_id` is not a foreign key.
- `SoundFileSource` (`sound_data`) has a free `name` and a `type` (`file` or `event`) and belongs to a `SoundEventEntity`, which belongs to a project. The sound endpoints live under `/sound/{id}/sources`.
- In `sounds.json` a source of type `file` names `namespace:path`, resolved to `assets/<namespace>/sounds/<path>.ogg`. That is the asset key of the sound.
- Minecraft only plays Ogg Vorbis. A sound with more than one channel is not attenuated by distance.
- Deployed environments run `hbm2ddl.auto: none`; development runs `update`.

## Goals / Non-Goals

**Goals:**
- Sound files are assets with the same storage, revisions and live sessions as textures.
- Files the client cannot play, or plays without attenuation, are refused on upload.
- A source that plays an uploaded file is linked to it, and the file cannot be deleted while it is in use.

**Non-Goals:** transcoding, down-mixing, pack generation, links for items, fonts or advancements.

## Decisions

### D1. Validation of `.ogg`
`AssetInspector` reads the first Ogg page and accepts the file only if:
1. it starts with the capture pattern `OggS` and has the beginning-of-stream flag;
2. the first packet is a Vorbis identification header (`0x01` `vorbis`) with version 0;
3. the channel count in that header is 1, otherwise `ASSET_SOUND_NOT_MONO` on field `file`.

An Opus stream (`OpusHead`) or anything else answers `ASSET_INVALID_CONTENT` with the message `must be an Ogg Vorbis file`. The sample rate comes from the identification header. The duration is the granule position of the last page divided by the sample rate; if it cannot be read, it stays `null` and the upload is still accepted. The parser is written by hand on the byte array; it reads the header and the last page only and needs no audio library.

### D2. Key space
`AssetKeySpace.SOUND` is the third key space. `existsByProjectIdAndKeySpaceAndKey` works unchanged. The key rules of `Asset keys are resource locations` apply as they are; the `sounds/` folder is not part of the key, as for textures.

### D3. Size limit per kind
Micronaut has one multipart limit per server. It rises to 32 MB (`33554432`) and the request limit to 33 MB. `AssetPersistence` checks the size after inspection: sounds up to 32 MB, all other kinds up to 8 MB. Too large answers `413` as a problem response, as today. Revisions keep every version, so large sounds grow the store faster; this is accepted (non-goal of `add-asset-revisions`).

### D4. Link from a source to its sound
`SoundFileSourceDTO` gets an optional `assetId`. On create and update with `assetId`:
- the asset must exist in the project of the sound event, otherwise `404 RESOURCE_NOT_FOUND` on field `assetId`;
- its kind must be `SOUND`, otherwise `400 VALIDATION_FAILED` with `SOUND_SOURCE_ASSET_INVALID` on `assetId`;
- `type` must be `file` and `name` must equal the asset key, otherwise `400` with `SOUND_SOURCE_ASSET_MISMATCH`.

The service then keeps exactly one link with `target_type = SOUND_SOURCE`, `target_id = <source id>` and `role = SOUND`: an update replaces the asset of an existing link, or removes the link when `assetId` is absent. The response carries `assetId`, read from the link. Rejecting a mismatched `name` instead of overwriting it keeps the request explicit; the web UI fills `name` from the chosen asset.

Alternative considered: resolve `name` against the `SOUND` key space on save, without a link row. Rejected, because the delete check and the export would then have to match strings, and a renamed or replaced key would silently break the reference.

### D5. Delete
- Deleting an asset with at least one link of role `SOUND` answers `409 ASSET_IN_USE`, naming the number of linked sources in `detail`. Deleting all assets of a project deletes the links first, because the whole project content goes away.
- Deleting a source (`deleteLinkedSource`) deletes its link in the same transaction. Deleting a sound event deletes the links of its sources first, because `target_id` has no foreign key and the database cascade on `sound_data` does not reach `asset_link`.

### D6. Copy
`SoundModelCopier.copySources` copies the link with each source when the target project is the source project. Into another project the asset does not exist, so the copied source keeps `name` and `type` and gets no link.

### D7. Export
`ProjectContentAssembler` already groups `asset_link` rows by asset. Sound links appear there with target type `SOUND_SOURCE`; the source entries in the export carry `assetId`.

## Risks / Trade-offs

- [Stereo music is common] → Refused on purpose: positional sounds need mono. Music tracks have to be down-mixed before the upload. If streamed, non-positional music needs stereo later, a per-source exception can be added without changing the stored data.
- [32 MB per file with unlimited revisions] → Accepted; pruning stays a non-goal.
- [Hand-written Ogg parser] → Small scope (identification header and last page), covered by fixtures for mono and stereo Vorbis, Opus, truncated files and a PNG renamed to `.ogg`.
- [Links without a foreign key on the target] → Every path that deletes sources or sound events deletes the links first (D5); a test covers each path.

## Migration Plan

- `vulpes-model` release with the new enum values and columns first, then bump `vulpes.model` in `settings.gradle.kts`.
- Hibernate 6 creates check constraints for `@Enumerated(EnumType.STRING)` columns, and `hbm2ddl update` does not change them. Existing databases need the constraints on `project_asset.kind`, `project_asset.key_space`, `asset_link.target_type` and `asset_link.role` dropped and recreated with the new values, plus the three new nullable columns. With Flyway (`add-flyway-migrations`) this is one `V<n>__sound_assets.sql`; without it the DDL goes into the rollout, also for development databases created before this change.
- Existing sound sources have no `assetId`; nothing is backfilled. A source whose `name` happens to match an uploaded sound is not linked automatically.
- Rollback: delete `SOUND` assets and `SOUND_SOURCE` links, restore the old constraints, lower the multipart limit.

## Open Questions

- Is 32 MB the right limit for sounds?
- Does `add-flyway-migrations` land before this change?
