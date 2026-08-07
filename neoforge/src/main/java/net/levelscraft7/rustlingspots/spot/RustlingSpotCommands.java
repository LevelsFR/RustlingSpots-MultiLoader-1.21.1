package net.levelscraft7.rustlingspots.neoforge.spot;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * NeoForge command registration bridge.
 */
public final class RustlingSpotCommands {
    @SubscribeEvent
    public void register(RegisterCommandsEvent event) {
        net.levelscraft7.rustlingspots.spot.RustlingSpotCommands.register(event.getDispatcher());
    }
}
