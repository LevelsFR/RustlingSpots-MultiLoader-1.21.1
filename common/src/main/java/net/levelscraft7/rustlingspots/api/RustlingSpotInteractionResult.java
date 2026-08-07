package net.levelscraft7.rustlingspots.api;

/**
 * Outcome returned by a rustling spot interaction callback.
 */
public enum RustlingSpotInteractionResult {
    /**
     * Continue with Rustling Spots' normal reward resolution.
     */
    PASS,
    /**
     * Consume the spot and route through the normal empty-spot feedback path.
     */
    CONSUME_AS_EMPTY,
    /**
     * Consume the spot without normal rewards or empty-spot feedback.
     */
    HANDLED
}
