package net.levelscraft7.rustlingspots.spot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.architectury.platform.Platform;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Loot pool loader backed by {@code config/rustlingspots/loot/global_loot.json} and
 * {@code config/rustlingspots/loot/families/<family>.json}.
 */
public final class LootPoolService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LootPoolService.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BASE = Platform.getConfigFolder().resolve("rustlingspots");
    private static final Path LOOT_DIR = BASE.resolve("loot");
    private static final Path GLOBAL_LOOT_FILE = LOOT_DIR.resolve("global_loot.json");
    private static final Path FAMILY_DIR = LOOT_DIR.resolve("families");
    private static final String DATAPACK_DIRECTORY = "rustling_spots/loot_families";
    private static final AtomicBoolean LOADED = new AtomicBoolean(false);
    private static final Map<String, List<LootEntry>> POOLS = new LinkedHashMap<>();
    private static final List<LootEntry> GLOBAL_POOL = new ArrayList<>();
    private static final Set<String> LOADED_FAMILIES = new HashSet<>();
    private static final Map<String, List<LootEntry>> DATAPACK_POOLS = new LinkedHashMap<>();
    private static final Set<String> DATAPACK_FAMILIES = new HashSet<>();

    private static final String DEFAULT_GLOBAL = """
        [
          { "item": "cobblemon:oran_berry", "min": 1, "max": 3, "weight": 4 },
          { "item": "cobblemon:poke_ball", "min": 1, "max": 1, "weight": 2 },
          { "item": "cobblemon:exp_candy_xs", "min": 1, "max": 1, "weight": 1 }
        ]
        """;


    private static final Map<RustlingSpotFamily, String> DEFAULT_FAMILY_LOOT = Map.of(
            RustlingSpotFamily.GRASS, """
                [
                  { "item": "cobblemon:revive", "min": 1, "max": 1, "weight": 1 },
                  { "item": "minecraft:wheat_seeds", "min": 2, "max": 6, "weight": 3 },
                  { "item": "minecraft:beetroot_seeds", "min": 1, "max": 4, "weight": 3 },
                  { "item": "minecraft:melon_seeds", "min": 1, "max": 3, "weight": 2 },
                  { "item": "minecraft:pumpkin_seeds", "min": 1, "max": 3, "weight": 2 },
                  { "item": "minecraft:sugar_cane", "min": 1, "max": 3, "weight": 2 },
                  { "item": "minecraft:feather", "min": 1, "max": 3, "weight": 2 },
                  { "item": "minecraft:flower_pot", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:black_apricorn_seed", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:red_apricorn_seed", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:yellow_apricorn_seed", "min": 1, "max": 3, "weight": 2 },
                  { "item": "cobblemon:green_apricorn_seed", "min": 1, "max": 3, "weight": 2 },
                  { "item": "cobblemon:oran_berry", "min": 1, "max": 3, "weight": 4 },
                  { "item": "cobblemon:sitrus_berry", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:lum_berry", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:vivichoke", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:medicinal_leek", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:energy_root", "min": 1, "max": 1, "weight": 1 },
                  { "item": "minecraft:apple", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:carrot", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:potato", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:wheat", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:dandelion", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:azure_bluet", "min": 1, "max": 2, "weight": 1 },
                  { "item": "minecraft:allium", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:poke_ball", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:nest_ball", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:grass_gem", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:exp_candy_xs", "min": 1, "max": 1, "weight": 1 }
                ]
                """,
            RustlingSpotFamily.LEAVES, """
                [
                  { "item": "cobblemon:exp_candy_s", "min": 1, "max": 1, "weight": 1 },
                  { "item": "minecraft:stick", "min": 1, "max": 3, "weight": 2 },
                  { "item": "minecraft:oak_sapling", "min": 1, "max": 2, "weight": 3 },
                  { "item": "minecraft:birch_sapling", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:jungle_sapling", "min": 1, "max": 2, "weight": 1 },
                  { "item": "minecraft:acacia_sapling", "min": 1, "max": 2, "weight": 1 },
                  { "item": "minecraft:apple", "min": 1, "max": 2, "weight": 3 },
                  { "item": "minecraft:vine", "min": 1, "max": 2, "weight": 3 },
                  { "item": "minecraft:glow_berries", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:honeycomb", "min": 1, "max": 1, "weight": 1 },
                  { "item": "minecraft:oak_leaves", "min": 1, "max": 3, "weight": 1 },
                  { "item": "cobblemon:red_apricorn", "min": 1, "max": 3, "weight": 4 },
                  { "item": "cobblemon:yellow_apricorn", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:green_apricorn", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:blue_apricorn", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:red_mint_leaf", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:blue_mint_leaf", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:green_mint_leaf", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:white_mint_leaf", "min": 1, "max": 2, "weight": 1 },
                  { "item": "cobblemon:cheri_berry", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:pecha_berry", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:pinap_berry", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:poke_ball", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:friend_ball", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:revival_herb", "min": 1, "max": 1, "weight": 1 }
                ]
                """,

            RustlingSpotFamily.WATER, """
                [
                  { "item": "minecraft:prismarine_shard", "min": 1, "max": 3, "weight": 1 },
                  { "item": "minecraft:cod", "min": 2, "max": 5, "weight": 4 },
                  { "item": "minecraft:salmon", "min": 2, "max": 5, "weight": 4 },
                  { "item": "minecraft:tropical_fish", "min": 1, "max": 3, "weight": 3 },
                  { "item": "minecraft:kelp", "min": 2, "max": 6, "weight": 4 },
                  { "item": "minecraft:seagrass", "min": 1, "max": 4, "weight": 3 },
                  { "item": "minecraft:ink_sac", "min": 1, "max": 4, "weight": 3 },
                  { "item": "cobblemon:razz_berry", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:passho_berry", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:water_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:poke_ball", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:net_ball", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:dive_ball", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:potion", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:antidote", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:fossilized_fish", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:nautilus_shell", "min": 1, "max": 1, "weight": 1 }
                ]
                """,
            RustlingSpotFamily.SAND, """
                [
                  { "item": "minecraft:feather", "min": 1, "max": 2, "weight": 1 },
                  { "item": "minecraft:flint", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:gunpowder", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:glass_bottle", "min": 1, "max": 2, "weight": 1 },
                  { "item": "minecraft:sand", "min": 3, "max": 8, "weight": 4 },
                  { "item": "minecraft:red_sand", "min": 2, "max": 6, "weight": 3 },
                  { "item": "minecraft:cactus", "min": 1, "max": 3, "weight": 3 },
                  { "item": "minecraft:dead_bush", "min": 1, "max": 2, "weight": 3 },
                  { "item": "minecraft:bone", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:razz_berry", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:figy_berry", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:fire_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:ground_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:rock_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:poke_ball", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:quick_ball", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:root_fossil", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:claw_fossil", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:jaw_fossil", "min": 1, "max": 1, "weight": 1 }
                ]
                """,
            RustlingSpotFamily.SNOW, """
                [
                  { "item": "minecraft:snowball", "min": 1, "max": 3, "weight": 2 },
                  { "item": "minecraft:blue_ice", "min": 1, "max": 1, "weight": 1 },
                  { "item": "minecraft:rabbit_foot", "min": 1, "max": 1, "weight": 1 },
                  { "item": "minecraft:rabbit_hide", "min": 1, "max": 1, "weight": 1 },
                  { "item": "minecraft:ice", "min": 1, "max": 3, "weight": 3 },
                  { "item": "minecraft:packed_ice", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:aspear_berry", "min": 1, "max": 3, "weight": 4 },
                  { "item": "cobblemon:leppa_berry", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:lum_berry", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:cooked_mutton", "min": 1, "max": 3, "weight": 3 },
                  { "item": "minecraft:cooked_beef", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:ice_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:potion", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:full_heal", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:ice_stone", "min": 1, "max": 1, "weight": 1 }
                ]
                """,
            RustlingSpotFamily.CAVE, """
                [
                  { "item": "minecraft:iron_ingot", "min": 1, "max": 3, "weight": 3 },
                  { "item": "minecraft:coal", "min": 3, "max": 8, "weight": 5 },
                  { "item": "minecraft:raw_iron", "min": 1, "max": 4, "weight": 4 },
                  { "item": "minecraft:raw_copper", "min": 1, "max": 4, "weight": 4 },
                  { "item": "cobblemon:tumblestone", "min": 1, "max": 4, "weight": 5 },
                  { "item": "cobblemon:black_tumblestone", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:sky_tumblestone", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:torch", "min": 2, "max": 6, "weight": 4 },
                  { "item": "minecraft:bread", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:oran_berry", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:sitrus_berry", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:remedy", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:antidote", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:paralyze_heal", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:poke_ball", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:great_ball", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:dusk_ball", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:rock_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:ground_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:exp_candy_xs", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:helix_fossil", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:dome_fossil", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:skull_fossil", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:armor_fossil", "min": 1, "max": 1, "weight": 1 },
                  { "item": "minecraft:iron_nugget", "min": 3, "max": 8, "weight": 3 },
                  { "item": "minecraft:gold_nugget", "min": 2, "max": 5, "weight": 2 },
                  { "item": "minecraft:raw_gold", "min": 1, "max": 3, "weight": 1 },
                  { "item": "minecraft:redstone", "min": 2, "max": 6, "weight": 2 },
                  { "item": "minecraft:lapis_lazuli", "min": 2, "max": 5, "weight": 2 },
                  { "item": "minecraft:amethyst_shard", "min": 1, "max": 3, "weight": 1 },
                  { "item": "minecraft:copper_ingot", "min": 1, "max": 3, "weight": 2 },
                  { "item": "minecraft:flint", "min": 1, "max": 3, "weight": 3 },
                  { "item": "minecraft:arrow", "min": 2, "max": 6, "weight": 2 },
                  { "item": "minecraft:bone", "min": 1, "max": 3, "weight": 2 },
                  { "item": "minecraft:string", "min": 1, "max": 3, "weight": 2 },
                  { "item": "minecraft:rail", "min": 3, "max": 8, "weight": 2 },
                  { "item": "minecraft:stone_pickaxe", "min": 1, "max": 1, "weight": 1 }
                ]
                """,
            RustlingSpotFamily.FLYING, """
                [
                  { "item": "minecraft:feather", "min": 2, "max": 6, "weight": 5 },
                  { "item": "minecraft:egg", "min": 1, "max": 3, "weight": 4 },
                  { "item": "minecraft:ghast_tear", "min": 1, "max": 2, "weight": 1 },
                  { "item": "minecraft:phantom_membrane", "min": 1, "max": 2, "weight": 1 },
                  { "item": "minecraft:string", "min": 1, "max": 4, "weight": 3 },
                  { "item": "minecraft:paper", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:health_feather", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:muscle_feather", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:resist_feather", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:genius_feather", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:clever_feather", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:swift_feather", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:flying_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:razz_berry", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:leppa_berry", "min": 1, "max": 2, "weight": 2 },
                  { "item": "minecraft:bread", "min": 1, "max": 3, "weight": 3 },
                  { "item": "minecraft:cooked_chicken", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:poke_ball", "min": 1, "max": 3, "weight": 3 },
                  { "item": "cobblemon:great_ball", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:quick_ball", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:potion", "min": 1, "max": 2, "weight": 2 },
                  { "item": "cobblemon:full_heal", "min": 1, "max": 1, "weight": 1 }
                ]
                """,
            RustlingSpotFamily.NETHERFLAMME, """
                [
                  { "item": "minecraft:magma_cream", "min": 1, "max": 3, "weight": 5 },
                  { "item": "minecraft:blaze_powder", "min": 1, "max": 2, "weight": 4 },
                  { "item": "minecraft:nether_wart", "min": 1, "max": 4, "weight": 4 },
                  { "item": "minecraft:coal", "min": 2, "max": 6, "weight": 4 },
                  { "item": "minecraft:fire_charge", "min": 1, "max": 2, "weight": 3 },
                  { "item": "minecraft:basalt", "min": 2, "max": 6, "weight": 4 },
                  { "item": "minecraft:blackstone", "min": 2, "max": 6, "weight": 4 },
                  { "item": "minecraft:gold_nugget", "min": 2, "max": 6, "weight": 3 },
                  { "item": "cobblemon:razz_berry", "min": 1, "max": 3, "weight": 4 },
                  { "item": "cobblemon:figy_berry", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:fire_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:poke_ball", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:great_ball", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:potion", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:burn_heal", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:exp_candy_xs", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:fire_stone", "min": 1, "max": 1, "weight": 1 }
                ]
                """,
            RustlingSpotFamily.SOULFLAME, """
                [
                  { "item": "minecraft:soul_sand", "min": 2, "max": 6, "weight": 5 },
                  { "item": "minecraft:soul_soil", "min": 2, "max": 6, "weight": 5 },
                  { "item": "minecraft:bone_meal", "min": 2, "max": 6, "weight": 3 },
                  { "item": "minecraft:soul_torch", "min": 1, "max": 3, "weight": 4 },
                  { "item": "minecraft:bone", "min": 1, "max": 3, "weight": 4 },
                  { "item": "minecraft:coal", "min": 2, "max": 5, "weight": 3 },
                  { "item": "minecraft:echo_shard", "min": 1, "max": 1, "weight": 1 },
                  { "item": "cobblemon:spell_tag", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:ghost_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:dark_gem", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:razz_berry", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:kasib_berry", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:poke_ball", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:dusk_ball", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:potion", "min": 1, "max": 2, "weight": 3 },
                  { "item": "cobblemon:full_heal", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:exp_candy_xs", "min": 1, "max": 1, "weight": 2 },
                  { "item": "cobblemon:dusk_stone", "min": 1, "max": 1, "weight": 1 }
                ]
                """
    );


    private LootPoolService() {
    }

    public static void ensureDefaultsExist() {
        createDefaults();
    }

    public static void reload() {
        LOADED.set(false);
        ensureLoaded();
    }

    public static boolean hasFamily(String family) {
        ensureLoaded();
        String key = normalizeFamilyKey(family);
        return !key.isEmpty() && (LOADED_FAMILIES.contains(key) || DATAPACK_FAMILIES.contains(key));
    }

    public static void reloadDatapack(ResourceManager resourceManager) {
        DATAPACK_POOLS.clear();
        DATAPACK_FAMILIES.clear();

        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                DATAPACK_DIRECTORY,
                resourceLocation -> resourceLocation.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            String familyKey = familyKeyFromDatapackResource(entry.getKey(), DATAPACK_DIRECTORY);
            if (familyKey == null) {
                LOGGER.warn("[Rustling Spots] Failed loading datapack loot family from {}: invalid file path", entry.getKey());
                continue;
            }

            List<LootEntry> entries = new ArrayList<>(GLOBAL_POOL);
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                LootEntryData[] data = GSON.fromJson(reader, LootEntryData[].class);
                if (data != null) {
                    for (LootEntryData lootData : data) {
                        toEntry(lootData).ifPresent(entries::add);
                    }
                }

                entries = mergeDuplicateEntries(entries);
                if (!entries.isEmpty()) {
                    DATAPACK_POOLS.put(familyKey, entries);
                }
                DATAPACK_FAMILIES.add(familyKey);
            } catch (IOException | JsonParseException e) {
                LOGGER.warn("[Rustling Spots] Failed to read datapack loot family {} from {}", familyKey, entry.getKey(), e);
            }
        }
    }

    public static ItemStack pickLoot(String family, RandomSource random) {
        ensureLoaded();
        String familyKey = family != null ? normalizeFamilyKey(family) : null;
        List<LootEntry> pool = familyKey != null
                ? DATAPACK_POOLS.getOrDefault(familyKey, POOLS.getOrDefault(familyKey, List.of()))
                : GLOBAL_POOL;
        if (pool.isEmpty()) {
            if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                LOGGER.debug("No configured loot entries for family {}", familyKey != null ? familyKey : "global");
            }
            return ItemStack.EMPTY;
        }

        int totalWeight = pool.stream().mapToInt(LootEntry::weight).sum();
        if (totalWeight <= 0) {
            return ItemStack.EMPTY;
        }

        int roll = random.nextInt(totalWeight);
        int accumulator = 0;
        for (LootEntry entry : pool) {
            accumulator += entry.weight();
            if (roll < accumulator) {
                int amount = entry.min() + random.nextInt(entry.max() - entry.min() + 1);
                ItemStack stack = new ItemStack(entry.item(), amount);
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static void ensureLoaded() {
        if (LOADED.getAndSet(true)) {
            return;
        }

        createDefaults();
        POOLS.clear();
        GLOBAL_POOL.clear();
        LOADED_FAMILIES.clear();
        List<LootEntry> global = readLootFile(GLOBAL_LOOT_FILE, "global loot");
        GLOBAL_POOL.addAll(mergeDuplicateEntries(global));
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            String familyKey = family.serializedName();
            List<LootEntry> entries = new ArrayList<>(global);
            Path familyFile = familyPath(familyKey);
            entries.addAll(readLootFile(familyFile, familyKey + " loot"));
            entries = mergeDuplicateEntries(entries);
            if (!entries.isEmpty()) {
                POOLS.put(familyKey, entries);
            }
            LOADED_FAMILIES.add(familyKey);
        }

        try (var paths = Files.walk(FAMILY_DIR)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        String familyKey = keyForPath(path);
                        if (familyKey == null || LOADED_FAMILIES.contains(familyKey)) {
                            return;
                        }

                        List<LootEntry> entries = new ArrayList<>(global);
                        entries.addAll(readLootFile(path, familyKey + " loot"));
                        entries = mergeDuplicateEntries(entries);
                        if (!entries.isEmpty()) {
                            POOLS.put(familyKey, entries);
                        }
                        LOADED_FAMILIES.add(familyKey);
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to scan custom loot family files under {}", FAMILY_DIR, e);
        }
    }

    private static void createDefaults() {
        try {
            Files.createDirectories(FAMILY_DIR);
            if (Files.notExists(GLOBAL_LOOT_FILE)) {
                Files.writeString(GLOBAL_LOOT_FILE, DEFAULT_GLOBAL);
            }
            for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
                Path file = familyPath(family.serializedName());
                Files.createDirectories(file.getParent());
                if (Files.notExists(file)) {
                    String template = DEFAULT_FAMILY_LOOT.getOrDefault(family, "[]");
                    Files.writeString(file, template);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to create default loot configuration", e);
        }
    }

    private static List<LootEntry> readLootFile(Path path, String label) {
        List<LootEntry> parsed = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path)) {
            LootEntryData[] data = GSON.fromJson(reader, LootEntryData[].class);
            if (data != null) {
                for (LootEntryData lootData : data) {
                    toEntry(lootData).ifPresent(parsed::add);
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.warn("Failed to read {} from {}", label, path, e);
        }
        return parsed;
    }

    private static Optional<LootEntry> toEntry(LootEntryData data) {
        if (data == null || data.item == null || data.weight <= 0) {
            return Optional.empty();
        }

        ResourceLocation id = ResourceLocation.tryParse(data.item);
        if (id == null) {
            LOGGER.warn("Invalid item id '{}' in loot configuration", data.item);
            return Optional.empty();
        }

        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) {
            LOGGER.warn("Unknown item '{}' in loot configuration", id);
            return Optional.empty();
        }

        int min = Math.max(1, data.min);
        int max = Math.max(min, data.max);
        return Optional.of(new LootEntry(item, min, max, data.weight));
    }

    private static List<LootEntry> mergeDuplicateEntries(List<LootEntry> entries) {
        Map<LootEntryKey, Integer> mergedWeights = new LinkedHashMap<>();
        for (LootEntry entry : entries) {
            LootEntryKey key = new LootEntryKey(entry.item(), entry.min(), entry.max());
            mergedWeights.merge(key, entry.weight(), Integer::sum);
        }

        List<LootEntry> merged = new ArrayList<>(mergedWeights.size());
        for (Map.Entry<LootEntryKey, Integer> entry : mergedWeights.entrySet()) {
            LootEntryKey key = entry.getKey();
            merged.add(new LootEntry(key.item(), key.min(), key.max(), entry.getValue()));
        }
        return merged;
    }

    private record LootEntry(Item item, int min, int max, int weight) {
    }

    private record LootEntryKey(Item item, int min, int max) {
    }

    private static class LootEntryData {
        String item;
        int min = 1;
        int max = 1;
        int weight = 1;
    }

    private static String normalizeFamilyKey(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    private static Path familyPath(String familyKey) {
        String normalized = normalizeFamilyKey(familyKey);
        if (normalized.contains(":")) {
            String[] split = normalized.split(":", 2);
            return FAMILY_DIR.resolve(split[0]).resolve(split[1] + ".json");
        }
        return FAMILY_DIR.resolve(normalized + ".json");
    }

    private static String keyForPath(Path path) {
        Path relative;
        try {
            relative = FAMILY_DIR.relativize(path);
        } catch (IllegalArgumentException ex) {
            return null;
        }

        String normalized = relative.toString().replace('\\', '/');
        if (!normalized.endsWith(".json")) {
            return null;
        }
        normalized = normalized.substring(0, normalized.length() - 5);
        int slash = normalized.indexOf('/');
        if (slash >= 0) {
            return normalized.substring(0, slash) + ":" + normalized.substring(slash + 1);
        }
        return normalized;
    }

    private static String familyKeyFromDatapackResource(ResourceLocation fileId, String directory) {
        String prefix = directory + "/";
        if (!fileId.getPath().startsWith(prefix) || !fileId.getPath().endsWith(".json")) {
            return null;
        }

        String relative = fileId.getPath().substring(prefix.length(), fileId.getPath().length() - 5);
        if (relative.isBlank()) {
            return null;
        }
        return normalizeFamilyKey(fileId.getNamespace() + ":" + relative);
    }
}
