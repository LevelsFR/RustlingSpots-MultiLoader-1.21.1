package net.levelscraft7.rustlingspots.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Controls Pokemon encounter behavior for rustling spots.
 * Stored as a shared JSON config used by all loaders in this project.
 */
public final class RustlingSpotsPokemonConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpotsPokemonConfig.class);

    public static Pokemon POKEMON = Pokemon.defaults();
    public static Pokemon POKEMON_SPAWN;

    private static final Path PATH = ConfigIO.configPath("rustlingspots/rustlingspots-pokemon.json");

    private RustlingSpotsPokemonConfig() {}

    public static void load() {
        POKEMON = ConfigIO.loadOrCreate(PATH, Pokemon.defaults(), Pokemon.class);
        POKEMON_SPAWN = POKEMON;
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
        public boolean enable = true;
        public double encounter_chance = 0.35D;
        public double default_shiny_chance = 0.05D;
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
