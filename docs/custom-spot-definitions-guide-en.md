# Rustling Spots - Custom Spot Definitions

A complete presentation of the custom rustling spot datapack system for `Rustling Spots 1.21.1`.

---

## Example datapack status

Yes, the example datapack is ready and complete for the format currently supported by the mod.

The `example_datapacks/custom_swamp_spot_pack` folder already includes:

- a valid `pack.mcmeta` for `1.21.1`
- a complete custom spot
- a custom Pokemon family
- a custom loot family
- an installation `README.txt`
- an example of mixed particles
- a directly testable example with commands

In short: it is a solid base to duplicate when creating your own spots.

> Important:
> the example datapack is complete for the current system, but it does not demonstrate every advanced option available. For example, it does not show `shiny`, `shinyChance`, or `families` inside Pokemon entries. This guide below also covers those cases.

---

## What this system does

This system lets you create custom rustling spots through JSON files, without addon code, without custom models, without a scripting layer, and without a separate API.

You can customize:

- where a spot can appear
- which dimensions it can use
- which biomes it can use
- which blocks it can use
- which Pokemon family it uses
- which loot family it uses
- which display name it shows
- which particles it plays
- its spawn priority and selection weight

What stays shared with the core mod:

- the overall rustling spot behavior
- the spot lifecycle
- the interaction flow
- the visual base selected through `visual_family`

## Visual family

Each custom spot can choose a visual family with the optional `visual_family` field.

This field controls the base rendering and fallback particles:

- supported values: `grass`, `sand`, `water`, `snow`, `leaves`, `cave`, `netherflamme`, `soulflame`, `flying`
- if the field is missing or invalid, the spot keeps the historical `grass` fallback
- custom particles remain the best way to give a spot a strong identity

The datapack format does not create a completely new rendering system, but it can now use the visual family that best matches the spot theme.

---

## Installing the example datapack

Place the folder here:

```text
world/datapacks/custom_swamp_spot_pack
```

Then in game:

```mcfunction
/reload
/rustlingspots spawn rustlingspots:swamp_custom
```

The included `pack.mcmeta` is:

```json
{
  "pack": {
    "description": "Rustling Spots example datapack: custom swamp spot",
    "pack_format": 48,
    "supported_formats": {
      "min_inclusive": 48,
      "max_inclusive": 48
    }
  }
}
```

---

## Custom datapack structure

The system is built around 3 file groups:

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

### Recognized folders

- `data/<namespace>/rustling_spots/spot_definitions/*.json`
  Role: defines the custom spots themselves.

- `data/<namespace>/rustling_spots/pokemon_families/*.json`
  Role: defines reusable Pokemon pools.

- `data/<namespace>/rustling_spots/loot_families/*.json`
  Role: defines reusable loot pools.

### Namespace

The `namespace` is the first part of an identifier.

Examples:

- `rustlingspots:swamp_custom`
- `mymodpack:haunted_forest`
- `myserver:red_canyon`

In `mymodpack:haunted_forest`:

- namespace = `mymodpack`
- path = `haunted_forest`

The example datapack uses `rustlingspots`, but you can use your own namespace.

---

## Files included in the example

The shipped example datapack contains exactly this:

```text
example_datapacks/custom_swamp_spot_pack/
|- pack.mcmeta
|- README.txt
`- data/
   `- rustlingspots/
      `- rustling_spots/
         |- spot_definitions/
         |  `- swamp.json
         |- pokemon_families/
         |  `- swamp.json
         `- loot_families/
            `- swamp.json
```

Its spot identifier is:

```text
rustlingspots:swamp_custom
```

---

## How the game chooses a spot

When the mod tries to spawn a rustling spot:

1. it first checks the built-in spot that would match the block
2. it then adds all datapack custom spots matching that location
3. it keeps only the candidates with the highest `priority`
4. it picks one final candidate among those using `weight`

## Practical consequence

- a custom spot with `priority: 10` will win over a normal built-in spot
- a custom spot with `priority: 0` can coexist with built-ins
- `weight` only matters between spots with the same priority

## Built-in priority

Built-in spots use:

- `priority = 0`
- `weight = 1`

So if you want your custom spot to naturally override the normal behavior in its area, you will usually want a priority above `0`.

---

## The `spot_definitions` file

Each custom spot is defined in a JSON file located in:

```text
data/<namespace>/rustling_spots/spot_definitions/<name>.json
```

### Full example

```json
{
  "format_version": 1,
  "id": "rustlingspots:swamp_custom",
  "display_name": "Swamp Spot",
  "priority": 10,
  "weight": 5,
  "visual_family": "leaves",
  "dimensions": [
    "minecraft:overworld"
  ],
  "biomes": [
    "minecraft:swamp",
    "minecraft:mangrove_swamp",
    "#minecraft:is_forest"
  ],
  "blocks": [
    "minecraft:mud",
    "minecraft:grass_block",
    "#minecraft:dirt"
  ],
  "pokemon_family": "rustlingspots:swamp",
  "loot_family": "rustlingspots:swamp",
  "particles": [
    {
      "type": "rustlingspots:grass_burst",
      "weight": 7
    },
    {
      "type": "minecraft:happy_villager",
      "weight": 3
    }
  ]
}
```

### Supported fields

| Field | Required | Description |
| --- | --- | --- |
| `format_version` | Yes | Format version. Currently only `1` is accepted. |
| `id` | Yes | Full custom spot identifier. |
| `display_name` | No | Readable name shown in messages and debug output. |
| `visual_family` | No | Visual family used for rendering and fallback particles. If missing or invalid, the spot keeps the historical `grass` fallback. |
| `priority` | Yes | Priority used when multiple spots match the same location. |
| `weight` | Yes | Selection weight between spots of the same priority. Must be > `0`. |
| `dimensions` | No | Allowed dimensions list. If missing or invalid, the spot is restricted to the Overworld. |
| `biomes` | Yes | Allowed biome list or biome tags. |
| `blocks` | Yes | Allowed block list or block tags for the ground block. |
| `pokemon_family` | Yes | Pokemon family used by the spot. |
| `loot_family` | Yes | Loot family used by the spot. |
| `particles` | No | Weighted particle list. If missing or empty, the spot falls back to default grass particles. |

### Accepted values for `biomes` and `blocks`

You can mix:

- direct IDs
- tags prefixed with `#`

Valid examples:

```json
"biomes": [
  "minecraft:swamp",
  "#minecraft:is_forest"
]
```

```json
"blocks": [
  "minecraft:mud",
  "#minecraft:dirt"
]
```

### Exact rules for the spot file

- `format_version` must be `1`
- `weight` must be strictly positive
- `biomes` cannot be empty
- `blocks` cannot be empty
- `pokemon_family` must exist, otherwise the definition is ignored
- `loot_family` may be unknown, but in that case the spot falls back to global loot
- `display_name` is optional
- `visual_family` is optional and accepts `grass`, `sand`, `water`, `snow`, `leaves`, `cave`, `netherflamme`, `soulflame` or `flying`
- `dimensions` is optional
- `particles` is optional

### Important fallbacks

- missing `dimensions` -> Overworld only
- missing or invalid `visual_family` -> grass visual family
- missing `particles` -> default grass particles
- present `particles` but all invalid -> default grass particles
- unknown `loot_family` -> global loot
- unknown `pokemon_family` -> the custom spot is not loaded

### Reserved IDs

The mod's internal IDs cannot be overridden by a datapack.

These IDs are reserved:

```text
rustlingspots:grass
rustlingspots:sand
rustlingspots:water
rustlingspots:snow
rustlingspots:leaves
rustlingspots:cave
rustlingspots:netherflamme
rustlingspots:soulflame
rustlingspots:flying
```

If you use one of these IDs in a custom spot, it will be rejected.

---

## The `pokemon_families` file

Pokemon are not defined directly in the spot file.
The spot points to a Pokemon family.

Location:

```text
data/<namespace>/rustling_spots/pokemon_families/<name>.json
```

### Example from the datapack

```json
[
  { "species": "wooper", "weight": 10, "min_level": 1, "max_level": 18 },
  { "species": "paldean-wooper", "weight": 10, "min_level": 1, "max_level": 18 },
  { "species": "lotad", "weight": 9, "min_level": 1, "max_level": 20 },
  { "species": "surskit", "weight": 9, "min_level": 1, "max_level": 20 },
  { "species": "croagunk", "weight": 8, "min_level": 8, "max_level": 24 },
  { "species": "stunfisk", "weight": 6, "min_level": 12, "max_level": 28 },
  { "species": "marshtomp", "weight": 3, "min_level": 18, "max_level": 36 },
  { "species": "quagsire", "weight": 3, "min_level": 18, "max_level": 36 },
  { "species": "swampert", "weight": 1, "min_level": 36, "max_level": 55 }
]
```

### Supported fields per Pokemon entry

| Field | Required | Description |
| --- | --- | --- |
| `species` | Yes | Cobblemon species to spawn. |
| `weight` | Yes | Selection weight. Must be > `0`. |
| `min_level` | No | Minimum possible level. |
| `max_level` | No | Maximum possible level. |
| `shiny` | No | If present, forces or forbids shiny state for that entry. |
| `shinyChance` | No | Entry-specific shiny chance. Note: the supported field name is `shinyChance` in camelCase. |
| `families` | No | Advanced list of families this entry belongs to. |

### Important note about `shinyChance`

The recognized JSON field is:

```json
"shinyChance": 0.15
```

The `shiny_chance` field is not read for Pokemon pool entries. Always use `shinyChance`.

### Advanced example

```json
[
  {
    "species": "gastly",
    "weight": 8,
    "min_level": 12,
    "max_level": 28,
    "shinyChance": 0.10
  },
  {
    "species": "mimikyu",
    "weight": 1,
    "min_level": 35,
    "max_level": 55,
    "shiny": true
  }
]
```

### What `families` is for

Inside a datapack family file, you usually do not need to define it.

If you omit `families`, the mod automatically treats the entry as belonging to the family represented by the file itself.

Example:

```text
data/rustlingspots/rustling_spots/pokemon_families/swamp.json
```

automatically gives the family:

```text
rustlingspots:swamp
```

### Linking it from the spot

If your file is:

```text
data/rustlingspots/rustling_spots/pokemon_families/swamp.json
```

then the spot should point to:

```json
"pokemon_family": "rustlingspots:swamp"
```

### Useful rules

- entries with `weight <= 0` are ignored
- empty species values are ignored
- levels are bounded by the mod config at spawn time
- if no valid entry exists for the family, no Pokemon will spawn

---

## The `loot_families` file

Loot follows the same logic: the spot references a loot family.

Location:

```text
data/<namespace>/rustling_spots/loot_families/<name>.json
```

### Example from the datapack

```json
[
  { "item": "minecraft:slime_ball", "min": 1, "max": 3, "weight": 5 },
  { "item": "minecraft:vine", "min": 1, "max": 3, "weight": 4 },
  { "item": "minecraft:lily_pad", "min": 1, "max": 2, "weight": 3 },
  { "item": "minecraft:mud", "min": 2, "max": 5, "weight": 4 },
  { "item": "minecraft:mangrove_propagule", "min": 1, "max": 2, "weight": 2 },
  { "item": "cobblemon:pecha_berry", "min": 1, "max": 2, "weight": 3 },
  { "item": "cobblemon:net_ball", "min": 1, "max": 1, "weight": 2 },
  { "item": "cobblemon:great_ball", "min": 1, "max": 1, "weight": 2 }
]
```

### Supported fields per loot entry

| Field | Required | Description |
| --- | --- | --- |
| `item` | Yes | Full item identifier. |
| `min` | No | Minimum amount. Default `1`. |
| `max` | No | Maximum amount. Default `1`. |
| `weight` | Yes | Selection weight. Must be > `0`. |

### Useful rules

- `item` must exist in the game registries
- `min` is automatically raised to at least `1`
- `max` is automatically raised to at least `min`
- invalid entries are ignored

### Merge with global loot

This is very important: a datapack loot family does not only use its own local entries.

The mod builds the final pool like this:

1. start from `global_loot`
2. add the entries from your family
3. merge duplicate entries by summing their weights

So a datapack `loot_family` naturally adds its own context on top of global loot.

### Linking it from the spot

If your file is:

```text
data/rustlingspots/rustling_spots/loot_families/swamp.json
```

then the spot should point to:

```json
"loot_family": "rustlingspots:swamp"
```

### If the loot family is unknown

The spot is not removed.
It still loads, but uses global loot instead.

---

## Supported particles

The `particles` field accepts a list of objects:

```json
"particles": [
  { "type": "rustlingspots:grass_burst", "weight": 7 },
  { "type": "minecraft:happy_villager", "weight": 3 }
]
```

### Supported format

Each entry only accepts:

- `type`
- `weight`

There is currently no support for:

- custom speed
- custom offsets
- per-entry particle count
- custom color
- advanced per-particle settings

### Technical rule

The particle type must be a recognized simple particle type in the game.
If it is unknown or unsupported, it is ignored.

### Useful Rustling Spots particle IDs

You can already reuse the mod particle types:

- `rustlingspots:grass_burst`
- `rustlingspots:sand_burst`
- `rustlingspots:water_burst`
- `rustlingspots:snow_burst`
- `rustlingspots:leaves_burst`
- `rustlingspots:cave_burst`
- `rustlingspots:netherflamme_burst`
- `rustlingspots:soulflame_burst`
- `rustlingspots:flying_burst`
- `rustlingspots:shiny_sparkle_one`
- `rustlingspots:shiny_sparkle_two`

You can also mix them with simple vanilla particles such as:

- `minecraft:happy_villager`
- `minecraft:poof`
- `minecraft:cloud`

### Particle fallback

If `particles` is missing, empty, or contains no valid entries:

- the spot still loads
- it falls back to default grass particles

---

## Useful commands

### Reload

```mcfunction
/reload
```

Reloads world datapacks, including custom spots, Pokemon families, and loot families.

```mcfunction
/rustlingspots reload
```

Reloads Rustling Spots resources and also refreshes datapack-driven data for the mod.

### Test a custom spot

```mcfunction
/rustlingspots spawn rustlingspots:swamp_custom
```

### Test a shiny version

```mcfunction
/rustlingspots spawnshiny rustlingspots:swamp_custom
```

### Scan active spots around the player

```mcfunction
/rustlingspots scan 64
```

or with a built-in family filter:

```mcfunction
/rustlingspots scan 64 grass
```

---

## How to create your own spot

This is the simplest and safest workflow.

### 1. Duplicate the example

Copy:

```text
example_datapacks/custom_swamp_spot_pack
```

into a new folder name, for example:

```text
my_biome_spot_pack
```

### 2. Edit `pack.mcmeta`

You usually only need to change the description.

### 3. Create your new ID

In `spot_definitions/swamp.json`, change:

```json
"id": "rustlingspots:swamp_custom"
```

to something unique, for example:

```json
"id": "mymodpack:haunted_forest"
```

Also give it a readable name:

```json
"display_name": "Haunted Forest Spot"
```

### 4. Adjust the spawn area

Edit:

- `dimensions`
- `biomes`
- `blocks`

Example:

```json
"dimensions": [
  "minecraft:overworld"
],
"biomes": [
  "minecraft:dark_forest",
  "#minecraft:is_forest"
],
"blocks": [
  "minecraft:grass_block",
  "minecraft:podzol",
  "#minecraft:dirt"
]
```

### 5. Choose the priority behavior

If you want your custom spot to win over built-ins:

```json
"priority": 10
```

If you want it to share chances with them:

```json
"priority": 0
```

Then tune its relative weight:

```json
"weight": 5
```

### 6. Create your Pokemon family

Edit the file in:

```text
data/<namespace>/rustling_spots/pokemon_families/<name>.json
```

Then link it from the spot:

```json
"pokemon_family": "<namespace>:<name>"
```

### 7. Create your loot family

Edit the file in:

```text
data/<namespace>/rustling_spots/loot_families/<name>.json
```

Then link it from the spot:

```json
"loot_family": "<namespace>:<name>"
```

### 8. Choose your particles

Simple example:

```json
"particles": [
  { "type": "rustlingspots:leaves_burst", "weight": 8 },
  { "type": "minecraft:happy_villager", "weight": 2 }
]
```

### 9. Test in game

Install the datapack, then run:

```mcfunction
/reload
/rustlingspots spawn mymodpack:haunted_forest
```

### 10. Verify natural spawning

Finally, confirm that the spot appears in the right biomes, on the right blocks, and with the intended priority behavior.

---

## Full custom example

### Spot

```json
{
  "format_version": 1,
  "id": "mymodpack:haunted_forest",
  "display_name": "Haunted Forest Spot",
  "priority": 12,
  "weight": 6,
  "dimensions": [
    "minecraft:overworld"
  ],
  "biomes": [
    "minecraft:dark_forest",
    "#minecraft:is_forest"
  ],
  "blocks": [
    "minecraft:grass_block",
    "minecraft:podzol",
    "#minecraft:dirt"
  ],
  "pokemon_family": "mymodpack:haunted_forest",
  "loot_family": "mymodpack:haunted_forest",
  "particles": [
    { "type": "rustlingspots:leaves_burst", "weight": 6 },
    { "type": "rustlingspots:shiny_sparkle_one", "weight": 1 },
    { "type": "minecraft:happy_villager", "weight": 2 }
  ]
}
```

### Pokemon

```json
[
  { "species": "murkrow", "weight": 8, "min_level": 10, "max_level": 24 },
  { "species": "phantump", "weight": 7, "min_level": 12, "max_level": 28 },
  { "species": "shuppet", "weight": 7, "min_level": 12, "max_level": 28 },
  { "species": "mimikyu", "weight": 1, "min_level": 30, "max_level": 45, "shinyChance": 0.05 }
]
```

### Loot

```json
[
  { "item": "minecraft:bone", "min": 1, "max": 3, "weight": 4 },
  { "item": "minecraft:spider_eye", "min": 1, "max": 2, "weight": 3 },
  { "item": "minecraft:string", "min": 1, "max": 4, "weight": 4 },
  { "item": "cobblemon:spell_tag", "min": 1, "max": 1, "weight": 1 }
]
```

---

## Validation, errors, and fallback behavior

The system is designed to be tolerant to bad data.
The goal is to avoid crashing a world or server because of an invalid datapack JSON file.

### What happens if...

- `format_version` is not supported
  The spot is not loaded.

- `id` is invalid
  The spot is not loaded.

- `pokemon_family` is unknown
  The spot is not loaded.

- `loot_family` is unknown
  The spot still loads, but uses global loot.

- `particles` contains invalid particle entries
  Invalid entries are ignored.

- no valid particle entry remains
  The spot falls back to grass particles.

- `biomes` or `blocks` become empty after validation
  The spot is not loaded.

- two files define the same `id`
  the last loaded definition wins

### Practical debugging advice

If a spot does not work:

1. test its ID with `/rustlingspots spawn`
2. run `/reload`
3. verify namespace spelling
4. verify that `pokemon_family` exists
5. verify that `biomes` and `blocks` are not empty

---

## Relationship with the mod config files

The datapack is not the only place where families can exist.

The mod also manages config files on disk, including:

```text
config/rustlingspots/pokemon/families/
config/rustlingspots/loot/families/
config/rustlingspots/loot/global_loot.json
```

### What this changes

- `pokemon_family` can point to a family defined in config or in a datapack
- `loot_family` can point to a family defined in config or in a datapack
- loot families are always built on top of `global_loot`

For a CurseForge-style public description, you can simply explain that:

- the datapack adds its own families
- the mod can also use its standard config files

---

## Checklist before publishing

- the datapack is placed in `world/datapacks/`
- `pack.mcmeta` is valid
- `/reload` works without errors
- `/rustlingspots spawn <id>` works
- `display_name` shows correctly
- the correct Pokemon spawn
- the correct loot drops
- the spot appears in the intended biomes
- the spot appears on the intended blocks
- the chosen priority behaves as expected
- the particles match the intended atmosphere

---

## Short CurseForge-style summary

`Rustling Spots` now supports custom rustling spots through datapacks.
You can create your own biome-based spots, define their biomes, blocks, dimensions, priorities, weights, Pokemon families, loot families, and particles, without touching the mod code.

The included example datapack `custom_swamp_spot_pack` is already functional and serves as the official base for creating your own packs.
It includes:

- one custom swamp spot
- one custom Pokemon family
- one custom loot family
- a readable display name
- mixed particles
- simple installation through `world/datapacks/`

In short: if you want to create your own spots for a server, a modpack, or a custom map, the current example is ready to use and this guide covers the full format supported today.
