package net.levelscraft7.rustlingspots.config;

import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loader-neutral copy of every setting controlled by the in-game Cloth Config screen.
 */
public record RustlingServerConfigSnapshot(
        boolean enabled,
        boolean loggingEnabled,
        int minDistanceBetweenSpots,
        int spotLifetimeTicks,
        double interactionRadius,
        double interactionVerticalAllowance,
        int playerSpotRadius,
        int maxSpotsPerPlayer,
        int maxSpotsTotal,
        boolean allowCobblemonRaidDenDimensions,
        double shinySpotChance,
        boolean announceShinyFindsGlobally,
        boolean enableEmptySpots,
        double emptySpotChance,
        boolean enableMultipleRewardRolls,
        int minRewardRolls,
        int maxRewardRolls,
        boolean allowMultiplePokemonPerSpot,
        double mixedRewardSpotChance,
        boolean pokemonEnabled,
        double pokemonEncounterChance,
        double defaultPokemonShinyChance,
        int minPokemonLevel,
        int maxPokemonLevel,
        boolean typedSpawnRules,
        double pokemonSpawnVolume,
        double itemRewardVolume,
        Map<RustlingSpotFamily, Double> familySpawnRates
) {
    public RustlingServerConfigSnapshot {
        familySpawnRates = Map.copyOf(familySpawnRates);
    }

    public static RustlingServerConfigSnapshot current() {
        EnumMap<RustlingSpotFamily, Double> rates = new EnumMap<>(RustlingSpotFamily.class);
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            rates.put(family, RustlingSpotsFamilySpawnConfig.FAMILIES.spawnRate(family));
        }

        var general = RustlingSpotsServerConfig.GENERAL;
        var pokemon = RustlingSpotsPokemonConfig.POKEMON;
        var sound = RustlingSpotsSoundConfig.SOUND;
        return new RustlingServerConfigSnapshot(
                general.enabled,
                general.enable_logging,
                general.min_distance_between_spots,
                general.spot_lifetime_ticks,
                general.interaction_radius,
                general.interaction_vertical_allowance,
                general.player_spot_radius,
                general.max_spots_per_player,
                general.max_spots_total,
                general.allow_cobblemon_raid_den_dimensions,
                general.shiny_spot_chance,
                general.announce_shiny_finds_globally,
                general.enable_empty_spots,
                general.empty_spot_chance,
                general.enable_multiple_reward_rolls,
                general.min_reward_rolls,
                general.max_reward_rolls,
                general.allow_multiple_pokemon_per_spot,
                general.mixed_reward_spot_chance,
                pokemon.enable,
                pokemon.encounter_chance,
                pokemon.default_shiny_chance,
                pokemon.min_level,
                pokemon.max_level,
                pokemon.typed_spawn_rules,
                sound.pokemon_spawn_volume,
                sound.item_reward_volume,
                rates
        );
    }

    public Optional<String> validationError() {
        if (!inRange(minDistanceBetweenSpots, 1, 512)) return Optional.of("min_distance_between_spots must be between 1 and 512");
        if (!inRange(spotLifetimeTicks, 20, 72000)) return Optional.of("spot_lifetime_ticks must be between 20 and 72000");
        if (!inRange(interactionRadius, 0.5D, 16.0D)) return Optional.of("interaction_radius must be between 0.5 and 16");
        if (!inRange(interactionVerticalAllowance, 0.5D, 16.0D)) return Optional.of("interaction_vertical_allowance must be between 0.5 and 16");
        if (!inRange(playerSpotRadius, 16, 512)) return Optional.of("player_spot_radius must be between 16 and 512");
        if (!inRange(maxSpotsPerPlayer, 1, 256)) return Optional.of("max_spots_per_player must be between 1 and 256");
        if (!inRange(maxSpotsTotal, 1, 2048)) return Optional.of("max_spots_total must be between 1 and 2048");
        if (!inRange(shinySpotChance, 0.0D, 1.0D)) return Optional.of("shiny_spot_chance must be between 0 and 1");
        if (!inRange(emptySpotChance, 0.0D, 0.05D)) return Optional.of("empty_spot_chance must be between 0 and 0.05");
        if (!inRange(minRewardRolls, 1, 16) || !inRange(maxRewardRolls, 1, 16)) return Optional.of("reward rolls must be between 1 and 16");
        if (minRewardRolls > maxRewardRolls) return Optional.of("minimum reward rolls cannot exceed maximum reward rolls");
        if (!inRange(mixedRewardSpotChance, 0.0D, 0.25D)) return Optional.of("mixed_reward_spot_chance must be between 0 and 0.25");
        if (!inRange(pokemonEncounterChance, 0.0D, 1.0D)) return Optional.of("encounter_chance must be between 0 and 1");
        if (!inRange(defaultPokemonShinyChance, 0.0D, 1.0D)) return Optional.of("default_shiny_chance must be between 0 and 1");
        if (!inRange(minPokemonLevel, 1, 100) || !inRange(maxPokemonLevel, 1, 100)) return Optional.of("Pokemon levels must be between 1 and 100");
        if (minPokemonLevel > maxPokemonLevel) return Optional.of("minimum Pokemon level cannot exceed maximum Pokemon level");
        if (!inRange(pokemonSpawnVolume, 0.0D, 2.0D) || !inRange(itemRewardVolume, 0.0D, 2.0D)) return Optional.of("sound volumes must be between 0 and 2");
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            Double rate = familySpawnRates.get(family);
            if (rate == null || !inRange(rate, 0.0D, 1.0D)) {
                return Optional.of(family.name().toLowerCase() + " family rate must be between 0 and 1");
            }
        }
        return Optional.empty();
    }

    public void applyAndSave() {
        Optional<String> error = validationError();
        if (error.isPresent()) {
            throw new IllegalArgumentException(error.get());
        }

        var general = RustlingSpotsServerConfig.GENERAL;
        general.enabled = enabled;
        general.enable_logging = loggingEnabled;
        general.min_distance_between_spots = minDistanceBetweenSpots;
        general.spot_lifetime_ticks = spotLifetimeTicks;
        general.interaction_radius = interactionRadius;
        general.interaction_vertical_allowance = interactionVerticalAllowance;
        general.player_spot_radius = playerSpotRadius;
        general.max_spots_per_player = maxSpotsPerPlayer;
        general.max_spots_total = maxSpotsTotal;
        general.allow_cobblemon_raid_den_dimensions = allowCobblemonRaidDenDimensions;
        general.shiny_spot_chance = shinySpotChance;
        general.announce_shiny_finds_globally = announceShinyFindsGlobally;
        general.enable_empty_spots = enableEmptySpots;
        general.empty_spot_chance = emptySpotChance;
        general.enable_multiple_reward_rolls = enableMultipleRewardRolls;
        general.min_reward_rolls = minRewardRolls;
        general.max_reward_rolls = maxRewardRolls;
        general.allow_multiple_pokemon_per_spot = allowMultiplePokemonPerSpot;
        general.mixed_reward_spot_chance = mixedRewardSpotChance;

        var pokemon = RustlingSpotsPokemonConfig.POKEMON;
        pokemon.enable = pokemonEnabled;
        pokemon.encounter_chance = pokemonEncounterChance;
        pokemon.default_shiny_chance = defaultPokemonShinyChance;
        pokemon.min_level = minPokemonLevel;
        pokemon.max_level = maxPokemonLevel;
        pokemon.typed_spawn_rules = typedSpawnRules;

        var sound = RustlingSpotsSoundConfig.SOUND;
        sound.pokemon_spawn_volume = pokemonSpawnVolume;
        sound.item_reward_volume = itemRewardVolume;

        RustlingSpotsFamilySpawnConfig.FAMILIES.familyRates.clear();
        familySpawnRates.forEach((family, rate) ->
                RustlingSpotsFamilySpawnConfig.FAMILIES.familyRates.put(family.name(), rate));

        RustlingSpotsServerConfig.save();
        RustlingSpotsPokemonConfig.save();
        RustlingSpotsSoundConfig.save();
        RustlingSpotsFamilySpawnConfig.save();
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(enabled);
        buf.writeBoolean(loggingEnabled);
        buf.writeVarInt(minDistanceBetweenSpots);
        buf.writeVarInt(spotLifetimeTicks);
        buf.writeDouble(interactionRadius);
        buf.writeDouble(interactionVerticalAllowance);
        buf.writeVarInt(playerSpotRadius);
        buf.writeVarInt(maxSpotsPerPlayer);
        buf.writeVarInt(maxSpotsTotal);
        buf.writeBoolean(allowCobblemonRaidDenDimensions);
        buf.writeDouble(shinySpotChance);
        buf.writeBoolean(announceShinyFindsGlobally);
        buf.writeBoolean(enableEmptySpots);
        buf.writeDouble(emptySpotChance);
        buf.writeBoolean(enableMultipleRewardRolls);
        buf.writeVarInt(minRewardRolls);
        buf.writeVarInt(maxRewardRolls);
        buf.writeBoolean(allowMultiplePokemonPerSpot);
        buf.writeDouble(mixedRewardSpotChance);
        buf.writeBoolean(pokemonEnabled);
        buf.writeDouble(pokemonEncounterChance);
        buf.writeDouble(defaultPokemonShinyChance);
        buf.writeVarInt(minPokemonLevel);
        buf.writeVarInt(maxPokemonLevel);
        buf.writeBoolean(typedSpawnRules);
        buf.writeDouble(pokemonSpawnVolume);
        buf.writeDouble(itemRewardVolume);
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            buf.writeDouble(familySpawnRates.getOrDefault(family, 0.0D));
        }
    }

    public static RustlingServerConfigSnapshot decode(RegistryFriendlyByteBuf buf) {
        boolean enabled = buf.readBoolean();
        boolean loggingEnabled = buf.readBoolean();
        int minDistanceBetweenSpots = buf.readVarInt();
        int spotLifetimeTicks = buf.readVarInt();
        double interactionRadius = buf.readDouble();
        double interactionVerticalAllowance = buf.readDouble();
        int playerSpotRadius = buf.readVarInt();
        int maxSpotsPerPlayer = buf.readVarInt();
        int maxSpotsTotal = buf.readVarInt();
        boolean allowRaidDenDimensions = buf.readBoolean();
        double shinySpotChance = buf.readDouble();
        boolean announceShiny = buf.readBoolean();
        boolean enableEmptySpots = buf.readBoolean();
        double emptySpotChance = buf.readDouble();
        boolean enableMultipleRolls = buf.readBoolean();
        int minRewardRolls = buf.readVarInt();
        int maxRewardRolls = buf.readVarInt();
        boolean allowMultiplePokemon = buf.readBoolean();
        double mixedRewardChance = buf.readDouble();
        boolean pokemonEnabled = buf.readBoolean();
        double encounterChance = buf.readDouble();
        double defaultShinyChance = buf.readDouble();
        int minLevel = buf.readVarInt();
        int maxLevel = buf.readVarInt();
        boolean typedSpawnRules = buf.readBoolean();
        double pokemonSpawnVolume = buf.readDouble();
        double itemRewardVolume = buf.readDouble();
        EnumMap<RustlingSpotFamily, Double> rates = new EnumMap<>(RustlingSpotFamily.class);
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            rates.put(family, buf.readDouble());
        }
        return new RustlingServerConfigSnapshot(enabled, loggingEnabled, minDistanceBetweenSpots, spotLifetimeTicks,
                interactionRadius, interactionVerticalAllowance, playerSpotRadius, maxSpotsPerPlayer, maxSpotsTotal,
                allowRaidDenDimensions, shinySpotChance, announceShiny, enableEmptySpots, emptySpotChance,
                enableMultipleRolls, minRewardRolls, maxRewardRolls, allowMultiplePokemon, mixedRewardChance,
                pokemonEnabled, encounterChance, defaultShinyChance, minLevel, maxLevel, typedSpawnRules,
                pokemonSpawnVolume, itemRewardVolume, rates);
    }

    private static boolean inRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    private static boolean inRange(double value, double min, double max) {
        return Double.isFinite(value) && value >= min && value <= max;
    }
}
