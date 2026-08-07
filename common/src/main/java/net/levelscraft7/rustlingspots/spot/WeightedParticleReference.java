package net.levelscraft7.rustlingspots.spot;

import net.minecraft.resources.ResourceLocation;

/**
 * Lightweight weighted particle entry shared by custom spot definitions and client sync.
 */
public record WeightedParticleReference(ResourceLocation particleId, int weight) {
    public WeightedParticleReference {
        if (particleId == null) {
            throw new IllegalArgumentException("particleId cannot be null");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be positive");
        }
    }
}
