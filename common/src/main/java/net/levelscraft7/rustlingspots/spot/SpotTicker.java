package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.api.RustlingSpotAddonApi;
import net.levelscraft7.rustlingspots.api.RustlingSpotLifecycleEvent;
import net.levelscraft7.rustlingspots.api.RustlingSpotInteractionResult;
import net.levelscraft7.rustlingspots.compat.CobblemonRaidDensCompat;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Ticks active spots, handling despawn and interaction.
 */
public class SpotTicker {
    public void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            tickLevel(level);
        }
    }

    private void tickLevel(ServerLevel level) {
        if (!RustlingSpotsServerConfig.GENERAL.enabled()) {
            List<RustlingSpot> toRemove = new ArrayList<>(RustlingSpotService.MANAGER.getAll(level.dimension()));
            toRemove.forEach(spot -> removeSpot(level, spot, RustlingSpotLifecycleEvent.RemovalReason.CLEANUP, null));
            return;
        }

        if (!CobblemonRaidDensCompat.shouldAllowSpots(level)) {
            List<RustlingSpot> toRemove = new ArrayList<>(RustlingSpotService.MANAGER.getAll(level.dimension()));
            toRemove.forEach(spot -> removeSpot(level, spot, RustlingSpotLifecycleEvent.RemovalReason.CLEANUP, null));
            return;
        }

        List<PendingRemoval> pendingRemoval = new ArrayList<>();
        for (RustlingSpot spot : RustlingSpotService.MANAGER.getAll(level.dimension())) {
            RustlingSpotLifecycleEvent.RemovalReason despawnReason = despawnReason(level, spot);
            if (despawnReason != null) {
                pendingRemoval.add(new PendingRemoval(spot, despawnReason, null));
                continue;
            }

            ServerPlayer player = findInteractingPlayer(level, spot);
            if (player != null) {
                RustlingSpotInteractionResult result = RustlingSpotAddonApi.fireInteraction(player, level, spot);
                if (result == RustlingSpotInteractionResult.CONSUME_AS_EMPTY) {
                    SpotRewardResolver.resolveAsEmpty(level, player, spot);
                } else if (result == RustlingSpotInteractionResult.PASS) {
                    SpotRewardResolver.resolve(level, player, spot);
                }
                pendingRemoval.add(new PendingRemoval(spot,
                        RustlingSpotLifecycleEvent.RemovalReason.INTERACTION, player));
            }
        }

        pendingRemoval.forEach(removal -> removeSpot(level, removal.spot(), removal.reason(), removal.player()));
    }

    private RustlingSpotLifecycleEvent.RemovalReason despawnReason(ServerLevel level, RustlingSpot spot) {
        if (!level.hasChunk(spot.getPosition().getX() >> 4, spot.getPosition().getZ() >> 4)) {
            return RustlingSpotLifecycleEvent.RemovalReason.CLEANUP;
        }

        long age = level.getGameTime() - spot.getCreatedTick();
        int lifetimeTicks = spot.getLifetimeTicks() > 0
                ? spot.getLifetimeTicks() : RustlingSpotsServerConfig.GENERAL.spotLifetimeTicks();
        if (age > lifetimeTicks) {
            return RustlingSpotLifecycleEvent.RemovalReason.EXPIRED;
        }

        double maxDistance = RustlingSpotsServerConfig.GENERAL.playerSpotRadius();
        double maxDistanceSq = maxDistance * maxDistance;
        boolean noNearbyPlayer = level.players().stream()
                .filter(p -> !p.isSpectator())
                .noneMatch(p -> p.distanceToSqr(
                        spot.getPosition().getX() + 0.5,
                        spot.getPosition().getY() + 0.5,
                        spot.getPosition().getZ() + 0.5
                ) <= maxDistanceSq);
        return noNearbyPlayer ? RustlingSpotLifecycleEvent.RemovalReason.CLEANUP : null;
    }

    private ServerPlayer findInteractingPlayer(ServerLevel level, RustlingSpot spot) {
        Vec3 lowerCorner = Vec3.atLowerCornerOf(spot.getPosition());
        double interactionRadius = RustlingSpotsServerConfig.GENERAL.interactionRadius();
        double interactionVerticalAllowance = RustlingSpotsServerConfig.GENERAL.interactionVerticalAllowance();
        double interactionRadiusSq = interactionRadius * interactionRadius;
        AABB box = AABB.unitCubeFromLowerCorner(lowerCorner)
                .inflate(interactionRadius)
                .expandTowards(0.0D, interactionVerticalAllowance, 0.0D);
        Vec3 spotCenter = lowerCorner.add(0.5D, 0.5D, 0.5D);
        for (Player player : level.getEntitiesOfClass(Player.class, box)) {
            if (RustlingSpotService.INTERACTION_BLOCKER.isBlocked(player.getUUID(), level.getGameTime())) {
                continue;
            }
            double dx = player.getX() - spotCenter.x;
            double dz = player.getZ() - spotCenter.z;
            double dy = player.getY() - spotCenter.y;
            if (dy >= -interactionRadius && dy <= interactionRadius + interactionVerticalAllowance && (dx * dx + dz * dz) <= interactionRadiusSq) {
                return (ServerPlayer) player;
            }
        }
        return null;
    }

    private void removeSpot(ServerLevel level, RustlingSpot spot,
                            RustlingSpotLifecycleEvent.RemovalReason reason, ServerPlayer player) {
        RustlingSpotLifecycleService.remove(level, spot, reason, player);
    }

    private record PendingRemoval(RustlingSpot spot, RustlingSpotLifecycleEvent.RemovalReason reason,
                                  ServerPlayer player) {
    }
}
