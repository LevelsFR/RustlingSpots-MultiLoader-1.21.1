package net.levelscraft7.rustlingspots.configscreen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.levelscraft7.rustlingspots.config.RustlingServerConfigSnapshot;
import net.levelscraft7.rustlingspots.config.RustlingSpotsClientConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsSoundConfig;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;

public final class RustlingSpotsClothConfigScreen {
    private RustlingSpotsClothConfigScreen() {
    }

    public static Screen create(
            Screen parent,
            RustlingServerConfigSnapshot snapshot,
            boolean canEditServer,
            boolean connected
    ) {
        Draft draft = new Draft(snapshot);
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.rustlingspots.title"))
                .setDoesConfirmSave(true)
                .setTransparentBackground(true);
        ConfigEntryBuilder entries = builder.entryBuilder();

        addClientCategory(builder, entries);
        if (canEditServer) {
            addGeneralCategory(builder, entries, draft);
            addPokemonCategory(builder, entries, draft);
            addRewardsCategory(builder, entries, draft);
            addFamilyCategory(builder, entries, draft);
        } else {
            ConfigCategory locked = builder.getOrCreateCategory(Component.translatable("config.rustlingspots.category.server"));
            locked.addEntry(entries.startTextDescription(Component.translatable("config.rustlingspots.server.locked")).build());
        }

        builder.setSavingRunnable(() -> {
            RustlingConfigClientState.saveClientConfig();
            if (canEditServer) {
                RustlingConfigClientState.saveServerConfig(draft.toSnapshot(), connected);
            }
        });
        return builder.build();
    }

    private static void addClientCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        var visuals = RustlingSpotsClientConfig.VISUALS;
        var defaults = RustlingSpotsClientConfig.Visuals.defaults();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.rustlingspots.category.client"));
        category.addEntry(entries.startDoubleField(key("shadow_opacity"), visuals.shadow_opacity)
                .setDefaultValue(defaults.shadow_opacity).setMin(0.0D).setMax(1.0D)
                .setSaveConsumer(value -> visuals.shadow_opacity = value).build());
        category.addEntry(entries.startDoubleField(key("water_shadow_opacity"), visuals.water_shadow_opacity)
                .setDefaultValue(defaults.water_shadow_opacity).setMin(0.0D).setMax(1.0D)
                .setSaveConsumer(value -> visuals.water_shadow_opacity = value).build());
        category.addEntry(entries.startBooleanToggle(key("show_pokemon_messages"), visuals.show_pokemon_messages)
                .setDefaultValue(defaults.show_pokemon_messages)
                .setSaveConsumer(value -> visuals.show_pokemon_messages = value).build());
        category.addEntry(entries.startBooleanToggle(key("show_loot_messages"), visuals.show_loot_messages)
                .setDefaultValue(defaults.show_loot_messages)
                .setSaveConsumer(value -> visuals.show_loot_messages = value).build());
        category.addEntry(entries.startBooleanToggle(key("show_empty_spot_messages"), visuals.show_empty_spot_messages)
                .setDefaultValue(defaults.show_empty_spot_messages)
                .setSaveConsumer(value -> visuals.show_empty_spot_messages = value).build());
    }

    private static void addGeneralCategory(ConfigBuilder builder, ConfigEntryBuilder entries, Draft draft) {
        var defaults = RustlingSpotsServerConfig.General.defaults();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.rustlingspots.category.general"));
        category.addEntry(entries.startBooleanToggle(key("enabled"), draft.enabled)
                .setDefaultValue(defaults.enabled).setSaveConsumer(value -> draft.enabled = value).build());
        category.addEntry(entries.startBooleanToggle(key("enable_logging"), draft.loggingEnabled)
                .setDefaultValue(defaults.enable_logging).setSaveConsumer(value -> draft.loggingEnabled = value).build());
        category.addEntry(entries.startIntField(key("min_distance_between_spots"), draft.minDistanceBetweenSpots)
                .setDefaultValue(defaults.min_distance_between_spots).setMin(1).setMax(512)
                .setSaveConsumer(value -> draft.minDistanceBetweenSpots = value).build());
        category.addEntry(entries.startIntField(key("spot_lifetime_ticks"), draft.spotLifetimeTicks)
                .setDefaultValue(defaults.spot_lifetime_ticks).setMin(20).setMax(72000)
                .setSaveConsumer(value -> draft.spotLifetimeTicks = value).build());
        category.addEntry(entries.startDoubleField(key("interaction_radius"), draft.interactionRadius)
                .setDefaultValue(defaults.interaction_radius).setMin(0.5D).setMax(16.0D)
                .setSaveConsumer(value -> draft.interactionRadius = value).build());
        category.addEntry(entries.startDoubleField(key("interaction_vertical_allowance"), draft.interactionVerticalAllowance)
                .setDefaultValue(defaults.interaction_vertical_allowance).setMin(0.5D).setMax(16.0D)
                .setSaveConsumer(value -> draft.interactionVerticalAllowance = value).build());
        category.addEntry(entries.startIntField(key("player_spot_radius"), draft.playerSpotRadius)
                .setDefaultValue(defaults.player_spot_radius).setMin(16).setMax(512)
                .setSaveConsumer(value -> draft.playerSpotRadius = value).build());
        category.addEntry(entries.startIntField(key("max_spots_per_player"), draft.maxSpotsPerPlayer)
                .setDefaultValue(defaults.max_spots_per_player).setMin(1).setMax(256)
                .setSaveConsumer(value -> draft.maxSpotsPerPlayer = value).build());
        category.addEntry(entries.startIntField(key("max_spots_total"), draft.maxSpotsTotal)
                .setDefaultValue(defaults.max_spots_total).setMin(1).setMax(2048)
                .setSaveConsumer(value -> draft.maxSpotsTotal = value).build());
        category.addEntry(entries.startBooleanToggle(key("allow_cobblemon_raid_den_dimensions"), draft.allowRaidDenDimensions)
                .setDefaultValue(defaults.allow_cobblemon_raid_den_dimensions)
                .setSaveConsumer(value -> draft.allowRaidDenDimensions = value).build());
    }

    private static void addPokemonCategory(ConfigBuilder builder, ConfigEntryBuilder entries, Draft draft) {
        var defaults = RustlingSpotsPokemonConfig.Pokemon.defaults();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.rustlingspots.category.pokemon"));
        category.addEntry(entries.startBooleanToggle(key("pokemon_enabled"), draft.pokemonEnabled)
                .setDefaultValue(defaults.enable).setSaveConsumer(value -> draft.pokemonEnabled = value).build());
        category.addEntry(entries.startDoubleField(key("pokemon_encounter_chance"), draft.pokemonEncounterChance)
                .setDefaultValue(defaults.encounter_chance).setMin(0.0D).setMax(1.0D)
                .setSaveConsumer(value -> draft.pokemonEncounterChance = value).build());
        category.addEntry(entries.startDoubleField(key("default_pokemon_shiny_chance"), draft.defaultPokemonShinyChance)
                .setDefaultValue(defaults.default_shiny_chance).setMin(0.0D).setMax(1.0D)
                .setSaveConsumer(value -> draft.defaultPokemonShinyChance = value).build());
        category.addEntry(entries.startIntField(key("min_pokemon_level"), draft.minPokemonLevel)
                .setDefaultValue(defaults.min_level).setMin(1).setMax(100)
                .setSaveConsumer(value -> draft.minPokemonLevel = value).build());
        category.addEntry(entries.startIntField(key("max_pokemon_level"), draft.maxPokemonLevel)
                .setDefaultValue(defaults.max_level).setMin(1).setMax(100)
                .setSaveConsumer(value -> draft.maxPokemonLevel = value).build());
        category.addEntry(entries.startBooleanToggle(key("typed_spawn_rules"), draft.typedSpawnRules)
                .setDefaultValue(defaults.typed_spawn_rules).setSaveConsumer(value -> draft.typedSpawnRules = value).build());
    }

    private static void addRewardsCategory(ConfigBuilder builder, ConfigEntryBuilder entries, Draft draft) {
        var defaults = RustlingSpotsServerConfig.General.defaults();
        var soundDefaults = RustlingSpotsSoundConfig.Sound.defaults();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.rustlingspots.category.rewards"));
        category.addEntry(entries.startDoubleField(key("shiny_spot_chance"), draft.shinySpotChance)
                .setDefaultValue(defaults.shiny_spot_chance).setMin(0.0D).setMax(1.0D)
                .setSaveConsumer(value -> draft.shinySpotChance = value).build());
        category.addEntry(entries.startBooleanToggle(key("announce_shiny_finds_globally"), draft.announceShinyGlobally)
                .setDefaultValue(defaults.announce_shiny_finds_globally)
                .setSaveConsumer(value -> draft.announceShinyGlobally = value).build());
        category.addEntry(entries.startBooleanToggle(key("enable_empty_spots"), draft.enableEmptySpots)
                .setDefaultValue(defaults.enable_empty_spots).setSaveConsumer(value -> draft.enableEmptySpots = value).build());
        category.addEntry(entries.startDoubleField(key("empty_spot_chance"), draft.emptySpotChance)
                .setDefaultValue(defaults.empty_spot_chance).setMin(0.0D).setMax(0.05D)
                .setSaveConsumer(value -> draft.emptySpotChance = value).build());
        category.addEntry(entries.startBooleanToggle(key("enable_multiple_reward_rolls"), draft.enableMultipleRewardRolls)
                .setDefaultValue(defaults.enable_multiple_reward_rolls)
                .setSaveConsumer(value -> draft.enableMultipleRewardRolls = value).build());
        category.addEntry(entries.startIntField(key("min_reward_rolls"), draft.minRewardRolls)
                .setDefaultValue(defaults.min_reward_rolls).setMin(1).setMax(16)
                .setSaveConsumer(value -> draft.minRewardRolls = value).build());
        category.addEntry(entries.startIntField(key("max_reward_rolls"), draft.maxRewardRolls)
                .setDefaultValue(defaults.max_reward_rolls).setMin(1).setMax(16)
                .setSaveConsumer(value -> draft.maxRewardRolls = value).build());
        category.addEntry(entries.startBooleanToggle(key("allow_multiple_pokemon_per_spot"), draft.allowMultiplePokemonPerSpot)
                .setDefaultValue(defaults.allow_multiple_pokemon_per_spot)
                .setSaveConsumer(value -> draft.allowMultiplePokemonPerSpot = value).build());
        category.addEntry(entries.startDoubleField(key("mixed_reward_spot_chance"), draft.mixedRewardSpotChance)
                .setDefaultValue(defaults.mixed_reward_spot_chance).setMin(0.0D).setMax(0.25D)
                .setSaveConsumer(value -> draft.mixedRewardSpotChance = value).build());
        category.addEntry(entries.startDoubleField(key("pokemon_spawn_volume"), draft.pokemonSpawnVolume)
                .setDefaultValue(soundDefaults.pokemon_spawn_volume).setMin(0.0D).setMax(2.0D)
                .setSaveConsumer(value -> draft.pokemonSpawnVolume = value).build());
        category.addEntry(entries.startDoubleField(key("item_reward_volume"), draft.itemRewardVolume)
                .setDefaultValue(soundDefaults.item_reward_volume).setMin(0.0D).setMax(2.0D)
                .setSaveConsumer(value -> draft.itemRewardVolume = value).build());
    }

    private static void addFamilyCategory(ConfigBuilder builder, ConfigEntryBuilder entries, Draft draft) {
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.rustlingspots.category.families"));
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            double defaultValue = defaultFamilyRate(family);
            category.addEntry(entries.startDoubleField(
                            Component.translatable("config.rustlingspots.family." + family.name().toLowerCase()),
                            draft.familyRates.get(family))
                    .setDefaultValue(defaultValue).setMin(0.0D).setMax(1.0D)
                    .setSaveConsumer(value -> draft.familyRates.put(family, value)).build());
        }
    }

    private static Component key(String name) {
        return Component.translatable("config.rustlingspots.option." + name);
    }

    private static double defaultFamilyRate(RustlingSpotFamily family) {
        return switch (family) {
            case GRASS, SAND, SNOW, CAVE, SOULFLAME -> 1.0D;
            case WATER, NETHERFLAMME -> 0.6D;
            case LEAVES -> 0.7D;
            case FLYING -> 0.25D;
        };
    }

    private static final class Draft {
        boolean enabled;
        boolean loggingEnabled;
        int minDistanceBetweenSpots;
        int spotLifetimeTicks;
        double interactionRadius;
        double interactionVerticalAllowance;
        int playerSpotRadius;
        int maxSpotsPerPlayer;
        int maxSpotsTotal;
        boolean allowRaidDenDimensions;
        double shinySpotChance;
        boolean announceShinyGlobally;
        boolean enableEmptySpots;
        double emptySpotChance;
        boolean enableMultipleRewardRolls;
        int minRewardRolls;
        int maxRewardRolls;
        boolean allowMultiplePokemonPerSpot;
        double mixedRewardSpotChance;
        boolean pokemonEnabled;
        double pokemonEncounterChance;
        double defaultPokemonShinyChance;
        int minPokemonLevel;
        int maxPokemonLevel;
        boolean typedSpawnRules;
        double pokemonSpawnVolume;
        double itemRewardVolume;
        final EnumMap<RustlingSpotFamily, Double> familyRates = new EnumMap<>(RustlingSpotFamily.class);

        Draft(RustlingServerConfigSnapshot config) {
            enabled = config.enabled();
            loggingEnabled = config.loggingEnabled();
            minDistanceBetweenSpots = config.minDistanceBetweenSpots();
            spotLifetimeTicks = config.spotLifetimeTicks();
            interactionRadius = config.interactionRadius();
            interactionVerticalAllowance = config.interactionVerticalAllowance();
            playerSpotRadius = config.playerSpotRadius();
            maxSpotsPerPlayer = config.maxSpotsPerPlayer();
            maxSpotsTotal = config.maxSpotsTotal();
            allowRaidDenDimensions = config.allowCobblemonRaidDenDimensions();
            shinySpotChance = config.shinySpotChance();
            announceShinyGlobally = config.announceShinyFindsGlobally();
            enableEmptySpots = config.enableEmptySpots();
            emptySpotChance = config.emptySpotChance();
            enableMultipleRewardRolls = config.enableMultipleRewardRolls();
            minRewardRolls = config.minRewardRolls();
            maxRewardRolls = config.maxRewardRolls();
            allowMultiplePokemonPerSpot = config.allowMultiplePokemonPerSpot();
            mixedRewardSpotChance = config.mixedRewardSpotChance();
            pokemonEnabled = config.pokemonEnabled();
            pokemonEncounterChance = config.pokemonEncounterChance();
            defaultPokemonShinyChance = config.defaultPokemonShinyChance();
            minPokemonLevel = config.minPokemonLevel();
            maxPokemonLevel = config.maxPokemonLevel();
            typedSpawnRules = config.typedSpawnRules();
            pokemonSpawnVolume = config.pokemonSpawnVolume();
            itemRewardVolume = config.itemRewardVolume();
            familyRates.putAll(config.familySpawnRates());
        }

        RustlingServerConfigSnapshot toSnapshot() {
            return new RustlingServerConfigSnapshot(enabled, loggingEnabled, minDistanceBetweenSpots,
                    spotLifetimeTicks, interactionRadius, interactionVerticalAllowance, playerSpotRadius,
                    maxSpotsPerPlayer, maxSpotsTotal, allowRaidDenDimensions, shinySpotChance,
                    announceShinyGlobally, enableEmptySpots, emptySpotChance, enableMultipleRewardRolls,
                    minRewardRolls, maxRewardRolls, allowMultiplePokemonPerSpot, mixedRewardSpotChance,
                    pokemonEnabled, pokemonEncounterChance, defaultPokemonShinyChance, minPokemonLevel,
                    maxPokemonLevel, typedSpawnRules, pokemonSpawnVolume, itemRewardVolume, familyRates);
        }
    }
}
