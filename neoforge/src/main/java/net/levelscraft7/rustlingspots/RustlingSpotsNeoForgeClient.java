package net.levelscraft7.rustlingspots;

import net.levelscraft7.rustlingspots.client.RustlingClientEvents;
import net.levelscraft7.rustlingspots.configscreen.RustlingConfigClientState;
import net.levelscraft7.rustlingspots.client.RustlingSpotClientHandler;
import net.levelscraft7.rustlingspots.config.RustlingSpotsClientConfig;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesC2SPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesS2CPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingConfigRequestC2SPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingConfigSnapshotS2CPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingConfigUpdateC2SPacket;
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
        IConfigScreenFactory configScreenFactory = (modContainer, parent) -> RustlingConfigClientState.createScreen(parent);
        container.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);
        RustlingConfigClientState.configure(
                () -> PacketDistributor.sendToServer(new RustlingConfigRequestC2SPacket()),
                config -> PacketDistributor.sendToServer(new RustlingConfigUpdateC2SPacket(config)),
                RustlingSpotsNeoForgeClient::syncRewardMessagePreference
        );
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

    public static void applyServerConfig(RustlingConfigSnapshotS2CPacket payload) {
        RustlingConfigClientState.receive(payload);
    }
}
