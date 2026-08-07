package net.levelscraft7.rustlingspots.spot;

/**
 * Holder for shared rustling spot state.
 */
public final class RustlingSpotService {
    public static final RustlingSpotManager MANAGER = new RustlingSpotManager();
    public static final SpotInteractionBlocker INTERACTION_BLOCKER = new SpotInteractionBlocker();

    private RustlingSpotService() {
    }
}
