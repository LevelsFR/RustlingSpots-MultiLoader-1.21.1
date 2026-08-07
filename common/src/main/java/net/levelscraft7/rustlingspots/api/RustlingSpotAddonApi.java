package net.levelscraft7.rustlingspots.api;

import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Public addon entry point for server-side rustling spot interaction hooks.
 */
public final class RustlingSpotAddonApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpotAddonApi.class);
    private static final List<RustlingSpotInteractionCallback> INTERACTION_CALLBACKS = new CopyOnWriteArrayList<>();

    private RustlingSpotAddonApi() {
    }

    /**
     * Registers a server-thread callback invoked after a rustling spot interaction is validated
     * but before normal reward resolution.
     *
     * <p>Callbacks run in registration order. The first result other than
     * {@link RustlingSpotInteractionResult#PASS} wins. Addons must not manually remove the spot.
     * Listener failures are isolated, logged, and treated as {@code PASS}.</p>
     */
    public static void registerInteractionCallback(RustlingSpotInteractionCallback callback) {
        INTERACTION_CALLBACKS.add(Objects.requireNonNull(callback, "callback"));
    }

    public static RustlingSpotInteractionResult fireInteraction(ServerPlayer player, ServerLevel level, RustlingSpot spot) {
        if (INTERACTION_CALLBACKS.isEmpty()) {
            return RustlingSpotInteractionResult.PASS;
        }

        RustlingSpotInteractionEvent event = RustlingSpotInteractionEvent.of(player, level, spot);
        for (RustlingSpotInteractionCallback callback : INTERACTION_CALLBACKS) {
            RustlingSpotInteractionResult result;
            try {
                result = callback.onInteract(event);
            } catch (Exception exception) {
                LOGGER.error(
                        "Rustling spot interaction callback failed for player {} at {} in {} (spotId={})",
                        player.getGameProfile().getName(),
                        spot.getPosition(),
                        level.dimension().location(),
                        spot.getSpotId(),
                        exception
                );
                continue;
            }

            if (result != null && result != RustlingSpotInteractionResult.PASS) {
                return result;
            }
        }

        return RustlingSpotInteractionResult.PASS;
    }
}
