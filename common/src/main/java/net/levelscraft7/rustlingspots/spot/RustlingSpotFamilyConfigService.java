package net.levelscraft7.rustlingspots.spot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Loads and resolves rustling spot family configurations from {@code config/rustlingspots/families}.
 * Only the surface blocks are user editable; the Java enum names stay authoritative.
 */
public final class RustlingSpotFamilyConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpotFamilyConfigService.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BASE = Platform.getConfigFolder().resolve("rustlingspots");
    private static final Path FAMILY_DIR = BASE.resolve("families");
    private static final Path BIOME_TAG_DIR = BASE.resolve("biome_tags");
    private static final AtomicBoolean LOADED = new AtomicBoolean(false);
    private static final Map<RustlingSpotFamily, FamilyConfig> CONFIGS = new EnumMap<>(RustlingSpotFamily.class);

    private static final Map<RustlingSpotFamily, List<String>> DEFAULT_BIOME_HINTS = Map.of(
            RustlingSpotFamily.GRASS, List.of(
                    "minecraft:plains",
                    "minecraft:sunflower_plains",
                    "minecraft:forest",
                    "minecraft:flower_forest",
                    "minecraft:birch_forest",
                    "minecraft:old_growth_birch_forest",
                    "minecraft:dark_forest",
                    "minecraft:meadow",
                    "biomesoplenty:cherry_blossom_grove",
                    "biomesoplenty:clover_patch",
                    "biomesoplenty:dead_forest",
                    "biomesoplenty:field",
                    "biomesoplenty:forested_field",
                    "biomesoplenty:grassland",
                    "biomesoplenty:highland_moor",
                    "biomesoplenty:lavender_field",
                    "biomesoplenty:maple_woods",
                    "biomesoplenty:mystic_grove",
                    "biomesoplenty:old_growth_dead_forest",
                    "biomesoplenty:orchard",
                    "biomesoplenty:pasture",
                    "biomesoplenty:pumpkin_patch",
                    "biomesoplenty:scrubland",
                    "biomesoplenty:shrubland",
                    "biomesoplenty:wooded_scrubland",
                    "byg:allium_fields",
                    "byg:amaranth_fields",
                    "byg:aspen_forest",
                    "byg:autumnal_valley",
                    "byg:cherry_blossom_forest",
                    "byg:cika_woods",
                    "byg:clover_patch",
                    "byg:crag_gardens",
                    "byg:ebony_woods",
                    "byg:evergreen_woodland",
                    "byg:forgotten_forest",
                    "byg:guiana_shield",
                    "byg:guiana_shield_highlands",
                    "byg:guiana_shield_plateau",
                    "byg:iris_fields",
                    "byg:lush_tundra",
                    "byg:maple_taiga",
                    "byg:meadow_forest",
                    "byg:mystic_plains",
                    "byg:orchard",
                    "byg:temperate_rainforest_basin",
                    "byg:temperate_rainforest_clearing",
                    "regions_unexplored:bamboo_forest",
                    "regions_unexplored:baobab_savanna",
                    "regions_unexplored:cherry_grove",
                    "regions_unexplored:flower_field",
                    "regions_unexplored:lavender_field",
                    "regions_unexplored:maple_forest",
                    "regions_unexplored:orchard",
                    "regions_unexplored:shrubland",
                    "regions_unexplored:silver_birch_forest",
                    "regions_unexplored:temperate_grove",
                    "regions_unexplored:temperate_highlands",
                    "regions_unexplored:temperate_rainforest",
                    "regions_unexplored:tropical_grassland",
                    "terralith:alpha_islands",
                    "terralith:alpha_islands_winter",
                    "terralith:blooming_valley",
                    "terralith:bryce_canyon",
                    "terralith:bryce_canyon_springs",
                    "terralith:clifftop_meadows",
                    "terralith:desert_oasis",
                    "terralith:emerald_peaks",
                    "terralith:flowery_plains",
                    "terralith:lavender_field",
                    "terralith:meadow",
                    "terralith:orchard",
                    "terralith:rocky_meadow",
                    "terralith:sakura_grove",
                    "terralith:sakura_valley",
                    "terralith:steppe",
                    "terralith:warped_mesa",
                    "terralith:white_mesa",
                    "natures_spirit:aspen_forest",
                    "natures_spirit:blooming_highlands",
                    "natures_spirit:blooming_sugi_forest",
                    "natures_spirit:carnation_fields",
                    "natures_spirit:cedar_thicket",
                    "natures_spirit:coniferous_covert",
                    "natures_spirit:cypress_fields",
                    "natures_spirit:fir_forest",
                    "natures_spirit:floral_ridges",
                    "natures_spirit:flowering_shrubland",
                    "natures_spirit:golden_wilds",
                    "natures_spirit:heather_fields",
                    "natures_spirit:lavender_fields",
                    "natures_spirit:maple_woodlands",
                    "natures_spirit:marigold_meadows",
                    "natures_spirit:oak_savanna",
                    "natures_spirit:prairie",
                    "natures_spirit:redwood_forest",
                    "natures_spirit:shrubland",
                    "natures_spirit:sugi_forest",
                    "natures_spirit:windswept_sugi_forest",
                    "natures_spirit:wisteria_forest",
                    "wythers:autumnal_forest",
                    "wythers:clover_field",
                    "wythers:fir_clearing",
                    "wythers:flower_field",
                    "wythers:flower_hills",
                    "wythers:flower_oak_forest",
                    "wythers:flower_taiga",
                    "wythers:lavender_field",
                    "wythers:maple_forest",
                    "wythers:oak_grove",
                    "wythers:orchard",
                    "wythers:orchard_hills",
                    "wythers:pasture",
                    "wythers:prairie",
                    "wythers:temperate_forest",
                    "wythers:temperate_grove",
                    "wythers:willow_meadow"
            ),
            RustlingSpotFamily.SAND, List.of(
                    "minecraft:desert",
                    "minecraft:badlands",
                    "minecraft:eroded_badlands",
                    "minecraft:wooded_badlands",
                    "minecraft:beach",
                    "minecraft:stony_shore",
                    "biomesoplenty:cold_desert",
                    "biomesoplenty:dune_beach",
                    "biomesoplenty:dunes",
                    "biomesoplenty:gravel_beach",
                    "biomesoplenty:oasis",
                    "biomesoplenty:rocky_beach",
                    "biomesoplenty:wasteland",
                    "byg:begrimed_dunes",
                    "byg:crag_gardens",
                    "byg:eroded_desert",
                    "byg:mojave_desert",
                    "byg:red_rock_mountains",
                    "byg:red_rock_valley",
                    "byg:rocky_shrubland",
                    "byg:steppe",
                    "byg:warped_desert",
                    "regions_unexplored:arid_mountains",
                    "regions_unexplored:arid_savanna",
                    "regions_unexplored:chalk_cliffs",
                    "regions_unexplored:cold_desert",
                    "regions_unexplored:rocky_desert",
                    "regions_unexplored:red_rock_valley",
                    "regions_unexplored:sand_dunes",
                    "regions_unexplored:shrubland",
                    "regions_unexplored:steppe",
                    "regions_unexplored:temperate_desert",
                    "terralith:ancient_sands",
                    "terralith:desert_canyon",
                    "terralith:desert_caves",
                    "terralith:desert_oasis",
                    "terralith:fractured_savanna",
                    "terralith:rocky_mountains",
                    "terralith:rocky_shrubland",
                    "terralith:steppe",
                    "terralith:warped_mesa",
                    "terralith:white_mesa",
                    "natures_spirit:arid_highlands",
                    "natures_spirit:arid_savanna",
                    "natures_spirit:blooming_dunes",
                    "natures_spirit:drylands",
                    "natures_spirit:lively_dunes",
                    "natures_spirit:scorched_dunes",
                    "natures_spirit:stratified_desert",
                    "natures_spirit:wooded_drylands",
                    "natures_spirit:xeric_plains",
                    "wythers:desert",
                    "wythers:desert_canyon",
                    "wythers:desert_island",
                    "wythers:desert_mountains",
                    "wythers:rocky_desert",
                    "wythers:rocky_savanna",
                    "wythers:sandy_jungle",
                    "wythers:savanna_plateau"
            ),
            RustlingSpotFamily.WATER, List.of(
                    "minecraft:river",
                    "minecraft:frozen_river",
                    "minecraft:ocean",
                    "minecraft:deep_ocean",
                    "minecraft:warm_ocean",
                    "minecraft:lukewarm_ocean",
                    "minecraft:deep_lukewarm_ocean",
                    "minecraft:cold_ocean",
                    "minecraft:deep_cold_ocean",
                    "minecraft:frozen_ocean",
                    "minecraft:deep_frozen_ocean",
                    "biomesoplenty:bayou",
                    "biomesoplenty:dead_swamp",
                    "biomesoplenty:fungal_jungle",
                    "biomesoplenty:marsh",
                    "biomesoplenty:murky_swamp",
                    "biomesoplenty:mystic_plains",
                    "biomesoplenty:overgrown_fungal_jungle",
                    "biomesoplenty:tropics",
                    "byg:algae_bay",
                    "byg:bayou",
                    "byg:crag_gardens",
                    "byg:guiana_shield_bay",
                    "byg:guiana_shield_lake",
                    "byg:kelp_forest",
                    "byg:luminous_grove",
                    "byg:mangrove_marshes",
                    "byg:murky_swamp",
                    "natures_spirit:bamboo_wetlands",
                    "natures_spirit:marsh",
                    "natures_spirit:tropical_basin",
                    "natures_spirit:tropical_shores",
                    "regions_unexplored:bayou",
                    "regions_unexplored:cypress_swamp",
                    "regions_unexplored:delta",
                    "regions_unexplored:glowing_grotto",
                    "regions_unexplored:marsh",
                    "regions_unexplored:river",
                    "terralith:bryce_canyon_springs",
                    "terralith:crystal_lake",
                    "terralith:frigid_river",
                    "terralith:kelp_forest",
                    "terralith:mirage_isles"
            ),
            RustlingSpotFamily.SNOW, List.of(
                    "minecraft:snowy_plains",
                    "minecraft:ice_spikes",
                    "minecraft:snowy_taiga",
                    "minecraft:grove",
                    "minecraft:snowy_slopes",
                    "minecraft:jagged_peaks",
                    "minecraft:frozen_peaks",
                    "biomesoplenty:cold_desert",
                    "biomesoplenty:crystalline_chasm",
                    "biomesoplenty:frostbitten_forest",
                    "biomesoplenty:glacier",
                    "biomesoplenty:highland_moor",
                    "biomesoplenty:icy_marsh",
                    "biomesoplenty:jade_cliffs",
                    "biomesoplenty:snowblossom_grove",
                    "byg:cold_swamplands",
                    "byg:frosted_coniferous_forest",
                    "byg:frosted_vegetation",
                    "byg:glacial_chasm",
                    "byg:howling_peaks",
                    "byg:lush_tundra",
                    "byg:shattered_glacier",
                    "regions_unexplored:cold_boreal_forest",
                    "regions_unexplored:cold_deciduous_forest",
                    "regions_unexplored:cold_mountains",
                    "regions_unexplored:cold_river",
                    "regions_unexplored:glacier",
                    "regions_unexplored:pine_slopes",
                    "regions_unexplored:snowy_boreal_forest",
                    "regions_unexplored:snowy_fir_forest",
                    "regions_unexplored:snowy_highlands",
                    "regions_unexplored:snowy_taiga",
                    "regions_unexplored:tundra",
                    "terralith:alpine_grove",
                    "terralith:alpine_highlands",
                    "terralith:alpine_slopes",
                    "terralith:ashen_savanna",
                    "terralith:ashen_savanna_slopes",
                    "terralith:frozen_cliffs",
                    "terralith:frozen_peaks",
                    "terralith:frozen_river",
                    "terralith:glacial_chasm",
                    "natures_spirit:alpine_clearings",
                    "natures_spirit:alpine_highlands",
                    "natures_spirit:boreal_taiga",
                    "natures_spirit:sleeted_slopes",
                    "natures_spirit:snowcapped_red_peaks",
                    "natures_spirit:snowy_fir_forest",
                    "natures_spirit:snowy_redwood_forest",
                    "natures_spirit:tundra",
                    "wythers:alpine_forest",
                    "wythers:alpine_highlands",
                    "wythers:cold_steppe",
                    "wythers:frosty_taiga",
                    "wythers:frozen_forest",
                    "wythers:glacier",
                    "wythers:snowy_fir_forest",
                    "wythers:snowy_oak_forest"
            ),
            RustlingSpotFamily.LEAVES, List.of(
                    "minecraft:forest",
                    "minecraft:flower_forest",
                    "minecraft:birch_forest",
                    "minecraft:old_growth_birch_forest",
                    "minecraft:dark_forest",
                    "minecraft:taiga",
                    "minecraft:old_growth_pine_taiga",
                    "minecraft:old_growth_spruce_taiga",
                    "minecraft:snowy_taiga",
                    "minecraft:jungle",
                    "minecraft:sparse_jungle",
                    "minecraft:bamboo_jungle",
                    "biomesoplenty:bayou",
                    "biomesoplenty:cherry_blossom_grove",
                    "biomesoplenty:coniferous_forest",
                    "biomesoplenty:dead_forest",
                    "biomesoplenty:deep_bayou",
                    "biomesoplenty:fir_clearing",
                    "biomesoplenty:fir_forest",
                    "biomesoplenty:flower_field",
                    "biomesoplenty:fungal_jungle",
                    "biomesoplenty:highland",
                    "biomesoplenty:jade_cliffs",
                    "biomesoplenty:lavender_field",
                    "biomesoplenty:maple_woods",
                    "biomesoplenty:mystic_grove",
                    "biomesoplenty:old_growth_dead_forest",
                    "biomesoplenty:orchard",
                    "biomesoplenty:overgrown_cliffs",
                    "biomesoplenty:overgrown_fungal_jungle",
                    "biomesoplenty:rainforest",
                    "biomesoplenty:redwood_forest",
                    "biomesoplenty:seasonal_forest",
                    "biomesoplenty:snowblossom_grove",
                    "biomesoplenty:snowy_coniferous_forest",
                    "biomesoplenty:temperate_rainforest",
                    "biomesoplenty:temperate_rainforest_slopes",
                    "biomesoplenty:tropical_rainforest",
                    "biomesoplenty:wooded_scrubland",
                    "byg:baobab_savanna",
                    "byg:bayou",
                    "byg:cold_swamplands",
                    "byg:evergreen_taiga",
                    "byg:fungal_forest",
                    "byg:glowshroom_bayou",
                    "byg:luminous_grove",
                    "byg:mangrove_marshes",
                    "byg:overgrowth",
                    "byg:overgrown_cliffs",
                    "byg:overgrown_dunes",
                    "byg:overgrown_fungal_jungle",
                    "byg:temperate_rainforest",
                    "byg:temperate_rainforest_slopes",
                    "byg:tropical_rainforest",
                    "byg:tropical_rainforest_lowlands",
                    "byg:tropical_rainforest_mountains",
                    "byg:tropical_rainforest_valley",
                    "byg:red_oak_forest",
                    "byg:jacaranda_forest",
                    "byg:redwood_thicket",
                    "byg:zelkova_forest",
                    "regions_unexplored:alpha_forest",
                    "regions_unexplored:autumnal_forest",
                    "regions_unexplored:baobab_savanna",
                    "regions_unexplored:birch_taiga",
                    "regions_unexplored:blue_taiga",
                    "regions_unexplored:cherry_grove",
                    "regions_unexplored:deciduous_forest",
                    "regions_unexplored:enchanted_forest",
                    "regions_unexplored:firefly_forest",
                    "regions_unexplored:flower_field",
                    "regions_unexplored:flower_forest",
                    "regions_unexplored:forested_highlands",
                    "regions_unexplored:forested_mountain",
                    "regions_unexplored:forest",
                    "regions_unexplored:glittering_weald",
                    "regions_unexplored:golden_birch_forest",
                    "regions_unexplored:jungle",
                    "regions_unexplored:lush_hills",
                    "regions_unexplored:lush_plateau",
                    "regions_unexplored:maple_forest",
                    "regions_unexplored:orchard",
                    "regions_unexplored:pine_forest",
                    "regions_unexplored:redwoods",
                    "regions_unexplored:silver_birch_forest",
                    "regions_unexplored:snowy_fir_forest",
                    "regions_unexplored:snowy_taiga",
                    "regions_unexplored:temperate_grove",
                    "regions_unexplored:temperate_highlands",
                    "regions_unexplored:temperate_rainforest",
                    "regions_unexplored:weeping_willows",
                    "regions_unexplored:willow_forest",
                    "terralith:alpine_grove",
                    "terralith:alpine_highlands",
                    "terralith:blooming_valley",
                    "terralith:brushland",
                    "terralith:clifftop_meadows",
                    "terralith:cloud_forest",
                    "terralith:flowering_grove",
                    "terralith:fractured_savanna",
                    "terralith:frigid_peaks",
                    "terralith:frozen_cliffs",
                    "terralith:frozen_peaks",
                    "terralith:highland_crags",
                    "terralith:lavender_field",
                    "terralith:mirage_isles",
                    "terralith:moonlight_grove",
                    "terralith:rocky_mountains",
                    "terralith:rocky_shrubland",
                    "terralith:sakura_grove",
                    "terralith:steppe",
                    "wythers:autumnal_forest",
                    "wythers:baobab_savanna",
                    "wythers:bamboo_forest",
                    "wythers:birch_highlands",
                    "wythers:birch_jungle",
                    "wythers:birch_rainforest",
                    "wythers:birch_redwoods",
                    "wythers:cedar_forest",
                    "wythers:cherry_forest",
                    "wythers:cherry_grove",
                    "wythers:conifer_clearing",
                    "wythers:coniferous_forest",
                    "wythers:coniferous_highlands",
                    "wythers:flower_oak_forest",
                    "wythers:flower_taiga",
                    "wythers:forest",
                    "wythers:golden_birch_forest",
                    "wythers:highland_forest",
                    "wythers:maple_forest",
                    "wythers:oak_grove",
                    "wythers:orchard",
                    "wythers:orchard_hills",
                    "wythers:redwood_forest",
                    "wythers:sequoia_forest",
                    "wythers:snowy_fir_forest",
                    "wythers:snowy_oak_forest",
                    "wythers:tropical_forest",
                    "wythers:willow_forest"
            ),
            RustlingSpotFamily.CAVE, List.of(
                    "minecraft:plains",
                    "minecraft:sunflower_plains",
                    "minecraft:forest",
                    "minecraft:flower_forest",
                    "minecraft:birch_forest",
                    "minecraft:old_growth_birch_forest",
                    "minecraft:dark_forest",
                    "minecraft:savanna",
                    "minecraft:savanna_plateau",
                    "minecraft:windswept_savanna",
                    "minecraft:desert",
                    "minecraft:swamp",
                    "minecraft:mangrove_swamp",
                    "minecraft:jungle",
                    "minecraft:bamboo_jungle",
                    "minecraft:sparse_jungle",
                    "minecraft:taiga",
                    "minecraft:snowy_taiga",
                    "minecraft:old_growth_pine_taiga",
                    "minecraft:old_growth_spruce_taiga",
                    "minecraft:snowy_plains",
                    "minecraft:ice_spikes",
                    "minecraft:snowy_slopes",
                    "minecraft:frozen_peaks",
                    "minecraft:jagged_peaks",
                    "minecraft:stony_peaks",
                    "minecraft:meadow",
                    "minecraft:grove",
                    "minecraft:windswept_hills",
                    "minecraft:windswept_gravelly_hills",
                    "minecraft:windswept_forest",
                    "minecraft:badlands",
                    "minecraft:eroded_badlands",
                    "minecraft:wooded_badlands",
                    "minecraft:beach",
                    "minecraft:snowy_beach",
                    "minecraft:stony_shore",
                    "minecraft:warm_ocean",
                    "minecraft:lukewarm_ocean",
                    "minecraft:deep_lukewarm_ocean",
                    "minecraft:ocean",
                    "minecraft:deep_ocean",
                    "minecraft:cold_ocean",
                    "minecraft:deep_cold_ocean",
                    "minecraft:frozen_ocean",
                    "minecraft:deep_frozen_ocean",
                    "minecraft:river",
                    "minecraft:frozen_river",
                    "minecraft:dripstone_caves",
                    "minecraft:lush_caves",
                    "minecraft:mushroom_fields",
                    "minecraft:cherry_grove",
                    "biomesoplenty:crystalline_chasm",
                    "biomesoplenty:undergrowth",
                    "byg:crag_gardens",
                    "byg:glowshroom_caves",
                    "byg:overgrown_dunes",
                    "byg:overgrown_fungal_caves",
                    "byg:overgrown_fungal_jungle",
                    "byg:shattered_glacier",
                    "regions_unexplored:cold_caves",
                    "regions_unexplored:crystal_caves",
                    "regions_unexplored:fungal_caves",
                    "regions_unexplored:glowing_grotto",
                    "regions_unexplored:ice_caves",
                    "regions_unexplored:lush_caves",
                    "regions_unexplored:primordial_caves",
                    "regions_unexplored:spooky_caves",
                    "terralith:abyss",
                    "terralith:crystal_caves",
                    "terralith:deep_caves",
                    "terralith:deep_dark",
                    "terralith:desert_caves",
                    "terralith:dripstone_caves",
                    "terralith:frostfire_caves",
                    "terralith:frozen_caves",
                    "terralith:infested_caves",
                    "terralith:mantle_caves",
                    "terralith:tuff_caves",
                    "natures_spirit:dusty_slopes",
                    "natures_spirit:red_peaks",
                    "natures_spirit:shrubby_highlands",
                    "natures_spirit:woody_highlands",
                    "natures_spirit:white_cliffs",
                    "wythers:crystal_caves",
                    "wythers:deep_caves",
                    "wythers:dripstone_caves",
                    "wythers:fungal_caves",
                    "wythers:glowshroom_caves",
                    "wythers:ice_caves",
                    "wythers:infested_caves",
                    "wythers:lush_caves",
                    "wythers:primordial_caves",
                    "wythers:spooky_caves",
                    "wythers:undergrowth_caves"
            ),
            RustlingSpotFamily.FLYING, List.of(
                    "minecraft:plains",
                    "minecraft:sunflower_plains",
                    "minecraft:meadow",
                    "minecraft:windswept_hills",
                    "minecraft:windswept_gravelly_hills",
                    "minecraft:windswept_forest",
                    "minecraft:cherry_grove",
                    "minecraft:stony_peaks",
                    "minecraft:jagged_peaks",
                    "minecraft:frozen_peaks",
                    "biomesoplenty:highland",
                    "biomesoplenty:highland_moor",
                    "biomesoplenty:jade_cliffs",
                    "biomesoplenty:mountain",
                    "biomesoplenty:rocky_mountain",
                    "byg:aspen_forest",
                    "byg:crag_gardens",
                    "byg:evergreen_taiga",
                    "byg:guiana_shield_highlands",
                    "byg:guiana_shield_plateau",
                    "byg:howling_peaks",
                    "byg:red_rock_mountains",
                    "byg:shattered_glacier",
                    "byg:sierra_valley",
                    "byg:tropical_rainforest_mountains",
                    "regions_unexplored:arid_mountains",
                    "regions_unexplored:cold_mountains",
                    "regions_unexplored:forested_highlands",
                    "regions_unexplored:forested_mountain",
                    "regions_unexplored:pine_slopes",
                    "regions_unexplored:red_rock_valley",
                    "regions_unexplored:snowy_highlands",
                    "regions_unexplored:temperate_highlands",
                    "regions_unexplored:volcanic_peaks",
                    "regions_unexplored:volcanic_plains",
                    "terralith:alpine_grove",
                    "terralith:alpine_highlands",
                    "terralith:alpine_slopes",
                    "terralith:ashen_savanna_slopes",
                    "terralith:emerald_peaks",
                    "terralith:frigid_peaks",
                    "terralith:frozen_peaks",
                    "terralith:highland_crags",
                    "terralith:rocky_mountains",
                    "terralith:rocky_shrubland",
                    "wythers:alpine_forest",
                    "wythers:alpine_highlands",
                    "wythers:birch_highlands",
                    "wythers:coniferous_highlands",
                    "wythers:desert_mountains",
                    "wythers:highland_forest",
                    "wythers:mountain",
                    "wythers:redwood_mountains",
                    "wythers:rocky_desert",
                    "wythers:rocky_savanna",
                    "wythers:rocky_tundra",
                    "wythers:volcanic_peaks",
                    "wythers:volcanic_plains"
            ),
            RustlingSpotFamily.NETHERFLAMME, List.of(
                    "minecraft:nether_wastes",
                    "minecraft:crimson_forest",
                    "minecraft:basalt_deltas",
                    "byg:brimstone_caverns",
                    "byg:embur_bog",
                    "byg:glowstone_gardens",
                    "byg:magma_wastes",
                    "byg:sythian_torrids",
                    "biomesoplenty:crystalline_chasm",
                    "biomesoplenty:infernal_chasm",
                    "biomesoplenty:visceral_heap",
                    "regions_unexplored:infernal_wastes",
                    "regions_unexplored:volcanic_chamber",
                    "regions_unexplored:basalt_barrens",
                    "incendium:ash_barrens",
                    "incendium:infernal_dunes",
                    "incendium:volcanic_deltas",
                    "incendium:blazing_chasm"
            ),
            RustlingSpotFamily.SOULFLAME, List.of(
                    "minecraft:soul_sand_valley",
                    "minecraft:warped_forest",
                    "byg:weeping_mire",
                    "byg:withering_woods",
                    "byg:subzero_hypogeal",
                    "biomesoplenty:undergrowth",
                    "biomesoplenty:withered_abyss",
                    "regions_unexplored:soul_barrens",
                    "regions_unexplored:withered_chasm",
                    "regions_unexplored:ghostly_forest",
                    "incendium:ashen_sands",
                    "incendium:soul_wastes",
                    "incendium:forgotten_tombs",
                    "incendium:haunted_basins"
            )

    );

    private static final Map<RustlingSpotFamily, String> DEFAULT_FAMILIES = Map.of(
            RustlingSpotFamily.GRASS, defaultFamilyJson(List.of(
                    "minecraft:grass_block",
                    "minecraft:dirt_path",
                    "minecraft:moss_block",
                    "minecraft:podzol",
                    "minecraft:coarse_dirt",
                    "minecraft:rooted_dirt",
                    "biomeswevegone:lush_grass_block",
                    "regions_unexplored:argillite_grass_block",
                    "regions_unexplored:chalk_grass_block",
                    "regions_unexplored:silt_grass_block",
                    "regions_unexplored:peat_grass_block",
                    "regions_unexplored:alpha_grass_block",
                    "regions_unexplored:deepslate_grass_block",
                    "regions_unexplored:stone_grass_block",
                    "minecraft:grass",
                    "minecraft:tall_grass",
                    "minecraft:fern",
                    "minecraft:large_fern"
            )),
            RustlingSpotFamily.SAND, defaultFamilyJson(List.of(
                    "minecraft:sand",
                    "minecraft:red_sand",
                    "minecraft:sandstone",
                    "minecraft:red_sandstone"
            )),
            RustlingSpotFamily.WATER, defaultFamilyJson(List.of(
                    "minecraft:water"
            )),
            RustlingSpotFamily.SNOW, defaultFamilyJson(List.of(
                    "minecraft:snow",
                    "minecraft:snow_block",
                    "minecraft:powder_snow",
                    "minecraft:ice",
                    "minecraft:packed_ice",
                    "minecraft:blue_ice",
                    "minecraft:snowy_grass_block"
            )),
            RustlingSpotFamily.LEAVES, defaultFamilyJson(List.of(
                    "minecraft:oak_leaves",
                    "minecraft:spruce_leaves",
                    "minecraft:birch_leaves",
                    "minecraft:jungle_leaves",
                    "minecraft:acacia_leaves",
                    "minecraft:dark_oak_leaves",
                    "minecraft:mangrove_leaves",
                    "minecraft:cherry_leaves",
                    "minecraft:azalea_leaves",
                    "minecraft:flowering_azalea_leaves"
            )),
            RustlingSpotFamily.CAVE, defaultFamilyJson(List.of(
                    // Stone family
                    "minecraft:stone",
                    "minecraft:andesite",
                    "minecraft:diorite",
                    "minecraft:granite",
                    "minecraft:tuff",
                    "minecraft:calcite",
                    "minecraft:smooth_basalt",
                    "minecraft:gravel",
                    "minecraft:deepslate",
                    "minecraft:cobbled_deepslate",
                    "minecraft:polished_deepslate",
                    "minecraft:deepslate_bricks",
                    "minecraft:cracked_deepslate_bricks",
                    "minecraft:deepslate_tiles",
                    "minecraft:cracked_deepslate_tiles",

                    // Ores (vanilla stone-based)
                    "minecraft:coal_ore",
                    "minecraft:iron_ore",
                    "minecraft:copper_ore",
                    "minecraft:gold_ore",
                    "minecraft:redstone_ore",
                    "minecraft:lapis_ore",
                    "minecraft:diamond_ore",
                    "minecraft:deepslate_coal_ore",
                    "minecraft:deepslate_iron_ore",
                    "minecraft:deepslate_copper_ore",
                    "minecraft:deepslate_gold_ore",
                    "minecraft:deepslate_redstone_ore",
                    "minecraft:deepslate_lapis_ore",
                    "minecraft:deepslate_diamond_ore",
                    "regions_unexplored:deepslate_grass_block",
                    "regions_unexplored:stone_grass_block"
            )),

            RustlingSpotFamily.FLYING, defaultFamilyJson(List.of(
                    "minecraft:grass_block",
                    "minecraft:dirt_path",
                    "minecraft:sand",
                    "minecraft:red_sand",
                    "minecraft:snow_block",
                    "minecraft:stone",
                    "minecraft:moss_block",
                    "biomeswevegone:lush_grass_block",
                    "regions_unexplored:argillite_grass_block",
                    "regions_unexplored:chalk_grass_block",
                    "regions_unexplored:silt_grass_block",
                    "regions_unexplored:peat_grass_block",
                    "regions_unexplored:alpha_grass_block",
                    "regions_unexplored:deepslate_grass_block",
                    "regions_unexplored:stone_grass_block"
            )),
            RustlingSpotFamily.NETHERFLAMME, defaultFamilyJson(List.of(
                    "minecraft:netherrack",
                    "minecraft:lava"
            )),
            RustlingSpotFamily.SOULFLAME, defaultFamilyJson(List.of(
                    "minecraft:soul_sand",
                    "minecraft:soul_soil"
            ))
    );


    private RustlingSpotFamilyConfigService() {
    }

    public static Optional<RustlingSpotFamily> resolve(BlockState state, Level level, BlockPos pos) {
        ensureLoaded();
        for (Map.Entry<RustlingSpotFamily, FamilyConfig> entry : CONFIGS.entrySet()) {
            if (entry.getValue().matches(state, level, pos, entry.getKey())) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public static void ensureDefaultsExist() {
        createDefaults();
    }

    public static void reload() {
        LOADED.set(false);
        ensureLoaded();
    }

    private static void ensureLoaded() {
        if (LOADED.getAndSet(true)) {
            return;
        }

        createDefaults();
        CONFIGS.clear();
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            Path file = FAMILY_DIR.resolve(family.name().toLowerCase() + ".json");
            CONFIGS.put(family, readFamily(file, family));
        }
    }

    private static void createDefaults() {
        try {
            Files.createDirectories(FAMILY_DIR);
            Files.createDirectories(BIOME_TAG_DIR);
            for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
                Path path = FAMILY_DIR.resolve(family.name().toLowerCase() + ".json");
                if (Files.notExists(path)) {
                    Files.writeString(path, DEFAULT_FAMILIES.getOrDefault(family, "{}"));
                }
                Path tagPath = BIOME_TAG_DIR.resolve(family.name().toLowerCase() + ".json");
                if (Files.notExists(tagPath)) {
                    TagFile file = new TagFile(false, DEFAULT_BIOME_HINTS.getOrDefault(family, List.of()));
                    Files.writeString(tagPath, GSON.toJson(file));
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to create rustling spot family defaults", e);
        }
    }

    private static FamilyConfig readFamily(Path file, RustlingSpotFamily family) {
        try (Reader reader = Files.newBufferedReader(file)) {
            FamilyFile data = GSON.fromJson(reader, FamilyFile.class);
            List<String> tagBiomeHints = readTagValues(family);
            return FamilyConfig.from(data, tagBiomeHints);
        } catch (IOException | JsonParseException e) {
            LOGGER.warn("Failed to read family config for {} from {}", family, file, e);
            return FamilyConfig.empty();
        }
    }

    private record FamilyConfig(List<BlockMatcher> blockMatchers, List<BiomeMatcher> biomeMatchers) {
        static FamilyConfig from(FamilyFile data, List<String> tagBiomeHints) {
            List<BlockMatcher> blocks = new ArrayList<>();
            if (data != null && data.surface_blocks != null) {
                for (String raw : data.surface_blocks) {
                    BlockMatcher matcher = BlockMatcher.parse(raw);
                    if (matcher != null) {
                        blocks.add(matcher);
                    }
                }
            }

            List<BiomeMatcher> biomes = new ArrayList<>();
            for (String raw : tagBiomeHints) {
                BiomeMatcher matcher = BiomeMatcher.parse(raw);
                if (matcher != null) {
                    biomes.add(matcher);
                }
            }

            return new FamilyConfig(blocks, biomes);
        }

        static FamilyConfig empty() {
            return new FamilyConfig(List.of(), List.of());
        }

        boolean matches(BlockState state, Level level, BlockPos pos, RustlingSpotFamily family) {
            boolean blockMatches = blockMatchers.stream().anyMatch(matcher -> matcher.matches(state, level, pos));
            if (!blockMatches) {
                return false;
            }

            if (family == RustlingSpotFamily.FLYING) {
                if (!level.canSeeSkyFromBelowWater(pos.above()) || !level.getBlockState(pos.above()).isAir()) {
                    return false;
                }
            }

            if (biomeMatchers.isEmpty()) {
                return true;
            }

            return biomeMatchers.stream().anyMatch(matcher -> matcher.matches(level, pos));
        }
    }

    private record BlockMatcher(Block block, TagKey<Block> tag) {
        static BlockMatcher parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String trimmed = raw.trim();
            if (trimmed.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(trimmed.substring(1));
                if (id != null) {
                    return new BlockMatcher(null, TagKey.create(Registries.BLOCK, id));
                }
                return null;
            }

            ResourceLocation id = ResourceLocation.tryParse(trimmed);
            if (id == null) {
                return null;
            }

            Block resolved = BuiltInRegistries.BLOCK.get(id);
            if (resolved == Blocks.AIR) {
                return null;
            }
            return new BlockMatcher(resolved, null);
        }

        boolean matches(BlockState state, Level level, BlockPos pos) {
            if (tag != null) {
                return state.is(tag);
            }

            if (block == Blocks.WATER) {
                return state.is(block) && state.getFluidState().isSource() && level.getBlockState(pos.above()).isAir();
            }
            if (block == Blocks.LAVA) {
                return state.is(block) && state.getFluidState().isSource() && level.getBlockState(pos.above()).isAir();
            }
            return state.is(block);
        }
    }

    private record BiomeMatcher(ResourceKey<net.minecraft.world.level.biome.Biome> biome,
                                TagKey<net.minecraft.world.level.biome.Biome> tag) {
        static BiomeMatcher parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String trimmed = raw.trim();
            if (trimmed.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(trimmed.substring(1));
                if (id != null) {
                    return new BiomeMatcher(null, TagKey.create(Registries.BIOME, id));
                }
                return null;
            }

            ResourceLocation id = ResourceLocation.tryParse(trimmed);
            if (id == null) {
                return null;
            }
            return new BiomeMatcher(ResourceKey.create(Registries.BIOME, id), null);
        }

        boolean matches(Level level, BlockPos pos) {
            var biomeHolder = level.getBiome(pos);
            if (tag != null) {
                return biomeHolder.is(tag);
            }
            return biome != null && biomeHolder.is(biome);
        }
    }

    private static class FamilyFile {
        List<String> surface_blocks;
    }

    private record TagFile(boolean replace, List<String> values) {
    }

    private static String defaultFamilyJson(List<String> blocks) {
        FamilyFile file = new FamilyFile();
        file.surface_blocks = blocks;
        return GSON.toJson(file);
    }

    private static List<String> readTagValues(RustlingSpotFamily family) {
        Path tagPath = BIOME_TAG_DIR.resolve(family.name().toLowerCase() + ".json");
        if (!Files.exists(tagPath)) {
            return DEFAULT_BIOME_HINTS.getOrDefault(family, List.of());
        }

        try (Reader reader = Files.newBufferedReader(tagPath)) {
            TagFile file = GSON.fromJson(reader, TagFile.class);
            if (file != null && file.values != null) {
                return new ArrayList<>(file.values);
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.warn("Failed to read biome tag config for {} from {}", family, tagPath, e);
        }
        return DEFAULT_BIOME_HINTS.getOrDefault(family, List.of());
    }

}
