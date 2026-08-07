package net.levelscraft7.rustlingspots;

import net.levelscraft7.rustlingspots.client.RustlingClientEvents;
import net.levelscraft7.rustlingspots.client.RustlingSpotsConfigScreen;
import net.levelscraft7.rustlingspots.client.RustlingSpotClientHandler;
import net.levelscraft7.rustlingspots.config.RustlingSpotsClientConfig;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesC2SPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesS2CPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RustlingSpotsNeoForgeClient {
    private RustlingSpotsNeoForgeClient() {
    }

    static void init(ModContainer container, IEventBus modBus) {
        IConfigScreenFactory configScreenFactory = (modContainer, parent) -> new RustlingSpotsConfigScreen(parent);
        container.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);
        RustlingClientEvents.register(modBus);
        modBus.addListener(RustlingSpotsNeoForgeClient::onClientSetup);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(RustlingSpotsNeoForgeClient::onPlayerLogin);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        RustlingSpotClientHandler.bootstrap();
    }

    private static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        syncRewardMessagePreference();
    }

    public static void syncRewardMessagePreference() {
        PacketDistributor.sendToServer(new RustlingMessagePreferencesC2SPacket(
                RustlingSpotsClientConfig.VISUALS.showPokemonMessages(),
                RustlingSpotsClientConfig.VISUALS.showLootMessages(),
                RustlingSpotsClientConfig.VISUALS.showEmptySpotMessages()
        ));
    }

    public static void applyMessagePreferences(RustlingMessagePreferencesS2CPacket payload) {
        RustlingSpotsClientConfig.VISUALS.show_pokemon_messages = payload.showPokemonMessages();
        RustlingSpotsClientConfig.VISUALS.show_loot_messages = payload.showLootMessages();
        RustlingSpotsClientConfig.VISUALS.show_empty_spot_messages = payload.showEmptySpotMessages();
        RustlingSpotsClientConfig.save();
    }
}
