package net.levelscraft7.rustlingspots.spot;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RustlingSpotManagerTest {
    private static final ResourceLocation EVENT = ResourceLocation.fromNamespaceAndPath("testaddon", "festival");

    @Test
    void sourceSpacingOverridesOnlySpacingWithinThatEvent() {
        RustlingSpotManager manager = new RustlingSpotManager();
        manager.addIfAbsent(spot(new BlockPos(0, 64, 0), EVENT, true));

        assertFalse(manager.isFarEnoughForSource(Level.OVERWORLD, 32.0D * 32.0D,
                EVENT, 4.0D * 4.0D, new BlockPos(3, 64, 0)));
        assertTrue(manager.isFarEnoughForSource(Level.OVERWORLD, 32.0D * 32.0D,
                EVENT, 4.0D * 4.0D, new BlockPos(5, 64, 0)));
        assertFalse(manager.isFarEnoughForSource(Level.OVERWORLD, 32.0D * 32.0D,
                ResourceLocation.fromNamespaceAndPath("otheraddon", "festival"), 4.0D * 4.0D,
                new BlockPos(5, 64, 0)));
    }

    @Test
    void aLaterCallCannotRelaxSpacingAlreadyRecordedForTheEvent() {
        RustlingSpotManager manager = new RustlingSpotManager();
        manager.addIfAbsent(spot(new BlockPos(0, 64, 0), EVENT, true, 8.0D));

        assertFalse(manager.isFarEnoughForSource(Level.OVERWORLD, 32.0D * 32.0D,
                EVENT, 3.0D * 3.0D, new BlockPos(5, 64, 0)));
    }

    @Test
    void clearRemovesSpotsAndResetsSessionCount() {
        RustlingSpotManager manager = new RustlingSpotManager();
        manager.addIfAbsent(spot(new BlockPos(0, 64, 0), EVENT, true));
        assertEquals(1, manager.totalCount());

        manager.clear();

        assertEquals(0, manager.totalCount());
        assertTrue(manager.getAll(Level.OVERWORLD).isEmpty());
    }

    private static RustlingSpot spot(BlockPos pos, ResourceLocation source, boolean external) {
        return spot(pos, source, external, 0.0D);
    }

    private static RustlingSpot spot(BlockPos pos, ResourceLocation source, boolean external, double spacing) {
        return new RustlingSpot(UUID.randomUUID(), Level.OVERWORLD, pos,
                RustlingSpotFamily.GRASS, "grass", "grass", RustlingSpotFamily.GRASS.spotId(),
                null, List.of(), 0L, 0, false, false, null, 0, source,
                external, spacing, true, null);
    }
}
