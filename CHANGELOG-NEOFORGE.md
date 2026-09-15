# Rustling Spots NeoForge changelog

## v5.0

- Replaced the old custom config screen with Cloth Config and exposed every regular setting, including all family spawn rates.
- Added permission-checked in-game server configuration: level 2 operators can save and apply settings immediately on dedicated servers.
- Preserved existing v4.x JSON values as the internal migration and persistence source.
- Updated the required Cobblemon version to 1.8.0+ for Minecraft 1.21.1.
- Moved encounter creation to the shared Cobblemon `PokemonProperties` implementation used by Fabric.
- Removed the manual species/form splitter and preserved namespaced addon species identifiers.
- Made Rustling's level and shiny result authoritative, including explicit non-shiny encounters.
- Rustling encounters continue not to count toward Cobblemon's natural spawn cap.
- Added safe species and explicit-form validation without overwriting custom pools.
- Revalidates config and datapack pools after Cobblemon finishes rebuilding its species registry.
- Deduplicates invalid pool-entry warnings per source entry until the next reload cycle.
- Preserved v4.x pool files and historical species aliases during the 1.8 transition.
- Kept Alpha unset and retained Cobblemon 1.8's intrinsic size initialization.

## v4.3

## New

- Added **visual_family** support for custom datapack spots.

## Changes

- Improved **spot synchronization** after joining, respawning, or changing dimensions.
- Invalid **JSON configs** are now backed up before defaults are restored.
- Custom spots without a valid **visual_family** still use the historical grass fallback.

## Fixes

- Fixed existing **spots** sometimes becoming invisible after joining, respawning, or changing dimensions.
- Fixed **Total Spots** counting multiple rewards from one spot as multiple spots.
- Fixed invalid **configuration files** being silently overwritten.
- Fixed outdated **custom spot documentation** for the supported shiny chance field.
- Fixed localized **origin messages** sometimes starting with lowercase text in chat.
