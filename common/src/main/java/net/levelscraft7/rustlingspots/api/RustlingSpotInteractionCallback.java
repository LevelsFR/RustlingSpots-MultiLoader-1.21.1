package net.levelscraft7.rustlingspots.api;

/**
 * Server-side callback invoked after a rustling spot interaction is validated but before
 * Rustling Spots resolves its normal reward.
 *
 * <p>Callbacks run on the server thread. Return {@link RustlingSpotInteractionResult#PASS}
 * to preserve default behavior, {@link RustlingSpotInteractionResult#CONSUME_AS_EMPTY} to
 * consume the spot through Rustling Spots' existing empty-spot path, or
 * {@link RustlingSpotInteractionResult#HANDLED} to consume the spot without default reward
 * or empty feedback. Addons must not manually remove the spot.</p>
 *
 * <p>Listener failures are isolated and treated as {@code PASS} so broken addons cannot
 * block normal gameplay.</p>
 */
@FunctionalInterface
public interface RustlingSpotInteractionCallback {
    RustlingSpotInteractionResult onInteract(RustlingSpotInteractionEvent event);
}
