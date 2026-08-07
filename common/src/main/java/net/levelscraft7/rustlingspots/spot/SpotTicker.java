package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.api.RustlingSpotAddonApi;
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
            toRemove.forEach(spot -> removeSpot(level, spot));
            return;
        }

        if (!CobblemonRaidDensCompat.shouldAllowSpots(level)) {
            List<RustlingSpot> toRemove = new ArrayList<>(RustlingSpotService.MANAGER.getAll(level.dimension()));
            toRemove.forEach(spot -> removeSpot(level, spot));
            return;
        }

        List<RustlingSpot> pendingRemoval = new ArrayList<>();
        for (RustlingSpot spot : RustlingSpotService.MANAGER.getAll(level.dimension())) {
            if (shouldDespawn(level, spot)) {
                pendingRemoval.add(spot);
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
                pendingRemoval.add(spot);
            }
        }

        pendingRemoval.forEach(spot -> removeSpot(level, spot));
    }

    private boolean shouldDespawn(ServerLevel level, RustlingSpot spot) {
        if (!level.hasChunk(spot.getPosition().getX() >> 4, spot.getPosition().getZ() >> 4)) {
            return true;
        }

        long age = level.getGameTime() - spot.getCreatedTick();
        if (age > RustlingSpotsServerConfig.GENERAL.spotLifetimeTicks()) {
            return true;
        }

        double maxDistance = RustlingSpotsServerConfig.GENERAL.playerSpotRadius();
        double maxDistanceSq = maxDistance * maxDistance;
        return level.players().stream()
                .filter(p -> !p.isSpectator())
                .noneMatch(p -> p.distanceToSqr(
                        spot.getPosition().getX() + 0.5,
                        spot.getPosition().getY() + 0.5,
                        spot.getPosition().getZ() + 0.5
                ) <= maxDistanceSq);
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

    private void removeSpot(ServerLevel level, RustlingSpot spot) {
        RustlingSpotService.MANAGER.remove(spot);
        RustlingSpotSyncService.broadcastRemove(level, spot);
    }
}
