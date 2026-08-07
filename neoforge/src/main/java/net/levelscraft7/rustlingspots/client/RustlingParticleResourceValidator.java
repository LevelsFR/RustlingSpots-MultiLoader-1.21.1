package net.levelscraft7.rustlingspots.client;

import net.levelscraft7.rustlingspots.RustlingSpotsMod;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.neoforge.registry.RustlingParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Emits debug logs when expected particle textures cannot be located so players can
 * fix missing-resource pink squares without guesswork.
 */
public class RustlingParticleResourceValidator implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingParticleResourceValidator.class);

    private static final List<ResourceLocation> EXPECTED_TEXTURES = RustlingParticleTypes.particleTextures();

    public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new RustlingParticleResourceValidator());
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
            LOGGER.info("Rustling Spots: validating particle textures ({} expected entries)", EXPECTED_TEXTURES.size());
        }

        for (ResourceLocation texture : EXPECTED_TEXTURES) {
            ResourceLocation textureFile = texture.withPath("textures/particle/" + texture.getPath() + ".png");
            var resource = resourceManager.getResource(textureFile);
            if (resource.isPresent()) {
                if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    LOGGER.info("Rustling Spots: texture found -> {} (pack: {})", texture, resource.get().sourcePackId());
                }
            } else {
                String filename = texture.getPath().substring(texture.getPath().lastIndexOf('/') + 1);
                LOGGER.warn("Rustling Spots: texture missing -> {} (resolved path: {}). Expected under assets/{}/textures/particle/{}.png including optional top-level /resources override.", texture, textureFile, RustlingSpotsMod.MOD_ID, filename);
            }
        }
    }
}
