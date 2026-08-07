package net.levelscraft7.rustlingspots.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sound mix configuration for rustling spots.
 * Stored as a shared JSON config used by all loaders in this project.
 */
public final class RustlingSpotsSoundConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpotsSoundConfig.class);

    public static Sound SOUND = Sound.defaults();
    public static Sound SOUNDS;

    private static final Path PATH = ConfigIO.configPath("rustlingspots/rustlingspots-sound.json");

    private RustlingSpotsSoundConfig() {}

    public static void load() {
        ExistingSoundConfig existing = readExistingConfig();
        SOUND = ConfigIO.loadOrCreate(PATH, Sound.defaults(), Sound.class);
        SOUNDS = SOUND;
        if (existing.hadAmbientVolume()) {
            LOGGER.info("[Rustling Spots] Removed obsolete ambient_volume from rustlingspots-sound.json; world spot ambience is disabled in v4.1.");
        }
        if (existing.pokemonSpawnVolume() != null && Math.abs(existing.pokemonSpawnVolume() - 0.8D) < 0.000001D) {
            LOGGER.info("[Rustling Spots] Preserving configured pokemon_spawn_volume 0.8; the v4.1 default for new configs is 0.4.");
        }
        if (existing.itemRewardVolume() != null && Math.abs(existing.itemRewardVolume() - 0.8D) < 0.000001D) {
            LOGGER.info("[Rustling Spots] Preserving configured item_reward_volume 0.8; the v4.1 default for new configs is 0.4.");
        }
    }

    public static void save() {
        try {
            ConfigIO.save(PATH, SOUND);
            SOUNDS = SOUND;
        } catch (Exception e) {
            LOGGER.error("[Rustling Spots] Failed to save sound config at {}", PATH, e);
        }
    }

    public static final class Sound {
        public double pokemon_spawn_volume = 0.4D;
        public double item_reward_volume = 0.4D;

        public static Sound defaults() { return new Sound(); }

        public double pokemonSpawnVolume() { return pokemon_spawn_volume; }
        public double itemRewardVolume() { return item_reward_volume; }
    }

    private static ExistingSoundConfig readExistingConfig() {
        if (!Files.exists(PATH)) {
            return ExistingSoundConfig.empty();
        }
        try (Reader reader = Files.newBufferedReader(PATH, java.nio.charset.StandardCharsets.UTF_8)) {
            JsonElement rootElement = ConfigIO.GSON.fromJson(reader, JsonElement.class);
            if (rootElement == null || !rootElement.isJsonObject()) {
                return ExistingSoundConfig.empty();
            }
            JsonObject root = rootElement.getAsJsonObject();
            return new ExistingSoundConfig(
                    root.has("ambient_volume"),
                    readDouble(root, "pokemon_spawn_volume"),
                    readDouble(root, "item_reward_volume")
            );
        } catch (Exception e) {
            LOGGER.warn("[Rustling Spots] Could not inspect existing sound config at {}", PATH, e);
            return ExistingSoundConfig.empty();
        }
    }

    private static Double readDouble(JsonObject root, String key) {
        JsonElement value = root.get(key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()
                ? value.getAsDouble()
                : null;
    }

    private record ExistingSoundConfig(boolean hadAmbientVolume, Double pokemonSpawnVolume, Double itemRewardVolume) {
        static ExistingSoundConfig empty() {
            return new ExistingSoundConfig(false, null, null);
        }
    }
}
