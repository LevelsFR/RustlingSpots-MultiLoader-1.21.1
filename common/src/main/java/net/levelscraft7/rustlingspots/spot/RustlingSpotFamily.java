package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.common.RustlingSpotsCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;


import java.util.Locale;
import java.util.Optional;

/**
 * Families describe the surface theme for rustling spots.
 */
public enum RustlingSpotFamily {
    GRASS,
    SAND,
    WATER,
    SNOW,
    LEAVES,
    CAVE,
    NETHERFLAMME,
    SOULFLAME,
    FLYING;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ResourceLocation spotId() {
        return ResourceLocation.fromNamespaceAndPath(RustlingSpotsCommon.MOD_ID, serializedName());
    }

    public static Optional<RustlingSpotFamily> fromSerializedName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        for (RustlingSpotFamily family : values()) {
            if (family.serializedName().equalsIgnoreCase(name.trim())) {
                return Optional.of(family);
            }
        }
        return Optional.empty();
    }

    /**
     * Determines the family based on the surface block beneath the chosen position.
     */
    public static Optional<RustlingSpotFamily> fromSurfaceBlock(BlockState state, Level level, BlockPos pos) {
        Optional<RustlingSpotFamily> configured = RustlingSpotFamilyConfigService.resolve(state, level, pos);
        if (configured.isPresent()) {
            return configured;
        }

        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH) || state.is(BlockTags.SMALL_FLOWERS)) {
            return Optional.of(GRASS);
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            return Optional.of(SAND);
        }
        FluidState fluid = state.getFluidState();
        if (fluid.isSource() && state.is(Blocks.WATER)) {
            BlockState above = level.getBlockState(pos.above());
            if (above.isAir()) {
                return Optional.of(WATER);
            }
        }
        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
            return Optional.of(SNOW);
        }
        if (state.is(BlockTags.LEAVES)) {
            return Optional.of(LEAVES);
        }
        if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.DEEPSLATE)) {
            if (!level.canSeeSkyFromBelowWater(pos.above()) && level.getBlockState(pos.above()).isAir()) {
                return Optional.of(CAVE);
            }
        }
        if (state.is(Blocks.NETHERRACK)) {
            return Optional.of(NETHERFLAMME);
        }
        if (state.is(Blocks.LAVA) && state.getFluidState().isSource() && level.getBlockState(pos.above()).isAir()) {
            return Optional.of(NETHERFLAMME);
        }
        if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            return Optional.of(SOULFLAME);
        }
        if (level.canSeeSkyFromBelowWater(pos.above()) && level.getBlockState(pos.above()).isAir()) {
            return Optional.of(FLYING);
        }
        return Optional.empty();
    }
}
