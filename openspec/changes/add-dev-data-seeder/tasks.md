# Tasks

## 1. Build wiring (spike first)

- [x] 1.1 Add a `dev` source set (`src/dev/java`, depends on `main` output, Micronaut annotation processors) and put its output plus `libs.datafaker` on the `developmentOnly` configuration; verify with a trivial `@Requires(env = "seed")` bean that logs on startup: it logs under `./gradlew run -Dmicronaut.environments=local,seed` and not without `seed`
- [x] 1.2 If 1.1 fails with AOT/`run`, implement the fallback `runSeed` `JavaExec` task from design D2 instead; verify the same log behaviour via `./gradlew runSeed` — not needed, 1.1 succeeded
- [x] 1.3 Hook `compileDevJava` into `check`; verify `./gradlew check` compiles the dev source set
- [x] 1.4 Verify that the production artifacts exclude the seeder: `./gradlew optimizedJitJarAll dockerBuild` (or the tasks CI uses) succeeds, and neither the jar nor the image layers contain `net/datafaker` or seeder classes (`unzip -l` / layer listing)

## 2. Seeding infrastructure

- [x] 2.1 Implement the seed runner (StartupEvent listener, `@Requires(env = "seed")`, single transaction) with the "projects exist → skip + INFO log" guard; verify that starting twice against the `local` database keeps entity counts unchanged and logs the skip message
- [x] 2.2 Implement reset (`vulpes.seed.reset=true`): delete children → aggregates → projects via repositories, then seed; verify after manually editing/deleting seeded records that a reset restart restores exactly the seed set
- [x] 2.3 Implement `Filler` (Datafaker with fixed `Random` seed from `vulpes.seed.random-seed`, default constant); verify two instances with the same seed produce identical sequences (unit test in `src/test` or dev-source-set test)
- [x] 2.4 Implement `MinecraftCatalog` with curated materials, enchantments (with vanilla max level), item flags, sound event ids, infiniburn tags, clocks and timeline keys; verify by review that all values are valid vanilla identifiers

## 3. Fixture sets

- [x] 3.1 Implement "Eldoria RPG" (`labor=false`): themed items (enchantments, `§`-coloured lore, flags), icon font with chars, quest/ambient sound events with file sources, custom attributes, notifications, "Schattenreich" dimension with attributes and timelines; verify via the API that each list endpoint of the project returns records and each aggregate type has children
- [x] 3.2 Implement "Skyblock Lab" (`labor=true`) with at least one shared `key` per entity type with Eldoria; verify via the API that the shared-key records are distinct with different content
- [x] 3.3 Implement "Edge Cases": 120 records per top-level entity type, aggregates with empty child collections, all enum values via `values()`, DTO min/max boundary values spread over several dimension types, long/Unicode/`§` texts, long lore and font char lists, unsafe enchantments above vanilla max; verify pagination (`totalPages > 1` for default page size) and enum coverage via the API
- [x] 3.4 Implement the empty project; verify every list endpoint for it returns an empty page

## 4. Validation guard

- [x] 4.1 Add the startup self-check that maps each seeded aggregate to its request DTO and validates it with the `Update` group, failing startup on violations; verify by temporarily injecting an invalid value (e.g. `height=8`) that startup fails with a clear message
- [x] 4.2 Verify determinism: seed two fresh databases (restart with `vulpes.seed.reset=true`) and compare `GET` item lists of "Eldoria RPG" — keys, names and materials are identical

## 5. Documentation and final check

- [x] 5.1 Add a README section "Seed data for manual testing" (start command, reset flag, warning that reset wipes all data, overview of the seeded projects); verify the documented commands work as written
- [ ] 5.2 End-to-end check: start the frontend against a freshly seeded backend and click through each entity screen of all four projects without errors
