package net.levelscraft7.rustlingspots.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSpawnPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSyncPacket;
import net.levelscraft7.rustlingspots.spot.RustlingSoundResolver;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.levelscraft7.rustlingspots.spot.WeightedParticleReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RustlingSpotClientHandler {
    private static final Map<UUID, ClientSpot> ACTIVE = new HashMap<>();
    private static ResourceKey<Level> currentDimension;

    private RustlingSpotClientHandler() {
    }

    public static void bootstrap() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTick());
        WorldRenderEvents.AFTER_TRANSLUCENT.register(RustlingSpotRenderer::render);
    }

    public static Iterable<ClientSpot> activeSpots() {
        return ACTIVE.values();
    }

    public static void handleSpawn(RustlingSpotSpawnPacket packet) {
        ACTIVE.put(packet.id(), new ClientSpot(packet, true));
    }

    public static void handleSync(RustlingSpotSyncPacket packet) {
        ACTIVE.clear();
        for (RustlingSpotSpawnPacket spot : packet.spots()) {
            ACTIVE.put(spot.id(), new ClientSpot(spot, false));
        }
    }

    public static void handleRemove(UUID id) {
        ACTIVE.remove(id);
    }

    private static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        ResourceKey<Level> dimension = mc.level != null ? mc.level.dimension() : null;

        if (mc.level == null || !Objects.equals(currentDimension, dimension)) {
            ACTIVE.clear();
            currentDimension = dimension;
            return;
        }

        long time = mc.level.getGameTime();
        ACTIVE.values().forEach(spot -> spot.tick(time, mc.player));
    }

    static final class ClientSpot {
        private final UUID id;
        private final BlockPos pos;
        private final RustlingSpotFamily family;
        private final int ambientVariant;
        private final SoundEvent ambientSound;
        private final boolean shiny;
        private final List<WeightedParticleReference> particles;
        private boolean approachCuePlayed;

        private ClientSpot(RustlingSpotSpawnPacket packet, boolean playApproachCue) {
            this.id = packet.id();
            this.pos = packet.pos();
            this.family = packet.family();
            this.ambientVariant = packet.ambientVariant();
            this.ambientSound = packet.ambientSoundId() == null
                    ? null : BuiltInRegistries.SOUND_EVENT.getOptional(packet.ambientSoundId()).orElse(null);
            this.shiny = packet.shiny();
            this.particles = packet.particles() == null ? List.of() : List.copyOf(packet.particles());
            this.approachCuePlayed = !playApproachCue;
        }

        BlockPos getPos() {
            return pos;
        }

        RustlingSpotFamily getFamily() {
            return family;
        }

        private void tick(long gameTime, LocalPlayer player) {
            if (gameTime % 10 == 0) {
                RustlingParticles.spawn(family, pos, shiny, particles);
            }
            if (!approachCuePlayed && player != null
                    && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    float pitch = 0.92F + mc.level.random.nextFloat() * 0.16F;
                    mc.level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                            ambientSound != null ? ambientSound : RustlingSoundResolver.ambientVariant(ambientVariant),
                            SoundSource.AMBIENT, 0.22F, pitch, false);
                    approachCuePlayed = true;
                }
            }
        }
    }
}
