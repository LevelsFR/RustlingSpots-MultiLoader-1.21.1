# Rustling Spots v6.1

## New

- Addons can create spots at validated positions with custom definitions, lifetimes and source IDs, then query or remove them through the public API.
- Addon lifecycle callbacks report creation, interaction, expiration and removal.
- Custom definitions support an `external_only` mode, custom ambient sounds and the existing visual-family and particle systems.
- Addons can choose spacing between spots sharing an event source and whether their spots despawn early when no players are nearby.
- Custom definitions can override Pokémon and loot reward chances; addon callbacks can adjust Pokémon and item weights for each player.
- Custom spot definitions can provide an optional translation key for their origin in Pokémon and loot reward messages.
- Spot interactions are claimed once before callbacks and rewards, spectators cannot consume spots, and session runtime caches are cleared when a server stops.

## Changes

- Existing datapack definitions remain natural-spawn definitions by default and keep their current behavior.
- Event spacing checks honor the largest spacing already recorded for that source, and retained spots cannot be interacted with while their chunks are unloaded.

# Rustling Spots v6.0

## New

- Added **Underwater spots** with encounters from Pokémon that can both swim and breathe underwater. Searches trigger when players submerge, with a configurable 10% default chance on regular out-of-water attempts; bubbles mark the spot instead of a ground shadow.
- Added **End spots** with a cosmic Pokémon pool, rising End-themed particles, and a ground shadow.
- Added **Geode spots** on exposed budding amethyst, with a crystal-themed Pokémon pool and rewards, more widely spread particles, and a ground shadow.
- Expanded the default encounter pools with **Tentacool, Tentacruel, Mareanie, Toxapex, Dewpider, Araquanid, Sandygast, Palossand, Noibat, Noivern, Slakoth, Vigoroth, Slaking, Greavard, and Houndstone**.

## Changes

- **Leaf spots** can now appear in open pockets above leafy branches, with a subtle drifting spore cue.
- Underground searches now prioritize **Geode spots** before Cave spots, while allowing rare surface spots as a fallback.

## Removed

- Removed **12 Pokémon and two Hisuian forms** without Cobblemon 1.8.3 textures from the default encounter pools.
