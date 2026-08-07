package net.levelscraft7.rustlingspots.spot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.levelscraft7.rustlingspots.common.RustlingSpotsCommon;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime cache for datapack-driven custom rustling spots.
 */
public final class CustomSpotDefinitionRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomSpotDefinitionRegistry.class);
    private static final Gson GSON = new GsonBuilder().create();
    private static final String DIRECTORY = "rustling_spots/spot_definitions";
    private static final Set<ResourceLocation> RESERVED_INTERNAL_IDS = buildReservedInternalIds();
    private static final Map<ResourceLocation, CustomSpotDefinition> DEFINITIONS = new ConcurrentHashMap<>();

    private CustomSpotDefinitionRegistry() {
    }

    public static void reload(ResourceManager resourceManager) {
        Map<ResourceLocation, CustomSpotDefinition> loaded = new LinkedHashMap<>();
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                DIRECTORY,
                resourceLocation -> resourceLocation.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (!element.isJsonObject()) {
                    warn(fileId.toString(), "Root JSON must be an object");
                    continue;
                }

                Optional<CustomSpotDefinition> parsed = parseDefinition(fileId, element.getAsJsonObject());
                if (parsed.isEmpty()) {
                    continue;
                }

                CustomSpotDefinition previous = loaded.put(parsed.get().id(), parsed.get());
                if (previous != null) {
                    warn(parsed.get().id().toString(), "Duplicate custom spot id encountered during reload; using the last loaded definition");
                }
            } catch (IOException | JsonParseException | IllegalStateException ex) {
                warn(fileId.toString(), ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            }
        }

        DEFINITIONS.clear();
        DEFINITIONS.putAll(loaded);
        LOGGER.info("[Rustling Spots] Loaded {} custom spot definition(s)", loaded.size());
    }

    public static Collection<CustomSpotDefinition> all() {
        return Collections.unmodifiableCollection(DEFINITIONS.values());
    }

    public static Optional<CustomSpotDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static List<SpotSpawnTemplate> matching(Level level, net.minecraft.core.BlockPos groundPos, BlockState groundState) {
        List<SpotSpawnTemplate> matches = new ArrayList<>();
        for (CustomSpotDefinition definition : DEFINITIONS.values()) {
            if (definition.matches(level, groundPos, groundState)) {
                matches.add(definition.template());
            }
        }
        return matches;
    }

    public static List<String> idsForSuggestions() {
        return DEFINITIONS.keySet().stream()
                .map(ResourceLocation::toString)
                .sorted()
                .toList();
    }

    private static Optional<CustomSpotDefinition> parseDefinition(ResourceLocation sourceFile, JsonObject root) {
        int formatVersion = requiredInt(root, "format_version");
        if (formatVersion != 1) {
            warn(sourceFile.toString(), "Unsupported format_version " + formatVersion);
            return Optional.empty();
        }

        String idRaw = requiredString(root, "id");
        ResourceLocation id = ResourceLocation.tryParse(idRaw);
        if (id == null) {
            warn(sourceFile.toString(), "Invalid custom spot id '" + idRaw + "'");
            return Optional.empty();
        }
        if (RESERVED_INTERNAL_IDS.contains(id)) {
            warn(id.toString(), "Internal spot ids cannot be overridden");
            return Optional.empty();
        }

        int priority = requiredInt(root, "priority");
        int weight = requiredInt(root, "weight");
        if (weight <= 0) {
            warn(id.toString(), "Weight must be positive");
            return Optional.empty();
        }

        List<String> biomeValues = requiredStringList(root, "biomes");
        List<String> blockValues = requiredStringList(root, "blocks");
        if (biomeValues.isEmpty()) {
            warn(id.toString(), "At least one biome entry is required");
            return Optional.empty();
        }
        if (blockValues.isEmpty()) {
            warn(id.toString(), "At least one block entry is required");
            return Optional.empty();
        }

        String displayName = optionalString(root, "display_name");
        String pokemonFamily = normalizeFamilyKey(requiredString(root, "pokemon_family"));
        if (!PokemonPoolService.hasFamily(pokemonFamily)) {
            warn(id.toString(), "Unknown pokemon family '" + pokemonFamily + "'");
            return Optional.empty();
        }

        String lootFamily = normalizeFamilyKey(requiredString(root, "loot_family"));
        if (!LootPoolService.hasFamily(lootFamily)) {
            LOGGER.warn("[Rustling Spots] Unknown loot family '{}' for custom spot {}; falling back to global loot", lootFamily, id);
            lootFamily = null;
        }

        Set<ResourceKey<Level>> dimensions = parseDimensions(root.get("dimensions"));
        List<CustomSpotDefinition.BlockMatcher> blocks = parseBlocks(id, blockValues);
        List<CustomSpotDefinition.BiomeMatcher> biomes = parseBiomes(id, biomeValues);
        if (blocks.isEmpty()) {
            warn(id.toString(), "No valid block entries were found");
            return Optional.empty();
        }
        if (biomes.isEmpty()) {
            warn(id.toString(), "No valid biome entries were found");
            return Optional.empty();
        }

        RustlingSpotFamily visualFamily = optionalVisualFamily(id, root);
        List<WeightedParticleReference> particles = parseParticles(id, root.get("particles"));
        SpotSpawnTemplate template = new SpotSpawnTemplate(
                id,
                visualFamily,
                pokemonFamily,
                lootFamily,
                displayName,
                priority,
                weight,
                particles,
                true
        );
        return Optional.of(new CustomSpotDefinition(template, dimensions, blocks, biomes));
    }

    private static RustlingSpotFamily optionalVisualFamily(ResourceLocation id, JsonObject root) {
        String raw = optionalString(root, "visual_family");
        if (raw == null) {
            return RustlingSpotFamily.GRASS;
        }
        Optional<RustlingSpotFamily> family = RustlingSpotFamily.fromSerializedName(raw);
        if (family.isPresent()) {
            return family.get();
        }
        warn(id.toString(), "Unknown visual_family '" + raw + "'; falling back to grass for compatibility");
        return RustlingSpotFamily.GRASS;
    }

    private static Set<ResourceKey<Level>> parseDimensions(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Set.of(Level.OVERWORLD);
        }

        List<String> rawValues = asStringList(element);
        Set<ResourceKey<Level>> dimensions = new LinkedHashSet<>();
        for (String raw : rawValues) {
            ResourceLocation id = ResourceLocation.tryParse(raw);
            if (id != null) {
                dimensions.add(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id));
            }
        }
        return dimensions.isEmpty() ? Set.of(Level.OVERWORLD) : Set.copyOf(dimensions);
    }

    private static List<CustomSpotDefinition.BlockMatcher> parseBlocks(ResourceLocation id, List<String> values) {
        List<CustomSpotDefinition.BlockMatcher> matchers = new ArrayList<>();
        for (String value : values) {
            CustomSpotDefinition.BlockMatcher matcher = CustomSpotDefinition.BlockMatcher.parse(value);
            if (matcher == null) {
                LOGGER.warn("[Rustling Spots] Failed loading custom spot definition {}: invalid block matcher '{}'", id, value);
                continue;
            }
            matchers.add(matcher);
        }
        return matchers;
    }

    private static List<CustomSpotDefinition.BiomeMatcher> parseBiomes(ResourceLocation id, List<String> values) {
        List<CustomSpotDefinition.BiomeMatcher> matchers = new ArrayList<>();
        for (String value : values) {
            CustomSpotDefinition.BiomeMatcher matcher = CustomSpotDefinition.BiomeMatcher.parse(value);
            if (matcher == null) {
                LOGGER.warn("[Rustling Spots] Failed loading custom spot definition {}: invalid biome matcher '{}'", id, value);
                continue;
            }
            matchers.add(matcher);
        }
        return matchers;
    }

    private static List<WeightedParticleReference> parseParticles(ResourceLocation id, JsonElement element) {
        if (element == null || element.isJsonNull()) {
            warn(id.toString(), "Missing particles array; falling back to grass particles");
            return List.of();
        }

        JsonArray array = element.getAsJsonArray();
        List<WeightedParticleReference> particles = new ArrayList<>();
        for (JsonElement entryElement : array) {
            if (!entryElement.isJsonObject()) {
                LOGGER.warn("[Rustling Spots] Failed loading custom spot definition {}: particle entries must be objects", id);
                continue;
            }

            JsonObject entry = entryElement.getAsJsonObject();
            ResourceLocation particleId = ResourceLocation.tryParse(requiredString(entry, "type"));
            int weight = requiredInt(entry, "weight");
            if (particleId == null || weight <= 0) {
                LOGGER.warn("[Rustling Spots] Failed loading custom spot definition {}: invalid particle entry '{}'", id, GSON.toJson(entry));
                continue;
            }

            ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.getOptional(particleId).orElse(null);
            if (!(particleType instanceof SimpleParticleType)) {
                LOGGER.warn("[Rustling Spots] Failed loading custom spot definition {}: unknown or unsupported particle '{}'", id, particleId);
                continue;
            }

            particles.add(new WeightedParticleReference(particleId, weight));
        }

        if (particles.isEmpty()) {
            LOGGER.warn("[Rustling Spots] Failed loading custom spot definition {}: no valid particles found, falling back to grass particles", id);
        }
        return List.copyOf(particles);
    }

    private static int requiredInt(JsonObject root, String key) {
        if (!root.has(key)) {
            throw new JsonParseException("Missing required field '" + key + "'");
        }
        return root.get(key).getAsInt();
    }

    private static String requiredString(JsonObject root, String key) {
        if (!root.has(key)) {
            throw new JsonParseException("Missing required field '" + key + "'");
        }
        return root.get(key).getAsString();
    }

    private static String optionalString(JsonObject root, String key) {
        if (!root.has(key) || root.get(key).isJsonNull()) {
            return null;
        }
        return root.get(key).getAsString();
    }

    private static List<String> requiredStringList(JsonObject root, String key) {
        if (!root.has(key)) {
            throw new JsonParseException("Missing required field '" + key + "'");
        }
        return asStringList(root.get(key));
    }

    private static List<String> asStringList(JsonElement element) {
        if (!element.isJsonArray()) {
            throw new JsonParseException("Expected an array");
        }

        List<String> values = new ArrayList<>();
        for (JsonElement arrayEntry : element.getAsJsonArray()) {
            if (arrayEntry.isJsonNull()) {
                continue;
            }
            values.add(arrayEntry.getAsString());
        }
        return values;
    }

    private static Set<ResourceLocation> buildReservedInternalIds() {
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            ids.add(family.spotId());
        }
        return Set.copyOf(ids);
    }

    private static String normalizeFamilyKey(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static void warn(String id, String message) {
        LOGGER.warn("[Rustling Spots] Failed loading custom spot definition {}: {}", id, message);
    }
}
