package net.levelscraft7.rustlingspots.network.packet;

import net.levelscraft7.rustlingspots.config.RustlingServerConfigSnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RustlingConfigUpdateC2SPacket(RustlingServerConfigSnapshot config) implements CustomPacketPayload {
    public static final Type<RustlingConfigUpdateC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("rustlingspots", "config_update_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RustlingConfigUpdateC2SPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> packet.config.encode(buf),
                    buf -> new RustlingConfigUpdateC2SPacket(RustlingServerConfigSnapshot.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
