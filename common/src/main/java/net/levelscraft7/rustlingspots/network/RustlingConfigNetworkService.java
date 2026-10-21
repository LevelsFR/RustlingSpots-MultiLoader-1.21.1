package net.levelscraft7.rustlingspots.network;

import net.levelscraft7.rustlingspots.config.RustlingServerConfigSnapshot;
import net.levelscraft7.rustlingspots.network.packet.RustlingConfigSnapshotS2CPacket;
import net.levelscraft7.rustlingspots.spot.RustlingSpotsReloadService;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RustlingConfigNetworkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingConfigNetworkService.class);

    private RustlingConfigNetworkService() {
    }

    public static void sendSnapshot(ServerPlayer player) {
        sendSnapshot(player, "");
    }

    public static void applyUpdate(ServerPlayer player, RustlingServerConfigSnapshot config) {
        if (!canEdit(player)) {
            LOGGER.warn("[Rustling Spots] {} tried to change server configuration without permission", player.getGameProfile().getName());
            sendSnapshot(player, "config.rustlingspots.status.permission_denied");
            return;
        }

        var error = config.validationError();
        if (error.isPresent()) {
            LOGGER.warn("[Rustling Spots] Rejected invalid in-game configuration from {}: {}", player.getGameProfile().getName(), error.get());
            sendSnapshot(player, "config.rustlingspots.status.invalid");
            return;
        }

        config.applyAndSave();
        RustlingSpotsReloadService.reloadAll();
        LOGGER.info("[Rustling Spots] Server configuration updated in game by {}", player.getGameProfile().getName());
        for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
            sendSnapshot(target, target == player ? "config.rustlingspots.status.saved" : "");
        }
    }

    private static void sendSnapshot(ServerPlayer player, String status) {
        player.connection.send(new ClientboundCustomPayloadPacket(new RustlingConfigSnapshotS2CPacket(
                RustlingServerConfigSnapshot.current(),
                canEdit(player),
                status
        )));
    }

    private static boolean canEdit(ServerPlayer player) {
        return player.hasPermissions(2)
                || player.server.isSingleplayerOwner(player.getGameProfile());
    }
}
