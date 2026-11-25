package net.levelscraft7.rustlingspots.api;

import net.minecraft.resources.ResourceLocation;

/** Options for an addon-created spot. Distances are measured in blocks. */
public record RustlingSpotSpawnOptions(
        int lifetimeTicks,
        ResourceLocation sourceId,
        double sameSourceSpacingBlocks,
        boolean despawnWhenNoPlayers
) {
    public static final double MAX_SOURCE_SPACING_BLOCKS = 512.0D;

    public RustlingSpotSpawnOptions {
        if (lifetimeTicks <= 0) {
            throw new IllegalArgumentException("lifetimeTicks must be positive");
        }
        if (!Double.isFinite(sameSourceSpacingBlocks)
                || sameSourceSpacingBlocks < 0.0D
                || sameSourceSpacingBlocks > MAX_SOURCE_SPACING_BLOCKS) {
            throw new IllegalArgumentException("sameSourceSpacingBlocks must be between 0 and 512");
        }
    }
}
