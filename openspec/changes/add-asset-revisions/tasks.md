# Tasks

## 1. Failing tests first

- [ ] 1.1 Unit test in `AssetStorageKeyTest`: a revision key differs from the legacy key of its asset and does not lie below it
- [ ] 1.2 Integration tests in `AssetRevisionIntegrationTest`: creating an asset makes revision 1; replacing it makes revision 2 and the content serves the new bytes; the list is newest first and marks the current revision; a revision's content answers with its bytes and its own ETag; restoring creates a new revision with the old bytes and a new ETag; restoring with a stale `If-Match` answers 412 and creates no revision; a legacy asset gets revision 1 on first listing and keeps its bytes; deleting an asset removes its revisions and their bytes; a revision of another asset answers 404; a comment longer than 500 characters answers 400; restoring a Blockbench model extracts its textures again

## 2. Implementation

- [ ] 2.1 Add `ProjectAssetRevisionEntity` and `ProjectAssetRevisionRepository`; verify the context starts with `hbm2ddl.auto: update` and creates `project_asset_revision`
- [ ] 2.2 Add `AssetStorage.revisionKeyFor` and the revision write path in `AssetPersistence` (create, replace, restore, lazy backfill, delete of revisions); replacement stops writing the legacy key
- [ ] 2.3 Add `comment` to `AssetUploadDTO`, read the optional `comment` form field on upload and replacement, add `ASSET_COMMENT_TOO_LONG`
- [ ] 2.4 Add `AssetRevisionResponseDTO`, the revision listing, the revision content and the restore to `AssetService` and `AssetServiceImpl`
- [ ] 2.5 Add the three routes to `AssetController` with their OpenAPI annotations; the restore answers 200 with the asset and its new `ETag`

## 3. Verification

- [ ] 3.1 Run the affected test classes with `./gradlew test --tests '<pattern>' --max-workers=2`; no full suite
- [ ] 3.2 Run `openspec validate add-asset-revisions --strict`

## 4. Delivery

- [ ] 4.1 Open the code pull request under the title `feat(asset): reject stale replacements and keep asset revisions`, from a branch based on the main branch that contains the merged project-assets and replace-precondition features
