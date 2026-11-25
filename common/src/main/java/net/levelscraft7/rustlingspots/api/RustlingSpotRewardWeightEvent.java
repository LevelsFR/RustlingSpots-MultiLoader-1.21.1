package net.levelscraft7.rustlingspots.api;

import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Context for adjusting a Pokémon or item reward entry's weight for a specific player and spot. */
public record RustlingSpotRewardWeightEvent(
        ServerPlayer player,
        RustlingSpot spot,
        Type type,
        String rewardId,
        int originalWeight,
        int currentWeight
) {
    public RustlingSpotRewardWeightEvent {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(spot, "spot");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(rewardId, "rewardId");
        if (originalWeight <= 0 || currentWeight < 0) {
            throw new IllegalArgumentException("Reward weights must be non-negative and originalWeight positive");
        }
    }

    public enum Type {
        POKEMON,
        ITEM
    }
}
