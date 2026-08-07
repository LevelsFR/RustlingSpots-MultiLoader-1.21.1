package net.levelscraft7.rustlingspots.registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.levelscraft7.rustlingspots.RustlingSpots;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class RustlingParticleTypes {
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RustlingSpots.MOD_ID, path);
    }

    private static final List<ResourceLocation> PARTICLE_TEXTURES = List.of(
            id("rs_bubbleone"),
            id("rs_bubbletwo"),
            id("rs_bubblethree"),
            id("rs_cloudone"),
            id("rs_cloudtwo"),
            id("rs_cloudthree"),
            id("rs_cloudfour"),
            id("rs_dustone"),
            id("rs_dusttwo"),
            id("rs_netherflameone"),
            id("rs_netherflametwo"),
            id("rs_netherflamethree"),
            id("rs_leafone"),
            id("rs_leaftwo"),
            id("rs_leafthree"),
            id("rs_leaffour"),
            id("rs_ombres"),
            id("rs_rockone"),
            id("rs_rocktwo"),
            id("rs_sandone"),
            id("rs_sandtwo"),
            id("rs_sandthree"),
            id("rs_shortgrass"),
            id("rs_smallleaf"),
            id("rs_shinysparkleone"),
            id("rs_shinysparkletwo"),
            id("rs_soulflameone"),
            id("rs_soulflametwo"),
            id("rs_soulflamethree"),
            id("rs_snowone"),
            id("rs_snowtwo"),
            id("rs_snowthree"),
            id("rs_snowfour"),
            id("rs_snowfive"),
            id("rs_stickone"),
            id("rs_sticktwo")
    );

    public static SimpleParticleType GRASS_BURST;
    public static SimpleParticleType SAND_BURST;
    public static SimpleParticleType WATER_BURST;
    public static SimpleParticleType SNOW_BURST;
    public static SimpleParticleType LEAVES_BURST;
    public static SimpleParticleType CAVE_BURST;
    public static SimpleParticleType NETHERFLAMME_BURST;
    public static SimpleParticleType SOULFLAME_BURST;
    public static SimpleParticleType FLYING_BURST;
    public static SimpleParticleType SHINY_SPARKLE_ONE;
    public static SimpleParticleType SHINY_SPARKLE_TWO;

    private RustlingParticleTypes() {
    }

    public static void register() {
        GRASS_BURST = register("grass_burst");
        SAND_BURST = register("sand_burst");
        WATER_BURST = register("water_burst");
        SNOW_BURST = register("snow_burst");
        LEAVES_BURST = register("leaves_burst");
        CAVE_BURST = register("cave_burst");
        NETHERFLAMME_BURST = register("netherflamme_burst");
        SOULFLAME_BURST = register("soulflame_burst");
        FLYING_BURST = register("flying_burst");
        SHINY_SPARKLE_ONE = register("shiny_sparkle_one");
        SHINY_SPARKLE_TWO = register("shiny_sparkle_two");
    }

    public static List<ResourceLocation> particleTextures() {
        return PARTICLE_TEXTURES;
    }

    private static SimpleParticleType register(String path) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, id(path), FabricParticleTypes.simple(false));
    }
}
