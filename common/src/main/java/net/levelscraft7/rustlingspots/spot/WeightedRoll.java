package net.levelscraft7.rustlingspots.spot;

import net.minecraft.util.RandomSource;

final class WeightedRoll {
    private WeightedRoll() {
    }

    static long nextLong(RandomSource random, long bound) {
        if (bound <= 0L) {
            throw new IllegalArgumentException("bound must be positive");
        }
        if (bound <= Integer.MAX_VALUE) {
            return random.nextInt((int) bound);
        }

        long bits;
        long value;
        do {
            bits = random.nextLong() >>> 1;
            value = bits % bound;
        } while (bits - value + (bound - 1L) < 0L);
        return value;
    }
}
