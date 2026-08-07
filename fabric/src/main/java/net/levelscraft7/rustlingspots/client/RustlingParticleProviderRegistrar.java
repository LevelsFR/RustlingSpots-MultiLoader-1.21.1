package net.levelscraft7.rustlingspots.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.levelscraft7.rustlingspots.RustlingSpots;
import net.levelscraft7.rustlingspots.client.particle.RustlingBurstParticle;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.registry.RustlingParticleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RustlingParticleProviderRegistrar {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpots.MOD_ID);

    private RustlingParticleProviderRegistrar() {
    }

    public static void register(ParticleFactoryRegistry registry) {
        if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
            LOGGER.info("Rustling Spots: registering particle providers");
        }

        registry.register(RustlingParticleTypes.GRASS_BURST, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(1.25F, 0.15F, 14, 20, 0.78F, 1.05F, 0.0025F), RustlingSpots.MOD_ID + ":grass_burst"));
        registry.register(RustlingParticleTypes.SAND_BURST, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, -0.0012F, 0.0025F), RustlingSpots.MOD_ID + ":sand_burst"));
        registry.register(RustlingParticleTypes.WATER_BURST, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(0.95F, 0.25F, 6, 17, 0.9F, 0.80F, 0.02F), RustlingSpots.MOD_ID + ":water_burst"));
        registry.register(RustlingParticleTypes.SNOW_BURST, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, -0.0012F, 0.0025F), RustlingSpots.MOD_ID + ":snow_burst"));
        registry.register(RustlingParticleTypes.LEAVES_BURST, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, 1.05F, 0.0025F), RustlingSpots.MOD_ID + ":leaves_burst"));
        registry.register(RustlingParticleTypes.CAVE_BURST, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, 1.05F, 0.0025F), RustlingSpots.MOD_ID + ":cave_burst"));
        registry.register(RustlingParticleTypes.NETHERFLAMME_BURST, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, -0.0012F, 0.0025F), RustlingSpots.MOD_ID + ":netherflamme_burst"));
        registry.register(RustlingParticleTypes.SOULFLAME_BURST, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(1.18F, 0.18F, 14, 20, 0.8F, -0.0012F, 0.0025F), RustlingSpots.MOD_ID + ":soulflame_burst"));
        registry.register(RustlingParticleTypes.FLYING_BURST, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(0.78F, 0.18F, 16, 34, 0.91F, 0.15F, 0.011F), RustlingSpots.MOD_ID + ":flying_burst"));
        registry.register(RustlingParticleTypes.SHINY_SPARKLE_ONE, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(0.66F, 0.08F, 10, 16, 0.88F, -0.01F, 0.001F), RustlingSpots.MOD_ID + ":shiny_sparkle_one"));
        registry.register(RustlingParticleTypes.SHINY_SPARKLE_TWO, spriteSet ->
                RustlingBurstParticle.provider(spriteSet, new RustlingBurstParticle.Style(0.9F, 0.1F, 12, 18, 0.9F, -0.012F, 0.001F), RustlingSpots.MOD_ID + ":shiny_sparkle_two"));
    }
}
