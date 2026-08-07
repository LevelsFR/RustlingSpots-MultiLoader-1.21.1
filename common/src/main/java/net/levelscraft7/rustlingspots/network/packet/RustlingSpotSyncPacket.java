package net.levelscraft7.rustlingspots.network.packet;

import net.levelscraft7.rustlingspots.common.RustlingSpotsCommon;
import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record RustlingSpotSyncPacket(List<RustlingSpotSpawnPacket> spots) implements CustomPacketPayload {
    public static final Type<RustlingSpotSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RustlingSpotsCommon.MOD_ID, "spot_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RustlingSpotSyncPacket> STREAM_CODEC =
            StreamCodec.of(RustlingSpotSyncPacket::encode, RustlingSpotSyncPacket::decode);

    public RustlingSpotSyncPacket {
        spots = spots == null ? List.of() : List.copyOf(spots);
    }

    public static RustlingSpotSyncPacket fromSpots(Iterable<RustlingSpot> spots) {
        List<RustlingSpotSpawnPacket> packets = new ArrayList<>();
        for (RustlingSpot spot : spots) {
            packets.add(new RustlingSpotSpawnPacket(spot));
        }
        return new RustlingSpotSyncPacket(packets);
    }

    public static void encode(RegistryFriendlyByteBuf buf, RustlingSpotSyncPacket packet) {
        buf.writeVarInt(packet.spots.size());
        for (RustlingSpotSpawnPacket spot : packet.spots) {
            RustlingSpotSpawnPacket.encode(buf, spot);
        }
    }

    public static RustlingSpotSyncPacket decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<RustlingSpotSpawnPacket> spots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            spots.add(RustlingSpotSpawnPacket.decode(buf));
        }
        return new RustlingSpotSyncPacket(spots);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
