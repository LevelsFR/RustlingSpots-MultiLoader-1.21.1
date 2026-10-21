package net.levelscraft7.rustlingspots.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RustlingConfigRequestC2SPacket() implements CustomPacketPayload {
    public static final Type<RustlingConfigRequestC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("rustlingspots", "config_request_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RustlingConfigRequestC2SPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> { }, buf -> new RustlingConfigRequestC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
