package exampleaddon;

import net.levelscraft7.rustlingspots.api.RustlingSpotAddonApi;
import net.levelscraft7.rustlingspots.api.RustlingSpotLifecycleEvent;
import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/** Example integration; call registerCallbacks during addon initialization. */
public final class RustlingSpotsAddonExample {
    private static final ResourceLocation DEFINITION_ID =
            ResourceLocation.fromNamespaceAndPath("exampleaddon", "festival");
    private static final ResourceLocation SOURCE_ID =
            ResourceLocation.fromNamespaceAndPath("exampleaddon", "spring_festival");

    private RustlingSpotsAddonExample() {
    }

    public static Optional<RustlingSpot> createFestivalSpot(ServerLevel level, BlockPos spotPosition) {
        return RustlingSpotAddonApi.createSpot(level, spotPosition, DEFINITION_ID, 20 * 60, SOURCE_ID);
    }

    public static void registerCallbacks() {
        RustlingSpotAddonApi.registerLifecycleCallback(event -> {
            if (event.spot().getSourceId().filter(SOURCE_ID::equals).isEmpty()) {
                return;
            }
            switch (event.type()) {
                case CREATED -> onFestivalSpotCreated(event);
                case INTERACTED -> onFestivalSpotInteracted(event);
                case EXPIRED -> onFestivalSpotExpired(event);
                case REMOVED -> onFestivalSpotRemoved(event);
            }
        });

    }

    private static void onFestivalSpotCreated(RustlingSpotLifecycleEvent event) {
        // Record the start of this addon event.
    }

    private static void onFestivalSpotInteracted(RustlingSpotLifecycleEvent event) {
        // Inspect event.player(); observational lifecycle listeners leave reward resolution intact.
    }

    private static void onFestivalSpotExpired(RustlingSpotLifecycleEvent event) {
        // This runs immediately before the corresponding REMOVED event.
    }

    private static void onFestivalSpotRemoved(RustlingSpotLifecycleEvent event) {
        // Inspect event.removalReason() to distinguish interaction, expiry and cleanup.
    }
}
