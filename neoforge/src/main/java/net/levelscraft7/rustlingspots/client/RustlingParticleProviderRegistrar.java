package net.levelscraft7.rustlingspots.client;

import net.levelscraft7.rustlingspots.RustlingSpotsMod;
import net.levelscraft7.rustlingspots.client.particle.RustlingBurstParticle;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.neoforge.registry.RustlingParticleTypes;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers particle factories on the logical client.
 */
public final class RustlingParticleProviderRegistrar {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingParticleProviderRegistrar.class);

    private RustlingParticleProviderRegistrar() {
    }

    public static void register(RegisterParticleProvidersEvent event) {
        if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
            LOGGER.info("Rustling Spots: registering particle providers");
        }

        event.registerSpriteSet(RustlingParticleTypes.GRASS_BURST.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(1.25F, 0.15F, 14, 20, 0.78F, 1.05F, 0.0025F),
                        RustlingSpotsMod.MOD_ID + ":grass_burst"
                ));
        event.registerSpriteSet(RustlingParticleTypes.SAND_BURST.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, -0.0012F, 0.0025F),
                        RustlingSpotsMod.MOD_ID + ":sand_burst"
                ));
        event.registerSpriteSet(RustlingParticleTypes.WATER_BURST.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(0.95F, 0.25F, 6, 17, 0.9F, 0.80F, 0.02F),
                        RustlingSpotsMod.MOD_ID + ":water_burst"
                ));
        event.registerSpriteSet(RustlingParticleTypes.SNOW_BURST.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, -0.0012F, 0.0025F),
                        RustlingSpotsMod.MOD_ID + ":snow_burst"
                ));
        event.registerSpriteSet(RustlingParticleTypes.LEAVES_BURST.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, 1.05F, 0.0025F),
                        RustlingSpotsMod.MOD_ID + ":leaves_burst"
                ));
        event.registerSpriteSet(RustlingParticleTypes.CAVE_BURST.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, 1.05F, 0.0025F),
                        RustlingSpotsMod.MOD_ID + ":cave_burst"
                ));
        event.registerSpriteSet(RustlingParticleTypes.NETHERFLAMME_BURST.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, -0.0012F, 0.0025F),
                        RustlingSpotsMod.MOD_ID + ":netherflamme_burst"
                ));
        event.registerSpriteSet(RustlingParticleTypes.SOULFLAME_BURST.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, -0.0012F, 0.0025F),
                        RustlingSpotsMod.MOD_ID + ":soulflame_burst"
                ));
        event.registerSpriteSet(RustlingParticleTypes.FLYING_BURST.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(0.78F, 0.18F, 16, 34, 0.91F, 0.15F, 0.011F),
                        RustlingSpotsMod.MOD_ID + ":flying_burst"
                ));
        event.registerSpriteSet(RustlingParticleTypes.SHINY_SPARKLE_ONE.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(0.66F, 0.08F, 10, 16, 0.88F, -0.01F, 0.001F),
                        RustlingSpotsMod.MOD_ID + ":shiny_sparkle_one"
                ));
        event.registerSpriteSet(RustlingParticleTypes.SHINY_SPARKLE_TWO.get(), spriteSet ->
                RustlingBurstParticle.provider(
                        spriteSet,
                        new RustlingBurstParticle.Style(0.9F, 0.1F, 12, 18, 0.9F, -0.012F, 0.001F),
                        RustlingSpotsMod.MOD_ID + ":shiny_sparkle_two"
                ));
    }
}
