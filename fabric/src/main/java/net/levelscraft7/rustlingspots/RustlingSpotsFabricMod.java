package net.levelscraft7.rustlingspots;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.levelscraft7.rustlingspots.config.RustlingSpotsFamilySpawnConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsSoundConfig;
import net.levelscraft7.rustlingspots.network.RustlingSpotsNetwork;
import net.levelscraft7.rustlingspots.registry.RustlingParticleTypes;
import net.levelscraft7.rustlingspots.registry.RustlingSoundEvents;
import net.levelscraft7.rustlingspots.spot.LootPoolService;
import net.levelscraft7.rustlingspots.spot.PokemonPoolService;
import net.levelscraft7.rustlingspots.spot.RustlingSpotCommands;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamilyConfigService;
import net.levelscraft7.rustlingspots.spot.RustlingSpotSyncService;
import net.levelscraft7.rustlingspots.spot.RustlingSpotsDatapackReloadService;
import net.levelscraft7.rustlingspots.spot.SpotSpawner;
import net.levelscraft7.rustlingspots.spot.SpotTicker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class RustlingSpotsFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        RustlingSpotsServerConfig.load();
        RustlingSpotsFamilySpawnConfig.load();
        RustlingSpotsPokemonConfig.load();
        RustlingSpotsSoundConfig.load();

        RustlingSpotFamilyConfigService.ensureDefaultsExist();
        LootPoolService.ensureDefaultsExist();
        PokemonPoolService.ensureDefaultsExist();

        RustlingSoundEvents.register();
        RustlingParticleTypes.register();
        RustlingSpotsNetwork.register();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(RustlingSpots.MOD_ID, "custom_spot_definitions");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                RustlingSpotsDatapackReloadService.reload(resourceManager);
            }
        });

        SpotSpawner spawner = new SpotSpawner();
        SpotTicker ticker = new SpotTicker();
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                RustlingSpotSyncService.sendFullSync(newPlayer)
        );
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                RustlingSpotSyncService.sendFullSync(player)
        );
        ServerPlayerEvents.LEAVE.register(player -> spawner.forgetPlayer(player.getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            spawner.onServerTick(server);
            ticker.onServerTick(server);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                RustlingSpotCommands.register(dispatcher)
        );
    }
}
