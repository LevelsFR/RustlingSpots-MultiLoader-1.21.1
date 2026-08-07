package net.levelscraft7.rustlingspots.spot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.architectury.platform.Platform;
import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Pokemon pool loader backed by {@code config/rustlingspots/pokemon/families/<family>.json}.
 */
public final class PokemonPoolService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonPoolService.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BASE = Platform.getConfigFolder().resolve("rustlingspots");
    private static final Path POKEMON_DIR = BASE.resolve("pokemon");
    private static final Path FAMILY_DIR = POKEMON_DIR.resolve("families");
    private static final String DATAPACK_DIRECTORY = "rustling_spots/pokemon_families";
    private static final String LAVA_FAMILY_KEY = "netherflamme_lava";
    private static final AtomicBoolean LOADED = new AtomicBoolean(false);
    private static final List<PokemonEntry> ENTRIES = new ArrayList <> ();
    private static final Set<String> LOADED_FAMILIES = new HashSet<>();
    private static final List<PokemonEntry> DATAPACK_ENTRIES = new ArrayList<>();
    private static final Set<String> DATAPACK_FAMILIES = new HashSet<>();
    private static SpeciesValidator speciesValidator = SpeciesValidator.unavailable("Cobblemon species API is not available on this loader");

    private static final Map<String, String> V4_1_DEFAULT_SHA256 = Map.of(
            "grass", "6997af0dc0d5e43ef392bc3ee1adbb1c6fd3903c7891a0bf9ed10c47ca3fc67b",
            "water", "aaeb4ca658251d24d822f9bf0e2fa29ea1a6f0318ebbe42b5501edbdc40b05bb",
            "sand", "f85b2eaa79f7b9775732ad0883cfc70e8b7e7a881d92c134c93914f84e8a3d9d",
            "snow", "d4b76b93eed01512d5a2cc95a8217eae07f8d9237158934004fbf6fb6ad6a9f2",
            "leaves", "dd9905f3163eb33ff75444d2dd7733e1e5096e1b5b70b626e69cf4c1d700bb51",
            "cave", "fa18cf562626b3cb549090f54bccb8d91f241c073c230a67d286582622757873",
            "flying", "654555dc49e104be396eac984607f8a44d0be06e0119ee86ac3eb4fe74669e47",
            "netherflamme", "9952fb8fd91064215c2bb8701193adc276d59852155b3e686123d8946fad48f3",
            "soulflame", "f11c2127fe3bfad256dea55b24e02fb6d9720b6e8fd6818b25f496c1435eddfd"
    );

    private static final Map<String, String> SPECIES_RENAMES = Map.ofEntries(
            Map.entry("treeko", "treecko"),
            Map.entry("tailow", "taillow"),
            Map.entry("wingle", "wingull"),
            Map.entry("crobats", "crobat"),
            Map.entry("arania", "ariados"),
            Map.entry("paldean-wooper", "wooper paldean"),
            Map.entry("vulpix-alolan", "vulpix alolan"),
            Map.entry("growlithe-hisuian", "growlithe hisuian"),
            Map.entry("avalugg-hisuian", "avalugg hisuian"),
            Map.entry("deerling-winter", "deerling")
    );

    private static final Set<String> REMOVED_DEFAULT_SPECIES = Set.of("rowlet-hisuian");

    private static final Map<RustlingSpotFamily, String> DEFAULT_FAMILY_V4_1 = java.util.Map.of(
            RustlingSpotFamily.GRASS, """
    [
  { "species": "oddish",      "weight": 12, "min_level": 1,  "max_level": 20 },
  { "species": "bellsprout",  "weight": 12, "min_level": 1,  "max_level": 20 },
  { "species": "hoppip",      "weight": 12, "min_level": 1,  "max_level": 20 },
  { "species": "sunkern",     "weight": 11, "min_level": 1,  "max_level": 18 },
  { "species": "budew",       "weight": 11, "min_level": 1,  "max_level": 20 },
  { "species": "petilil",     "weight": 11, "min_level": 1,  "max_level": 20 },
  { "species": "cottonee",    "weight": 11, "min_level": 1,  "max_level": 20 },
  { "species": "gossifleur",  "weight": 11, "min_level": 1,  "max_level": 20 },
  { "species": "smoliv",      "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "applin",      "weight": 10, "min_level": 1,  "max_level": 22 },

  { "species": "seedot",      "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "shroomish",   "weight": 10, "min_level": 1,  "max_level": 22 },
  { "species": "skiddo",      "weight": 9,  "min_level": 1,  "max_level": 24 },
  { "species": "bounsweet",   "weight": 9,  "min_level": 1,  "max_level": 20 },

  { "species": "bulbasaur",   "weight": 6,  "min_level": 1,  "max_level": 20 },
  { "species": "chikorita",   "weight": 6,  "min_level": 1,  "max_level": 20 },
  { "species": "treecko",     "weight": 6,  "min_level": 1,  "max_level": 20 },
  { "species": "turtwig",     "weight": 6,  "min_level": 1,  "max_level": 20 },
  { "species": "chespin",     "weight": 6,  "min_level": 1,  "max_level": 20 },
  { "species": "sprigatito",  "weight": 6,  "min_level": 1,  "max_level": 20 },
  { "species": "rowlet",      "weight": 6,  "min_level": 1,  "max_level": 20 },

  { "species": "nuzleaf",     "weight": 5,  "min_level": 18, "max_level": 40 },
  { "species": "tangela",     "weight": 5,  "min_level": 15, "max_level": 35 },
  { "species": "lombre",      "weight": 5,  "min_level": 18, "max_level": 40 },

  { "species": "arboliva",    "weight": 3,  "min_level": 30, "max_level": 60 },
  { "species": "leafeon",     "weight": 2,  "min_level": 30, "max_level": 60 },

  { "species": "zigzagoon",   "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "skwovet",     "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "bidoof",      "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "sentret",     "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "patrat",      "weight": 9,  "min_level": 1,  "max_level": 18 },
  { "species": "buneary",     "weight": 9,  "min_level": 1,  "max_level": 18 },
  { "species": "minccino",    "weight": 9,  "min_level": 1,  "max_level": 18 },
  { "species": "deerling",    "weight": 9,  "min_level": 1,  "max_level": 20 },
  { "species": "stantler",    "weight": 8,  "min_level": 1,  "max_level": 30 },

  { "species": "pidgey",      "weight": 9,  "min_level": 1,  "max_level": 18 },
  { "species": "starly",      "weight": 9,  "min_level": 1,  "max_level": 18 },
  { "species": "fletchling",  "weight": 9,  "min_level": 1,  "max_level": 18 },
  { "species": "swablu",      "weight": 7,  "min_level": 10, "max_level": 30 },

  { "species": "combee",      "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "scatterbug",  "weight": 10, "min_level": 1,  "max_level": 15 },
  { "species": "grubbin",     "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "venonat",     "weight": 9,  "min_level": 1,  "max_level": 20 },
  { "species": "kricketot",   "weight": 10, "min_level": 1,  "max_level": 15 },
  { "species": "dwebble",     "weight": 9,  "min_level": 1,  "max_level": 20 },

  { "species": "heracross",   "weight": 2,  "min_level": 20, "max_level": 50 },
  { "species": "larvesta",    "weight": 2,  "min_level": 20, "max_level": 50 },

  { "species": "luxio",       "weight": 5,  "min_level": 18, "max_level": 40 },
  { "species": "pikachu",     "weight": 4,  "min_level": 12, "max_level": 30 },
  { "species": "eevee",       "weight": 4,  "min_level": 10, "max_level": 25 }
    ]
""",
            RustlingSpotFamily.WATER, """
    [
  { "species": "magikarp",   "weight": 12, "min_level": 1,  "max_level": 20 },
  { "species": "poliwag",    "weight": 11, "min_level": 1,  "max_level": 22 },
  { "species": "psyduck",    "weight": 11, "min_level": 1,  "max_level": 22 },
  { "species": "buizel",     "weight": 11, "min_level": 1,  "max_level": 22 },
  { "species": "lotad",      "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "marill",     "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "goldeen",    "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "finneon",    "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "barboach",   "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "wooper",     "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "shellos",    "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "tentacool",  "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "wingull",    "weight": 9,  "min_level": 1,  "max_level": 20 },
  { "species": "ducklett",   "weight": 9,  "min_level": 1,  "max_level": 22 },
  { "species": "staryu",     "weight": 9,  "min_level": 1,  "max_level": 22 },
  { "species": "shellder",   "weight": 9,  "min_level": 1,  "max_level": 22 },
  { "species": "clauncher",  "weight": 9,  "min_level": 1,  "max_level": 24 },
  { "species": "qwilfish",   "weight": 9,  "min_level": 1,  "max_level": 22 },
  { "species": "frillish",   "weight": 8,  "min_level": 1,  "max_level": 25 },
  { "species": "feebas",     "weight": 6,  "min_level": 1,  "max_level": 20 },

  { "species": "floatzel",   "weight": 6,  "min_level": 20, "max_level": 50 },
  { "species": "lombre",     "weight": 6,  "min_level": 18, "max_level": 45 },
  { "species": "azumarill",  "weight": 6,  "min_level": 20, "max_level": 50 },
  { "species": "slowbro",    "weight": 5,  "min_level": 20, "max_level": 50 },
  { "species": "seaking",    "weight": 5,  "min_level": 20, "max_level": 50 },
  { "species": "tentacruel", "weight": 5,  "min_level": 22, "max_level": 55 },
  { "species": "pelipper",   "weight": 5,  "min_level": 22, "max_level": 50 },
  { "species": "lumineon",   "weight": 5,  "min_level": 22, "max_level": 50 },
  { "species": "whiscash",   "weight": 5,  "min_level": 22, "max_level": 50 },
  { "species": "quagsire",   "weight": 5,  "min_level": 20, "max_level": 50 },
  { "species": "gastrodon",  "weight": 5,  "min_level": 22, "max_level": 50 },
  { "species": "clawitzer",  "weight": 4,  "min_level": 25, "max_level": 55 },
  { "species": "jellicent",  "weight": 4,  "min_level": 25, "max_level": 55 },
  { "species": "swanna",     "weight": 4,  "min_level": 22, "max_level": 50 },

  { "species": "milotic",    "weight": 2,  "min_level": 30, "max_level": 60 },
  { "species": "starmie",    "weight": 3,  "min_level": 25, "max_level": 55 },
  { "species": "cloyster",   "weight": 3,  "min_level": 25, "max_level": 55 },

  { "species": "totodile",   "weight": 6,  "min_level": 1,  "max_level": 20 },
  { "species": "squirtle",   "weight": 5,  "min_level": 1,  "max_level": 20 },
  { "species": "piplup",     "weight": 5,  "min_level": 1,  "max_level": 20 },
  { "species": "froakie",    "weight": 5,  "min_level": 1,  "max_level": 20 },
  { "species": "sobble",     "weight": 5,  "min_level": 1,  "max_level": 20 },
  { "species": "oshawott",   "weight": 5,  "min_level": 1,  "max_level": 20 },
  { "species": "popplio",    "weight": 5,  "min_level": 1,  "max_level": 20 },

  { "species": "croconaw",   "weight": 3,  "min_level": 20, "max_level": 45 },
  { "species": "wartortle",  "weight": 3,  "min_level": 20, "max_level": 45 },
  { "species": "prinplup",   "weight": 3,  "min_level": 18, "max_level": 45 },
  { "species": "frogadier",  "weight": 3,  "min_level": 20, "max_level": 45 },
  { "species": "drizzile",   "weight": 3,  "min_level": 20, "max_level": 45 },
  { "species": "dewott",     "weight": 3,  "min_level": 20, "max_level": 45 },
  { "species": "brionne",    "weight": 3,  "min_level": 20, "max_level": 45 },

  { "species": "gyarados",   "weight": 2,  "min_level": 30, "max_level": 70 },
  { "species": "lapras",     "weight": 2,  "min_level": 35, "max_level": 70 },
  { "species": "vaporeon",   "weight": 2,  "min_level": 30, "max_level": 65 },

  { "species": "empoleon",   "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "blastoise",  "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "greninja",   "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "inteleon",   "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "samurott",   "weight": 2,  "min_level": 40, "max_level": 70 },

  { "species": "palafin",    "weight": 1,  "min_level": 35, "max_level": 65 }
    ]
""",
            RustlingSpotFamily.SAND, """
    [
  { "species": "sandshrew",   "weight": 12, "min_level": 1,  "max_level": 22 },
  { "species": "diglett",     "weight": 12, "min_level": 1,  "max_level": 20 },
  { "species": "sandile",     "weight": 11, "min_level": 1,  "max_level": 24 },
  { "species": "trapinch",    "weight": 11, "min_level": 1,  "max_level": 24 },
  { "species": "hippopotas",  "weight": 10, "min_level": 1,  "max_level": 26 },
  { "species": "drilbur",     "weight": 10, "min_level": 1,  "max_level": 24 },

  { "species": "cacnea",      "weight": 9,  "min_level": 1,  "max_level": 24 },
  { "species": "helioptile",  "weight": 9,  "min_level": 1,  "max_level": 22 },
  { "species": "numel",       "weight": 9,  "min_level": 1,  "max_level": 24 },

  { "species": "vulpix",      "weight": 8,  "min_level": 1,  "max_level": 22 },
  { "species": "growlithe",   "weight": 8,  "min_level": 1,  "max_level": 22 },
  { "species": "slugma",      "weight": 7,  "min_level": 5,  "max_level": 28 },
  { "species": "houndour",    "weight": 7,  "min_level": 5,  "max_level": 30 },

  { "species": "baltoy",      "weight": 8,  "min_level": 1,  "max_level": 26 },
  { "species": "yamask",      "weight": 6,  "min_level": 10, "max_level": 30 },

  { "species": "geodude",     "weight": 8,  "min_level": 1,  "max_level": 26 },
  { "species": "rockruff",    "weight": 8,  "min_level": 1,  "max_level": 24 },
  { "species": "rhyhorn",     "weight": 7,  "min_level": 10, "max_level": 35 },

  { "species": "skorupi",     "weight": 7,  "min_level": 8,  "max_level": 26 },
  { "species": "stunky",      "weight": 7,  "min_level": 8,  "max_level": 24 },
  { "species": "grimer",      "weight": 7,  "min_level": 10, "max_level": 26 },

  { "species": "maractus",    "weight": 5,  "min_level": 20, "max_level": 45 },

  { "species": "dugtrio",     "weight": 6,  "min_level": 20, "max_level": 50 },
  { "species": "krokorok",    "weight": 6,  "min_level": 20, "max_level": 50 },
  { "species": "vibrava",     "weight": 6,  "min_level": 20, "max_level": 50 },
  { "species": "hippowdon",   "weight": 5,  "min_level": 30, "max_level": 60 },
  { "species": "excadrill",   "weight": 5,  "min_level": 30, "max_level": 60 },
  { "species": "camerupt",    "weight": 5,  "min_level": 30, "max_level": 60 },

  { "species": "lycanroc",    "weight": 4,  "min_level": 25, "max_level": 55 },
  { "species": "onix",        "weight": 4,  "min_level": 20, "max_level": 50 },

  { "species": "cacturne",    "weight": 4,  "min_level": 25, "max_level": 55 },
  { "species": "heliolisk",   "weight": 4,  "min_level": 25, "max_level": 55 },

  { "species": "flygon",      "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "krookodile",  "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "rhyperior",   "weight": 2,  "min_level": 45, "max_level": 75 },

  { "species": "volcarona",   "weight": 1,  "min_level": 50, "max_level": 75 }
    ]
""",
            RustlingSpotFamily.SNOW, """
    [
  { "species": "swinub",     "weight": 12, "min_level": 1,  "max_level": 25 },
  { "species": "snorunt",    "weight": 12, "min_level": 1,  "max_level": 25 },
  { "species": "cubchoo",    "weight": 12, "min_level": 1,  "max_level": 25 },
  { "species": "bergmite",   "weight": 11, "min_level": 1,  "max_level": 28 },
  { "species": "spheal",     "weight": 11, "min_level": 1,  "max_level": 25 },
  { "species": "snom",       "weight": 11, "min_level": 1,  "max_level": 22 },
  { "species": "snover",     "weight": 11, "min_level": 1,  "max_level": 25 },
  { "species": "vanillite",  "weight": 10, "min_level": 1,  "max_level": 25 },
  { "species": "cryogonal",  "weight": 9,  "min_level": 10, "max_level": 40 },
  { "species": "delibird",   "weight": 9,  "min_level": 10, "max_level": 35 },

  { "species": "deerling",   "weight": 9,  "min_level": 1,  "max_level": 25 },
  { "species": "stantler",   "weight": 8,  "min_level": 1,  "max_level": 30 },
  { "species": "teddiursa",  "weight": 8,  "min_level": 1,  "max_level": 25 },

  { "species": "piplup",     "weight": 9,  "min_level": 1,  "max_level": 20 },
  { "species": "spheal",     "weight": 8,  "min_level": 1,  "max_level": 25 },

  { "species": "sneasel",    "weight": 8,  "min_level": 10, "max_level": 35 },
  { "species": "snorunt",    "weight": 7,  "min_level": 1,  "max_level": 25 },

  { "species": "piloswine",  "weight": 6,  "min_level": 25, "max_level": 55 },
  { "species": "glalie",     "weight": 6,  "min_level": 25, "max_level": 55 },
  { "species": "froslass",   "weight": 5,  "min_level": 25, "max_level": 55 },
  { "species": "beartic",    "weight": 6,  "min_level": 25, "max_level": 55 },
  { "species": "avalugg",    "weight": 5,  "min_level": 30, "max_level": 60 },
  { "species": "walrein",    "weight": 5,  "min_level": 30, "max_level": 60 },
  { "species": "abomasnow",  "weight": 5,  "min_level": 30, "max_level": 60 },
  { "species": "vanillish",  "weight": 5,  "min_level": 25, "max_level": 55 },

  { "species": "empoleon",   "weight": 4,  "min_level": 35, "max_level": 65 },
  { "species": "samurott",   "weight": 3,  "min_level": 35, "max_level": 65 },

  { "species": "amaura",     "weight": 3,  "min_level": 20, "max_level": 50 },
  { "species": "aurorus",    "weight": 2,  "min_level": 35, "max_level": 65 },

  { "species": "mamoswine",  "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "weavile",    "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "vanilluxe",  "weight": 2,  "min_level": 40, "max_level": 70 },

  { "species": "lapras",     "weight": 1,  "min_level": 40, "max_level": 70 }
    ]
""",
            RustlingSpotFamily.LEAVES, """
    [
  { "species": "scatterbug", "weight": 12, "min_level": 1,  "max_level": 15 },
  { "species": "grubbin",    "weight": 11, "min_level": 1,  "max_level": 18 },
  { "species": "combee",     "weight": 11, "min_level": 1,  "max_level": 16 },
  { "species": "hoppip",     "weight": 10, "min_level": 1,  "max_level": 16 },

  { "species": "pidgey",     "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "starly",     "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "fletchling", "weight": 10, "min_level": 1,  "max_level": 18 },
  { "species": "hoothoot",   "weight": 9,  "min_level": 1,  "max_level": 18 },
  { "species": "natu",       "weight": 9,  "min_level": 1,  "max_level": 18 },

  { "species": "skwovet",    "weight": 10, "min_level": 1,  "max_level": 16 },
  { "species": "zigzagoon",  "weight": 10, "min_level": 1,  "max_level": 16 },
  { "species": "buneary",    "weight": 9,  "min_level": 1,  "max_level": 16 },
  { "species": "minccino",   "weight": 9,  "min_level": 1,  "max_level": 16 },
  { "species": "pachirisu",  "weight": 9,  "min_level": 1,  "max_level": 18 },
  { "species": "teddiursa",  "weight": 8,  "min_level": 1,  "max_level": 20 },

  { "species": "applin",     "weight": 8,  "min_level": 5,  "max_level": 22 },
  { "species": "smoliv",     "weight": 8,  "min_level": 1,  "max_level": 16 },

  { "species": "charjabug",  "weight": 5,  "min_level": 18, "max_level": 35 },
  { "species": "spewpa",     "weight": 5,  "min_level": 15, "max_level": 30 },
  { "species": "vespiquen",  "weight": 3,  "min_level": 20, "max_level": 45 },

  { "species": "pidgeotto",  "weight": 5,  "min_level": 16, "max_level": 35 },
  { "species": "staravia",   "weight": 5,  "min_level": 16, "max_level": 35 },
  { "species": "fletchinder","weight": 5,  "min_level": 16, "max_level": 35 },
  { "species": "noctowl",    "weight": 4,  "min_level": 18, "max_level": 40 },
  { "species": "xatu",       "weight": 4,  "min_level": 18, "max_level": 40 },

  { "species": "aipom",      "weight": 6,  "min_level": 10, "max_level": 30 },
  { "species": "ambipom",    "weight": 3,  "min_level": 25, "max_level": 50 },

  { "species": "leavanny",   "weight": 2,  "min_level": 30, "max_level": 55 },
  { "species": "vikavolt",   "weight": 2,  "min_level": 30, "max_level": 55 },
  { "species": "beautifly",  "weight": 3,  "min_level": 20, "max_level": 45 },

  { "species": "swablu",     "weight": 4,  "min_level": 10, "max_level": 30 },
  { "species": "altaria",    "weight": 2,  "min_level": 35, "max_level": 55 },

  { "species": "heracross",  "weight": 2,  "min_level": 20, "max_level": 50 },
  { "species": "pinsir",     "weight": 2,  "min_level": 20, "max_level": 50 },

  { "species": "rowlet",          "weight": 3, "min_level": 5,  "max_level": 20 },
  { "species": "rowlet-hisuian",  "weight": 2, "min_level": 8,  "max_level": 22 },

  { "species": "eevee",      "weight": 2,  "min_level": 10, "max_level": 25 }
    ]
""",
            RustlingSpotFamily.CAVE, """
    [
  { "species": "zubat",      "weight": 12, "min_level": 1,  "max_level": 25 },
  { "species": "geodude",    "weight": 12, "min_level": 1,  "max_level": 28 },
  { "species": "diglett",    "weight": 11, "min_level": 1,  "max_level": 25 },
  { "species": "drilbur",    "weight": 11, "min_level": 1,  "max_level": 28 },
  { "species": "roggenrola", "weight": 10, "min_level": 1,  "max_level": 30 },
  { "species": "sandshrew",  "weight": 10, "min_level": 1,  "max_level": 25 },
  { "species": "woobat",     "weight": 10, "min_level": 1,  "max_level": 28 },
  { "species": "whismur",    "weight": 10, "min_level": 1,  "max_level": 25 },
  { "species": "nacli",      "weight": 9,  "min_level": 1,  "max_level": 30 },
  { "species": "dwebble",    "weight": 9,  "min_level": 1,  "max_level": 28 },
  { "species": "barboach",   "weight": 9,  "min_level": 1,  "max_level": 28 },
  { "species": "wooper",     "weight": 9,  "min_level": 1,  "max_level": 25 },
  { "species": "paldean-wooper","weight": 9,"min_level": 1, "max_level": 25 },

  { "species": "golbat",     "weight": 7,  "min_level": 20, "max_level": 50 },
  { "species": "graveler",   "weight": 7,  "min_level": 22, "max_level": 50 },
  { "species": "dugtrio",    "weight": 7,  "min_level": 22, "max_level": 50 },
  { "species": "excadrill",  "weight": 6,  "min_level": 26, "max_level": 55 },
  { "species": "boldore",    "weight": 6,  "min_level": 24, "max_level": 55 },
  { "species": "onix",       "weight": 6,  "min_level": 20, "max_level": 55 },
  { "species": "rhyhorn",    "weight": 6,  "min_level": 20, "max_level": 55 },
  { "species": "hippopotas", "weight": 6,  "min_level": 20, "max_level": 50 },
  { "species": "skorupi",    "weight": 6,  "min_level": 18, "max_level": 45 },
  { "species": "koffing",    "weight": 6,  "min_level": 18, "max_level": 45 },
  { "species": "grimer",     "weight": 6,  "min_level": 18, "max_level": 45 },

  { "species": "golett",     "weight": 5,  "min_level": 20, "max_level": 55 },
  { "species": "baltoy",     "weight": 5,  "min_level": 18, "max_level": 50 },
  { "species": "yamask",     "weight": 4,  "min_level": 18, "max_level": 45 },
  { "species": "sableye",    "weight": 4,  "min_level": 20, "max_level": 55 },
  { "species": "mawile",     "weight": 4,  "min_level": 20, "max_level": 55 },
  { "species": "glimmet",    "weight": 4,  "min_level": 24, "max_level": 55 },

  { "species": "camerupt",   "weight": 3,  "min_level": 30, "max_level": 65 },
  { "species": "steelix",    "weight": 3,  "min_level": 35, "max_level": 70 },
  { "species": "gigalith",   "weight": 3,  "min_level": 35, "max_level": 70 },
  { "species": "rhyperior",  "weight": 2,  "min_level": 40, "max_level": 75 },
  { "species": "gliscor",    "weight": 2,  "min_level": 40, "max_level": 75 },
  { "species": "clodsire",   "weight": 3,  "min_level": 30, "max_level": 65 },
  { "species": "whiscash",   "weight": 3,  "min_level": 30, "max_level": 65 },

  { "species": "cranidos",   "weight": 2,  "min_level": 20, "max_level": 50 },
  { "species": "shieldon",   "weight": 2,  "min_level": 20, "max_level": 50 },
  { "species": "tyrunt",     "weight": 1,  "min_level": 30, "max_level": 60 },
  { "species": "amaura",     "weight": 1,  "min_level": 30, "max_level": 60 },

  { "species": "tyranitar",  "weight": 1,  "min_level": 50, "max_level": 75 },
  { "species": "garchomp",   "weight": 1,  "min_level": 50, "max_level": 75 },
  { "species": "excadrill",  "weight": 1,  "min_level": 55, "max_level": 75 }
    ]
""",
            RustlingSpotFamily.FLYING, """
    [
  { "species": "pidgey",      "weight": 12, "min_level": 1,  "max_level": 20 },
  { "species": "spearow",     "weight": 12, "min_level": 1,  "max_level": 22 },
  { "species": "starly",      "weight": 12, "min_level": 1,  "max_level": 20 },
  { "species": "fletchling",  "weight": 12, "min_level": 1,  "max_level": 20 },
  { "species": "pidove",      "weight": 12, "min_level": 1,  "max_level": 20 },
  { "species": "tailow",      "weight": 11, "min_level": 1,  "max_level": 20 },
  { "species": "rookidee",    "weight": 11, "min_level": 1,  "max_level": 20 },
  { "species": "pikipek",     "weight": 11, "min_level": 1,  "max_level": 20 },
  { "species": "hoppip",      "weight": 10, "min_level": 1,  "max_level": 22 },
  { "species": "hoothoot",    "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "wingle",      "weight": 10, "min_level": 1,  "max_level": 22 },
  { "species": "ducklett",    "weight": 10, "min_level": 1,  "max_level": 24 },
  { "species": "combee",      "weight": 10, "min_level": 1,  "max_level": 20 },
  { "species": "swablu",      "weight": 9,  "min_level": 1,  "max_level": 24 },
  { "species": "natu",        "weight": 9,  "min_level": 1,  "max_level": 22 },
  { "species": "yanma",       "weight": 9,  "min_level": 1,  "max_level": 28 },

  { "species": "pidgeotto",   "weight": 6,  "min_level": 18, "max_level": 40 },
  { "species": "fearow",      "weight": 6,  "min_level": 18, "max_level": 42 },
  { "species": "staravia",    "weight": 6,  "min_level": 16, "max_level": 40 },
  { "species": "fletchinder", "weight": 6,  "min_level": 16, "max_level": 40 },
  { "species": "tranquill",   "weight": 6,  "min_level": 16, "max_level": 40 },
  { "species": "corvisquire", "weight": 6,  "min_level": 18, "max_level": 42 },
  { "species": "trumbeak",    "weight": 6,  "min_level": 18, "max_level": 42 },
  { "species": "noibat",      "weight": 5,  "min_level": 20, "max_level": 50 },
  { "species": "golbat",      "weight": 6,  "min_level": 18, "max_level": 45 },

  { "species": "pidgeot",     "weight": 4,  "min_level": 36, "max_level": 70 },
  { "species": "staraptor",   "weight": 4,  "min_level": 36, "max_level": 70 },
  { "species": "talonflame",  "weight": 4,  "min_level": 36, "max_level": 70 },
  { "species": "unfezant",    "weight": 4,  "min_level": 36, "max_level": 70 },
  { "species": "corviknight", "weight": 4,  "min_level": 38, "max_level": 72 },
  { "species": "toucannon",   "weight": 4,  "min_level": 36, "max_level": 70 },
  { "species": "noivern",     "weight": 3,  "min_level": 40, "max_level": 72 },
  { "species": "crobats",     "weight": 3,  "min_level": 40, "max_level": 72 },

  { "species": "skarmory",    "weight": 3,  "min_level": 30, "max_level": 70 },
  { "species": "tropius",     "weight": 3,  "min_level": 30, "max_level": 65 },
  { "species": "hawlucha",    "weight": 3,  "min_level": 30, "max_level": 65 },
  { "species": "gligar",      "weight": 4,  "min_level": 20, "max_level": 50 },
  { "species": "togetic",     "weight": 3,  "min_level": 20, "max_level": 60 },
  { "species": "vespiquen",   "weight": 2,  "min_level": 25, "max_level": 60 },

  { "species": "braviary",    "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "mandibuzz",   "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "bombirdier",  "weight": 2,  "min_level": 35, "max_level": 70 },
  { "species": "oricorio",    "weight": 2,  "min_level": 25, "max_level": 60 },
  { "species": "chatot",      "weight": 3,  "min_level": 15, "max_level": 40 },
  { "species": "delibird",    "weight": 2,  "min_level": 20, "max_level": 50 },

  { "species": "aerodactyl",  "weight": 1,  "min_level": 50, "max_level": 75 },
  { "species": "dragonite",   "weight": 1,  "min_level": 55, "max_level": 75 },
  { "species": "salamence",   "weight": 1,  "min_level": 55, "max_level": 75 }
    ]
""",
            RustlingSpotFamily.NETHERFLAMME, """
    [
  { "species": "slugma",     "weight": 12, "min_level": 1,  "max_level": 45 },
  { "species": "magby",      "weight": 11, "min_level": 1,  "max_level": 45 },
  { "species": "numel",      "weight": 11, "min_level": 1,  "max_level": 48 },
  { "species": "salandit",   "weight": 10, "min_level": 5,  "max_level": 50 },
  { "species": "rolycoly",   "weight": 10, "min_level": 5,  "max_level": 50 },
  { "species": "darumaka",   "weight": 9,  "min_level": 5,  "max_level": 50 },

  { "species": "pansear",    "weight": 8,  "min_level": 5,  "max_level": 48 },
  { "species": "litleo",     "weight": 8,  "min_level": 5,  "max_level": 50 },
  { "species": "houndour",   "weight": 8,  "min_level": 5,  "max_level": 50 },

  { "species": "charcadet",  "weight": 7,  "min_level": 10, "max_level": 55 },

  { "species": "magmar",     "weight": 5,  "min_level": 30, "max_level": 65 },
  { "species": "camerupt",   "weight": 5,  "min_level": 32, "max_level": 65 },
  { "species": "salazzle",   "weight": 4,  "min_level": 32, "max_level": 65 },
  { "species": "coalossal",  "weight": 4,  "min_level": 40, "max_level": 70 },
  { "species": "darmanitan", "weight": 3,  "min_level": 35, "max_level": 70 },

  { "species": "torkoal",    "weight": 5,  "min_level": 25, "max_level": 65 },
  { "species": "heatmor",    "weight": 5,  "min_level": 25, "max_level": 65 },

  { "species": "arcanine",   "weight": 3,  "min_level": 40, "max_level": 70 },
  { "species": "ninetales",  "weight": 3,  "min_level": 40, "max_level": 70 },
  { "species": "pyroar",     "weight": 3,  "min_level": 35, "max_level": 65 },

  { "species": "flareon",    "weight": 2,  "min_level": 40, "max_level": 70 },
  { "species": "turtonator", "weight": 2,  "min_level": 45, "max_level": 70 },

  { "species": "typhlosion", "weight": 1,  "min_level": 50, "max_level": 75 },
  { "species": "blaziken",   "weight": 1,  "min_level": 50, "max_level": 75 },
  { "species": "skeledirge", "weight": 1,  "min_level": 50, "max_level": 75 }
    ]""",
            RustlingSpotFamily.SOULFLAME, """
    [
  { "species": "gastly",    "weight": 12, "min_level": 1,  "max_level": 45 },
  { "species": "shuppet",   "weight": 11, "min_level": 1,  "max_level": 42 },
  { "species": "duskull",   "weight": 11, "min_level": 1,  "max_level": 44 },
  { "species": "litwick",   "weight": 10, "min_level": 1,  "max_level": 44 },
  { "species": "drifloon",  "weight": 9,  "min_level": 1,  "max_level": 46 },
  { "species": "yamask",    "weight": 9,  "min_level": 1,  "max_level": 48 },

  { "species": "misdreavus","weight": 8,  "min_level": 5,  "max_level": 50 },
  { "species": "phantump",  "weight": 7,  "min_level": 5,  "max_level": 50 },
  { "species": "pumpkaboo", "weight": 7,  "min_level": 5,  "max_level": 50 },

  { "species": "haunter",   "weight": 5,  "min_level": 30, "max_level": 60 },
  { "species": "dusclops",  "weight": 4,  "min_level": 32, "max_level": 60 },
  { "species": "lampent",   "weight": 5,  "min_level": 30, "max_level": 60 },

  { "species": "sinistea",  "weight": 6,  "min_level": 10, "max_level": 55 },
  { "species": "baltoy",    "weight": 6,  "min_level": 10, "max_level": 55 },
  { "species": "golett",    "weight": 5,  "min_level": 15, "max_level": 60 },

  { "species": "murkrow",   "weight": 5,  "min_level": 10, "max_level": 55 },
  { "species": "zorua",     "weight": 5,  "min_level": 10, "max_level": 55 },

  { "species": "houndour",  "weight": 6,  "min_level": 5,  "max_level": 50 },
  { "species": "slugma",    "weight": 6,  "min_level": 5,  "max_level": 50 },
  { "species": "salandit",  "weight": 5,  "min_level": 10, "max_level": 55 },
  { "species": "magby",     "weight": 4,  "min_level": 10, "max_level": 55 },
  { "species": "vulpix",    "weight": 4,  "min_level": 10, "max_level": 55 },
  { "species": "charcadet", "weight": 5,  "min_level": 15, "max_level": 60 },

  { "species": "banette",    "weight": 3, "min_level": 40, "max_level": 70 },
  { "species": "cofagrigus", "weight": 3, "min_level": 42, "max_level": 70 },
  { "species": "drifblim",   "weight": 3, "min_level": 40, "max_level": 70 },
  { "species": "trevenant",  "weight": 2, "min_level": 42, "max_level": 70 },
  { "species": "gourgeist",  "weight": 2, "min_level": 42, "max_level": 70 },

  { "species": "sableye",   "weight": 4,  "min_level": 20, "max_level": 65 },
  { "species": "mawile",    "weight": 4,  "min_level": 20, "max_level": 65 },
  { "species": "absol",     "weight": 3,  "min_level": 30, "max_level": 70 },
  { "species": "houndoom",  "weight": 3,  "min_level": 35, "max_level": 70 },

  { "species": "chandelure","weight": 1,  "min_level": 50, "max_level": 75 },
  { "species": "mimikyu",   "weight": 1,  "min_level": 45, "max_level": 75 },
  { "species": "spiritomb", "weight": 1,  "min_level": 50, "max_level": 75 }
    ]"""
    );

    private static String defaultJsonFor(RustlingSpotFamily family) {
        JsonArray array = parseDefaultArray(DEFAULT_FAMILY_V4_1.getOrDefault(family, "[]"));
        LinkedHashMap<String, JsonObject> entries = new LinkedHashMap<>();

        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            String species = object.has("species") ? object.get("species").getAsString().trim() : "";
            species = normalizeSpeciesForDefaults(species);
            if (species.isBlank() || REMOVED_DEFAULT_SPECIES.contains(species) || removedFromFamily(family).contains(species)) {
                continue;
            }
            object.addProperty("species", species);
            entries.putIfAbsent(species, object);
        }

        for (DefaultPokemonEntry entry : defaultAdditions(family)) {
            entries.putIfAbsent(entry.species(), entry.toJson());
        }

        JsonArray result = new JsonArray();
        entries.values().forEach(result::add);
        return GSON.toJson(result);
    }

    private static String lavaDefaultJson() {
        JsonArray result = new JsonArray();
        result.add(e("slugma", 12, 1, 45).toJson());
        result.add(e("magcargo", 5, 30, 65).toJson());
        return GSON.toJson(result);
    }

    private static JsonArray parseDefaultArray(String json) {
        try {
            JsonElement element = GSON.fromJson(json, JsonElement.class);
            return element != null && element.isJsonArray() ? element.getAsJsonArray() : new JsonArray();
        } catch (JsonParseException e) {
            LOGGER.warn("Failed to parse embedded Pokemon defaults", e);
            return new JsonArray();
        }
    }

    private static String normalizeSpeciesForDefaults(String species) {
        String normalized = species.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return SPECIES_RENAMES.getOrDefault(normalized, normalized);
    }

    private static Set<String> removedFromFamily(RustlingSpotFamily family) {
        return switch (family) {
            case SOULFLAME -> Set.of("slugma", "salandit", "magby", "vulpix");
            default -> Set.of();
        };
    }

    private static List<DefaultPokemonEntry> defaultAdditions(RustlingSpotFamily family) {
        return switch (family) {
            case GRASS -> entries(
                    e("grovyle", 3, 18, 40), e("sceptile", 1, 40, 70),
                    e("snivy", 6, 1, 20), e("servine", 3, 18, 40), e("serperior", 1, 40, 70),
                    e("grookey", 6, 1, 20), e("thwackey", 3, 18, 40), e("rillaboom", 1, 40, 70),
                    e("ivysaur", 3, 18, 40), e("venusaur", 1, 40, 70),
                    e("bayleef", 3, 18, 40), e("meganium", 1, 40, 70),
                    e("grotle", 3, 18, 40), e("torterra", 1, 40, 70),
                    e("quilladin", 3, 18, 40), e("chesnaught", 1, 40, 70),
                    e("floragato", 3, 18, 40), e("meowscarada", 1, 40, 70),
                    e("dolliv", 5, 18, 40), e("steenee", 5, 18, 40), e("tsareena", 2, 30, 60),
                    e("lilligant", 3, 25, 55), e("whimsicott", 3, 25, 55),
                    e("roselia", 5, 18, 40), e("roserade", 2, 30, 60),
                    e("gloom", 5, 18, 40), e("vileplume", 2, 30, 60), e("bellossom", 2, 30, 60),
                    e("weepinbell", 5, 18, 40), e("victreebel", 2, 30, 60),
                    e("breloom", 3, 25, 55), e("shiftry", 2, 30, 60), e("sunflora", 3, 25, 55),
                    e("gogoat", 2, 30, 60), e("eldegoss", 3, 25, 55),
                    e("toedscool", 7, 1, 24), e("toedscruel", 2, 30, 60),
                    e("dipplin", 2, 30, 60)
            );
            case WATER -> entries(
                    e("mudkip", 7, 1, 22), e("marshtomp", 3, 18, 45), e("swampert", 1, 40, 70),
                    e("quaxly", 5, 1, 20), e("quaxwell", 3, 18, 45), e("quaquaval", 1, 40, 70),
                    e("horsea", 9, 1, 22), e("seadra", 4, 22, 55), e("kingdra", 1, 40, 70),
                    e("krabby", 9, 1, 22), e("kingler", 4, 22, 55),
                    e("chinchou", 9, 1, 22), e("lanturn", 4, 22, 55),
                    e("remoraid", 8, 1, 22), e("octillery", 3, 22, 55),
                    e("carvanha", 8, 1, 24), e("sharpedo", 3, 25, 60),
                    e("corphish", 8, 1, 24), e("crawdaunt", 3, 25, 60),
                    e("tympole", 8, 1, 22), e("palpitoad", 4, 22, 50), e("seismitoad", 2, 35, 65),
                    e("basculin", 6, 10, 45), e("basculegion", 1, 40, 70),
                    e("wishiwashi", 7, 1, 30), e("mareanie", 7, 1, 30), e("toxapex", 2, 35, 65),
                    e("dewpider", 7, 1, 24), e("araquanid", 3, 25, 60),
                    e("chewtle", 7, 1, 24), e("drednaw", 2, 35, 65),
                    e("arrokuda", 8, 1, 24), e("barraskewda", 3, 25, 60),
                    e("tatsugiri", 4, 20, 55), e("veluza", 3, 25, 60),
                    e("wiglett", 7, 1, 24), e("wugtrio", 3, 25, 60),
                    e("luvdisc", 6, 10, 35), e("alomomola", 3, 25, 60),
                    e("clamperl", 7, 1, 24), e("huntail", 2, 30, 60), e("gorebyss", 2, 30, 60),
                    e("mantyke", 6, 1, 24), e("mantine", 2, 30, 65),
                    e("azurill", 8, 1, 18), e("slowpoke", 8, 1, 25),
                    e("poliwhirl", 5, 18, 45), e("poliwrath", 2, 35, 65), e("politoed", 2, 35, 65),
                    e("golduck", 4, 22, 55), e("ludicolo", 2, 30, 60),
                    e("feraligatr", 1, 40, 70), e("primarina", 1, 40, 70)
            );
            case SAND -> entries(
                    e("sandslash", 5, 22, 55), e("krookodile", 2, 40, 70),
                    e("silicobra", 9, 1, 24), e("sandaconda", 3, 28, 60),
                    e("sandslash", 5, 22, 55), e("claydol", 3, 25, 60),
                    e("graveler", 5, 22, 55), e("golem", 2, 35, 70),
                    e("glimmet", 5, 10, 35), e("glimmora", 2, 35, 65),
                    e("drapion", 2, 35, 65), e("skuntank", 3, 25, 55), e("muk", 3, 25, 55),
                    e("meowth", 7, 1, 22), e("persian", 3, 22, 50),
                    e("rhydon", 4, 30, 60), e("cubone", 7, 1, 25), e("marowak", 3, 25, 55),
                    e("magcargo", 3, 30, 65), e("orthworm", 3, 20, 55)
            );
            case SNOW -> entries(
                    e("sealeo", 5, 22, 50), e("frosmoth", 3, 25, 60),
                    e("prinplup", 4, 18, 45),
                    e("vulpix alolan", 6, 1, 25), e("ninetales alolan", 2, 35, 65),
                    e("growlithe hisuian", 5, 1, 28), e("arcanine hisuian", 2, 40, 70),
                    e("avalugg hisuian", 2, 35, 65),
                    e("cetoddle", 7, 1, 28), e("cetitan", 2, 35, 70),
                    e("eiscue", 3, 20, 55)
            );
            case LEAVES -> entries(
                    e("caterpie", 10, 1, 15), e("metapod", 5, 7, 20), e("butterfree", 3, 18, 45),
                    e("weedle", 10, 1, 15), e("kakuna", 5, 7, 20), e("beedrill", 3, 18, 45),
                    e("wurmple", 10, 1, 15), e("silcoon", 5, 7, 20), e("cascoon", 5, 7, 20), e("dustox", 3, 18, 45),
                    e("sewaddle", 9, 1, 18), e("swadloon", 5, 18, 35),
                    e("burmy", 8, 1, 20), e("wormadam", 3, 20, 45), e("mothim", 3, 20, 45),
                    e("cherubi", 8, 1, 20), e("cherrim", 3, 20, 45),
                    e("foongus", 8, 1, 24), e("amoonguss", 3, 25, 55),
                    e("morelull", 8, 1, 24), e("shiinotic", 3, 25, 55),
                    e("fomantis", 8, 1, 24), e("lurantis", 3, 25, 55),
                    e("tarountula", 9, 1, 18), e("spidops", 4, 18, 45),
                    e("nymble", 9, 1, 20), e("lokix", 4, 20, 50),
                    e("blipbug", 9, 1, 18), e("dottler", 5, 18, 35), e("orbeetle", 2, 30, 60),
                    e("seedot", 7, 1, 20), e("nuzleaf", 4, 18, 40), e("shiftry", 2, 30, 60),
                    e("dipplin", 2, 30, 60), e("dartrix", 3, 18, 40), e("decidueye", 1, 40, 70),
                    e("trumbeak", 5, 18, 42), e("toucannon", 3, 36, 70),
                    e("greedent", 4, 20, 45)
            );
            case CAVE -> entries(
                    e("crobat", 2, 40, 72), e("golem", 2, 35, 70), e("swoobat", 4, 22, 55),
                    e("mienfoo", 5, 15, 45), e("golurk", 2, 40, 70), e("glimmora", 2, 35, 65),
                    e("dunsparce", 5, 10, 45), e("dudunsparce", 1, 35, 70),
                    e("loudred", 4, 20, 45), e("exploud", 2, 35, 65), e("carbink", 3, 20, 55),
                    e("makuhita", 6, 1, 28), e("hariyama", 3, 25, 60), e("mankey", 6, 1, 28),
                    e("primeape", 3, 25, 60), e("annihilape", 1, 40, 70),
                    e("phanpy", 6, 1, 28), e("donphan", 3, 25, 60), e("cubone", 6, 1, 28), e("marowak", 3, 25, 60),
                    e("timburr", 6, 1, 28), e("gurdurr", 3, 25, 55), e("conkeldurr", 1, 40, 70),
                    e("axew", 2, 20, 45), e("fraxure", 1, 35, 60), e("haxorus", 1, 50, 75),
                    e("litwick", 5, 1, 35), e("lampent", 3, 25, 55), e("chandelure", 1, 45, 75),
                    e("shuppet", 5, 1, 35), e("banette", 2, 35, 65), e("gastly", 5, 1, 35),
                    e("haunter", 3, 25, 55), e("gengar", 1, 45, 75),
                    e("pawniard", 4, 15, 45), e("bisharp", 2, 35, 65), e("kingambit", 1, 50, 75),
                    e("drapion", 2, 35, 65), e("sandslash", 4, 22, 55), e("paras", 6, 1, 28), e("parasect", 3, 25, 55),
                    e("skuntank", 3, 25, 55), e("gulpin", 5, 1, 28), e("swalot", 3, 25, 55), e("muk", 3, 25, 55),
                    e("machop", 6, 1, 28), e("machoke", 3, 25, 55), e("machamp", 1, 40, 70),
                    e("aron", 5, 1, 30), e("lairon", 3, 25, 55), e("aggron", 1, 45, 75),
                    e("larvitar", 2, 20, 45), e("pupitar", 1, 35, 60),
                    e("wooper paldean", 9, 1, 25), e("torkoal", 3, 25, 65),
                    e("naclstack", 4, 24, 55), e("garganacl", 2, 40, 70),
                    e("riolu", 3, 10, 35), e("lucario", 1, 35, 70),
                    e("venipede", 5, 1, 28), e("whirlipede", 3, 22, 50), e("scolipede", 1, 40, 70),
                    e("shelmet", 5, 1, 28), e("accelgor", 2, 35, 65),
                    e("rhydon", 4, 30, 60), e("hippowdon", 3, 30, 60), e("weezing", 3, 30, 60),
                    e("yamask galarian", 3, 18, 45), e("cofagrigus", 2, 35, 65), e("runerigus", 2, 35, 65),
                    e("houndour", 5, 5, 35), e("houndoom", 2, 35, 65),
                    e("spinarak", 5, 1, 28), e("ariados", 3, 25, 55),
                    e("zangoose", 2, 25, 60), e("seviper", 2, 25, 60), e("numel", 5, 1, 28),
                    e("feebas", 2, 1, 25), e("milotic", 1, 40, 70)
            );
            case FLYING -> entries(
                    e("taillow", 11, 1, 20), e("wingull", 10, 1, 22), e("swellow", 5, 22, 50),
                    e("pelipper", 5, 22, 50), e("honchkrow", 2, 35, 70), e("xatu", 4, 18, 40),
                    e("altaria", 2, 35, 60), e("drifloon", 8, 1, 24), e("drifblim", 3, 28, 60),
                    e("yanmega", 3, 28, 60), e("swanna", 4, 22, 50), e("rufflet", 6, 10, 35),
                    e("vullaby", 6, 10, 35), e("crobat", 3, 40, 72), e("togepi", 5, 1, 25),
                    e("togekiss", 1, 40, 70), e("squawkabilly", 4, 10, 45),
                    e("gliscor", 2, 40, 75), e("scyther", 3, 20, 55),
                    e("archen", 2, 20, 50), e("archeops", 1, 45, 75),
                    e("rowlet", 4, 1, 22), e("dartrix", 2, 18, 45), e("decidueye", 1, 40, 70)
            );
            case NETHERFLAMME -> entries(
                    e("charmander", 5, 1, 22), e("charmeleon", 3, 18, 45), e("charizard", 1, 45, 75),
                    e("cyndaquil", 5, 1, 22), e("quilava", 3, 18, 45),
                    e("chimchar", 5, 1, 22), e("monferno", 3, 18, 45), e("infernape", 1, 45, 75),
                    e("tepig", 5, 1, 22), e("pignite", 3, 18, 45), e("emboar", 1, 45, 75),
                    e("fennekin", 5, 1, 22), e("braixen", 3, 18, 45), e("delphox", 1, 45, 75),
                    e("litten", 5, 1, 22), e("torracat", 3, 18, 45), e("incineroar", 1, 45, 75),
                    e("scorbunny", 5, 1, 22), e("raboot", 3, 18, 45), e("cinderace", 1, 45, 75),
                    e("torchic", 5, 1, 22), e("combusken", 3, 18, 45),
                    e("fuecoco", 5, 1, 22), e("crocalor", 3, 18, 45),
                    e("magcargo", 5, 30, 65), e("magmortar", 2, 45, 75),
                    e("sizzlipede", 8, 1, 28), e("centiskorch", 3, 28, 65),
                    e("growlithe", 7, 1, 28), e("vulpix", 7, 1, 28),
                    e("armarouge", 2, 35, 70), e("ceruledge", 2, 35, 70),
                    e("simisear", 3, 25, 60), e("larvesta", 3, 20, 50), e("volcarona", 1, 50, 75),
                    e("carkol", 5, 22, 55)
            );
            case SOULFLAME -> entries(
                    e("dusknoir", 1, 45, 75), e("gengar", 1, 45, 75), e("polteageist", 2, 35, 65),
                    e("yamask galarian", 5, 1, 48), e("runerigus", 2, 40, 70),
                    e("zoroark", 2, 35, 70), e("honchkrow", 2, 35, 70),
                    e("ceruledge", 1, 40, 75), e("claydol", 3, 25, 60), e("golurk", 2, 40, 70)
            );
        };
    }

    private static List<DefaultPokemonEntry> entries(DefaultPokemonEntry... entries) {
        return List.of(entries);
    }

    private static DefaultPokemonEntry e(String species, int weight, int minLevel, int maxLevel) {
        return new DefaultPokemonEntry(species, weight, minLevel, maxLevel);
    }

    private PokemonPoolService() {
    }

    public static void ensureDefaultsExist() {
        createDefaults();
    }

    public static void setSpeciesValidator(SpeciesValidator validator) {
        speciesValidator = validator != null ? validator : SpeciesValidator.unavailable("Cobblemon species API is not available on this loader");
        LOADED.set(false);
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
        DATAPACK_ENTRIES.clear();
        DATAPACK_FAMILIES.clear();

        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                DATAPACK_DIRECTORY,
                resourceLocation -> resourceLocation.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            String familyKey = familyKeyFromDatapackResource(entry.getKey(), DATAPACK_DIRECTORY);
            if (familyKey == null) {
                LOGGER.warn("[Rustling Spots] Failed loading datapack pokemon family from {}: invalid file path", entry.getKey());
                continue;
            }

            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                PokemonEntryData[] data = GSON.fromJson(reader, PokemonEntryData[].class);
                if (data != null) {
                    Path sourcePath = Path.of(entry.getKey().getNamespace()).resolve(entry.getKey().getPath());
                    for (int i = 0; i < data.length; i++) {
                        toEntry(data[i], familyKey, sourcePath, i).ifPresent(DATAPACK_ENTRIES::add);
                    }
                }
                DATAPACK_FAMILIES.add(familyKey);
            } catch (IOException | JsonParseException e) {
                LOGGER.warn("[Rustling Spots] Failed to read datapack pokemon family {} from {}", familyKey, entry.getKey(), e);
            }
        }
    }

    public static PokemonEntry pickRandom(ServerLevel level, RustlingSpot spot, RandomSource random) {
        return pickRandomSelection(level, spot, random).entry();
    }

    public static PoolSelection pickRandomSelection(ServerLevel level, RustlingSpot spot, RandomSource random) {
        ensureLoaded();
        boolean enforceTypes = RustlingSpotsPokemonConfig.POKEMON_SPAWN.typedSpawnRules();
        PoolContext context = resolvePoolContext(level, spot);
        String familyKey = context.effectiveFamilyKey();
        List < PokemonEntry > candidates = Stream.concat(ENTRIES.stream(), DATAPACK_ENTRIES.stream())
                .filter(entry -> !enforceTypes || entry.matchesFamily(familyKey))
                .toList();
        if (candidates.isEmpty()) {
            if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                LOGGER.debug("No configured Pokemon entries for family {} at {}", familyKey, spot.getPosition());
            }
            return PoolSelection.empty(context);
        }

        int totalWeight = candidates.stream().mapToInt(PokemonEntry:: weight).sum();
        if (totalWeight <= 0) {
            if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                LOGGER.debug("Configured Pokemon pool had no weight for family {} at {}", familyKey, spot.getPosition());
            }
            return PoolSelection.empty(context);
        }

        int roll = random.nextInt(totalWeight);
        int accumulator = 0;
        for (PokemonEntry entry : candidates) {
            accumulator += entry.weight();
            if (roll < accumulator) {
                return new PoolSelection(entry, context, candidates.size());
            }
        }
        return new PoolSelection(candidates.get(candidates.size() - 1), context, candidates.size());
    }

    private static void ensureLoaded() {
        if (LOADED.getAndSet(true)) {
            return;
        }

        createDefaults();
        ENTRIES.clear();
        LOADED_FAMILIES.clear();

        if (!speciesValidator.isAvailable()) {
            LOGGER.warn("[Rustling Spots] Full Cobblemon Pokemon pool species/form validation skipped: {}", speciesValidator.unavailableReason());
        }

        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            String familyKey = family.serializedName();
            Path file = familyPath(familyKey);
            ENTRIES.addAll(readPool(file, familyKey));
            LOADED_FAMILIES.add(familyKey);
        }

        try (var paths = Files.walk(FAMILY_DIR)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .filter(path -> !isGeneratedDefaultReference(path))
                    .forEach(path -> {
                        String familyKey = keyForPath(path);
                        if (familyKey == null || LOADED_FAMILIES.contains(familyKey)) {
                            return;
                        }
                        ENTRIES.addAll(readPool(path, familyKey));
                        LOADED_FAMILIES.add(familyKey);
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to scan custom pokemon family files under {}", FAMILY_DIR, e);
        }

        if (ENTRIES.isEmpty()) {
            LOGGER.warn("Pokemon pool configuration is empty; rustling spots will not spawn Pokemon.");
        }
    }

    private static void createDefaults() {
        try {
            Files.createDirectories(FAMILY_DIR);
            for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
                String familyKey = family.serializedName();
                Path path = familyPath(familyKey);
                Files.createDirectories(path.getParent());
                writeOrMigrateDefault(path, familyKey, defaultJsonFor(family), V4_1_DEFAULT_SHA256.get(familyKey));
            }
            writeOrMigrateDefault(familyPath(LAVA_FAMILY_KEY), LAVA_FAMILY_KEY, lavaDefaultJson(), null);
        } catch (IOException e) {
            LOGGER.warn("Failed to create default pokemon pool configuration", e);
        }
    }

    private static List<PokemonEntry> readPool(Path path, String familyTag) {
        List < PokemonEntry > parsed = new ArrayList <> ();
        try (Reader reader = Files.newBufferedReader(path)) {
            PokemonEntryData[] data = GSON.fromJson(reader, PokemonEntryData[].class);
            if (data != null) {
                Set<String> seen = new HashSet<>();
                for (int i = 0; i < data.length; i++) {
                    int entryIndex = i;
                    PokemonEntryData entry = data[i];
                    Optional<PokemonEntry> parsedEntry = toEntry(entry, familyTag, path, entryIndex);
                    parsedEntry.ifPresent(pokemonEntry -> {
                        String duplicateKey = pokemonEntry.species() + "|" + pokemonEntry.weight()
                                + "|" + pokemonEntry.minLevel() + "|" + pokemonEntry.maxLevel();
                        if (!seen.add(duplicateKey)) {
                            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, species={}, reason=duplicate identical entry, action=skipped",
                                    path, entryIndex, pokemonEntry.species());
                            return;
                        }
                        parsed.add(pokemonEntry);
                    });
                }
            }
        } catch (IOException | JsonParseException e) {
            LOGGER.warn("Failed to read pokemon pool from {}", path, e);
        }
        if (parsed.isEmpty()) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, reason=empty pool or no valid entries, action=kept loaded as empty", path);
        }
        return parsed;
    }

    private static Optional<PokemonEntry> toEntry(PokemonEntryData data, String familyTag, Path path, int index) {
        if (data == null) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, reason=entry could not be parsed, action=skipped", path, index);
            return Optional.empty();
        }
        if (data.species == null) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, reason=missing required field species, action=skipped", path, index);
            return Optional.empty();
        }
        if (data.weight <= 0) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, species={}, reason=weight <= 0, action=skipped",
                    path, index, data.species);
            return Optional.empty();
        }

        String species = normalizeSpeciesForDefaults(data.species);
        if (species.isEmpty()) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, species={}, reason=empty species, action=skipped",
                    path, index, data.species);
            return Optional.empty();
        }
        if (REMOVED_DEFAULT_SPECIES.contains(species)) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, species={}, reason=known invalid form syntax, action=skipped",
                    path, index, data.species);
            return Optional.empty();
        }
        SpeciesValidation validation = speciesValidator.validate(species);
        if (validation.available() && !validation.valid()) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, species={}, reason={}, action=skipped",
                    path, index, species, validation.reason());
            return Optional.empty();
        }

        Set<String> families = new HashSet<>();
        if (data.families != null) {
            for (String name : data.families) {
                String normalized = normalizeFamilyKey(name);
                if (!normalized.isEmpty()) {
                    families.add(normalized);
                }
            }
        }
        if (families.isEmpty() && familyTag != null && !familyTag.isBlank()) {
            families.add(normalizeFamilyKey(familyTag));
        }

        Integer minLevel = data.min_level != null ? Math.max(1, data.min_level) : null;
        Integer maxLevel = data.max_level != null ? Math.max(1, data.max_level) : null;
        if (data.min_level != null && data.min_level < 1) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, species={}, reason=min_level < 1, action=kept clamped",
                    path, index, species);
        }
        if (data.max_level != null && data.max_level < 1) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, species={}, reason=max_level < 1, action=kept clamped",
                    path, index, species);
        }
        if (minLevel != null && maxLevel != null && maxLevel < minLevel) {
            LOGGER.warn("[Rustling Spots] Pokemon pool warning: file={}, entry={}, species={}, reason=max_level is below min_level, action=kept with resolver bounds",
                    path, index, species);
        }
        Boolean shiny = data.shiny;
        Double shinyChance = data.shinyChance;
        return Optional.of(new PokemonEntry(species, data.weight, minLevel, maxLevel, shiny, shinyChance, families, path.toString()));
    }

    private static void writeOrMigrateDefault(Path path, String familyKey, String v42Default, String v41Sha256) throws IOException {
        Files.createDirectories(path.getParent());
        if (Files.notExists(path)) {
            Files.writeString(path, v42Default, StandardCharsets.UTF_8);
            return;
        }

        String existing = Files.readString(path, StandardCharsets.UTF_8);
        if (Objects.equals(existing, v42Default)) {
            return;
        }

        if (v41Sha256 != null && Objects.equals(sha256(existing), v41Sha256)) {
            Path backup = uniqueSibling(path, ".v4.1.bak");
            Files.copy(path, backup, StandardCopyOption.COPY_ATTRIBUTES);
            Files.writeString(path, v42Default, StandardCharsets.UTF_8);
            LOGGER.info("[Rustling Spots] Migrated untouched v4.1 Pokemon pool {} to v4.2. Backup created at {}", path, backup);
            return;
        }

        Path reference = v42ReferencePath(path);
        if (Files.notExists(reference) || !Objects.equals(Files.readString(reference, StandardCharsets.UTF_8), v42Default)) {
            Files.writeString(reference, v42Default, StandardCharsets.UTF_8);
        }
        LOGGER.warn("[Rustling Spots] Custom Pokemon pool {} was not overwritten. Official v4.2 default for family '{}' was written to {}",
                path, familyKey, reference);
    }

    private static Path uniqueSibling(Path path, String suffixBeforeExtension) {
        String fileName = path.getFileName().toString();
        String stem = fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
        Path candidate = path.resolveSibling(stem + suffixBeforeExtension + ".json");
        int index = 2;
        while (Files.exists(candidate)) {
            candidate = path.resolveSibling(stem + suffixBeforeExtension + "." + index + ".json");
            index++;
        }
        return candidate;
    }

    private static Path v42ReferencePath(Path path) {
        String fileName = path.getFileName().toString();
        String stem = fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
        return path.resolveSibling(stem + ".v4.2-default.json");
    }

    private static boolean isGeneratedDefaultReference(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".v4.2-default.json") || name.contains(".v4.1.bak");
    }

    private static String sha256(String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format(Locale.ROOT, "%02x", b));
            }
            return builder.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static PoolContext resolvePoolContext(ServerLevel level, RustlingSpot spot) {
        String requestedFamilyKey = normalizeFamilyKey(spot.getPokemonFamily());
        BlockState blockAtSpot = level.getBlockState(spot.getPosition());
        FluidState fluidAtSpot = level.getFluidState(spot.getPosition());
        BlockState blockBelowSpot = level.getBlockState(spot.getPosition().below());
        FluidState fluidBelowSpot = level.getFluidState(spot.getPosition().below());
        boolean water = fluidAtSpot.is(FluidTags.WATER) || fluidBelowSpot.is(FluidTags.WATER);
        boolean lava = fluidAtSpot.is(FluidTags.LAVA) || fluidBelowSpot.is(FluidTags.LAVA);
        String effectiveFamilyKey = requestedFamilyKey;
        if (spot.getFamily() == RustlingSpotFamily.NETHERFLAMME && lava) {
            effectiveFamilyKey = LAVA_FAMILY_KEY;
        }
        return new PoolContext(
                requestedFamilyKey,
                effectiveFamilyKey,
                familyPath(effectiveFamilyKey).toString(),
                blockId(blockAtSpot),
                fluidId(fluidAtSpot),
                water,
                lava,
                !water && !lava
        );
    }

    private static String blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private static String fluidId(FluidState state) {
        if (state.isEmpty()) {
            return "minecraft:empty";
        }
        return BuiltInRegistries.FLUID.getKey(state.getType()).toString();
    }

    public record PoolSelection(PokemonEntry entry, PoolContext context, int validEntryCount) {
        static PoolSelection empty(PoolContext context) {
            return new PoolSelection(null, context, 0);
        }
    }

    public record PoolContext(
            String requestedFamilyKey,
            String effectiveFamilyKey,
            String poolPath,
            String blockAtSpot,
            String fluidAtSpot,
            boolean water,
            boolean lava,
            boolean solid
    ) {
    }

    public record PokemonEntry(String species, int weight, Integer minLevel, Integer maxLevel,
                               Boolean shiny, Double shinyChance, Set<String> families, String sourcePath) {

        public boolean matchesFamily(String family) {
            return families == null || families.isEmpty() || families.contains(family);
        }

        public int resolvedMinLevel() {
            int floor = RustlingSpotsPokemonConfig.POKEMON_SPAWN.minLevel();
            int ceiling = RustlingSpotsPokemonConfig.POKEMON_SPAWN.maxLevel();
            int configuredMin = Math.max(1, Objects.requireNonNullElse(minLevel, floor));
            int configuredMax = Math.max(1, Objects.requireNonNullElse(maxLevel, ceiling));
            if (configuredMax < floor || configuredMin > ceiling) {
                return floor;
            }
            int boundedMin = Math.max(floor, Math.min(configuredMin, configuredMax));
            return Math.min(boundedMin, ceiling);
        }

        public int resolvedMaxLevel() {
            int floor = RustlingSpotsPokemonConfig.POKEMON_SPAWN.minLevel();
            int ceiling = RustlingSpotsPokemonConfig.POKEMON_SPAWN.maxLevel();
            int configuredMin = Math.max(1, Objects.requireNonNullElse(minLevel, floor));
            int configuredMax = Math.max(1, Objects.requireNonNullElse(maxLevel, ceiling));
            if (configuredMax < floor || configuredMin > ceiling) {
                return ceiling;
            }
            int boundedMax = Math.min(ceiling, Math.max(configuredMin, configuredMax));
            return Math.max(boundedMax, floor);
        }
    }

    private static class PokemonEntryData {
        String species;
        int weight = 1;
        Integer min_level;
        Integer max_level;
        Boolean shiny;
        Double shinyChance;
        List<String> families;
    }

    private record DefaultPokemonEntry(String species, int weight, int minLevel, int maxLevel) {
        JsonObject toJson() {
            JsonObject object = new JsonObject();
            object.addProperty("species", species);
            object.addProperty("weight", weight);
            object.addProperty("min_level", minLevel);
            object.addProperty("max_level", maxLevel);
            return object;
        }
    }

    public interface SpeciesValidator {
        SpeciesValidation validate(String species);

        boolean isAvailable();

        String unavailableReason();

        static SpeciesValidator unavailable(String reason) {
            return new SpeciesValidator() {
                @Override
                public SpeciesValidation validate(String species) {
                    return new SpeciesValidation(false, true, reason);
                }

                @Override
                public boolean isAvailable() {
                    return false;
                }

                @Override
                public String unavailableReason() {
                    return reason;
                }
            };
        }
    }

    public record SpeciesValidation(boolean available, boolean valid, String reason) {
        public static SpeciesValidation ok() {
            return new SpeciesValidation(true, true, "");
        }

        public static SpeciesValidation failed(String reason) {
            return new SpeciesValidation(true, false, reason);
        }
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
