## 1. Failing tests first

- [ ] 1.1 Unit tests in `ContentRevisionTest`: object keys are sorted at every depth, no whitespace, the `revision` field is excluded, equal content gives equal hashes, a changed nested value changes the hash
- [ ] 1.2 Unit tests in `EntityTagMatcherTest`: `*`, quoted, weak, list, unquoted, missing and stale `If-None-Match` values
- [ ] 1.3 Unit tests in `ProjectContentAssemblerTest` with in-memory rows: children are grouped under their parent, every list is in the specified order with `id` tie-break, insertion order does not change the result, timestamps are absent
- [ ] 1.4 Integration test in `ProjectContentControllerIntegrationTest`: a project with every content type is exported completely; another project's content stays out; an empty project has empty lists; an unknown project answers `404`; two exports of unchanged content are byte-identical; a lore change changes the revision; `ETag` equals the revision; a matching `If-None-Match` answers `304`, a stale one `200`
- [ ] 1.5 Integration test that the export transaction is read-only and runs with isolation `repeatable read`

## 2. Implementation

- [ ] 2.1 Add the response records under `domain/content`
- [ ] 2.2 Add the project-wide read queries for item components, lore and enchantments, font chars, sound sources, dimension attributes and timelines, asset revisions, and an asset link repository
- [ ] 2.3 Add `ProjectContentService` with a `@Transactional(readOnly = true, isolation = REPEATABLE_READ)` export method, the assembler that groups and orders, and the canonical revision
- [ ] 2.4 Add `ProjectContentController` with `GET /project/{projectId}/content`, `ETag` and `304` handling, and its OpenAPI annotations

## 3. Verification

- [ ] 3.1 Run the affected test classes with `./gradlew test --tests '<pattern>' --max-workers=2`; no full suite
- [ ] 3.2 Run `openspec validate add-project-content-export --strict`

## 4. Delivery

- [ ] 4.1 Open the code pull request under the title `feat(project): export the complete content of a project in one request`, from a branch based on the main branch that contains this merged proposal
