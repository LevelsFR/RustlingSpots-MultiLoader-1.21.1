package net.levelscraft7.rustlingspots.neoforge.registry;

import net.levelscraft7.rustlingspots.RustlingSpotsMod;
import net.levelscraft7.rustlingspots.spot.RustlingSoundResolver;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RustlingSoundEvents {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(
            BuiltInRegistries.SOUND_EVENT, RustlingSpotsMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> POKEMON_SPAWN = register("spot/pokemon_spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_REWARD = register("spot/item_reward");

    private RustlingSoundEvents() {
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
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

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, path);
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
