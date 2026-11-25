Install this folder as a normal datapack at world/datapacks/external_only_spot_pack.

This definition is excluded from natural Rustling Spots selection. An addon can create
it at an air block above a leaf block with RustlingSpotAddonApi.createSpot(...), using:
- definition ID: exampleaddon:festival
- lifetime: any positive number of ticks
- source ID: exampleaddon:spring_festival (optional)

The definition reuses the built-in grass Pokémon and loot families, the leaves visual,
and a vanilla ambient sound. The example uses a forest leaf block as its required ground.

After installing or changing the pack, run /reload. The external-only definition is
intentionally unavailable to natural spawns and the admin spawn command; it appears
when an addon calls RustlingSpotAddonApi.createSpot with the definition ID above.

See docs/addon-api-guide-en.md for Java integration.
