package net.levelscraft7.rustlingspots.network.packet;

import net.levelscraft7.rustlingspots.common.RustlingSpotsCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RustlingSpotRemovePacket(UUID id) implements CustomPacketPayload {
    public static final Type<RustlingSpotRemovePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RustlingSpotsCommon.MOD_ID, "spot_remove"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RustlingSpotRemovePacket> STREAM_CODEC = StreamCodec.of((buf, packet) -> encode(buf, packet), RustlingSpotRemovePacket::decode);

    public static void encode(RegistryFriendlyByteBuf buf, RustlingSpotRemovePacket packet) {
        buf.writeUUID(packet.id);
    }

    public static RustlingSpotRemovePacket decode(RegistryFriendlyByteBuf buf) {
        return new RustlingSpotRemovePacket(buf.readUUID());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
