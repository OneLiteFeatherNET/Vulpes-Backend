# Tasks

## 1. Failing tests first

- [ ] 1.1 Integration tests in `AssetControllerIntegrationTest`: a replace with a matching `If-Match` answers 200 and sends the new `ETag`; a stale `If-Match` answers 412 with the current `ETag` and leaves the content unchanged; a replace without `If-Match` answers 200; `If-Match: *` answers 200; weak and malformed values answer 412; the upload response carries the `ETag` of the stored bytes
- [ ] 1.2 Unit test in `ApiExceptionHandlerTest`: an asset precondition exception produces a 412 whose response carries the `ETag` header

## 2. Implementation

- [ ] 2.1 Add `ErrorCode.ASSET_PRECONDITION_FAILED` (412)
- [ ] 2.2 Let `ApiException` carry an optional current ETag and let `ApiExceptionHandler` write it as the `ETag` header
- [ ] 2.3 Add a locking finder to `ProjectAssetRepository`; in `AssetPersistence.replaceContent` take the lock first, then compare `If-Match` against the stored SHA-1 before any write
- [ ] 2.4 Thread the `If-Match` value through `AssetService` and `AssetServiceImpl`
- [ ] 2.5 In `AssetController`, read `If-Match` on replace, add the `412` to the OpenAPI annotations, and send `ETag` on upload and replace responses

## 3. Verification

- [ ] 3.1 Run the affected test classes with `./gradlew test --tests '<pattern>' --max-workers=2`; no full suite
- [ ] 3.2 Run `openspec validate add-asset-replace-precondition --strict`

## 4. Delivery

- [ ] 4.1 Open the code pull request under the title `feat(asset): reject stale asset replacements with if-match`, from a branch based on the main branch that contains the merged project-assets feature
