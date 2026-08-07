package net.levelscraft7.rustlingspots.compat;

import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

/**
 * Shared compatibility helpers for Cobblemon Raid Dens.
 */
public final class CobblemonRaidDensCompat {
    private static final ResourceLocation RAID_DIMENSION_ID =
            ResourceLocation.fromNamespaceAndPath("cobblemonraiddens", "raid_dimension");

    private CobblemonRaidDensCompat() {
    }

    public static boolean shouldAllowSpots(ServerLevel level) {
        if (RustlingSpotsServerConfig.GENERAL.allowCobblemonRaidDenDimensions()) {
            return true;
        }
        return !isRaidDimension(level);
    }

    public static boolean isRaidDimension(ServerLevel level) {
        return level.dimension().location().equals(RAID_DIMENSION_ID);
    }
}
