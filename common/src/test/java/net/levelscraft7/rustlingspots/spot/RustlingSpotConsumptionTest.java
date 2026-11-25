package net.levelscraft7.rustlingspots.spot;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RustlingSpotConsumptionTest {
    @Test
    void interactionClaimCannotBeReenteredOrRepeatedAfterCallbackFailure() {
        RustlingSpot spot = createSpot(false, true);
        assertTrue(spot.tryClaimInteraction());

        try {
            assertFalse(spot.tryClaimInteraction());
            throw new IllegalStateException("simulated addon callback failure");
        } catch (IllegalStateException ignored) {
            assertFalse(spot.tryClaimInteraction());
        }
    }

    @Test
    void noPlayerDespawnOverrideAppliesOnlyToExternalSpots() {
        assertFalse(SpotTicker.shouldDespawnWithoutNearbyPlayers(createSpot(true, false)));
        assertTrue(SpotTicker.shouldDespawnWithoutNearbyPlayers(createSpot(true, true)));
        assertTrue(SpotTicker.shouldDespawnWithoutNearbyPlayers(createSpot(false, false)));
    }

    @Test
    void unloadedRetainedSpotCannotBeInteractedWithUntilItsChunkLoads() {
        assertFalse(SpotTicker.shouldAttemptInteraction(false));
        assertTrue(SpotTicker.shouldAttemptInteraction(true));
    }

    private static RustlingSpot createSpot(boolean external, boolean despawnWhenNoPlayers) {
        return new RustlingSpot(UUID.randomUUID(), Level.OVERWORLD, new BlockPos(0, 64, 0),
                RustlingSpotFamily.GRASS, "grass", "grass", RustlingSpotFamily.GRASS.spotId(),
                null, List.of(), 0L, 0, false, false, null, 0, null,
                external, 0.0D, despawnWhenNoPlayers, null);
    }
}
