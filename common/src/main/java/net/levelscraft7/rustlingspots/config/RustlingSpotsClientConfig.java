package net.levelscraft7.rustlingspots.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Client-only visuals configuration.
 * Stored as a shared JSON config used by all loaders in this project.
 */
public final class RustlingSpotsClientConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpotsClientConfig.class);

    public static Visuals VISUALS = Visuals.defaults();
    public static Visuals VISUAL = VISUALS;

    private static final Path PATH = ConfigIO.configPath("rustlingspots/rustlingspots-client.json");

    private RustlingSpotsClientConfig() {}

    public static void load() {
        VISUALS = ConfigIO.loadOrCreate(PATH, Visuals.defaults(), Visuals.class);
        VISUAL = VISUALS;
    }

    public static void save() {
        try {
            ConfigIO.save(PATH, VISUALS);
            VISUAL = VISUALS;
        } catch (Exception e) {
            LOGGER.error("[Rustling Spots] Failed to save client config at {}", PATH, e);
        }
    }

    public static final class Visuals {
        public double shadow_opacity = 0.5D;
        public double water_shadow_opacity = 0.8D;
        public boolean show_pokemon_messages = true;
        public boolean show_loot_messages = false;
        public boolean show_empty_spot_messages = true;

        public static Visuals defaults() { return new Visuals(); }

        public double shadowOpacity() { return shadow_opacity; }
        public double waterShadowOpacity() { return water_shadow_opacity; }
        public boolean showPokemonMessages() { return show_pokemon_messages; }
        public boolean showLootMessages() { return show_loot_messages; }
        public boolean showEmptySpotMessages() { return show_empty_spot_messages; }
    }
}
