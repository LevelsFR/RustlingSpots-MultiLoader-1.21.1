package net.levelscraft7.rustlingspots.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSpawnPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSyncPacket;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.levelscraft7.rustlingspots.spot.WeightedParticleReference;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
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
        ACTIVE.put(packet.id(), new ClientSpot(packet.id(), packet.pos(), packet.family(), packet.shiny(), packet.particles()));
    }

    public static void handleSync(RustlingSpotSyncPacket packet) {
        ACTIVE.clear();
        for (RustlingSpotSpawnPacket spot : packet.spots()) {
            handleSpawn(spot);
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
        ACTIVE.values().forEach(spot -> spot.tick(time));
    }

    static final class ClientSpot {
        private final UUID id;
        private final BlockPos pos;
        private final RustlingSpotFamily family;
        private final boolean shiny;
        private final List<WeightedParticleReference> particles;

        private ClientSpot(UUID id, BlockPos pos, RustlingSpotFamily family, boolean shiny, List<WeightedParticleReference> particles) {
            this.id = id;
            this.pos = pos;
            this.family = family;
            this.shiny = shiny;
            this.particles = particles == null ? List.of() : List.copyOf(particles);
        }

        BlockPos getPos() {
            return pos;
        }

        RustlingSpotFamily getFamily() {
            return family;
        }

        private void tick(long gameTime) {
            if (gameTime % 10 == 0) {
                RustlingParticles.spawn(family, pos, shiny, particles);
            }
        }
    }
}
