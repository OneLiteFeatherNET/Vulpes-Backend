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

## 5. Verify and open

- [ ] 5.1 Run `./gradlew build` and verify it is green, using the totals from `build/test-results/test/*.xml`
- [ ] 5.2 Send the updated asset contract (no link routes, key spaces) to the Stelaris and Blockbench plugin sessions
- [ ] 5.3 Open the code pull request titled `feat(asset): upload textures and blockbench models to projects` (replacing draft PR #238)
