package net.levelscraft7.rustlingspots.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesC2SPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesS2CPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotRemovePacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSpawnPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSyncPacket;
import net.levelscraft7.rustlingspots.spot.RustlingPlayerPreferences;
import net.levelscraft7.rustlingspots.spot.RustlingSpotSyncService;

public final class RustlingSpotsNetwork {
    private RustlingSpotsNetwork() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(RustlingSpotSpawnPacket.TYPE, RustlingSpotSpawnPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(RustlingSpotRemovePacket.TYPE, RustlingSpotRemovePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(RustlingSpotSyncPacket.TYPE, RustlingSpotSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(RustlingMessagePreferencesS2CPacket.TYPE, RustlingMessagePreferencesS2CPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RustlingMessagePreferencesC2SPacket.TYPE, RustlingMessagePreferencesC2SPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RustlingMessagePreferencesC2SPacket.TYPE, (payload, context) ->
                context.server().execute(() ->
                        RustlingPlayerPreferences.set(
                                context.player(),
                                payload.showPokemonMessages(),
                                payload.showLootMessages(),
                                payload.showEmptySpotMessages()
                        ))
        );
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                server.execute(() -> RustlingSpotSyncService.sendFullSync(handler.player))
        );
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                RustlingPlayerPreferences.clear(handler.player)
        );
    }
}
