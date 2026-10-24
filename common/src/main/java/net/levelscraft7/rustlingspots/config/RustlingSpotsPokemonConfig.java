package net.levelscraft7.rustlingspots.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controls Pokemon encounter behavior for rustling spots.
 * Stored as a shared JSON config used by all loaders in this project.
 */
public final class RustlingSpotsPokemonConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpotsPokemonConfig.class);
    private static final int CONFIG_VERSION = 1;
    private static final double PREVIOUS_DEFAULT_SHINY_CHANCE = 0.05D;

    public static Pokemon POKEMON = Pokemon.defaults();
    public static Pokemon POKEMON_SPAWN;

    private static final Path PATH = ConfigIO.configPath("rustlingspots/rustlingspots-pokemon.json");

    private RustlingSpotsPokemonConfig() {}

    public static void load() {
        int storedConfigVersion = readStoredConfigVersion();
        POKEMON = ConfigIO.loadOrCreate(PATH, Pokemon.defaults(), Pokemon.class);
        if (storedConfigVersion < CONFIG_VERSION) {
            if (usesPreviousDefaultShinyChance(POKEMON)) {
                POKEMON.default_shiny_chance = 0.01D;
            }
            POKEMON.config_version = CONFIG_VERSION;
            save();
        }
        POKEMON_SPAWN = POKEMON;
    }

    private static int readStoredConfigVersion() {
        if (!Files.exists(PATH)) {
            return CONFIG_VERSION;
        }

        try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            JsonElement json = JsonParser.parseReader(reader);
            if (json == null || !json.isJsonObject()) {
                return CONFIG_VERSION;
            }
            JsonElement version = json.getAsJsonObject().get("config_version");
            return version == null ? 0 : version.getAsInt();
        } catch (Exception e) {
            return CONFIG_VERSION;
        }
    }

    private static boolean usesPreviousDefaultShinyChance(Pokemon pokemon) {
        return Double.compare(pokemon.default_shiny_chance, PREVIOUS_DEFAULT_SHINY_CHANCE) == 0;
    }

    public static void save() {
        try {
            ConfigIO.save(PATH, POKEMON);
            POKEMON_SPAWN = POKEMON;
        } catch (Exception e) {
            LOGGER.error("[Rustling Spots] Failed to save Pokemon config at {}", PATH, e);
        }
    }

    public static final class Pokemon {
        public int config_version = CONFIG_VERSION;
        public boolean enable = true;
        public double encounter_chance = 0.35D;
        public double default_shiny_chance = 0.01D;
        public int min_level = 5;
        public int max_level = 75;
        public boolean typed_spawn_rules = true;

        public boolean enable() {
            return enabled();
        }

        public static Pokemon defaults() { return new Pokemon(); }

        public boolean enabled() { return enable; }
        public double encounterChance() { return encounter_chance; }
        public double defaultShinyChance() { return default_shiny_chance; }
        public int minLevel() { return min_level; }
        public int maxLevel() { return max_level; }
        public boolean typedSpawnRules() { return typed_spawn_rules; }
    }
}
