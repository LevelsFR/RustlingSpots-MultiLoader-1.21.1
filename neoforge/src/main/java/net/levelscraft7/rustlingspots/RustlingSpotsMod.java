package net.levelscraft7.rustlingspots;

import net.levelscraft7.rustlingspots.config.RustlingSpotsClientConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsFamilySpawnConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsSoundConfig;
import net.levelscraft7.rustlingspots.neoforge.spot.PokemonSpawnResolver;
import net.levelscraft7.rustlingspots.neoforge.network.RustlingSpotsNetwork;
import net.levelscraft7.rustlingspots.neoforge.registry.RustlingParticleTypes;
import net.levelscraft7.rustlingspots.neoforge.registry.RustlingSoundEvents;
import net.levelscraft7.rustlingspots.neoforge.spot.RustlingSpotCommands;
import net.levelscraft7.rustlingspots.spot.LootPoolService;
import net.levelscraft7.rustlingspots.spot.PokemonPoolService;
import net.levelscraft7.rustlingspots.spot.RustlingPlayerPreferences;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamilyConfigService;
import net.levelscraft7.rustlingspots.spot.RustlingSpotSyncService;
import net.levelscraft7.rustlingspots.spot.RustlingSpotsDatapackReloadService;
import net.levelscraft7.rustlingspots.spot.SpotRewardResolver;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Main mod entry point for Rustling Spots.
 */
@Mod(RustlingSpotsMod.MOD_ID)
public class RustlingSpotsMod {
    public static final String MOD_ID = "rustlingspots";

    public RustlingSpotsMod() {
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        IEventBus modBus = container.getEventBus();
        RustlingSoundEvents.register(modBus);
        RustlingParticleTypes.register(modBus);
        modBus.addListener(RustlingSpotsNetwork::register);

        if (FMLEnvironment.dist.isClient()) {
            RustlingSpotsNeoForgeClient.init(container, modBus);
            RustlingSpotsClientConfig.load();
        }
        RustlingSpotsServerConfig.load();
        RustlingSpotsFamilySpawnConfig.load();
        RustlingSpotsPokemonConfig.load();
        RustlingSpotsSoundConfig.load();
        SpotRewardResolver.setPokemonEncounterSpawner(PokemonSpawnResolver::spawn);
        PokemonPoolService.setSpeciesValidator(PokemonSpawnResolver.speciesValidator());
        RustlingSpotFamilyConfigService.ensureDefaultsExist();
        LootPoolService.ensureDefaultsExist();
        PokemonPoolService.ensureDefaultsExist();
        NeoForgeSpotSpawnerBridge spawnerBridge = new NeoForgeSpotSpawnerBridge();
        NeoForge.EVENT_BUS.register(spawnerBridge);
        NeoForge.EVENT_BUS.register(new NeoForgeSpotTickerBridge());
        NeoForge.EVENT_BUS.register(new RustlingSpotCommands());
        NeoForge.EVENT_BUS.addListener(this::registerReloadListeners);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                RustlingSpotSyncService.sendFullSync(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                RustlingSpotSyncService.sendFullSync(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                RustlingSpotSyncService.sendFullSync(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                RustlingPlayerPreferences.clear(player);
                spawnerBridge.forgetPlayer(player.getUUID());
            }
        });
    }

    private void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                RustlingSpotsDatapackReloadService.reload(resourceManager);
            }
        });
    }
}
