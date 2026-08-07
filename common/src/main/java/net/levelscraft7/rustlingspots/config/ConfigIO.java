package net.levelscraft7.rustlingspots.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Minimal JSON config IO shared across loaders.
 */
public final class ConfigIO {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigIO.class);
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigIO() {}

    static Path configPath(String relative) {
        return Platform.getConfigFolder().resolve(relative);
    }

    static <T> T loadOrCreate(Path path, T defaults, Class<T> type) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            LOGGER.error("[Rustling Spots] Failed to create config directory for {} at {}", path.getFileName(), path, e);
            return defaults;
        }

        if (!Files.exists(path)) {
            try {
                save(path, defaults);
            } catch (IOException e) {
                LOGGER.error("[Rustling Spots] Failed to write default config {} at {}", path.getFileName(), path, e);
            }
            return defaults;
        }

        JsonElement loadedJson;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            loadedJson = GSON.fromJson(reader, JsonElement.class);
        } catch (Exception e) {
            replaceInvalidConfigSafely(path, defaults, e);
            return defaults;
        }

        if (loadedJson == null || loadedJson.isJsonNull()) {
            replaceInvalidConfigSafely(path, defaults, new IllegalArgumentException("empty JSON document"));
            return defaults;
        }

        T loaded;
        try {
            JsonElement mergedJson = mergeJson(GSON.toJsonTree(defaults), loadedJson);
            loaded = GSON.fromJson(mergedJson, type);
        } catch (Exception e) {
            replaceInvalidConfigSafely(path, defaults, e);
            return defaults;
        }
        if (loaded == null) {
            replaceInvalidConfigSafely(path, defaults, new IllegalArgumentException("JSON could not be deserialized as " + type.getSimpleName()));
            return defaults;
        }

        try {
            save(path, loaded);
        } catch (IOException e) {
            LOGGER.error("[Rustling Spots] Failed to save merged config {} at {}", path.getFileName(), path, e);
        }
        return loaded;
    }

    private static <T> void replaceInvalidConfigSafely(Path path, T defaults, Exception reason) {
        try {
            replaceInvalidConfig(path, defaults, reason.getMessage() != null ? reason.getMessage() : reason.getClass().getSimpleName());
        } catch (IOException backupError) {
            LOGGER.error("[Rustling Spots] Failed to back up invalid config {} at {} before replacement", path.getFileName(), path, backupError);
        }
    }

    static void save(Path path, Object value) throws IOException {
        Files.createDirectories(path.getParent());
        Path temp = path.resolveSibling(path.getFileName().toString() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            GSON.toJson(value, writer);
        }
        try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static <T> void replaceInvalidConfig(Path path, T defaults, String reason) throws IOException {
        LOGGER.error("[Rustling Spots] Invalid JSON config {} at {}: {}", path.getFileName(), path, reason);
        if (Files.exists(path)) {
            Path backup = uniqueInvalidBackupPath(path);
            Files.move(path, backup);
            LOGGER.warn("[Rustling Spots] Backed up invalid config {} to {}", path.getFileName(), backup);
        }
        save(path, defaults);
    }

    private static Path uniqueInvalidBackupPath(Path path) {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "").replace(".", "-");
        String fileName = path.getFileName().toString();
        Path candidate = path.resolveSibling(fileName + "." + timestamp + ".invalid.bak");
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = path.resolveSibling(fileName + "." + timestamp + "." + suffix + ".invalid.bak");
            suffix++;
        }
        return candidate;
    }

    private static JsonElement mergeJson(JsonElement defaults, JsonElement loaded) {
        if (defaults == null || defaults.isJsonNull()) {
            return loaded;
        }
        if (loaded == null || loaded.isJsonNull()) {
            return defaults;
        }
        if (defaults.isJsonObject() && loaded.isJsonObject()) {
            JsonObject merged = defaults.getAsJsonObject().deepCopy();
            JsonObject loadedObject = loaded.getAsJsonObject();
            for (String key : loadedObject.keySet()) {
                JsonElement defaultValue = merged.get(key);
                JsonElement loadedValue = loadedObject.get(key);
                merged.add(key, mergeJson(defaultValue, loadedValue));
            }
            return merged;
        }
        return loaded;
    }
}
