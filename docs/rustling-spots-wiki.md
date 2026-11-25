# Rustling Spots Wiki

> **Documentation target:** Rustling Spots **v6.1** for Minecraft **1.21.1** and Cobblemon **1.8.0+**.
>
> Français: [rustling-spots-wiki-fr.md](./rustling-spots-wiki-fr.md)

---

## Overview

**Rustling Spots** is a Cobblemon exploration mod inspired by the rustling grass system from **Pokémon Black & White**.

It adds temporary dynamic spots around players while they explore. Spots are represented directly in the world with visual effects and can trigger a **Cobblemon encounter**, **themed loot**, a **shiny reward**, or an optional **empty result** depending on configuration.

Rustling Spots does **not** modify world generation, making it safe to add to existing worlds.

---

## Main Features

### Dynamic exploration events

- Temporary spots appear around players during exploration
- Visual particles help players notice nearby spots
- Pokémon encounters use Cobblemon
- Loot can be themed by spot family
- Spots are temporary and disappear after interaction or lifecycle cleanup
- Addons can create spots at chosen positions and receive lifecycle events
- External-only custom definitions stay out of natural spawning until an addon triggers them
- Existing worlds do not need to be regenerated

### Built-in spot families

Rustling Spots includes **12 built-in families**. Each family has its own visual identity, surface rules, particles, Pokémon pools and loot pools.

| Family | Typical default surface | Theme |
| --- | --- | --- |
| **Grass** | `grass_block`, `dirt_path`, small flowers | General outdoor encounters |
| **Sand** | `sand`, `red_sand` | Desert species, fossils and dry loot |
| **Water** | Source water with open space above | Aquatic Pokémon and water rewards |
| **Underwater** | Submerged source water at any depth | Underwater-capable Pokémon, marked by rising bubbles |
| **Snow** | `snow`, `snow_block`, `powder_snow` | Ice-themed encounters and rewards |
| **Leaves** | Blocks tagged `#leaves`, including clear pockets in tree canopies | Forest encounters, canopy spots and a subtle spore cue |
| **Cave** | Stone or deepslate cave surfaces | Underground encounters and mining loot |
| **Geode** | Exposed budding amethyst inside underground geodes | Crystal-themed encounters with amethyst chimes and vanilla particle sparkles |
| **Flying** | Open air beneath the sky | Flying encounters and high-altitude themes |
| **NetherFlamme** | `netherrack` or source lava | Fire and Nether-themed rewards |
| **SoulFlame** | `soul_sand`, `soul_soil` | Dark, spectral and soul-themed encounters |
| **End** | End Stone, End Stone Bricks and Purpur in the vanilla End | Cosmic encounters with rising vanilla portal particles and End Rod sparkles |

Each family can be enabled, disabled or rebalanced through configuration.

Underwater searches always run when a player's eyes are submerged. They also run on 10% of regular spawn attempts by default while the player is out of water; server owners can tune this chance from 0 to 100% in General settings. If no underwater position is available, normal canopy, surface and cave searches continue. The spot position can use any submerged source-water block in range, without a fixed depth band. Its default pool contains 126 Pokémon with underwater breathing and swimming assets verified against Cobblemon 1.8.3, including all 64 amphibious species. Underwater spots render bubbles instead of the flat shadow used by surface spots.

End spots are restricted to the vanilla End dimension. They can rest on End Stone, End Stone Bricks or Purpur blocks when there is safe ground beneath them; otherwise, normal surface checks reject the position above the void. Their animation reuses Grass's two rising pulses with a stronger upward drift for vanilla Enderman teleport (`PORTAL`), chorus-like reverse portal (`REVERSE_PORTAL`), dragon-breath (`DRAGON_BREATH`) and End Rod particles. End spots render the same soft ground shadow as the other spot families.

Geode spots are found in underground Overworld geodes on exposed Budding Amethyst with room above it. This unmovable vanilla block anchors the spot to generated geodes. Their animation reuses Grass's two rising pulses with vanilla enchanting-table particles, plus a small number of Witch and End Rod accents spread around the spot. The family uses its own mineral-themed Pokémon pool, amethyst chimes and amethyst-themed rewards, and renders the same soft ground shadow as the other spot families.

When a player explores underground in the Overworld, the mod searches for a geode on every spawn attempt and prioritizes a Geode spot when one is in range. Cave spots are the fallback when no suitable geode is found. Surface and canopy spots remain possible as an occasional fallback (10% of underground searches).

---

## Gameplay Flow

A typical interaction works like this:

1. A Rustling Spot appears near a player during exploration.
2. Particles indicate the spot position.
3. The player approaches and interacts with it.
4. The spot resolves into a Cobblemon encounter, themed loot, an empty result, or another configured reward outcome.
5. The spot is consumed and removed.

The lifecycle also includes server-side limits and cleanup:

- spots have a limited lifetime
- spots disappear after interaction
- inactive spots can be removed when players are no longer nearby
- per-player and global limits prevent uncontrolled accumulation
- since v4.3, active spots are resynchronized after login, respawn and dimension changes

### Important default values

| Setting | Default |
| --- | --- |
| Player spot radius | `200` blocks |
| Minimum distance between spots | `16` blocks |
| Maximum spots per player | `8` |
| Maximum spots on the server | `64` |
| Spot lifetime | `6000` ticks, about 5 minutes |
| Interaction radius | `2` blocks |
| Vertical interaction allowance | `3` blocks |

---

## Rewards and Variants

A spot is not limited to a simple Pokémon spawn.

### Possible outcomes

- **Cobblemon encounter**
- **Themed loot**
- **Empty spot** when enabled
- **Shiny spot** with optional global announcement

### Important reward defaults

| Option | Default |
| --- | --- |
| Shiny spot chance | `0.0025` or 0.25% |
| Global shiny discovery announcement | `true` |
| Empty spots enabled | `true` |
| Empty spot chance | `0.02` or 2% |
| Pokémon encounter chance | `0.35` or 35% |
| Pokémon minimum level | `5` |
| Pokémon maximum level | `75` |
| Default Pokémon shiny chance | `0.01` or 1% |
| Multiple reward rolls | disabled by default |

A shiny spot forces a shiny reward outcome where supported and can announce the discovery to the server when global announcements are enabled.

---

## Configuration

All normal settings are now available exclusively through the in-game **Cloth Config** screen.

- On Fabric, install Mod Menu and open **Rustling Spots > Configure**.
- On NeoForge, open **Mods > Rustling Spots > Config**.
- Client preferences are always editable.
- Server settings are editable only by the local world owner or a level 2 operator.
- Saving applies server settings immediately.

The screen contains Client, General, Pokémon, Rewards and Sounds, and Spot Families categories. Cloth Config validates numeric ranges before saving, and the server validates every submitted value again.

Existing v4.x JSON settings are loaded and preserved automatically during the upgrade. They remain on disk only as the mod's persistence format; editing them manually is no longer the supported workflow. Pokémon pools, loot pools, surface/biome data and custom datapack spots remain advanced data-driven content rather than normal settings.

### Safer config recovery in v4.3

Starting with v4.3, invalid legacy JSON configuration files are backed up with an `.invalid.bak` suffix before valid defaults are regenerated.

This prevents malformed files from being silently overwritten without leaving a copy for diagnosis.

### Default family spawn multipliers

- `grass`: `1.0`
- `sand`: `1.0`
- `water`: `0.6`
- `snow`: `1.0`
- `leaves`: `0.7`
- `cave`: `1.0`
- `flying`: `0.25`
- `netherflamme`: `0.6`
- `soulflame`: `1.0`

### Player message preferences

Players can control which Rustling Spots messages they receive, including:

- Pokémon encounter messages
- loot messages
- empty spot messages

---

## Commands

### Player commands

```mcfunction
/rustlingspots messages
/rustlingspots messages on
/rustlingspots messages off
/rustlingspots messages pokemon on
/rustlingspots messages loot off
/rustlingspots messages empty on
```

### Admin and debug commands

```mcfunction
/rustlingspots spawn grass
/rustlingspots spawn rustlingspots:grass
/rustlingspots spawn <namespace:spot_id>
/rustlingspots spawnshiny rustlingspots:grass
/rustlingspots reload
/rustlingspots stats
/rustlingspots stats <player>
/rustlingspots scan 64
/rustlingspots scan 64 grass
```

Useful notes:

- `spawn` accepts built-in families such as `grass` and `water`, plus complete custom spot IDs
- `spawnshiny` forces a shiny spot for testing
- `reload` reloads configs, family rules, loot pools, Pokémon pools and custom spot definitions
- `stats` exposes Rustling Spots statistics
- `scan` helps admins inspect active spots in a nearby radius
- v4.3 fixed **Total Spots** statistics so multiple rewards from one spot are no longer counted as multiple spots

---

## Custom Spots via Datapacks

Rustling Spots includes a data-driven custom spot system. Servers and modpacks can create new spot definitions without writing a Java addon and without changing world generation.

A custom datapack can define:

- custom spot IDs
- dimensions
- biomes
- blocks and surface rules
- selection priority
- selection weight
- display names
- Pokémon families
- loot families
- weighted particles
- natural or external-only spawn mode
- optional registered ambient sound
- shiny options
- visual family

### Visual family, particles and sounds

Since v4.3, custom spots can use the optional `visual_family` field to select the built-in rendering base that best matches the spot.

Supported values are:

```text
grass, sand, water, underwater, snow, leaves, cave, geode, end,
netherflamme, soulflame, flying
```

If `visual_family` is missing or invalid, the custom spot keeps the historical `grass` visual fallback.

Custom particles can still be used to give a datapack spot its own stronger identity.

The optional `spawn_mode` field accepts `natural` or `external_only`. Existing definitions without this field remain natural. The `ambient_sound` field can select a registered sound event; missing client registrations fall back to the visual family's sound.

## Addon API

The shared Java API creates a spot from a loaded custom definition at an exact block position and accepts an independent lifetime and optional source ID. Addons can query active spots by dimension, remove spots through the normal remove packet, and listen for `CREATED`, `INTERACTED`, `EXPIRED` and `REMOVED` events. Creation checks that the target chunk is loaded, validates the position against the definition, rejects nearby duplicates and respects the configured global cap.

```java
RustlingSpotAddonApi.createSpot(level, position, definitionId, 20 * 60, sourceId);
RustlingSpotAddonApi.registerLifecycleCallback(event -> handle(event));
```

For event-scoped clusters, pass `RustlingSpotSpawnOptions` with a same-source spacing and `despawnWhenNoPlayers` policy. When early despawn is disabled, the spot stays registered through chunk unload until its lifetime expires. Definitions may also specify `reward_chances`; reward weight callbacks can adjust Pokémon or item entries for a player. Interactions are claimed once before callbacks and reward resolution, and spectators cannot activate spots. Session-owned spots, cooldowns, preferences and datapack caches clear when the server stops; mod-level addon listeners remain registered. The existing interaction callback remains compatible and can return `PASS` to keep the normal Pokémon and loot rewards. See the [Addon API Guide](./addon-api-guide-en.md) and the [external-only example](../example_datapacks/external_only_spot_pack).

### Datapack structure

The custom system uses three main data folders:

```text
<your_datapack>/
|- pack.mcmeta
`- data/
   `- <namespace>/
      `- rustling_spots/
         |- spot_definitions/
         |- pokemon_families/
         `- loot_families/
```

### Full guides

- [Custom Spot Definitions Guide, English](./custom-spot-definitions-guide-en.md)
- [Custom Spot Definitions Guide, Français](./custom-spot-definitions-guide.md)

A ready-to-use example datapack is included in:

```text
example_datapacks/custom_swamp_spot_pack
```

The example contains a custom spot, a custom Pokémon family, a custom loot family and mixed particle usage.

An [external-only example datapack](../example_datapacks/external_only_spot_pack) demonstrates an addon-triggered definition.
The corresponding [Java addon example](../examples/java/RustlingSpotsAddonExample.java) shows creation and callbacks.

Official example datapack downloads:

- [CurseForge - Rustling Spots Example Addon Datapack](https://www.curseforge.com/minecraft/data-packs/rustling-spots-example-addon-datapack)
- [Modrinth - Rustling Spots Addon Datapack](https://modrinth.com/datapack/rustling-spots-addon-datapack)

---

## Official Add-ons

### Rustling Spots: Team Rocket

**Rustling Spots: Team Rocket** is the official dedicated Team Rocket add-on for Rustling Spots, bringing Team Rocket-themed encounters into the Rustling Spots gameplay loop. It requires the main Rustling Spots mod.

- [CurseForge - Rustling Spots: Team Rocket](https://www.curseforge.com/minecraft/mc-mods/rs-teamrocket-addon)
- [Modrinth - Rustling Spots: Team Rocket](https://modrinth.com/mod/rs-teamrocket-addon)

---

## Compatibility and World Safety

- No world generation changes
- Safe for existing worlds
- Fabric and NeoForge builds are maintained from the same multi-loader source tree
- Spots can work across dimensions when their family rules or custom definitions match
- Cobblemon Raid Dens dimension support can be allowed or disabled through configuration
- Custom datapack spots are data-driven and do not require extra Java code

---

## v5.1 Documentation Notes

The v5.1 documentation reflects the current source and changelogs, including:

- a 1% default Pokémon shiny chance and one-time migration of the old 5% shiny setting
- shared Fabric/NeoForge Pokemon creation through Cobblemon's property API
- namespaced addon species and Cobblemon property/form support
- Fakemon registered by Cobblemon addons are supported through their namespaced species IDs
- pool revalidation after Cobblemon species reloads, including species supplied by addons/datapacks
- v4.x pool preservation, historical compatibility aliases and deduplicated invalid-entry warnings
- malformed pool entries are skipped individually with file and entry diagnostics, leaving other valid entries available
- authoritative Rustling level and shiny rolls with no natural spawn-cap usage
- custom `visual_family` support
- historical `grass` fallback when `visual_family` is missing or invalid
- improved spot synchronization after login, respawn and dimension changes
- `.invalid.bak` backup for malformed JSON configuration files
- corrected Total Spots statistics with multiple reward rolls
- corrected custom spot documentation for the supported shiny chance field

For the unified release history across Fabric and NeoForge, see the [Rustling Spots changelog](../CHANGELOG.md).

---

## Short Summary

**Rustling Spots** brings temporary Gen 5-inspired exploration events to Cobblemon.

Players discover visible spots in the world and interact with them to trigger Pokémon encounters or themed rewards. With **12 built-in families**, JSON configuration, admin tools, player statistics, shiny and empty spots, and a complete datapack system for custom definitions, the mod adds a configurable exploration loop without modifying world generation.
