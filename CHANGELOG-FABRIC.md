# Rustling Spots Fabric v4.3

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
