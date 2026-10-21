package net.levelscraft7.rustlingspots.network.packet;

import net.levelscraft7.rustlingspots.config.RustlingServerConfigSnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RustlingConfigSnapshotS2CPacket(
        RustlingServerConfigSnapshot config,
        boolean editable,
        String status
) implements CustomPacketPayload {
    public static final Type<RustlingConfigSnapshotS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("rustlingspots", "config_snapshot_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RustlingConfigSnapshotS2CPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                packet.config.encode(buf);
                buf.writeBoolean(packet.editable);
                buf.writeUtf(packet.status, 256);
            }, buf -> new RustlingConfigSnapshotS2CPacket(
                    RustlingServerConfigSnapshot.decode(buf),
                    buf.readBoolean(),
                    buf.readUtf(256)
            ));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
