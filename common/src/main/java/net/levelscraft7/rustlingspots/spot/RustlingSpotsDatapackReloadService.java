package net.levelscraft7.rustlingspots.spot;

import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Reloads all Rustling Spots datapack-driven runtime caches in dependency order.
 */
public final class RustlingSpotsDatapackReloadService {
    private RustlingSpotsDatapackReloadService() {
    }

    public static void reload(ResourceManager resourceManager) {
        LootPoolService.reloadDatapack(resourceManager);
        PokemonPoolService.reloadDatapack(resourceManager);
        CustomSpotDefinitionRegistry.reload(resourceManager);
    }
}
