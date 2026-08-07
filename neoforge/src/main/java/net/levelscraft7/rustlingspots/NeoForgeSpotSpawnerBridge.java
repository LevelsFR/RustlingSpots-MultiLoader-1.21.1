package net.levelscraft7.rustlingspots;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.UUID;

/**
 * NeoForge bridge for the shared spot spawner.
 */
public final class NeoForgeSpotSpawnerBridge {
    private final net.levelscraft7.rustlingspots.spot.SpotSpawner delegate = new net.levelscraft7.rustlingspots.spot.SpotSpawner();

    public void forgetPlayer(UUID playerId) {
        delegate.forgetPlayer(playerId);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        delegate.onServerTick(event.getServer());
    }
}
