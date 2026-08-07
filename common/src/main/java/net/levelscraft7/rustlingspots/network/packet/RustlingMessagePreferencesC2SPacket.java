package net.levelscraft7.rustlingspots.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RustlingMessagePreferencesC2SPacket(
        boolean showPokemonMessages,
        boolean showLootMessages,
        boolean showEmptySpotMessages
) implements CustomPacketPayload {
    public static final Type<RustlingMessagePreferencesC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("rustlingspots", "message_preferences_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RustlingMessagePreferencesC2SPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> encode(buf, packet), RustlingMessagePreferencesC2SPacket::decode);

    public static void encode(RegistryFriendlyByteBuf buf, RustlingMessagePreferencesC2SPacket packet) {
        buf.writeBoolean(packet.showPokemonMessages);
        buf.writeBoolean(packet.showLootMessages);
        buf.writeBoolean(packet.showEmptySpotMessages);
    }

    public static RustlingMessagePreferencesC2SPacket decode(RegistryFriendlyByteBuf buf) {
        return new RustlingMessagePreferencesC2SPacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
