package net.levelscraft7.rustlingspots.registry;

import net.levelscraft7.rustlingspots.RustlingSpots;
import net.levelscraft7.rustlingspots.spot.RustlingSoundResolver;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class RustlingSoundEvents {
    public static SoundEvent POKEMON_SPAWN;
    public static SoundEvent ITEM_REWARD;

    private RustlingSoundEvents() {
    }

    public static void register() {
        POKEMON_SPAWN = register("spot/pokemon_spawn");
        ITEM_REWARD = register("spot/item_reward");
    }

    public static SoundEvent ambientVariant(int index) {
        return RustlingSoundResolver.ambientVariant(index);
    }

    public static int indexForFamily(RustlingSpotFamily family) {
        return RustlingSoundResolver.indexForFamily(family);
    }

    public static int ambientVariantCount() {
        return RustlingSoundResolver.ambientVariantCount();
    }

    private static SoundEvent register(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RustlingSpots.MOD_ID, path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
}
