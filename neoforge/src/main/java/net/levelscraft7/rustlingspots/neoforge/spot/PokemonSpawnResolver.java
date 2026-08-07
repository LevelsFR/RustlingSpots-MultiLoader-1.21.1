package net.levelscraft7.rustlingspots.neoforge.spot;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Species;
import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.spot.PokemonPoolService;
import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * NeoForge Cobblemon integration that spawns Pokemon directly through the API.
 */
public final class PokemonSpawnResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonSpawnResolver.class);
    private static final String COBBLEMON_MOD_ID = "cobblemon";

    private PokemonSpawnResolver() {
    }

    public static Optional<String> spawn(ServerLevel level, ServerPlayer player, RustlingSpot spot, RandomSource random) {
        return spawn(level, player, spot, random, false);
    }

    public static Optional<String> spawn(ServerLevel level, ServerPlayer player, RustlingSpot spot, RandomSource random, boolean forceShiny) {
        if (!RustlingSpotsPokemonConfig.POKEMON_SPAWN.enable()) {
            return Optional.empty();
        }

        if (!ModList.get().isLoaded(COBBLEMON_MOD_ID)) {
            return Optional.empty();
        }

        PokemonPoolService.PoolSelection selection = PokemonPoolService.pickRandomSelection(level, spot, random);
        PokemonPoolService.PokemonEntry entry = selection.entry();
        if (entry == null) {
            logDebugSelection(spot, selection, null, 0, null, false);
            return Optional.empty();
        }

        String species = sanitizeSpecies(entry.species());
        if (species.isEmpty()) {
            LOGGER.warn("Failed to resolve Pokemon species '{}' for rustling spot", entry.species());
            return Optional.empty();
        }

        boolean shiny = forceShiny || resolveShiny(random, entry);
        int rolledLevel = randomLevel(entry, random);
        SpawnProperties spawnProperties = SpawnProperties.from(species, shiny, rolledLevel);
        Optional<String> spawned = spawnCobblemon(level, player, spot, spawnProperties);
        logDebugSelection(spot, selection, species, rolledLevel, spawnProperties.debugText(), spawned.isPresent());
        return spawned;
    }

    public static PokemonPoolService.SpeciesValidation validateSpecies(String rawSpecies) {
        if (!ModList.get().isLoaded(COBBLEMON_MOD_ID)) {
            return new PokemonPoolService.SpeciesValidation(false, true, "Cobblemon is not loaded");
        }
        if (PokemonSpecies.count() <= 0) {
            return new PokemonPoolService.SpeciesValidation(false, true, "Cobblemon species registry is not loaded yet");
        }

        SpawnProperties parsed = SpawnProperties.from(sanitizeSpecies(rawSpecies), false, 1);
        Species species = PokemonSpecies.getByName(parsed.species());
        if (species == null) {
            return PokemonPoolService.SpeciesValidation.failed("unknown species");
        }
        if (parsed.form() != null
                && species.getFormByName(parsed.form()) == null
                && species.getFormByShowdownId(parsed.form()) == null) {
            return PokemonPoolService.SpeciesValidation.failed("invalid form syntax or unknown form");
        }
        return PokemonPoolService.SpeciesValidation.ok();
    }

    public static PokemonPoolService.SpeciesValidator speciesValidator() {
        return new PokemonPoolService.SpeciesValidator() {
            @Override
            public PokemonPoolService.SpeciesValidation validate(String species) {
                return validateSpecies(species);
            }

            @Override
            public boolean isAvailable() {
                return ModList.get().isLoaded(COBBLEMON_MOD_ID) && PokemonSpecies.count() > 0;
            }

            @Override
            public String unavailableReason() {
                if (!ModList.get().isLoaded(COBBLEMON_MOD_ID)) {
                    return "Cobblemon is not loaded";
                }
                if (PokemonSpecies.count() <= 0) {
                    return "Cobblemon species registry is not loaded yet";
                }
                return "";
            }
        };
    }

    private static Optional<String> spawnCobblemon(ServerLevel level, ServerPlayer player, RustlingSpot spot, SpawnProperties spawnProperties) {
        try {
            PokemonProperties properties = new PokemonProperties();
            properties.setSpecies(spawnProperties.species());
            properties.setForm(spawnProperties.form());
            properties.setLevel(spawnProperties.level());
            properties.setShiny(spawnProperties.shiny());

            PokemonEntity entity = properties.createEntity(level, player);
            if (entity == null) {
                LOGGER.warn("Cobblemon returned no entity for rustling spot properties {}", spawnProperties.debugText());
                return Optional.empty();
            }

            entity.setCountsTowardsSpawnCap(false);

            Vec3 spawnPos = Vec3.atBottomCenterOf(spot.getPosition()).add(0.0D, 0.15D, 0.0D);
            float yaw = level.random.nextFloat() * 360.0F;
            entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, yaw, 0.0F);
            entity.setYHeadRot(yaw);
            entity.setYBodyRot(yaw);

            if (!level.addFreshEntity(entity)) {
                LOGGER.warn("Cobblemon rejected rustling spot spawn for properties {}", spawnProperties.debugText());
                return Optional.empty();
            }

            return Optional.of(entity.getPokemon().getDisplayName(false).getString());
        } catch (Exception e) {
            LOGGER.warn("Error while spawning Cobblemon properties {} from rustling spot", spawnProperties.debugText(), e);
            return Optional.empty();
        }
    }

    private static void logDebugSelection(RustlingSpot spot, PokemonPoolService.PoolSelection selection,
                                          String species, int selectedLevel, String apiRequest, boolean success) {
        if (!RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
            return;
        }
        PokemonPoolService.PoolContext context = selection.context();
        LOGGER.info("[Rustling Spots Pokemon Debug] family={}, pos={}, block={}, fluid={}, surface={}, pool={}, validEntries={}, selected={}, level={}, apiRequest={}, spawnSucceeded={}",
                spot.getFamily().serializedName(),
                spot.getPosition(),
                context.blockAtSpot(),
                context.fluidAtSpot(),
                context.lava() ? "lava" : context.water() ? "water" : "solid",
                context.poolPath(),
                selection.validEntryCount(),
                species == null ? "<none>" : species,
                selectedLevel > 0 ? selectedLevel : "<none>",
                apiRequest == null ? "<none>" : apiRequest,
                success);
    }

    private static boolean resolveShiny(RandomSource random, PokemonPoolService.PokemonEntry entry) {
        if (entry.shiny() != null) {
            return entry.shiny();
        }
        if (entry.shinyChance() != null) {
            return random.nextDouble() < entry.shinyChance();
        }
        return random.nextDouble() < RustlingSpotsPokemonConfig.POKEMON_SPAWN.defaultShinyChance();
    }

    private static String sanitizeSpecies(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        int idx = trimmed.indexOf(":");
        return idx >= 0 ? trimmed.substring(idx + 1) : trimmed;
    }

    private static int randomLevel(PokemonPoolService.PokemonEntry entry, RandomSource random) {
        int min = entry.resolvedMinLevel();
        int max = entry.resolvedMaxLevel();
        if (max < min) {
            return min;
        }
        return random.nextInt(max - min + 1) + min;
    }

    private record SpawnProperties(String species, String form, boolean shiny, int level) {
        static SpawnProperties from(String rawSpecies, boolean shiny, int level) {
            String normalized = rawSpecies == null ? "" : rawSpecies.trim().replaceAll("\\s+", " ");
            String[] split = normalized.split(" ", 2);
            String species = split.length > 0 ? split[0] : normalized;
            String form = split.length > 1 && !split[1].isBlank() ? split[1].trim() : null;
            return new SpawnProperties(species, form, shiny, level);
        }

        String debugText() {
            return "species=" + species
                    + (form == null ? "" : ", form=" + form)
                    + ", level=" + level
                    + ", shiny=" + shiny;
        }
    }
}
