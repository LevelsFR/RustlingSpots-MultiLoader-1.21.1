package net.levelscraft7.rustlingspots.network.packet;

import net.levelscraft7.rustlingspots.common.RustlingSpotsCommon;
import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.levelscraft7.rustlingspots.spot.WeightedParticleReference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record RustlingSpotSpawnPacket(
        UUID id,
        BlockPos pos,
        RustlingSpotFamily family,
        int ambientVariant,
        boolean shiny,
        List<WeightedParticleReference> particles,
        ResourceLocation ambientSoundId
) implements CustomPacketPayload {
    public static final Type<RustlingSpotSpawnPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RustlingSpotsCommon.MOD_ID, "spot_spawn"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RustlingSpotSpawnPacket> STREAM_CODEC = StreamCodec.of((buf, packet) -> encode(buf, packet), RustlingSpotSpawnPacket::decode);

    public RustlingSpotSpawnPacket(RustlingSpot spot) {
        this(spot.getId(), spot.getPosition(), spot.getFamily(), spot.getAmbientVariant(), spot.isShiny(),
                spot.getParticles(), spot.getAmbientSoundId().orElse(null));
    }

    public RustlingSpotSpawnPacket(UUID id, BlockPos pos, RustlingSpotFamily family, int ambientVariant,
                                   boolean shiny, List<WeightedParticleReference> particles) {
        this(id, pos, family, ambientVariant, shiny, particles, null);
    }

    public static void encode(RegistryFriendlyByteBuf buf, RustlingSpotSpawnPacket packet) {
        buf.writeUUID(packet.id);
        buf.writeBlockPos(packet.pos);
        buf.writeEnum(packet.family);
        buf.writeVarInt(packet.ambientVariant);
        buf.writeBoolean(packet.shiny);
        buf.writeBoolean(packet.ambientSoundId != null);
        if (packet.ambientSoundId != null) {
            buf.writeResourceLocation(packet.ambientSoundId);
        }
        buf.writeVarInt(packet.particles.size());
        for (WeightedParticleReference particle : packet.particles) {
            buf.writeResourceLocation(particle.particleId());
            buf.writeVarInt(particle.weight());
        }
    }

    public static RustlingSpotSpawnPacket decode(RegistryFriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        BlockPos pos = buf.readBlockPos();
        RustlingSpotFamily family = buf.readEnum(RustlingSpotFamily.class);
        int variant = buf.readVarInt();
        boolean shiny = buf.readBoolean();
        ResourceLocation ambientSoundId = buf.readBoolean() ? buf.readResourceLocation() : null;
        int particleCount = buf.readVarInt();
        List<WeightedParticleReference> particles = new ArrayList<>(particleCount);
        for (int i = 0; i < particleCount; i++) {
            particles.add(new WeightedParticleReference(buf.readResourceLocation(), buf.readVarInt()));
        }
        return new RustlingSpotSpawnPacket(id, pos, family, variant, shiny, particles, ambientSoundId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
