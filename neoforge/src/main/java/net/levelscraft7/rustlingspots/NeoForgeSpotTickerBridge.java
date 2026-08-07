package net.levelscraft7.rustlingspots;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * NeoForge bridge for the shared spot ticker.
 */
public final class NeoForgeSpotTickerBridge {
    private final net.levelscraft7.rustlingspots.spot.SpotTicker delegate = new net.levelscraft7.rustlingspots.spot.SpotTicker();

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        delegate.onServerTick(event.getServer());
    }
}
