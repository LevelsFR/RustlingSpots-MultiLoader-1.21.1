# Rustling Spots Addon API

This guide targets Rustling Spots 6.1, Minecraft 1.21.1 and Java 21. The API is shared by Fabric and NeoForge and must be called from the logical server.

## Create and manage spots

Define a custom spot in a datapack, then create it at the desired spot block position. The position must be air, or source water for an `underwater` visual family; its chunk must already be loaded, and the ground block (or underwater water block) must match the definition. Creation also respects Rustling Spots' enabled setting, compatibility checks, global spot limit and configured minimum spacing. It never requests a chunk load.

```java
ResourceLocation definitionId = ResourceLocation.fromNamespaceAndPath("exampleaddon", "festival");
ResourceLocation sourceId = ResourceLocation.fromNamespaceAndPath("exampleaddon", "spring_festival");

Optional<RustlingSpot> created = RustlingSpotAddonApi.createSpot(
        serverLevel,
        spotPosition,
        definitionId,
        20 * 60, // 60 seconds
        sourceId
);

Optional<RustlingSpot> current = RustlingSpotAddonApi.getSpot(serverLevel, spotId);
Collection<RustlingSpot> inDimension = RustlingSpotAddonApi.getSpots(serverLevel);
boolean removed = RustlingSpotAddonApi.removeSpot(serverLevel, spotId);
```

`createSpot` returns an empty `Optional` if a definition is missing, the call is off the server thread, the location is invalid or unloaded, another spot is too close, or the global limit is reached. `lifetimeTicks` must be positive. Pass `null` for `sourceId` when the spot does not belong to a particular addon event. A created spot uses the selected definition's Pokémon pool, loot pool, shiny rules, visuals, particles and sounds.

Use `RustlingSpotSpawnOptions` when an event needs closer or more widely spaced spots, or when its spots should remain until their lifetime expires after players leave:

```java
RustlingSpotSpawnOptions options = new RustlingSpotSpawnOptions(
        20 * 60,
        sourceId,
        6.0D, // minimum distance between spots with this same non-null source ID
        false // do not remove early when no non-spectator players are nearby
);
Optional<RustlingSpot> created = RustlingSpotAddonApi.createSpot(
        serverLevel, spotPosition, definitionId, options);
```

The source-specific spacing is bounded to 0–512 blocks. Spots from other sources and natural spots still obey the configured global minimum spacing. A spot keeps the spacing requested when it was created, so a later call cannot loosen that event's spacing. With `despawnWhenNoPlayers=false`, a spot stays registered in unloaded chunks until its lifetime expires, but cannot be interacted with until its chunk is loaded again. The original `createSpot(..., lifetimeTicks, sourceId)` overload remains available and keeps its configured spacing and early-despawn defaults. A `null` source does not form a spacing group.

The definition can also be inspected before creating a spot:

```java
Optional<CustomSpotDefinition> definition = RustlingSpotAddonApi.getCustomSpotDefinition(definitionId);
```

Active spots expose their source through `spot.getSourceId()` and their lifetime override through `spot.getLifetimeTicks()`; zero means the normal server-configured lifetime.

## Lifecycle callbacks

```java
RustlingSpotAddonApi.registerLifecycleCallback(event -> {
    if (event.spot().getSourceId().filter(sourceId::equals).isEmpty()) {
        return;
    }

    switch (event.type()) {
        case CREATED -> onCreated(event.spot());
        case INTERACTED -> onInteracted(event.player().orElseThrow(), event.spot());
        case EXPIRED -> onExpired(event.spot());
        case REMOVED -> onRemoved(event.spot(), event.removalReason());
    }
});
```

Callbacks observe all Rustling Spots in the server, so filter by `sourceId` or definition ID when an addon only owns a subset. They run on the server thread in registration order. A listener exception is logged and isolated. The spot cannot be removed through this API while its lifecycle or interaction callback is running; use the interaction callback result to consume or override an interaction. Expiration is followed by a removal event with reason `EXPIRED`; interactions also result in a removal event with reason `INTERACTION`. `unregisterLifecycleCallback` removes a registered listener.

The v6.0 interaction hook remains source-compatible:

```java
RustlingSpotAddonApi.registerInteractionCallback(event -> {
    // PASS keeps the normal Rustling Spots Pokémon and loot resolution.
    return RustlingSpotInteractionResult.PASS;
});
```

Interaction callbacks still run after the interaction is validated and before normal reward resolution. The first non-`PASS` result wins. A spot is claimed and removed exactly once before reward resolution, including when a callback or reward handler throws or re-enters the API. Spectators are ignored. `unregisterInteractionCallback` removes a listener.

### Reward chances and player-specific weights

Definitions may include `reward_chances` with optional `pokemon` and `loot` values from `0.0` to `1.0`. Missing values keep the existing server-configured Pokémon chance and full loot fallback chance. An addon can adjust individual Pokémon and item pool weights for a player:

```java
RustlingSpotRewardWeightModifier modifier = event -> {
    if (event.type() == RustlingSpotRewardWeightEvent.Type.ITEM
            && event.rewardId().equals("minecraft:glow_berries")
            && event.spot().getSourceId().filter(sourceId::equals).isPresent()
            && event.player().isCreative()) {
        return event.currentWeight() * 2;
    }
    return event.currentWeight();
};
RustlingSpotAddonApi.registerRewardWeightModifier(modifier);
// Later, during addon shutdown:
RustlingSpotAddonApi.unregisterRewardWeightModifier(modifier);
```

The event identifies the player, spot, reward type (`POKEMON` or `ITEM`), configured reward ID, original weight and current weight. A returned weight of zero excludes that entry; callback exceptions leave the current weight unchanged. Keep lifecycle and reward registrations at mod initialization; the mod clears world, cooldown, preference and datapack caches when each server stops, while addon registrations remain process-wide.

## Datapack definitions

The optional `spawn_mode` field accepts `natural` or `external_only`. Omitted fields default to `natural`, so existing datapacks continue to participate in natural selection. External-only definitions are excluded from both natural selection and the admin spawn command; only an addon can create them through the API.

```json
{
  "format_version": 1,
  "id": "exampleaddon:festival",
  "display_name": "Festival Rustle",
  "spawn_mode": "external_only",
  "visual_family": "leaves",
  "ambient_sound": "minecraft:block.amethyst_block.chime",
  "priority": 0,
  "weight": 1,
  "dimensions": ["minecraft:overworld"],
  "biomes": ["#minecraft:is_forest"],
  "blocks": ["#minecraft:leaves"],
  "pokemon_family": "grass",
  "loot_family": "grass",
  "reward_chances": { "pokemon": 0.65, "loot": 0.25 },
  "particles": [{"type": "minecraft:happy_villager", "weight": 1}]
}
```

`visual_family` selects one of the existing spot renderers. `particles` uses the existing weighted particle path. `ambient_sound` is an optional registered sound-event ID available on both client and server; if it is absent or unavailable, the selected visual family's existing sound is used. Addons that introduce a new sound must register the sound event and ship its client sound assets.

See [the external-only example datapack](../example_datapacks/external_only_spot_pack) and the [custom definition guide](./custom-spot-definitions-guide-en.md).

## OS Cobblemon Library

OSCL 1.7.0 is published for both loaders. Its documented Cobblemon range is `1.8.2` through `<1.8.5`, so requiring it would raise Rustling Spots' current Cobblemon 1.8.0 minimum and exclude 1.8.0/1.8.1.

The new `SpawnModifiers` API changes weights for Cobblemon spawn details that already passed native eligibility checks. Rustling Spots encounters are created separately after a player interacts with a spot; they do not pass through Cobblemon's natural spawn-detail selection. Applying a spawn modifier would not improve this flow and could change encounter balance.

OSCL's `PokemonCreation` and `PokemonSpeciesResolver` overlap with parts of Rustling Spots' `PokemonSpawnResolver`. Rustling Spots also validates configured species and forms, preserves addon namespaces, disables the normal spawn cap, and positions and rotates the entity at the spot. Replacing that path would either lose those behaviors or need loader/version-specific fallback paths for Cobblemon 1.8.0/1.8.1. That extra dependency and branching do not provide enough benefit for v6.1, so no OSCL dependency is added and Cobblemon 1.8.0 compatibility is preserved.

For addons that choose to use OSCL independently, the Curse Maven files are `curse.maven:os-cobblemon-library-1698981:9803831` (Fabric) and `curse.maven:os-cobblemon-library-1698981:9803832` (NeoForge).

References: [OSCL v1.7.0 docs](https://levelsfr.github.io/OS-Cobblemon-Library/) · [CurseForge Fabric file](https://www.curseforge.com/minecraft/mc-mods/os-cobblemon-library/files/9803831) · [CurseForge NeoForge file](https://www.curseforge.com/minecraft/mc-mods/os-cobblemon-library/files/9803832)
