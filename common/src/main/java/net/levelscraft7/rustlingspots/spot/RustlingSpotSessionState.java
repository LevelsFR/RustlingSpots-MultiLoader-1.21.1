package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.api.RustlingSpotAddonApi;

/** Clears runtime data owned by one server instance while keeping mod-level addon registrations. */
public final class RustlingSpotSessionState {
    private RustlingSpotSessionState() {
    }

    public static void clear() {
        RustlingSpotService.clearSessionState();
        RustlingSpotAddonApi.clearThreadState();
        RustlingPlayerPreferences.clearAll();
        CustomSpotDefinitionRegistry.clear();
        LootPoolService.clearDatapackSession();
        PokemonPoolService.clearDatapackSession();
    }
}
