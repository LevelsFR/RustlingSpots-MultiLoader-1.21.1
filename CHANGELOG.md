# Rustling Spots Changelog

## v5.1

### New

- Added precise pool-entry diagnostics for configuration and datapack Pokémon pools.
- Added a soft, one-time terrain sound as players approach each spot and a family-matched particle burst when they activate it.

### Changes

- Reduced the default Pokémon shiny chance from 5% to 1%.
- Migrates an unchanged 5% default shiny setting to 1% once, while preserving customized rates and datapack overrides.
- Documented support for Fakemon registered by Cobblemon addons and referenced by namespaced species ID.

### Fixes

- Invalid pool entries are skipped individually, preserving valid entries in the same file.

## v5.0.1

### Fixes

- Form validation now accepts matching Cobblemon form names or Showdown IDs despite differences in spaces, dashes, and underscores, improving compatibility with addon forms.

## v5.0

### New

- Replaced the old custom config screen with Cloth Config and exposed regular settings, including family spawn rates.
- Added permission-checked in-game server configuration for level 2 operators, with changes applied immediately on dedicated servers.
- Added Cobblemon species and form validation for pools, including registered namespaced addon species such as Fakemon.
- Revalidates configuration and datapack pools after Cobblemon rebuilds its species registry.

### Changes

- Updated the required Cobblemon version to 1.8.0+ for Minecraft 1.21.1.
- Preserved existing v4.x JSON settings, pool files, and historical species aliases during migration.
- Uses Cobblemon's shared `PokemonProperties` entity API to create encounters.
- Rustling Spots applies the encounter's level and shiny result explicitly; Rustling encounters do not count toward Cobblemon's natural spawn cap.
- Keeps Alpha unset and uses Cobblemon 1.8's intrinsic size initialization.

### Fixes

- Invalid species and forms are skipped with source-entry warnings instead of being left to fail during spawning.
- Deduplicates invalid pool-entry warnings until the next reload cycle.

### Removed

- Removed command-based Pokémon spawning and the manual species/form splitter.

## v4.3

### New

- Added `visual_family` support for custom datapack spots.

### Changes

- Custom spots without a valid `visual_family` retain the historical grass fallback.
- Improved spot synchronization after joining, respawning, or changing dimensions.

### Fixes

- Fixed spots sometimes becoming invisible after joining, respawning, or changing dimensions.
- Fixed Total Spots counting multiple rewards from one spot as multiple spots.
- Invalid JSON configuration files are backed up before defaults are restored instead of being silently overwritten.
- Fixed custom spot documentation for the supported shiny chance field and localized origin messages starting with lowercase text.
