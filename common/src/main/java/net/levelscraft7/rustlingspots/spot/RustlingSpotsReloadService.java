package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.config.RustlingSpotsFamilySpawnConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsSoundConfig;
import net.minecraft.server.MinecraftServer;

/**
 * Shared reload entrypoint for server-side rustling spot data and configs.
 */
public final class RustlingSpotsReloadService {
    private RustlingSpotsReloadService() {
    }

    public static void reloadAll() {
        reloadAll(null);
    }

    public static void reloadAll(MinecraftServer server) {
        RustlingSpotsServerConfig.load();
        RustlingSpotsFamilySpawnConfig.load();
        RustlingSpotsPokemonConfig.load();
        RustlingSpotsSoundConfig.load();
        RustlingSpotFamilyConfigService.reload();
        LootPoolService.reload();
        PokemonPoolService.reload();
        if (server != null) {
            RustlingSpotsDatapackReloadService.reload(server.getResourceManager());
        }
    }
}
