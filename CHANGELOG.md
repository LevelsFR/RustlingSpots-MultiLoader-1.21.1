# Rustling Spots v6.1

## New

- Addons can create spots at validated positions with custom definitions, lifetimes and source IDs, then query or remove them through the public API.
- Addon lifecycle callbacks report creation, interaction, expiration and removal.
- Custom definitions support an `external_only` mode, custom ambient sounds and the existing visual-family and particle systems.

## Changes

- Existing datapack definitions remain natural-spawn definitions by default and keep their current behavior.

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
