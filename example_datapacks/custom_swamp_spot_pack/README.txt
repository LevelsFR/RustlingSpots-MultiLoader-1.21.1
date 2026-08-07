Install this folder as a normal datapack.

Path:
world/datapacks/custom_swamp_spot_pack

This example adds a custom swamp rustling spot:
- id: rustlingspots:swamp_custom
- display_name: Swamp Spot
- visual_family: leaves
- overworld only
- swamp + mangrove swamp biomes, plus forest biome tags
- mud, grass_block, and #minecraft:dirt blocks
- uses the datapack pokemon family rustlingspots:swamp
- uses the datapack loot family rustlingspots:swamp
- uses the leaves visual family with custom grass burst and happy_villager particles

Included in this datapack:
- data/rustlingspots/rustling_spots/spot_definitions/swamp.json
- data/rustlingspots/rustling_spots/pokemon_families/swamp.json
- data/rustlingspots/rustling_spots/loot_families/swamp.json

Test commands:
- /reload
- /rustlingspots spawn rustlingspots:swamp_custom
