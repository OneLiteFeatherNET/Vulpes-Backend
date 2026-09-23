# Design

## Context

- Entities, repositories and the project scoping model come from the external `vulpes-model` library (2.3.x). Every domain entity extends `AbstractEntity` (`project` + `key`); items, fonts, sound events and dimension types own child collections (lore/enchantments/flags, chars, file sources, attributes/timelines).
- `application.yml` uses `schema-generate: CREATE_DROP` (the database is empty after every start); `application-local.yml` uses `schema-generate: NONE` + `hbm2ddl.auto: update`, so data survives restarts. The seeder has to behave sensibly in both.
- Datafaker (`libs.datafaker`, 2.7.0) is already in the version catalogue but is only a `testImplementation` dependency.
- The production artifact is built by the Micronaut Gradle plugin (optimized jar with AOT, Docker image). Anything on the `main` source set/runtime classpath ends up in that image.
- Validation bounds that the seed data has to respect live in the request DTOs (e.g. `DimensionModelDTO`: `minY` -2032..2031, `height` 16..4064, `coordinateScale` 0.00001..30000000, `monsterSpawnBlockLightLimit` 0..15). Micronaut Data's default page size is 100.

## Goals / Non-Goals

**Goals:**
- One command to get a fully populated local backend for manual UI testing.
- Seed data that is realistic for Minecraft content, deterministic, and covers edge cases listed in the spec.
- Zero footprint in the production artifact.

**Non-Goals:**
- Fixtures for automated tests (JUnit/Testcontainers) — the fixture classes may be reused later, but this change does not wire them into tests.
- A runtime HTTP endpoint for seeding/resetting.
- Seeding shared/staging environments via Helm.
- Configurable data-set sizes or multiple profiles beyond the single defined data set.

## Decisions

### D1: Startup listener gated by the `seed` environment
A singleton listening to `StartupEvent`, annotated with `@Requires(env = "seed")`, performs seeding in one transaction before the server accepts traffic.
- *Alternatives*: a separate Gradle task / `main` class (needs its own application context bootstrap and duplicates datasource config); a dev REST endpoint (only useful for automated UI tests, which are out of scope); static SQL fixtures (brittle against the external entity model and UUID ids, not realistic).
- Usage: `./gradlew run -Dmicronaut.environments=local,seed` (plus `-Dvulpes.seed.reset=true` for a reset).

### D2: Dedicated `dev` source set, only on the `run` classpath
Seeder code lives in `src/dev/java` (new source set that depends on `main` output). Its output plus `libs.datafaker` are added to the `developmentOnly` configuration of the Micronaut plugin, which is included in `./gradlew run` but not in the runtime jar, optimized jar or Docker image.
- *Alternatives*: put the seeder into `src/main` guarded by `@Requires` — simplest, but ships Datafaker and seed data in the production image and exposes a dormant data-wiping code path in prod. Rejected.
- Must be verified early (task 1) because AOT (`deduceEnvironment`, `cacheEnvironment`) and annotation processing for the extra source set are the main uncertainty. Fallback if `developmentOnly` cannot carry the source-set output: a `runSeed` `JavaExec` task that uses `main` + `dev` runtime classpaths.

### D3: Write through repositories, not services
The seeder builds entities and saves them via the `vulpes-model` repositories, attaching children through the parent's collections (cascade) or their own repositories as the model requires.
- *Why*: services map from request DTOs and enforce API semantics (e.g. ids must be null on create) — convenient but indirect, and services do not expose child-bulk creation. Repositories are faster and straightforward.
- *Guard*: the spec requires seeded records to pass API validation. A seeder self-check validates each seeded aggregate by mapping it to the corresponding request DTO and running the Micronaut `Validator` with the `Default` group, failing startup on violations.
- *Why `Default` and not `Update`*: probing the running API showed that only group-less constraints (e.g. `@Min`/`@Max` on `DimensionModelDTO`) are enforced; the `@Null(groups = {Create, Update})` markers on fields such as `material`, `key` or `provider` are not, and the API accepts and returns those values. Validating with `Update` would reject every realistic item, font, sound and notification.

### D4: Three-layer data composition
```
SeedRunner (StartupEvent, @Requires env=seed)
  |-- guard: projects exist? -> reset? -> wipe : skip
  v
DataSet
  |-- EldoriaFixtures      hand-written, themed
  |-- SkyblockFixtures     hand-written, themed, shares keys with Eldoria
  |-- EdgeCaseFixtures     boundaries, enums, empty children, bulk filler
  |-- EmptyProjectFixture
       |            uses
       v
  MinecraftCatalog (static lists of real ids)   Filler (Datafaker, fixed Random seed)
```
- Named fixtures are plain Java builders — readable and stable for bug reports.
- `MinecraftCatalog` holds curated constants: materials, enchantments with vanilla max levels, `ItemFlag` names, sound event ids, infiniburn tags, clocks/timelines, attribute defaults.
- `Filler` wraps `new Faker(Locale.ENGLISH, new Random(seed))` with the seed from `vulpes.seed.random-seed` (default constant). Used only for bulk edge-case records (names, comments, lore text).
- Enum coverage iterates `EnvironmentAttributeKey.values()`, `AttributeOperator.values()`, `Skybox.values()`, `CardinalLight.values()` so new enum constants in future model versions are covered automatically.

### D5: Idempotency and reset
- Guard: `projectRepository.count() > 0` → skip with an INFO log line naming how to reset.
- Reset: delete in dependency order (children → aggregates → projects) through repositories, then seed, all in one transaction so a failure leaves the previous state.
- The check is global ("any project"), not per seed project, which also protects manually created data (spec: "Database with user-created data").

### D6: Bulk size for pagination
Edge-case project creates 120 records for each top-level entity type (> default page size 100), with a constant defined in one place.

## Risks / Trade-offs

- [`developmentOnly` + extra source set does not work with AOT/`run`] → spike first (task 1); fallback `runSeed` JavaExec task (D2).
- [Model changes in `vulpes-model` break the seeder] → the dev source set compiles in CI (`compileDevJava` hooked into `check`), and enum coverage uses `values()`.
- [Seeded data drifts from API validation rules] → startup self-check against the request DTO validator (D3).
- [Dimension cross-field rules not encoded in DTOs (vanilla requires `minY`/`height` multiples of 16 and `minY + height <= 2032`)] → boundary values are spread over several dimension types so each record stays plausible (e.g. `minY=-2032, height=16`; `minY=-2032, height=4064`; `minY=2016, height=16`). The API does not enforce the vanilla rules, so hitting DTO extremes such as `minY=2031` is allowed in one explicitly labelled "invalid for vanilla" record; documented in the fixture.
- [Reset deletes everything, including data a developer wanted to keep] → reset only with explicit flag, and only in the `seed` environment; README warns.
- [Large single transaction on startup] → data set is small (~a few thousand rows); acceptable.

## Migration Plan

No migration: development-only addition. Rollback = remove the source set and the build wiring; production artifacts are unaffected either way.

## Open Questions

- Exact thematic content (item names, lore wording) is left to implementation and can be refined without changing specs or tasks.
