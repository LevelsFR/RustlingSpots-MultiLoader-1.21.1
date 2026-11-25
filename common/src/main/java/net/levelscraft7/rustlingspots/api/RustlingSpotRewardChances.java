package net.levelscraft7.rustlingspots.api;

/** Optional reward chance overrides for one custom spot definition. Values must be in [0, 1]. */
public record RustlingSpotRewardChances(Double pokemon, Double loot) {
    public RustlingSpotRewardChances {
        validate("pokemon", pokemon);
        validate("loot", loot);
        if (pokemon == null && loot == null) {
            throw new IllegalArgumentException("At least one reward chance must be specified");
        }
    }

    public double pokemonOr(double fallback) {
        return pokemon != null ? pokemon : fallback;
    }

    public double lootOr(double fallback) {
        return loot != null ? loot : fallback;
    }

    private static void validate(String name, Double value) {
        if (value != null && (!Double.isFinite(value) || value < 0.0D || value > 1.0D)) {
            throw new IllegalArgumentException(name + " chance must be between 0 and 1");
        }
    }
}
