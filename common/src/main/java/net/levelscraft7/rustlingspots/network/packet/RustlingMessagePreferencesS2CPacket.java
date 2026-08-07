package net.levelscraft7.rustlingspots.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RustlingMessagePreferencesS2CPacket(
        boolean showPokemonMessages,
        boolean showLootMessages,
        boolean showEmptySpotMessages
) implements CustomPacketPayload {
    public static final Type<RustlingMessagePreferencesS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("rustlingspots", "message_preferences_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RustlingMessagePreferencesS2CPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> encode(buf, packet), RustlingMessagePreferencesS2CPacket::decode);

    public static void encode(RegistryFriendlyByteBuf buf, RustlingMessagePreferencesS2CPacket packet) {
        buf.writeBoolean(packet.showPokemonMessages);
        buf.writeBoolean(packet.showLootMessages);
        buf.writeBoolean(packet.showEmptySpotMessages);
    }

    public static RustlingMessagePreferencesS2CPacket decode(RegistryFriendlyByteBuf buf) {
        return new RustlingMessagePreferencesS2CPacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
