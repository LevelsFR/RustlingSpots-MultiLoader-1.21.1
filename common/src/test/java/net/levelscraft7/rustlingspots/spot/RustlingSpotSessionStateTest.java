package net.levelscraft7.rustlingspots.spot;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RustlingSpotSessionStateTest {
    @Test
    void resetClearsWorldAndPlayerRuntimeStateBetweenSessions() {
        RustlingSpot spot = new RustlingSpot(UUID.randomUUID(), Level.OVERWORLD, new BlockPos(0, 64, 0),
                RustlingSpotFamily.GRASS, "grass", "grass", RustlingSpotFamily.GRASS.spotId(),
                null, List.of(), 0L, 0, false, false);
        UUID playerId = UUID.randomUUID();
        RustlingSpotService.MANAGER.addIfAbsent(spot);
        RustlingSpotService.INTERACTION_BLOCKER.blockForTicks(playerId, 10L, 20L);

        RustlingSpotSessionState.clear();

        assertFalse(RustlingSpotService.MANAGER.get(Level.OVERWORLD, spot.getId()).isPresent());
        assertFalse(RustlingSpotService.INTERACTION_BLOCKER.isBlocked(playerId, 11L));
    }
}
