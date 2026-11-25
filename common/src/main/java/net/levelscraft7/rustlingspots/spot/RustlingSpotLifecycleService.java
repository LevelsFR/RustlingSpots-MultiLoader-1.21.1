package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.api.RustlingSpotAddonApi;
import net.levelscraft7.rustlingspots.api.RustlingSpotLifecycleEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/** Keeps spot registration, client synchronization and addon notifications in one path. */
public final class RustlingSpotLifecycleService {
    private RustlingSpotLifecycleService() {
    }

    public static boolean add(ServerLevel level, RustlingSpot spot) {
        if (level == null || spot == null || !level.getServer().isSameThread()
                || !level.dimension().equals(spot.getDimension())) {
            return false;
        }
        if (!RustlingSpotService.MANAGER.addIfAbsent(spot)) {
            return false;
        }
        RustlingSpotSyncService.broadcastSpawn(level, spot);
        RustlingSpotAddonApi.fireLifecycleEvent(new RustlingSpotLifecycleEvent(
                RustlingSpotLifecycleEvent.Type.CREATED, level, spot, Optional.empty(), null));
        return true;
    }

    public static boolean remove(ServerLevel level, RustlingSpot spot, RustlingSpotLifecycleEvent.RemovalReason reason,
                                 ServerPlayer player) {
        if (level == null || spot == null || reason == null || !level.getServer().isSameThread()
                || !level.dimension().equals(spot.getDimension()) || (player != null && player.serverLevel() != level)) {
            return false;
        }
        if (!RustlingSpotService.MANAGER.removeIfPresent(spot)) {
            return false;
        }
        RustlingSpotSyncService.broadcastRemove(level, spot);
        Optional<ServerPlayer> optionalPlayer = Optional.ofNullable(player);
        if (reason == RustlingSpotLifecycleEvent.RemovalReason.EXPIRED) {
            RustlingSpotAddonApi.fireLifecycleEvent(new RustlingSpotLifecycleEvent(
                    RustlingSpotLifecycleEvent.Type.EXPIRED, level, spot, optionalPlayer, reason));
        }
        RustlingSpotAddonApi.fireLifecycleEvent(new RustlingSpotLifecycleEvent(
                RustlingSpotLifecycleEvent.Type.REMOVED, level, spot, optionalPlayer, reason));
        return true;
    }
}
