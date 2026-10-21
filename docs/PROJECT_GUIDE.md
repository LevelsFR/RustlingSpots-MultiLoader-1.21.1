# Cobblemon Rustling Spots — project guide

Temporary exploration encounters inspired by Pokémon Black and White rustling grass.

Source and publication snapshot: **2026-10-21**. This guide describes the checked-out source; a download page can contain an earlier release.

## Status and official links

- [Source repository](https://github.com/LevelsFR/RustlingSpots-MultiLoader-1.21.1/tree/master) (public).
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/rustling-spots) — published project verified under LevelsFR.
- [Modrinth](https://modrinth.com/mod/cobblemon-rustling-spots) — approved project in the LevelsFR profile.

## Features in the current source

- Nine built-in spot families spanning grass, sand, water, snow, leaves, caves, flying and Nether themes.
- Pokémon, themed loot, shiny rewards, optional empty outcomes and multiple rolls.
- Server/player limits, announcements, preferences and active-spot resynchronization.
- Datapack-defined spot families, eligibility rules, pools and particles.
- Client/server Cloth Config integration and administrative diagnostics.

## Compatibility and dependencies

| Component | Value |
| --- | --- |
| Source version | `5.0.1` |
| Minecraft baseline | `1.21.1` |
| JDK | `21` |
| Active branch | `master` |

| Loader | Dependency | Declared constraint | Requirement |
| --- | --- | --- | --- |
| Fabric | `fabricloader` | `>=0.18.4` | required |
| Fabric | `minecraft` | `~1.21.1` | required |
| Fabric | `java` | `>=21` | required |
| Fabric | `architectury` | `>=13.0.8` | required |
| Fabric | `fabric-api` | `*` | required |
| Fabric | `cloth-config` | `>=${clothConfigVersion}` | required |
| Fabric | `cobblemon` | `>=${cobblemonVersion}` | required |
| Fabric | `modmenu` | `*` | suggests |
| NeoForge | `neoforge` | `[21.1,)` | required; BOTH |
| NeoForge | `minecraft` | `[1.21.1,1.21.2)` | required; BOTH |
| NeoForge | `architectury` | `[13.0.8,)` | required; BOTH |
| NeoForge | `cobblemon` | `[${cobblemonVersion},)` | required; BOTH |
| NeoForge | `cloth_config` | `[${clothConfigVersion},16)` | required; CLIENT |

The table reflects loader metadata. Build-time version pins are in [gradle.properties](../gradle.properties). A dependency used only for compilation or optional integration is not automatically required for every installation.

## Installation and first use

1. Choose a released JAR matching both the Minecraft version and loader, when this project has a release page.
2. Install the required dependencies declared for that loader. A source-only development version may require locally built dependencies.
3. Use a separate development instance for unreleased builds. Keep client-side integrations on the correct side and follow the main README for server installation.
4. Start the game/server once to create configuration, then use the project settings or documented commands.

## Command implementation reference

These are literal builder nodes found in each source file, not independent full command syntaxes. Read the builder tree for nesting, arguments and permissions.

| Source | Declared command nodes |
| --- | --- |
| [common/src/main/java/net/levelscraft7/rustlingspots/spot/RustlingSpotCommands.java](../common/src/main/java/net/levelscraft7/rustlingspots/spot/RustlingSpotCommands.java) | `rustlingspots`, `messages`, `on`, `off`, `pokemon`, `loot`, `empty`, `spawn`, `spawnshiny`, `reload`, `stats`, `scan`, `debugspawn` |

## Configuration and saved data

| Configuration implementation | File names or path literals |
| --- | --- |
| [common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsClientConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsClientConfig.java) | `rustlingspots/rustlingspots-client.json` |
| [common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsFamilySpawnConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsFamilySpawnConfig.java) | `rustlingspots/rustlingspots-families.json` |
| [common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsPokemonConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsPokemonConfig.java) | `rustlingspots/rustlingspots-pokemon.json` |
| [common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsServerConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsServerConfig.java) | `rustlingspots/rustlingspots-server.json` |
| [common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsSoundConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsSoundConfig.java) | `rustlingspots/rustlingspots-sound.json` |
| [common/src/main/java/net/levelscraft7/rustlingspots/spot/RustlingSpotFamilyConfigService.java](../common/src/main/java/net/levelscraft7/rustlingspots/spot/RustlingSpotFamilyConfigService.java) | `.json` |

### Selected source defaults

These values are field initializers from the current configuration classes. Nested sections, validation, migrations and server synchronization can affect the final effective value.

[common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsClientConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsClientConfig.java)

- `shadow_opacity = 0.5D`
- `water_shadow_opacity = 0.8D`
- `show_pokemon_messages = true`
- `show_loot_messages = false`
- `show_empty_spot_messages = true`

[common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsPokemonConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsPokemonConfig.java)

- `enable = true`
- `encounter_chance = 0.35D`
- `default_shiny_chance = 0.05D`
- `min_level = 5`
- `max_level = 75`
- `typed_spawn_rules = true`

[common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsServerConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsServerConfig.java)

- `enabled = true`
- `enable_logging = false`
- `min_distance_between_spots = 16`
- `spot_lifetime_ticks = 6000`
- `interaction_radius = 2.0D`
- `interaction_vertical_allowance = 3.0D`
- `player_spot_radius = 200`
- `max_spots_per_player = 8`
- `max_spots_total = 64`
- `allow_cobblemon_raid_den_dimensions = true`
- `shiny_spot_chance = 0.0025D`
- `announce_shiny_finds_globally = true`
- `enable_empty_spots = true`
- `empty_spot_chance = 0.02D`
- `enable_multiple_reward_rolls = false`
- `min_reward_rolls = 1`
- `max_reward_rolls = 2`
- `allow_multiple_pokemon_per_spot = false`
- `mixed_reward_spot_chance = 0.01D`

[common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsSoundConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsSoundConfig.java)

- `pokemon_spawn_volume = 0.4D`
- `item_reward_volume = 0.4D`

Keep world data and player data with the matching world backup. Use the mod-specific reload/configuration route documented in the main README; changing an arbitrary file does not imply live reloading. Do not copy development-world data into the source repository.

## Source map

| Directory | Responsibility |
| --- | --- |
| [common](../common) | Shared behavior and resources; loader-independent contracts where the architecture provides them. |
| [fabric](../fabric) | Fabric entrypoints, metadata and platform integration. |
| [neoforge](../neoforge) | NeoForge entrypoints, metadata and platform integration. |
| [gradle](../gradle) | Committed wrapper and build configuration. |

Metadata: [fabric/src/main/resources/fabric.mod.json](../fabric/src/main/resources/fabric.mod.json), [neoforge/src/main/resources/META-INF/neoforge.mods.toml](../neoforge/src/main/resources/META-INF/neoforge.mods.toml).

| Package | Entry points to inspect |
| --- | --- |
| `net/levelscraft7/rustlingspots/api` | [common/src/main/java/net/levelscraft7/rustlingspots/api/RustlingSpotAddonApi.java](../common/src/main/java/net/levelscraft7/rustlingspots/api/RustlingSpotAddonApi.java), [common/src/main/java/net/levelscraft7/rustlingspots/api/RustlingSpotInteractionCallback.java](../common/src/main/java/net/levelscraft7/rustlingspots/api/RustlingSpotInteractionCallback.java), [common/src/main/java/net/levelscraft7/rustlingspots/api/RustlingSpotInteractionEvent.java](../common/src/main/java/net/levelscraft7/rustlingspots/api/RustlingSpotInteractionEvent.java), [common/src/main/java/net/levelscraft7/rustlingspots/api/RustlingSpotInteractionResult.java](../common/src/main/java/net/levelscraft7/rustlingspots/api/RustlingSpotInteractionResult.java) |
| `net/levelscraft7/rustlingspots/compat` | [common/src/main/java/net/levelscraft7/rustlingspots/compat/CobblemonRaidDensCompat.java](../common/src/main/java/net/levelscraft7/rustlingspots/compat/CobblemonRaidDensCompat.java) |
| `net/levelscraft7/rustlingspots/config` | [common/src/main/java/net/levelscraft7/rustlingspots/config/ConfigIO.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/ConfigIO.java), [common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingServerConfigSnapshot.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingServerConfigSnapshot.java), [common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsClientConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsClientConfig.java), [common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsFamilySpawnConfig.java](../common/src/main/java/net/levelscraft7/rustlingspots/config/RustlingSpotsFamilySpawnConfig.java) (additional classes in the same package) |
| `net/levelscraft7/rustlingspots/configscreen` | [common/src/main/java/net/levelscraft7/rustlingspots/configscreen/RustlingConfigClientState.java](../common/src/main/java/net/levelscraft7/rustlingspots/configscreen/RustlingConfigClientState.java), [common/src/main/java/net/levelscraft7/rustlingspots/configscreen/RustlingSpotsClothConfigScreen.java](../common/src/main/java/net/levelscraft7/rustlingspots/configscreen/RustlingSpotsClothConfigScreen.java) |
| `net/levelscraft7/rustlingspots/network` | [common/src/main/java/net/levelscraft7/rustlingspots/network/RustlingConfigNetworkService.java](../common/src/main/java/net/levelscraft7/rustlingspots/network/RustlingConfigNetworkService.java), [fabric/src/main/java/net/levelscraft7/rustlingspots/network/RustlingSpotsNetwork.java](../fabric/src/main/java/net/levelscraft7/rustlingspots/network/RustlingSpotsNetwork.java), [neoforge/src/main/java/net/levelscraft7/rustlingspots/network/RustlingSpotsNetwork.java](../neoforge/src/main/java/net/levelscraft7/rustlingspots/network/RustlingSpotsNetwork.java) |
| `net/levelscraft7/rustlingspots/network/packet` | [common/src/main/java/net/levelscraft7/rustlingspots/network/packet/RustlingConfigRequestC2SPacket.java](../common/src/main/java/net/levelscraft7/rustlingspots/network/packet/RustlingConfigRequestC2SPacket.java), [common/src/main/java/net/levelscraft7/rustlingspots/network/packet/RustlingConfigSnapshotS2CPacket.java](../common/src/main/java/net/levelscraft7/rustlingspots/network/packet/RustlingConfigSnapshotS2CPacket.java), [common/src/main/java/net/levelscraft7/rustlingspots/network/packet/RustlingConfigUpdateC2SPacket.java](../common/src/main/java/net/levelscraft7/rustlingspots/network/packet/RustlingConfigUpdateC2SPacket.java), [common/src/main/java/net/levelscraft7/rustlingspots/network/packet/RustlingMessagePreferencesC2SPacket.java](../common/src/main/java/net/levelscraft7/rustlingspots/network/packet/RustlingMessagePreferencesC2SPacket.java) (additional classes in the same package) |

## Build from source

Run commands from the repository root using a matching JDK and the committed Gradle wrapper.

```powershell
.\gradlew.bat :fabric:build :neoforge:build
```

Loader JARs are normally written under the corresponding `build/libs` directory. Aggregate projects document their collected artifact layout in the main README or build helper.

## Documentation index

- [CHANGELOG-FABRIC.md](../CHANGELOG-FABRIC.md)
- [CHANGELOG-NEOFORGE.md](../CHANGELOG-NEOFORGE.md)
- [README.md](../README.md)
- [docs/custom-spot-definitions-guide-en.md](../docs/custom-spot-definitions-guide-en.md)
- [docs/custom-spot-definitions-guide.md](../docs/custom-spot-definitions-guide.md)
- [docs/rustling-spots-wiki-fr.md](../docs/rustling-spots-wiki-fr.md)
- [docs/rustling-spots-wiki.md](../docs/rustling-spots-wiki.md)

## Validation and reporting a problem

A source audit, successful compilation, automated checks and in-game validation are different forms of evidence. This documentation update alone does not establish runtime compatibility. Preserve the project-specific remaining release gates.

When reporting a problem, include:

- Exact mod version, Minecraft version, loader and dependency versions.
- Client, integrated server or dedicated-server context.
- Minimal reproduction steps and the relevant log/stack trace.
- Configuration or datapack excerpts required to reproduce it, with credentials and personal data removed.

Before a release, compile every affected loader and configured version, inspect packaged metadata/resources, and perform the relevant client and dedicated-server checks. Rendering, interactions, networking and persistence need real runtime validation.

## License and attribution

See [LICENSE](../LICENSE). Retain upstream notices and third-party asset attribution.
