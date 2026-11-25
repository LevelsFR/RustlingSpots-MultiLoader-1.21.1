package net.levelscraft7.rustlingspots.api;

import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;

/** Immutable server-side notification for a spot lifecycle transition. */
public record RustlingSpotLifecycleEvent(
        Type type,
        ServerLevel level,
        RustlingSpot spot,
        Optional<ServerPlayer> player,
        RemovalReason removalReason
) {
    public RustlingSpotLifecycleEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(spot, "spot");
        player = player == null ? Optional.empty() : player;
    }

    public enum Type {
        CREATED,
        INTERACTED,
        EXPIRED,
        REMOVED
    }

    public enum RemovalReason {
        INTERACTION,
        EXPIRED,
        ADDON,
        CLEANUP
    }
}
