package net.levelscraft7.rustlingspots.client;

import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.neoforge.registry.RustlingParticleTypes;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-only event wiring for particles and diagnostics.
 */
public final class RustlingClientEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingClientEvents.class);

    private RustlingClientEvents() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RustlingParticleProviderRegistrar::register);
        modBus.addListener(RustlingParticleResourceValidator::register);
        modBus.addListener(RustlingClientEvents::onParticleAtlasStitched);
    }

    @SuppressWarnings("deprecation")
    private static void onParticleAtlasStitched(TextureAtlasStitchedEvent event) {
        TextureAtlas atlas = event.getAtlas();
        if (!atlas.location().equals(TextureAtlas.LOCATION_PARTICLES)) {
            return;
        }

        if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
            LOGGER.info("Rustling Spots: particle atlas stitched, verifying {} custom sprites",
                    RustlingParticleTypes.particleTextures().size());
        }

        for (ResourceLocation texture : RustlingParticleTypes.particleTextures()) {
            TextureAtlasSprite sprite = atlas.getSprite(texture);
            ResourceLocation spriteName = sprite.contents().name();
            if (MissingTextureAtlasSprite.getLocation().equals(spriteName)) {
                LOGGER.warn("Rustling Spots: {} not found in particle atlas (resolved to missing sprite)", texture);
            } else {
                if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    LOGGER.info("Rustling Spots: stitched particle sprite -> {}", spriteName);
                }
            }
        }
    }
}
