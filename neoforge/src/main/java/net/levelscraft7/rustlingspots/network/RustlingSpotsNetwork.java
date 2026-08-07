package net.levelscraft7.rustlingspots.neoforge.network;

import net.levelscraft7.rustlingspots.RustlingSpotsMod;
import net.levelscraft7.rustlingspots.client.RustlingSpotClientHandler;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesC2SPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesS2CPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotRemovePacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSpawnPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSyncPacket;
import net.levelscraft7.rustlingspots.spot.RustlingPlayerPreferences;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

/**
 * Network registration for rustling spot synchronization.
 */
public final class RustlingSpotsNetwork {
    private RustlingSpotsNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(RustlingSpotsMod.MOD_ID).optional().versioned("1").executesOn(HandlerThread.MAIN);
        registrar.playToClient(RustlingSpotSpawnPacket.TYPE, RustlingSpotSpawnPacket.STREAM_CODEC, (packet, context) ->
                context.enqueueWork(() -> RustlingSpotClientHandler.handleSpawn(packet))
        );
        registrar.playToClient(RustlingSpotRemovePacket.TYPE, RustlingSpotRemovePacket.STREAM_CODEC, (packet, context) ->
                context.enqueueWork(() -> RustlingSpotClientHandler.handleRemove(packet.id()))
        );
        registrar.playToClient(RustlingSpotSyncPacket.TYPE, RustlingSpotSyncPacket.STREAM_CODEC, (packet, context) ->
                context.enqueueWork(() -> RustlingSpotClientHandler.handleSync(packet))
        );
        registrar.playToClient(RustlingMessagePreferencesS2CPacket.TYPE, RustlingMessagePreferencesS2CPacket.STREAM_CODEC, (packet, context) ->
                context.enqueueWork(() -> net.levelscraft7.rustlingspots.RustlingSpotsNeoForgeClient.applyMessagePreferences(packet))
        );
        registrar.playToServer(RustlingMessagePreferencesC2SPacket.TYPE, RustlingMessagePreferencesC2SPacket.STREAM_CODEC, (packet, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        RustlingPlayerPreferences.set(
                                player,
                                packet.showPokemonMessages(),
                                packet.showLootMessages(),
                                packet.showEmptySpotMessages()
                        );
                    }
                })
        );
    }
}
