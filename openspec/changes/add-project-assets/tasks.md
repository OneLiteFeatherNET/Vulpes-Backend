# Tasks

## 1. Base the code on the prototype

- [ ] 1.1 Create the code branch from `origin/main` and bring over the prototype commits of PR #238 (object storage dependency, asset feature, CORS `ETag`) without any file under `openspec/`; verify `./gradlew compileJava compileTestJava`

## 2. Remove asset links

- [ ] 2.1 Delete the link entity, repository, services, DTOs, the `AssetLinkRole` and `AssetTargetType` enums, the `ASSET_ROLE_INVALID` code and the link routes; remove the link handling from `AssetPersistence` and the link requests from `https/asset.http`; verify compilation and that `grep -rni 'assetlink\|AssetTargetType\|ROLE_INVALID' src https` finds nothing
- [ ] 2.2 Add an integration test that deleting an asset whose key an item's `minecraft:item_model` names succeeds and leaves the component unchanged

## 3. Separate key spaces

- [ ] 3.1 Add `AssetKeySpace` (`TEXTURE`, `MODEL`) mapped from `AssetKind`; verify with a unit test that every kind maps to its key space
- [ ] 3.2 Add `keySpace` to `ProjectAssetEntity`, replace the unique constraint with `(project_id, key_space, key)`, switch the existence checks to `existsByProjectIdAndKeySpaceAndKey`, and stop seeding the extraction collision check with the model key; verify with integration tests for the four key-space scenarios of the spec

## 4. Schema

- [ ] 4.1 If Flyway is on `main`, add `V<n>__project_assets.sql` and verify the context starts with `hbm2ddl.auto: validate`; otherwise write the table DDL into the PR description for the rollout

## 5. Problem responses

- [ ] 5.1 Answer an upload larger than the multipart limit with 413 as a problem response from the `ErrorCode` catalog; verify with an integration test that also checks nothing is stored

## 6. Code standards (separate small code PR on top of the feature)

- [ ] 6.1 Add Spotless with a Java formatter, limited to the asset sources and tests; verify `./gradlew spotlessCheck` passes
- [ ] 6.2 Add Error Prone with NullAway in JSpecify mode, limited to the asset packages; annotate nullable asset fields and parameters with `org.jspecify.annotations.Nullable`; verify `./gradlew compileJava` has no NullAway findings
- [ ] 6.3 Add ArchUnit tests for the asset layers; verify they pass and fail when a controller uses an asset repository directly
- [ ] 6.4 Open the pull request titled `build(asset): check the asset code with spotless, nullaway and archunit`

## 7. Verify and open

- [ ] 7.1 Run `./gradlew build` and verify it is green, using the totals from `build/test-results/test/*.xml`
- [ ] 7.2 Send the updated asset contract (no link routes, key spaces) to the Stelaris and Blockbench plugin sessions
- [ ] 7.3 Open the code pull request titled `feat(asset): upload textures and blockbench models to projects` (replacing draft PR #238)
