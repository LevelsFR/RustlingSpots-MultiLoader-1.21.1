package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Minimal Pokemon spawning logic. Uses Cobblemon when available.
 */
public final class PokemonSpawnResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonSpawnResolver.class);

    private PokemonSpawnResolver() {
    }

    public static Optional<String> spawn(ServerLevel level, ServerPlayer player, RustlingSpot spot, RandomSource random) {
        return spawn(level, player, spot, random, false);
    }

    public static Optional<String> spawn(ServerLevel level, ServerPlayer player, RustlingSpot spot, RandomSource random, boolean forceShiny) {
        if (!RustlingSpotsPokemonConfig.POKEMON_SPAWN.enable()) {
            return Optional.empty();
        }

        PokemonPoolService.PoolSelection selection = PokemonPoolService.pickRandomSelection(level, spot, random);
        PokemonPoolService.PokemonEntry entry = selection.entry();
        if (entry == null) {
            logDebugSelection(level, spot, selection, null, 0, null, false);
            return Optional.empty();
        }

        String species = sanitizeSpecies(entry.species());
        if (species.isEmpty()) {
            LOGGER.warn("Failed to resolve Pokemon species '{}' for rustling spot", entry.species());
            return Optional.empty();
        }

        boolean shiny = forceShiny || resolveShiny(random, entry);
        int levelRoll = randomLevel(entry, random);
        String command = spawnCommand(species, shiny, levelRoll);
        Optional<String> spawned = tryCobblemonSpawn(level, player, spot, species, shiny, levelRoll, command);
        logDebugSelection(level, spot, selection, species, levelRoll, command, spawned.isPresent());
        return spawned.map(PokemonSpawnResolver::formatForMessage);
    }

    private static Optional<String> tryCobblemonSpawn(ServerLevel level, ServerPlayer player, RustlingSpot spot, String species, boolean shiny, int rolledLevel, String command) {
        if (level.getServer() == null) {
            return Optional.empty();
        }

        Vec3 spawnOrigin = Vec3.atCenterOf(spot.getPosition());
        CommandSourceStack source = level.getServer()
                .createCommandSourceStack()
                .withLevel(level)
                .withPermission(2)
                .withPosition(spawnOrigin)
                .withSuppressedOutput();
        try {
            int result = level.getServer().getCommands().getDispatcher().execute(command, source);
            if (result <= 0) {
                LOGGER.warn("Cobblemon not present or failed to spawn Pokemon {}", species);
                return Optional.empty();
            }
            return Optional.of(species);
        } catch (Exception e) {
            LOGGER.warn("Error while executing Cobblemon spawn command for {}", species, e);
            return Optional.empty();
        }
    }

    private static String spawnCommand(String species, boolean shiny, int rolledLevel) {
        return "pokespawn " + species + " level=" + rolledLevel + (shiny ? " shiny" : "");
    }

    private static void logDebugSelection(ServerLevel level, RustlingSpot spot, PokemonPoolService.PoolSelection selection,
                                          String species, int selectedLevel, String command, boolean success) {
        if (!net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
            return;
        }
        PokemonPoolService.PoolContext context = selection.context();
        LOGGER.info("[Rustling Spots Pokemon Debug] family={}, pos={}, block={}, fluid={}, surface={}, pool={}, validEntries={}, selected={}, level={}, command={}, spawnSucceeded={}",
                spot.getFamily().serializedName(),
                spot.getPosition(),
                context.blockAtSpot(),
                context.fluidAtSpot(),
                context.lava() ? "lava" : context.water() ? "water" : "solid",
                context.poolPath(),
                selection.validEntryCount(),
                species == null ? "<none>" : species,
                selectedLevel > 0 ? selectedLevel : "<none>",
                command == null ? "<none>" : command,
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

    private static String formatForMessage(String speciesId) {
        if (speciesId.isEmpty()) {
            return speciesId;
        }
        String cleaned = sanitizeSpecies(speciesId);
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
    }
}
