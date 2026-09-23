# Proposal

## Why

Manual UI testing of the Vulpes frontend currently starts from an empty database: every tester has to create projects, items, fonts, sounds, dimensions and their child records by hand before a single screen can be checked. That is slow, inconsistent between testers, and misses edge cases (pagination, empty states, enum coverage, limits) that are tedious to set up manually. A development-only seeder that fills the local database with realistic, Minecraft-flavoured data lets every developer exercise all features of the UI immediately.

## What Changes

- Add a development-only data seeder that runs on application startup when the Micronaut environment `seed` is active, and is completely inert otherwise.
- Seed a curated, deterministic data set:
  - **Themed projects** ("Eldoria RPG", "Skyblock Lab") that look like real Vulpes usage: items with enchantments, coloured lore and flags; an icon font with chars; sound events with file sources; custom attributes; notifications; a fully configured custom dimension with attributes and timelines.
  - **An edge-case project** that covers pagination (more than one page per entity type), empty child collections, every enum value, validation boundary values, long and Unicode/`§`-formatted texts, and lore/font lines suited for reordering.
  - **An empty project** for empty-state screens.
- Use a curated catalogue of real Minecraft identifiers (materials, enchantments, item flags, sound keys, dimension settings) so the UI shows plausible values; use Datafaker with a fixed seed only for free-text filler.
- Seed only when the database contains no projects; support an explicit reset (`vulpes.seed.reset=true`) that wipes all Vulpes data and seeds again.
- Keep the seeder and Datafaker out of the production runtime artifact (Docker image / optimized jar).

## Capabilities

### New Capabilities
- `dev-data-seeding`: Development-only population of the database with a deterministic, realistic data set for manual UI testing, including activation, idempotency/reset behaviour and required data coverage.

### Modified Capabilities
<!-- None: no existing specs; REST API behaviour is unchanged. -->

## Impact

- **Code**: new seeder components (runner, fixture sets, Minecraft catalogue, filler generator) in a dedicated development-only source location; no changes to controllers, services or DTOs.
- **Build**: `build.gradle.kts` gains a development-only source set / dependency wiring so that `./gradlew run` can include the seeder and Datafaker while production jars and the Docker image do not.
- **Dependencies**: `net.datafaker:datafaker` (already in the version catalogue, currently test-only) becomes available to the development run classpath.
- **Configuration**: new optional properties `vulpes.seed.reset` (and possibly `vulpes.seed.random-seed`); new environment name `seed`. Default and production configuration are unaffected.
- **Docs**: README section on how to start the backend with seed data.
- **Data**: writes through the `vulpes-model` repositories; relies on the entity model of `vulpes-model` 2.3.x.
