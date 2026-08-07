package net.levelscraft7.rustlingspots.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Server configuration for rustling spot spawning and lifecycle tuning.
 * Stored as a shared JSON config used by all loaders in this project.
 */
public final class RustlingSpotsServerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpotsServerConfig.class);

    public static General GENERAL = General.defaults();

    private static final Path PATH = ConfigIO.configPath("rustlingspots/rustlingspots-server.json");

    private RustlingSpotsServerConfig() {}

    public static void load() {
        GENERAL = ConfigIO.loadOrCreate(PATH, General.defaults(), General.class);
    }

    public static void save() {
        try {
            ConfigIO.save(PATH, GENERAL);
        } catch (Exception e) {
            LOGGER.error("[Rustling Spots] Failed to save server config at {}", PATH, e);
        }
    }

    public static final class General {
        public boolean enabled = true;
        public boolean enable_logging = false;

        // Spacing and lifetime
        public int min_distance_between_spots = 16;
        public int spot_lifetime_ticks = 6000;

        // Interaction
        public double interaction_radius = 2.0D;
        public double interaction_vertical_allowance = 3.0D;

        // New simplified spawning model
        public int player_spot_radius = 200;
        public int max_spots_per_player = 8;
        public int max_spots_total = 64;
        public boolean allow_cobblemon_raid_den_dimensions = true;
        public double shiny_spot_chance = 0.0025D;
        public boolean announce_shiny_finds_globally = true;
        public boolean enable_empty_spots = true;
        public double empty_spot_chance = 0.02D;
        public boolean enable_multiple_reward_rolls = false;
        public int min_reward_rolls = 1;
        public int max_reward_rolls = 2;
        public boolean allow_multiple_pokemon_per_spot = false;
        public double mixed_reward_spot_chance = 0.01D;

        public static General defaults() {
            return new General();
        }

        public boolean enabled() { return enabled; }
        public boolean loggingEnabled() { return enable_logging; }

        public int minDistanceBetweenSpots() { return min_distance_between_spots; }
        public int spotLifetimeTicks() { return spot_lifetime_ticks; }

        public double interactionRadius() { return interaction_radius; }
        public double interactionVerticalAllowance() { return interaction_vertical_allowance; }

        public int playerSpotRadius() { return player_spot_radius; }
        public int maxSpotsPerPlayer() { return max_spots_per_player; }
        public int maxSpotsTotal() { return max_spots_total; }
        public boolean allowCobblemonRaidDenDimensions() { return allow_cobblemon_raid_den_dimensions; }
        public double shinySpotChance() { return Math.max(0.0D, Math.min(1.0D, shiny_spot_chance)); }
        public boolean announceShinyFindsGlobally() { return announce_shiny_finds_globally; }
        public boolean enableEmptySpots() { return enable_empty_spots; }
        public double emptySpotChance() { return Math.max(0.0D, Math.min(0.05D, empty_spot_chance)); }
        public boolean enableMultipleRewardRolls() { return enable_multiple_reward_rolls; }
        public int minRewardRolls() {
            if (!enableMultipleRewardRolls()) {
                return 1;
            }
            return Math.max(1, min_reward_rolls);
        }
        public int maxRewardRolls() {
            if (!enableMultipleRewardRolls()) {
                return 1;
            }
            return Math.max(minRewardRolls(), max_reward_rolls);
        }
        public boolean allowMultiplePokemonPerSpot() {
            return enableMultipleRewardRolls() && allow_multiple_pokemon_per_spot;
        }
        public double mixedRewardSpotChance() {
            if (!enableMultipleRewardRolls()) {
                return 0.0D;
            }
            return Math.max(0.0D, Math.min(0.25D, mixed_reward_spot_chance));
        }
    }
}
