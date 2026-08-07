package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsSoundConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves rewards when a player interacts with a rustling spot.
 */
public final class SpotRewardResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotRewardResolver.class);

    @FunctionalInterface
    public interface PokemonEncounterSpawner {
        Optional<String> spawn(ServerLevel level, ServerPlayer player, RustlingSpot spot, RandomSource random, boolean guaranteedShiny);
    }

    private static PokemonEncounterSpawner pokemonEncounterSpawner = PokemonSpawnResolver::spawn;

    private SpotRewardResolver() {
    }

    public static void setPokemonEncounterSpawner(PokemonEncounterSpawner spawner) {
        pokemonEncounterSpawner = spawner != null ? spawner : PokemonSpawnResolver::spawn;
    }

    public static void resolve(ServerLevel level, ServerPlayer player, RustlingSpot spot) {
        if (!RustlingSpotsServerConfig.GENERAL.enabled()) {
            return;
        }

        playInteractionParticles(level, spot);
        RustlingSpotStatsService.recordSpotConsumed(level, player.getUUID(), spot.isShiny());

        RandomSource random = level.random;
        boolean guaranteedShiny = spot.isShiny();
        RustlingPlayerPreferences.MessagePreferences messagePreferences = RustlingPlayerPreferences.get(player);
        if (shouldResolveEmptySpot(random, guaranteedShiny)) {
            resolveEmptySpot(player, random, messagePreferences);
            return;
        }

        int rewardRolls = resolveRewardRollCount(random);
        boolean allowMultiplePokemon = RustlingSpotsServerConfig.GENERAL.allowMultiplePokemonPerSpot();
        boolean allowMixedRewardTypes = rewardRolls > 1
                && random.nextDouble() < RustlingSpotsServerConfig.GENERAL.mixedRewardSpotChance();
        boolean pokemonSpawned = false;
        boolean lootAwarded = false;
        boolean shinyRewardResolved = false;
        String shinyPokemonName = null;
        List<String> spawnedPokemon = new ArrayList<>();
        List<ItemStack> lootRewards = new ArrayList<>();

        for (int roll = 0; roll < rewardRolls; roll++) {
            boolean guaranteedShinyRoll = guaranteedShiny && !shinyRewardResolved;
            boolean blockPokemonBecauseLootAlreadyWon = lootAwarded && !allowMixedRewardTypes;
            boolean canAttemptPokemon = allowMultiplePokemon || !pokemonSpawned;
            boolean attemptPokemon = !blockPokemonBecauseLootAlreadyWon && canAttemptPokemon && (guaranteedShinyRoll
                    || (RustlingSpotsPokemonConfig.POKEMON_SPAWN.enable()
                    && random.nextDouble() < RustlingSpotsPokemonConfig.POKEMON_SPAWN.encounterChance()));
            if (attemptPokemon) {
                Optional<String> spawned = pokemonEncounterSpawner.spawn(level, player, spot, random, guaranteedShinyRoll);
                if (spawned.isPresent()) {
                    pokemonSpawned = true;
                    RustlingSpotStatsService.recordPokemonReward(level, player.getUUID());
                    spawnedPokemon.add(spawned.get());
                    if (guaranteedShinyRoll) {
                        shinyRewardResolved = true;
                        shinyPokemonName = spawned.get();
                    }
                    continue;
                }
            }

            if (pokemonSpawned && !allowMixedRewardTypes) {
                continue;
            }

            ItemStack stack = giveLoot(level, spot, random);
            if (!stack.isEmpty()) {
                lootAwarded = true;
                if (attemptPokemon && RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    LOGGER.info("[Rustling Spots Pokemon Debug] Pokemon spawn failed for family={} at {}; fallbackLootUsed=true, loot={}",
                            spot.getFamily().serializedName(), spot.getPosition(), stack.getItem());
                }
                RustlingSpotStatsService.recordLootReward(level, player.getUUID());
                lootRewards.add(stack);
                if (guaranteedShinyRoll) {
                    shinyRewardResolved = true;
                }
            }
            if (attemptPokemon && stack.isEmpty() && RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                LOGGER.info("[Rustling Spots Pokemon Debug] Pokemon spawn failed for family={} at {}; fallbackLootUsed=false",
                        spot.getFamily().serializedName(), spot.getPosition());
            }
        }

        playRewardSound(level, spot, pokemonSpawned, lootAwarded);

        if (spawnedPokemon.size() == 1) {
            if (messagePreferences.showPokemonMessages()) {
                player.sendSystemMessage(RustlingMessages.randomPokemonMessage(spot, spawnedPokemon.get(0), random));
            }
        } else if (!spawnedPokemon.isEmpty()) {
            if (messagePreferences.showPokemonMessages()) {
                player.sendSystemMessage(RustlingMessages.randomMultiPokemonMessage(spot, spawnedPokemon, random));
            }
        }

        if (lootRewards.size() == 1) {
            if (messagePreferences.showLootMessages()) {
                player.sendSystemMessage(RustlingMessages.randomLootMessage(spot, lootRewards.get(0), random));
            }
        } else if (!lootRewards.isEmpty()) {
            if (messagePreferences.showLootMessages()) {
                player.sendSystemMessage(RustlingMessages.randomMultiLootMessage(spot, lootRewards, random));
            }
        }

        if (shinyRewardResolved) {
            announceShinySpotFind(level, player, spot, shinyPokemonName);
        }
    }

    private static void playRewardSound(ServerLevel level, RustlingSpot spot, boolean pokemonSpawned, boolean lootAwarded) {
        if (pokemonSpawned) {
            float volume = (float) RustlingSpotsSoundConfig.SOUNDS.pokemonSpawnVolume();
            if (volume > 0.0F) {
                level.playSound(null, spot.getPosition(), RustlingSoundResolver.pokemonSpawnSound(), SoundSource.PLAYERS, volume, 1.0F);
            }
        } else if (lootAwarded) {
            float volume = (float) RustlingSpotsSoundConfig.SOUNDS.itemRewardVolume();
            if (volume > 0.0F) {
                level.playSound(null, spot.getPosition(), RustlingSoundResolver.itemRewardSound(), SoundSource.PLAYERS, volume, 1.0F);
            }
        }
    }

    public static void resolveAsEmpty(ServerLevel level, ServerPlayer player, RustlingSpot spot) {
        if (!RustlingSpotsServerConfig.GENERAL.enabled()) {
            return;
        }

        playInteractionParticles(level, spot);
        RustlingSpotStatsService.recordSpotConsumed(level, player.getUUID(), spot.isShiny());
        resolveEmptySpot(player, level.random, RustlingPlayerPreferences.get(player));
    }

    private static void playInteractionParticles(ServerLevel level, RustlingSpot spot) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, spot.getPosition().getX() + 0.5, spot.getPosition().getY() + 1.0, spot.getPosition().getZ() + 0.5, 14, 0.3, 0.3, 0.3, 0.01);
    }

    private static void resolveEmptySpot(ServerPlayer player, RandomSource random, RustlingPlayerPreferences.MessagePreferences messagePreferences) {
        if (messagePreferences.showEmptySpotMessages()) {
            player.sendSystemMessage(RustlingMessages.randomEmptySpotMessage(random));
        }
    }

    private static ItemStack giveLoot(ServerLevel level, RustlingSpot spot, RandomSource random) {
        ItemStack stack = LootPoolService.pickLoot(spot.getLootFamily(), random);
        if (!stack.isEmpty()) {
            ItemStack copy = stack.copy();
            double x = spot.getPosition().getX() + 0.5;
            double y = spot.getPosition().getY() + 1.0;
            double z = spot.getPosition().getZ() + 0.5;
            ItemEntity entity = new ItemEntity(level, x, y, z, copy);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
            return copy;
        }
        return ItemStack.EMPTY;
    }

    private static void announceShinySpotFind(ServerLevel level, ServerPlayer player, RustlingSpot spot, String pokemonName) {
        if (!spot.isShiny() || !RustlingSpotsServerConfig.GENERAL.announceShinyFindsGlobally()) {
            return;
        }

        String familyName = spot.displayName();
        String rewardSuffix = pokemonName != null
                ? " and triggered a shiny " + pokemonName + " encounter"
                : "";
        level.getServer().getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        player.getName().getString() + " found a shiny " + familyName + " rustling spot" + rewardSuffix + "!"
                ),
                false
        );
    }

    private static boolean shouldResolveEmptySpot(RandomSource random, boolean guaranteedShiny) {
        if (guaranteedShiny || !RustlingSpotsServerConfig.GENERAL.enableEmptySpots()) {
            return false;
        }
        return random.nextDouble() < RustlingSpotsServerConfig.GENERAL.emptySpotChance();
    }

    private static int resolveRewardRollCount(RandomSource random) {
        int min = RustlingSpotsServerConfig.GENERAL.minRewardRolls();
        int max = RustlingSpotsServerConfig.GENERAL.maxRewardRolls();
        if (max <= min) {
            return min;
        }
        return random.nextInt(max - min + 1) + min;
    }
}
