package net.levelscraft7.rustlingspots.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.levelscraft7.rustlingspots.RustlingSpots;
import net.levelscraft7.rustlingspots.config.RustlingSpotsClientConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesC2SPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesS2CPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotRemovePacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSpawnPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSyncPacket;
import net.levelscraft7.rustlingspots.registry.RustlingParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class RustlingSpotsFabricClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpots.MOD_ID);

    @Override
    public void onInitializeClient() {
        RustlingSpotsClientConfig.load();
        RustlingSpotClientHandler.bootstrap();
        registerParticleFactories();
        registerClientReceivers();
        registerParticleTextureValidator();
    }

    private static void registerClientReceivers() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> syncRewardMessagePreference());
        ClientPlayNetworking.registerGlobalReceiver(RustlingMessagePreferencesS2CPacket.TYPE, (payload, context) ->
                context.client().execute(() -> applyMessagePreferences(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(RustlingSpotSpawnPacket.TYPE, (payload, context) ->
                context.client().execute(() -> RustlingSpotClientHandler.handleSpawn(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(RustlingSpotRemovePacket.TYPE, (payload, context) ->
                context.client().execute(() -> RustlingSpotClientHandler.handleRemove(payload.id()))
        );
        ClientPlayNetworking.registerGlobalReceiver(RustlingSpotSyncPacket.TYPE, (payload, context) ->
                context.client().execute(() -> RustlingSpotClientHandler.handleSync(payload))
        );
    }

    private static void registerParticleFactories() {
        RustlingParticleProviderRegistrar.register(ParticleFactoryRegistry.getInstance());
    }

    private static void registerParticleTextureValidator() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            private final List<ResourceLocation> expected = RustlingParticleTypes.particleTextures();

            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(RustlingSpots.MOD_ID, "particle_texture_validator");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                if (!RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    return;
                }
                LOGGER.info("Rustling Spots: validating particle textures ({} expected entries)", expected.size());
                for (ResourceLocation texture : expected) {
                    ResourceLocation textureFile = texture.withPath("textures/particle/" + texture.getPath() + ".png");
                    var resource = resourceManager.getResource(textureFile);
                    if (resource.isPresent()) {
                        LOGGER.info("Rustling Spots: texture found -> {} (pack: {})", texture, resource.get().sourcePackId());
                    } else {
                        String filename = texture.getPath().substring(texture.getPath().lastIndexOf('/') + 1);
                        LOGGER.warn("Rustling Spots: texture missing -> {} (resolved path: {}). Expected under assets/{}/textures/particle/{}.png including optional top-level /resources override.", texture, textureFile, RustlingSpots.MOD_ID, filename);
                    }
                }
            }
        });
    }

    public static void syncRewardMessagePreference() {
        ClientPlayNetworking.send(new RustlingMessagePreferencesC2SPacket(
                RustlingSpotsClientConfig.VISUALS.showPokemonMessages(),
                RustlingSpotsClientConfig.VISUALS.showLootMessages(),
                RustlingSpotsClientConfig.VISUALS.showEmptySpotMessages()
        ));
    }

    public static void applyMessagePreferences(RustlingMessagePreferencesS2CPacket payload) {
        RustlingSpotsClientConfig.VISUALS.show_pokemon_messages = payload.showPokemonMessages();
        RustlingSpotsClientConfig.VISUALS.show_loot_messages = payload.showLootMessages();
        RustlingSpotsClientConfig.VISUALS.show_empty_spot_messages = payload.showEmptySpotMessages();
        RustlingSpotsClientConfig.save();
    }
}
