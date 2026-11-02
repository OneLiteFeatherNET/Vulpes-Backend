# Tasks

## 1. Failing tests first

- [ ] 1.1 Unit tests in `LiveSessionRegistryTest` with an adjustable clock: create, refresh keeps the start instant, conflict with another active token, expired session is replaced, release with matching and mismatching token, release without session, expiry removes only expired sessions
- [ ] 1.2 Unit tests in `AssetEventHubTest`: a `saved` event reaches the subscriber of its asset, other assets receive nothing, the quoted ETag and revision are carried, publishing without subscribers succeeds
- [ ] 1.3 Unit test in `LiveSessionSweeperTest`: a sweep after expiry emits `ended` with the token and host; a sweep before expiry emits nothing
- [ ] 1.4 Integration tests in `LiveSessionControllerIntegrationTest`: GET answers 404 without a session and for an unknown asset; PUT answers 200 with the body shape and refreshes; PUT with another active token answers 409; PUT with a malformed token or host answers 400 `VALIDATION_FAILED`; DELETE answers 204 with the matching token and then GET answers 404; DELETE with another token answers 409
- [ ] 1.5 Integration test in `LiveSessionEventsIntegrationTest`: a replacement emits `saved` with the new `ETag` and revision; starting and deleting a session emit `session` events; the stream of an unknown asset answers 404

## 2. Implementation

- [ ] 2.1 Add the request and response records, the `Clock` bean and the registry with its TTL
- [ ] 2.2 Add `LiveSessionService` and its implementation, and `LiveSessionController` with the four routes and their OpenAPI annotations
- [ ] 2.3 Add `AssetEventHub`, `AssetContentSaved`, the sweeper, and publish `AssetContentSaved` inside `AssetPersistence` create, replace and restore
- [ ] 2.4 Document the single-replica limitation in the controller and the hub javadoc

## 3. Verification

- [ ] 3.1 Run the affected test classes with `./gradlew test --tests '<pattern>' --max-workers=2`; no full suite
- [ ] 3.2 Run `openspec validate add-live-sessions --strict`

## 4. Delivery

- [ ] 4.1 Open the code pull request under the title `feat(session): register live edit sessions and push asset events`, from a branch based on the main branch that contains the merged `add-asset-revisions` proposal and this proposal
