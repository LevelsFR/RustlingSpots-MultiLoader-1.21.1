package net.levelscraft7.rustlingspots.api;

import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Immutable server-side context for a validated rustling spot interaction.
 */
public record RustlingSpotInteractionEvent(
        ServerPlayer player,
        ServerLevel level,
        RustlingSpot spot,
        BlockPos position,
        ResourceKey<Level> dimension,
        ResourceLocation dimensionId,
        RustlingSpotFamily family,
        ResourceLocation spotId,
        boolean customSpot,
        RandomSource random
) {
    public static RustlingSpotInteractionEvent of(ServerPlayer player, ServerLevel level, RustlingSpot spot) {
        return new RustlingSpotInteractionEvent(
                player,
                level,
                spot,
                spot.getPosition(),
                level.dimension(),
                level.dimension().location(),
                spot.getFamily(),
                spot.getSpotId(),
                spot.isCustom(),
                level.random
        );
    }
}
