package net.levelscraft7.rustlingspots.spot;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Shared Cobblemon integration for Rustling Spot encounters.
 */
public final class PokemonSpawnResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonSpawnResolver.class);
    private static final String COBBLEMON_NAMESPACE = "cobblemon";
    private static final AtomicBoolean VALIDATION_RELOAD_HOOK_INSTALLED = new AtomicBoolean(false);

    private PokemonSpawnResolver() {
    }

    /**
     * Installs the shared validator and refreshes Rustling pools whenever Cobblemon finishes
     * rebuilding its species registry. The Cobblemon observable fires only after its registry
     * and Showdown species data are ready, which avoids validating addon species too early.
     */
    public static void installSpeciesValidation() {
        if (VALIDATION_RELOAD_HOOK_INSTALLED.compareAndSet(false, true)) {
            PokemonPoolService.setSpeciesValidator(speciesValidator());
            PokemonSpecies.INSTANCE.getObservable().subscribe((Consumer<PokemonSpecies>) ignored ->
                    PokemonPoolService.reloadAfterSpeciesRegistry(PokemonSpecies.count())
            );
            if (PokemonSpecies.count() > 0) {
                PokemonPoolService.reloadAfterSpeciesRegistry(PokemonSpecies.count());
            }
        }
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
            logDebugSelection(spot, selection, null, 0, null, false);
            return Optional.empty();
        }

        boolean shiny = forceShiny || resolveShiny(random, entry);
        int rolledLevel = randomLevel(entry, random);
        ParsedProperties parsed = parseAndValidate(entry.species());
        if (!parsed.valid()) {
            PokemonPoolService.warnInvalidEncounter(entry, parsed.error());
            logDebugSelection(spot, selection, entry.species(), rolledLevel, null, false);
            return Optional.empty();
        }

        PokemonProperties properties = parsed.properties();
        properties.setLevel(rolledLevel);
        properties.setShiny(shiny);

        Optional<String> spawned = spawnCobblemon(level, player, spot, properties);
        logDebugSelection(spot, selection, entry.species(), rolledLevel, properties.asString(" "), spawned.isPresent());
        return spawned;
    }

    public static PokemonPoolService.SpeciesValidation validateSpecies(String configuredProperties) {
        if (PokemonSpecies.count() <= 0) {
            return new PokemonPoolService.SpeciesValidation(false, true, "Cobblemon species registry is not loaded yet");
        }

        ParsedProperties parsed = parseAndValidate(configuredProperties);
        return parsed.valid()
                ? PokemonPoolService.SpeciesValidation.ok()
                : PokemonPoolService.SpeciesValidation.failed(parsed.error());
    }

    public static PokemonPoolService.SpeciesValidator speciesValidator() {
        return new PokemonPoolService.SpeciesValidator() {
            @Override
            public PokemonPoolService.SpeciesValidation validate(String configuredProperties) {
                return validateSpecies(configuredProperties);
            }

            @Override
            public boolean isAvailable() {
                return PokemonSpecies.count() > 0;
            }

            @Override
            public String unavailableReason() {
                return PokemonSpecies.count() > 0 ? "" : "Cobblemon species registry is not loaded yet";
            }
        };
    }

    private static ParsedProperties parseAndValidate(String configuredProperties) {
        if (configuredProperties == null || configuredProperties.isBlank()) {
            return ParsedProperties.failed("empty Pokemon properties");
        }

        PokemonProperties properties;
        try {
            properties = PokemonProperties.Companion.parse(configuredProperties.trim());
        } catch (RuntimeException e) {
            return ParsedProperties.failed("invalid Pokemon property syntax");
        }

        String parsedSpecies = properties.getSpecies();
        if (parsedSpecies == null || parsedSpecies.isBlank()) {
            return ParsedProperties.failed("unknown species or invalid Pokemon property syntax");
        }

        Species species = resolveSpecies(parsedSpecies);
        if (species == null) {
            return ParsedProperties.failed("unknown species '" + parsedSpecies + "'");
        }

        String requestedForm = properties.getForm();
        if (requestedForm != null && species.getForms().stream().noneMatch(form -> matchesForm(form, requestedForm))) {
            return ParsedProperties.failed("unknown form '" + requestedForm + "' for species '" + species.getResourceIdentifier() + "'");
        }

        return ParsedProperties.ok(properties);
    }

    private static Species resolveSpecies(String parsedSpecies) {
        ResourceLocation identifier = parsedSpecies.indexOf(':') >= 0
                ? ResourceLocation.tryParse(parsedSpecies)
                : ResourceLocation.fromNamespaceAndPath(COBBLEMON_NAMESPACE, parsedSpecies);
        return identifier == null ? null : PokemonSpecies.getByIdentifier(identifier);
    }

    private static boolean matchesForm(FormData form, String requestedForm) {
        return form.formOnlyShowdownId().equalsIgnoreCase(requestedForm);
    }

    private static Optional<String> spawnCobblemon(ServerLevel level, ServerPlayer player, RustlingSpot spot, PokemonProperties properties) {
        try {
            PokemonEntity entity = properties.createEntity(level, player);
            if (entity == null) {
                LOGGER.warn("[Rustling Spots] Cobblemon returned no entity for Pokemon properties '{}'", properties.getOriginalString());
                return Optional.empty();
            }

            entity.setCountsTowardsSpawnCap(false);

            Vec3 spawnPos = Vec3.atBottomCenterOf(spot.getPosition()).add(0.0D, 0.15D, 0.0D);
            float yaw = level.random.nextFloat() * 360.0F;
            entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, yaw, 0.0F);
            entity.setYHeadRot(yaw);
            entity.setYBodyRot(yaw);

            if (!level.addFreshEntity(entity)) {
                LOGGER.warn("[Rustling Spots] Cobblemon rejected rustling spot spawn for Pokemon properties '{}'", properties.getOriginalString());
                return Optional.empty();
            }

            return Optional.of(entity.getPokemon().getDisplayName(false).getString());
        } catch (Exception e) {
            LOGGER.warn("[Rustling Spots] Error while spawning Pokemon properties '{}' from rustling spot", properties.getOriginalString(), e);
            return Optional.empty();
        }
    }

    private static void logDebugSelection(RustlingSpot spot, PokemonPoolService.PoolSelection selection,
                                          String configuredProperties, int selectedLevel, String apiRequest, boolean success) {
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
                configuredProperties == null ? "<none>" : configuredProperties,
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

    private static int randomLevel(PokemonPoolService.PokemonEntry entry, RandomSource random) {
        int min = entry.resolvedMinLevel();
        int max = entry.resolvedMaxLevel();
        if (max < min) {
            return min;
        }
        return random.nextInt(max - min + 1) + min;
    }

    private record ParsedProperties(PokemonProperties properties, String error) {
        static ParsedProperties ok(PokemonProperties properties) {
            return new ParsedProperties(properties, "");
        }

        static ParsedProperties failed(String error) {
            return new ParsedProperties(null, error);
        }

        boolean valid() {
            return properties != null;
        }
    }
}
