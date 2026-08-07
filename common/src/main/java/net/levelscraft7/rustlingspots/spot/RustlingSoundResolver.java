package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.common.RustlingSpotsCommon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

/**
 * Shared sound lookup helpers used by loader-specific and common server logic.
 */
public final class RustlingSoundResolver {
    private static final ResourceLocation POKEMON_SPAWN_ID =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsCommon.MOD_ID, "spot/pokemon_spawn");
    private static final ResourceLocation ITEM_REWARD_ID =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsCommon.MOD_ID, "spot/item_reward");

    private static final List<SoundEvent> AMBIENT_VARIANTS = List.of(
            SoundEvents.GRASS_STEP,
            SoundEvents.SAND_STEP,
            SoundEvents.WATER_AMBIENT,
            SoundEvents.SNOW_STEP,
            SoundEvents.AZALEA_LEAVES_STEP,
            SoundEvents.AMBIENT_CAVE.value(),
            SoundEvents.FIRE_AMBIENT,
            SoundEvents.SOUL_SAND_STEP,
            SoundEvents.PHANTOM_FLAP
    );

    private RustlingSoundResolver() {
    }

    public static int indexForFamily(RustlingSpotFamily family) {
        return switch (family) {
            case GRASS -> 0;
            case SAND -> 1;
            case WATER -> 2;
            case SNOW -> 3;
            case LEAVES -> 4;
            case CAVE -> 5;
            case NETHERFLAMME -> 6;
            case SOULFLAME -> 7;
            case FLYING -> 8;
        };
    }

    public static SoundEvent ambientVariant(int index) {
        if (index < 0 || index >= AMBIENT_VARIANTS.size()) {
            return AMBIENT_VARIANTS.get(0);
        }
        return AMBIENT_VARIANTS.get(index);
    }

    public static int ambientVariantCount() {
        return AMBIENT_VARIANTS.size();
    }

    public static SoundEvent pokemonSpawnSound() {
        return BuiltInRegistries.SOUND_EVENT.getOptional(POKEMON_SPAWN_ID).orElse(SoundEvents.EXPERIENCE_ORB_PICKUP);
    }

    public static SoundEvent itemRewardSound() {
        return BuiltInRegistries.SOUND_EVENT.getOptional(ITEM_REWARD_ID).orElse(SoundEvents.ITEM_PICKUP);
    }
}
