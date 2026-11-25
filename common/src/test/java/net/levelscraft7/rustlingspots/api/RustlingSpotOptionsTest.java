package net.levelscraft7.rustlingspots.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RustlingSpotOptionsTest {
    @Test
    void externalOptionsRejectInvalidSpacingAndAllowNoPlayerRetention() {
        RustlingSpotSpawnOptions options = new RustlingSpotSpawnOptions(1200, null, 0.0D, false);

        assertEquals(1200, options.lifetimeTicks());
        assertEquals(0.0D, options.sameSourceSpacingBlocks());
        assertEquals(false, options.despawnWhenNoPlayers());
        assertThrows(IllegalArgumentException.class,
                () -> new RustlingSpotSpawnOptions(1200, null, Double.NaN, true));
    }

    @Test
    void rewardChanceOverridesPreserveUnspecifiedDefaults() {
        RustlingSpotRewardChances chances = new RustlingSpotRewardChances(0.8D, null);

        assertEquals(0.8D, chances.pokemonOr(0.35D));
        assertEquals(1.0D, chances.lootOr(1.0D));
        assertThrows(IllegalArgumentException.class,
                () -> new RustlingSpotRewardChances(1.1D, 0.5D));
    }
}
