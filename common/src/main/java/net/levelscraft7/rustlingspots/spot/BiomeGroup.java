package net.levelscraft7.rustlingspots.spot;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Simplified biome grouping to guide Pokémon selection.
 */
public enum BiomeGroup {
    FOREST,
    PLAINS,
    SWAMP,
    MOUNTAIN,
    SNOWY,
    DESERT,
    RIVER,
    OCEAN,
    OTHER;

    private static final TagKey<Biome> FOREST_TAG = TagKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:is_forest"));
    private static final TagKey<Biome> PLAINS_TAG = TagKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:is_plains"));
    private static final TagKey<Biome> SWAMP_TAG = TagKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:is_swamp"));
    private static final TagKey<Biome> MOUNTAIN_TAG = TagKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:is_mountain"));
    private static final TagKey<Biome> SNOWY_TAG = TagKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:is_snowy"));
    private static final TagKey<Biome> DESERT_TAG = TagKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:is_desert"));
    private static final TagKey<Biome> RIVER_TAG = TagKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:is_river"));
    private static final TagKey<Biome> OCEAN_TAG = TagKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:is_ocean"));

    public static BiomeGroup fromBiome(Holder<Biome> biome) {
        if (biome.is(FOREST_TAG)) {
            return FOREST;
        }
        if (biome.is(PLAINS_TAG)) {
            return PLAINS;
        }
        if (biome.is(SWAMP_TAG)) {
            return SWAMP;
        }
        if (biome.is(MOUNTAIN_TAG)) {
            return MOUNTAIN;
        }
        if (biome.is(SNOWY_TAG)) {
            return SNOWY;
        }
        if (biome.is(DESERT_TAG)) {
            return DESERT;
        }
        if (biome.is(RIVER_TAG)) {
            return RIVER;
        }
        if (biome.is(OCEAN_TAG)) {
            return OCEAN;
        }
        return OTHER;
    }
}
