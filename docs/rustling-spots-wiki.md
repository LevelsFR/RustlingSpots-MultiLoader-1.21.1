# Rustling Spots Wiki

> **Documentation target:** Rustling Spots **v4.3** for Minecraft **1.21.1**.
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
- Existing worlds do not need to be regenerated

### Built-in spot families

Rustling Spots includes **9 built-in families**. Each family has its own visual identity, surface rules, particles, Pokémon pools and loot pools.

| Family | Typical default surface | Theme |
| --- | --- | --- |
| **Grass** | `grass_block`, `dirt_path`, small flowers | General outdoor encounters |
| **Sand** | `sand`, `red_sand` | Desert species, fossils and dry loot |
| **Water** | Source water with open space above | Aquatic Pokémon and water rewards |
| **Snow** | `snow`, `snow_block`, `powder_snow` | Ice-themed encounters and rewards |
| **Leaves** | Blocks tagged `#leaves` | Forest encounters and lightweight spots |
| **Cave** | Stone or deepslate cave surfaces | Underground encounters and mining loot |
| **Flying** | Open air beneath the sky | Flying encounters and high-altitude themes |
| **NetherFlamme** | `netherrack` or source lava | Fire and Nether-themed rewards |
| **SoulFlame** | `soul_sand`, `soul_soil` | Dark, spectral and soul-themed encounters |

Each family can be enabled, disabled or rebalanced through configuration.

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
| Default Pokémon shiny chance | `0.05` or 5% |
| Multiple reward rolls | disabled by default |

A shiny spot forces a shiny reward outcome where supported and can announce the discovery to the server when global announcements are enabled.

---

## Configuration

Rustling Spots stores its main JSON configuration under:

```text
config/rustlingspots/
```

| File | Purpose |
| --- | --- |
| `rustlingspots-server.json` | Global activation, radius, lifetime, limits, shiny settings, empty spots and reward behavior |
| `rustlingspots-pokemon.json` | Pokémon encounter chance, levels and spawn rules |
| `rustlingspots-client.json` | Client display options and message preferences |
| `rustlingspots-sound.json` | Reward sound volumes |
| `rustlingspots-families.json` | Spawn multipliers for built-in families |

### Safer config recovery in v4.3

Starting with v4.3, invalid JSON configuration files are backed up with an `.invalid.bak` suffix before valid defaults are regenerated.

This prevents malformed files from being silently overwritten without leaving a copy for diagnosis.

### Default family spawn multipliers

- `grass`: `1.0`
- `sand`: `1.0`
- `water`: `1.0`
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
- shiny options
- visual family

### `visual_family` in v4.3

Since v4.3, custom spots can use the optional `visual_family` field to select the built-in rendering base that best matches the spot.

Supported values are:

```text
grass
sand
water
snow
leaves
cave
netherflamme
soulflame
flying
```

If `visual_family` is missing or invalid, the custom spot keeps the historical `grass` visual fallback.

Custom particles can still be used to give a datapack spot its own stronger identity.

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

## v4.3 Documentation Notes

The v4.3 documentation reflects the current source and changelogs, including:

- custom `visual_family` support
- historical `grass` fallback when `visual_family` is missing or invalid
- improved spot synchronization after login, respawn and dimension changes
- `.invalid.bak` backup for malformed JSON configuration files
- corrected Total Spots statistics with multiple reward rolls
- corrected custom spot documentation for the supported shiny chance field

For loader-specific release notes:

- [Fabric changelog](../CHANGELOG-FABRIC.md)
- [NeoForge changelog](../CHANGELOG-NEOFORGE.md)

---

## Short Summary

**Rustling Spots** brings temporary Gen 5-inspired exploration events to Cobblemon.

Players discover visible spots in the world and interact with them to trigger Pokémon encounters or themed rewards. With **9 built-in families**, JSON configuration, admin tools, player statistics, shiny and empty spots, and a complete datapack system for custom definitions, the mod adds a configurable exploration loop without modifying world generation.
