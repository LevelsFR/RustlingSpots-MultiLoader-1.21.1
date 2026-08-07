package net.levelscraft7.rustlingspots.spot;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-player runtime preferences sent from the client.
 */
public final class RustlingPlayerPreferences {
    private static final Map<UUID, MessagePreferences> MESSAGE_PREFERENCES = new ConcurrentHashMap<>();

    private RustlingPlayerPreferences() {
    }

    public static MessagePreferences get(ServerPlayer player) {
        return MESSAGE_PREFERENCES.getOrDefault(player.getUUID(), MessagePreferences.defaults());
    }

    public static void set(ServerPlayer player, boolean showPokemonMessages, boolean showLootMessages, boolean showEmptySpotMessages) {
        MESSAGE_PREFERENCES.put(player.getUUID(), new MessagePreferences(showPokemonMessages, showLootMessages, showEmptySpotMessages));
    }

    public static void clear(ServerPlayer player) {
        MESSAGE_PREFERENCES.remove(player.getUUID());
    }

    public record MessagePreferences(boolean showPokemonMessages, boolean showLootMessages, boolean showEmptySpotMessages) {
        public static MessagePreferences defaults() {
            return new MessagePreferences(true, false, true);
        }
    }
}
