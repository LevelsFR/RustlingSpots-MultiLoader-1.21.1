package net.levelscraft7.rustlingspots.spot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent per-player rustling spot stats stored in world data.
 */
public final class RustlingSpotStatsService {
    private static final String DATA_NAME = "rustlingspots_player_stats";

    private RustlingSpotStatsService() {
    }

    public static PlayerStats get(MinecraftServer server, UUID playerId) {
        return getData(server).get(playerId);
    }

    public static void recordSpotConsumed(ServerLevel level, UUID playerId, boolean shinySpot) {
        StatsSavedData data = getData(level.getServer());
        data.recordSpotConsumed(playerId, shinySpot);
        data.setDirty();
    }

    public static void recordLootReward(ServerLevel level, UUID playerId) {
        StatsSavedData data = getData(level.getServer());
        data.recordLootReward(playerId);
        data.setDirty();
    }

    public static void recordPokemonReward(ServerLevel level, UUID playerId) {
        StatsSavedData data = getData(level.getServer());
        data.recordPokemonReward(playerId);
        data.setDirty();
    }

    private static StatsSavedData getData(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            throw new IllegalStateException("Rustling spot stats require an overworld data storage.");
        }
        Supplier<StatsSavedData> constructor = StatsSavedData::new;
        BiFunction<CompoundTag, net.minecraft.core.HolderLookup.Provider, StatsSavedData> deserializer = StatsSavedData::load;
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(constructor, deserializer, DataFixTypes.LEVEL),
                DATA_NAME
        );
    }

    public record PlayerStats(int totalSpotsFound, int shinySpotsFound, int pokemonRewards, int lootRewards) {
        public static final PlayerStats EMPTY = new PlayerStats(0, 0, 0, 0);
    }

    private static final class MutablePlayerStats {
        private int totalSpotsFound;
        private int shinySpotsFound;
        private int pokemonRewards;
        private int lootRewards;

        private PlayerStats snapshot() {
            return new PlayerStats(totalSpotsFound, shinySpotsFound, pokemonRewards, lootRewards);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("total_spots_found", totalSpotsFound);
            tag.putInt("shiny_spots_found", shinySpotsFound);
            tag.putInt("pokemon_rewards", pokemonRewards);
            tag.putInt("loot_rewards", lootRewards);
            return tag;
        }

        private static MutablePlayerStats load(CompoundTag tag) {
            MutablePlayerStats stats = new MutablePlayerStats();
            stats.totalSpotsFound = tag.getInt("total_spots_found");
            stats.shinySpotsFound = tag.getInt("shiny_spots_found");
            stats.pokemonRewards = tag.getInt("pokemon_rewards");
            stats.lootRewards = tag.getInt("loot_rewards");
            return stats;
        }
    }

    private static final class StatsSavedData extends SavedData {
        private final Map<UUID, MutablePlayerStats> statsByPlayer = new HashMap<>();

        private PlayerStats get(UUID playerId) {
            MutablePlayerStats stats = statsByPlayer.get(playerId);
            return stats != null ? stats.snapshot() : PlayerStats.EMPTY;
        }

        private void recordSpotConsumed(UUID playerId, boolean shinySpot) {
            MutablePlayerStats stats = statsByPlayer.computeIfAbsent(playerId, ignored -> new MutablePlayerStats());
            stats.totalSpotsFound++;
            if (shinySpot) {
                stats.shinySpotsFound++;
            }
        }

        private void recordLootReward(UUID playerId) {
            MutablePlayerStats stats = statsByPlayer.computeIfAbsent(playerId, ignored -> new MutablePlayerStats());
            stats.lootRewards++;
        }

        private void recordPokemonReward(UUID playerId) {
            MutablePlayerStats stats = statsByPlayer.computeIfAbsent(playerId, ignored -> new MutablePlayerStats());
            stats.pokemonRewards++;
        }

        @Override
        public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            CompoundTag playersTag = new CompoundTag();
            for (Map.Entry<UUID, MutablePlayerStats> entry : statsByPlayer.entrySet()) {
                playersTag.put(entry.getKey().toString(), entry.getValue().save());
            }
            tag.put("players", playersTag);
            return tag;
        }

        private static StatsSavedData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            StatsSavedData data = new StatsSavedData();
            CompoundTag playersTag = tag.getCompound("players");
            for (String key : playersTag.getAllKeys()) {
                try {
                    data.statsByPlayer.put(UUID.fromString(key), MutablePlayerStats.load(playersTag.getCompound(key)));
                } catch (IllegalArgumentException ignored) {
                    // Skip malformed entries to avoid breaking world loading.
                }
            }
            return data;
        }
    }
}
