package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.network.packet.RustlingSpotRemovePacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSpawnPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSyncPacket;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class RustlingSpotSyncService {
    private RustlingSpotSyncService() {
    }

    public static void sendFullSync(ServerPlayer player) {
        RustlingSpotSyncPacket packet = RustlingSpotSyncPacket.fromSpots(
                RustlingSpotService.MANAGER.getAll(player.serverLevel().dimension())
        );
        player.connection.send(new ClientboundCustomPayloadPacket(packet));
    }

    public static void broadcastSpawn(ServerLevel level, RustlingSpot spot) {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(new RustlingSpotSpawnPacket(spot));
        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    public static void broadcastRemove(ServerLevel level, RustlingSpot spot) {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(new RustlingSpotRemovePacket(spot.getId()));
        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }
}
