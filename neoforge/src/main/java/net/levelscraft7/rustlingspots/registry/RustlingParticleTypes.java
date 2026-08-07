package net.levelscraft7.rustlingspots.neoforge.registry;

import net.levelscraft7.rustlingspots.RustlingSpotsMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Registers the bespoke particle types used by rustling spots.
 */
public final class RustlingParticleTypes {
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(
            BuiltInRegistries.PARTICLE_TYPE, RustlingSpotsMod.MOD_ID
    );

    private static final ResourceLocation RS_BUBBLE_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_bubbleone");
    private static final ResourceLocation RS_BUBBLE_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_bubbletwo");
    private static final ResourceLocation RS_BUBBLE_THREE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_bubblethree");

    private static final ResourceLocation RS_CLOUD_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_cloudone");
    private static final ResourceLocation RS_CLOUD_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_cloudtwo");
    private static final ResourceLocation RS_CLOUD_THREE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_cloudthree");
    private static final ResourceLocation RS_CLOUD_FOUR =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_cloudfour");

    private static final ResourceLocation RS_DUST_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_dustone");
    private static final ResourceLocation RS_DUST_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_dusttwo");

    private static final ResourceLocation RS_NETHER_FLAMME_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_netherflameone");
    private static final ResourceLocation RS_NETHER_FLAMME_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_netherflametwo");
    private static final ResourceLocation RS_NETHER_FLAMME_THREE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_netherflamethree");

    private static final ResourceLocation RS_SOUL_FLAME_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_soulflameone");
    private static final ResourceLocation RS_SOUL_FLAME_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_soulflametwo");
    private static final ResourceLocation RS_SOUL_FLAME_THREE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_soulflamethree");

    private static final ResourceLocation RS_LEAF_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_leafone");
    private static final ResourceLocation RS_LEAF_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_leaftwo");
    private static final ResourceLocation RS_LEAF_THREE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_leafthree");
    private static final ResourceLocation RS_LEAF_FOUR =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_leaffour");

    private static final ResourceLocation RS_OMBRES =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_ombres");

    private static final ResourceLocation RS_ROCK_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_rockone");
    private static final ResourceLocation RS_ROCK_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_rocktwo");

    private static final ResourceLocation RS_SAND_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_sandone");
    private static final ResourceLocation RS_SAND_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_sandtwo");
    private static final ResourceLocation RS_SAND_THREE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_sandthree");

    private static final ResourceLocation RS_SHORT_GRASS =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_shortgrass");
    private static final ResourceLocation RS_SMALL_LEAF =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_smallleaf");
    private static final ResourceLocation RS_SHINY_SPARKLE_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_shinysparkleone");
    private static final ResourceLocation RS_SHINY_SPARKLE_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_shinysparkletwo");

    private static final ResourceLocation RS_SNOW_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_snowone");
    private static final ResourceLocation RS_SNOW_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_snowtwo");
    private static final ResourceLocation RS_SNOW_THREE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_snowthree");
    private static final ResourceLocation RS_SNOW_FOUR =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_snowfour");
    private static final ResourceLocation RS_SNOW_FIVE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_snowfive");

    private static final ResourceLocation RS_STICK_ONE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_stickone");
    private static final ResourceLocation RS_STICK_TWO =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "rs_sticktwo");

    private static final List<ResourceLocation> PARTICLE_TEXTURES = List.of(
            RS_BUBBLE_ONE,
            RS_BUBBLE_TWO,
            RS_BUBBLE_THREE,
            RS_CLOUD_ONE,
            RS_CLOUD_TWO,
            RS_CLOUD_THREE,
            RS_CLOUD_FOUR,
            RS_DUST_ONE,
            RS_DUST_TWO,
            RS_NETHER_FLAMME_ONE,
            RS_NETHER_FLAMME_TWO,
            RS_NETHER_FLAMME_THREE,
            RS_LEAF_ONE,
            RS_LEAF_TWO,
            RS_LEAF_THREE,
            RS_LEAF_FOUR,
            RS_OMBRES,
            RS_ROCK_ONE,
            RS_ROCK_TWO,
            RS_SAND_ONE,
            RS_SAND_TWO,
            RS_SAND_THREE,
            RS_SHORT_GRASS,
            RS_SMALL_LEAF,
            RS_SHINY_SPARKLE_ONE,
            RS_SHINY_SPARKLE_TWO,
            RS_SOUL_FLAME_ONE,
            RS_SOUL_FLAME_TWO,
            RS_SOUL_FLAME_THREE,
            RS_SNOW_ONE,
            RS_SNOW_TWO,
            RS_SNOW_THREE,
            RS_SNOW_FOUR,
            RS_SNOW_FIVE,
            RS_STICK_ONE,
            RS_STICK_TWO
    );

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GRASS_BURST =
            PARTICLES.register("grass_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SAND_BURST =
            PARTICLES.register("sand_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WATER_BURST =
            PARTICLES.register("water_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SNOW_BURST =
            PARTICLES.register("snow_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LEAVES_BURST =
            PARTICLES.register("leaves_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CAVE_BURST =
            PARTICLES.register("cave_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NETHERFLAMME_BURST =
            PARTICLES.register("netherflamme_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOULFLAME_BURST =
            PARTICLES.register("soulflame_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLYING_BURST =
            PARTICLES.register("flying_burst", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHINY_SPARKLE_ONE =
            PARTICLES.register("shiny_sparkle_one", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHINY_SPARKLE_TWO =
            PARTICLES.register("shiny_sparkle_two", () -> new SimpleParticleType(false));

    private RustlingParticleTypes() {
    }

    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }

    public static List<ResourceLocation> particleTextures() {
        return PARTICLE_TEXTURES;
    }
}
