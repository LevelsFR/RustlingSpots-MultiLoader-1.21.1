package net.levelscraft7.rustlingspots.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Server-side multipliers for each rustling spot family spawn rate.
 * Stored as a shared JSON config used by all loaders in this project.
 */
public final class RustlingSpotsFamilySpawnConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpotsFamilySpawnConfig.class);
    public static Families FAMILIES = Families.defaults();

    private static final Path PATH = ConfigIO.configPath("rustlingspots/rustlingspots-families.json");

    private RustlingSpotsFamilySpawnConfig() {}

    public static void load() {
        Double configuredWaterRate = readConfiguredRate(RustlingSpotFamily.WATER);
        FAMILIES = ConfigIO.loadOrCreate(PATH, Families.defaults(), Families.class);
        // Ensure newly added families get defaults without nuking the file.
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            FAMILIES.familyRates.putIfAbsent(family.name(), defaultRateFor(family));
        }
        if (configuredWaterRate != null && Math.abs(configuredWaterRate - 1.0D) < 0.000001D) {
            LOGGER.info("[Rustling Spots] Preserving configured WATER spawn rate 1.0 in rustlingspots-families.json; the v4.1 default for new configs is 0.6.");
        }
    }

    private static double defaultRateFor(RustlingSpotFamily family) {
        return switch (family) {
            case GRASS -> 1.0D;
            case SAND -> 1.0D;
            case WATER -> 0.6D;
            case SNOW -> 1.0D;
            case LEAVES ->0.7D;
            case CAVE -> 1.0D;
            case NETHERFLAMME -> 0.6D;
            case SOULFLAME -> 1.0D;
            case FLYING -> 0.25D;
        };
    }

    private static Double readConfiguredRate(RustlingSpotFamily family) {
        if (!Files.exists(PATH)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            JsonElement rootElement = ConfigIO.GSON.fromJson(reader, JsonElement.class);
            if (rootElement == null || !rootElement.isJsonObject()) {
                return null;
            }
            JsonObject root = rootElement.getAsJsonObject();
            JsonElement ratesElement = root.get("familyRates");
            if (ratesElement == null || !ratesElement.isJsonObject()) {
                return null;
            }
            JsonElement value = ratesElement.getAsJsonObject().get(family.name());
            return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()
                    ? value.getAsDouble()
                    : null;
        } catch (Exception e) {
            LOGGER.warn("[Rustling Spots] Could not inspect configured {} family rate at {}", family, PATH, e);
            return null;
        }
    }

    public static final class Families {
        /**
         * Stored by enum name to keep JSON simple and stable.
         */
        public Map<String, Double> familyRates = new java.util.HashMap<>();

        public static Families defaults() {
            Families f = new Families();
            for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
                f.familyRates.put(family.name(), defaultRateFor(family));
            }
            return f;
        }

        public double spawnRate(RustlingSpotFamily family) {
            Double v = familyRates.get(family.name());
            if (v == null) {
                return defaultRateFor(family);
            }
            return Math.max(0.0D, Math.min(1.0D, v));
        }
    }
}
