package net.levelscraft7.rustlingspots.configscreen;

import net.levelscraft7.rustlingspots.config.RustlingServerConfigSnapshot;
import net.levelscraft7.rustlingspots.config.RustlingSpotsClientConfig;
import net.levelscraft7.rustlingspots.network.packet.RustlingConfigSnapshotS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Small loader bridge used by the shared Cloth Config screen.
 */
public final class RustlingConfigClientState {
    private static Runnable requestSender = () -> { };
    private static Consumer<RustlingServerConfigSnapshot> updateSender = config -> { };
    private static Runnable preferenceSync = () -> { };
    private static RustlingServerConfigSnapshot serverConfig = RustlingServerConfigSnapshot.current();
    private static boolean serverConfigEditable;
    private static Screen pendingParent;

    private RustlingConfigClientState() {
    }

    public static void configure(
            Runnable requestSender,
            Consumer<RustlingServerConfigSnapshot> updateSender,
            Runnable preferenceSync
    ) {
        RustlingConfigClientState.requestSender = requestSender;
        RustlingConfigClientState.updateSender = updateSender;
        RustlingConfigClientState.preferenceSync = preferenceSync;
    }

    public static void requestServerConfig() {
        requestSender.run();
    }

    public static void receive(RustlingConfigSnapshotS2CPacket packet) {
        serverConfig = packet.config();
        serverConfigEditable = packet.editable();
        Minecraft minecraft = Minecraft.getInstance();
        if (pendingParent != null) {
            Screen parent = pendingParent;
            pendingParent = null;
            if (minecraft.screen instanceof LoadingScreen) {
                minecraft.setScreen(RustlingSpotsClothConfigScreen.create(parent, serverConfig, serverConfigEditable, true));
            }
        }
        if (!packet.status().isBlank()) {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.translatable(packet.status()), false);
            }
        }
    }

    public static Screen createScreen(Screen parent) {
        boolean connected = Minecraft.getInstance().getConnection() != null;
        if (connected) {
            return new LoadingScreen(parent);
        }
        serverConfig = RustlingServerConfigSnapshot.current();
        serverConfigEditable = true;
        return RustlingSpotsClothConfigScreen.create(parent, serverConfig, true, false);
    }

    static void saveClientConfig() {
        RustlingSpotsClientConfig.save();
        preferenceSync.run();
    }

    static void saveServerConfig(RustlingServerConfigSnapshot config, boolean connected) {
        serverConfig = config;
        if (connected) {
            updateSender.accept(config);
        } else {
            config.applyAndSave();
        }
    }

    private static final class LoadingScreen extends Screen {
        private final Screen parent;

        private LoadingScreen(Screen parent) {
            super(Component.translatable("config.rustlingspots.title"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            pendingParent = parent;
            requestServerConfig();
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
            graphics.drawCenteredString(font, Component.translatable("config.rustlingspots.loading"),
                    width / 2, height / 2 - 5, 0xFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            pendingParent = null;
            Minecraft.getInstance().setScreen(parent);
        }
    }
}
