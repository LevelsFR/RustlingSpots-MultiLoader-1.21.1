package net.levelscraft7.rustlingspots.spot;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized message templates for player feedback.
 */
public final class RustlingMessages {
    private static final String ORIGIN_KEY_PREFIX = "message.rustlingspots.origin.";
    private static final List<String> POKEMON_MESSAGE_KEYS = List.of(
            "message.rustlingspots.pokemon.1",
            "message.rustlingspots.pokemon.2",
            "message.rustlingspots.pokemon.3"
    );

    private static final List<String> LOOT_MESSAGE_KEYS = List.of(
            "message.rustlingspots.loot.1",
            "message.rustlingspots.loot.2",
            "message.rustlingspots.loot.3"
    );

    private static final List<String> MULTI_POKEMON_MESSAGE_KEYS = List.of(
            "message.rustlingspots.multi_pokemon.1",
            "message.rustlingspots.multi_pokemon.2"
    );

    private static final List<String> MULTI_LOOT_MESSAGE_KEYS = List.of(
            "message.rustlingspots.multi_loot.1",
            "message.rustlingspots.multi_loot.2"
    );

    private static final List<String> EMPTY_SPOT_MESSAGE_KEYS = List.of(
            "message.rustlingspots.empty.1",
            "message.rustlingspots.empty.2",
            "message.rustlingspots.empty.3"
    );

    private RustlingMessages() {
    }

    public static Component randomPokemonMessage(RustlingSpot spot, String pokemon, RandomSource random) {
        String key = pickRandom(POKEMON_MESSAGE_KEYS, random);
        return Component.translatable(key, pokemon, originDescription(spot));
    }

    public static Component randomLootMessage(RustlingSpot spot, ItemStack stack, RandomSource random) {
        String key = pickRandom(LOOT_MESSAGE_KEYS, random);
        return Component.translatable(key, stack.getCount(), stack.getHoverName(), originDescription(spot));
    }

    public static Component randomMultiPokemonMessage(RustlingSpot spot, List<String> pokemon, RandomSource random) {
        String key = pickRandom(MULTI_POKEMON_MESSAGE_KEYS, random);
        return Component.translatable(key, joinSummary(pokemon), originDescription(spot));
    }

    public static Component randomMultiLootMessage(RustlingSpot spot, List<ItemStack> stacks, RandomSource random) {
        List<String> parts = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            parts.add(stack.getCount() + "x " + stack.getHoverName().getString());
        }
        String key = pickRandom(MULTI_LOOT_MESSAGE_KEYS, random);
        return Component.translatable(key, joinSummary(parts), originDescription(spot));
    }

    public static Component randomEmptySpotMessage(RandomSource random) {
        return Component.translatable(pickRandom(EMPTY_SPOT_MESSAGE_KEYS, random));
    }

    private static Component originDescription(RustlingSpot spot) {
        if (spot == null || spot.getFamily() == null) {
            return Component.translatable(ORIGIN_KEY_PREFIX + "generic");
        }
        return Component.translatable(ORIGIN_KEY_PREFIX + spot.getFamily().serializedName());
    }

    private static String pickRandom(List<String> templates, RandomSource random) {
        if (templates.isEmpty()) {
            return "";
        }
        return templates.get(random.nextInt(templates.size()));
    }

    private static String joinSummary(List<String> parts) {
        return String.join(", ", parts);
    }
}
