package net.levelscraft7.rustlingspots.spot;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/**
 * Fully resolved spawn template used by the server once a built-in or custom spot has been selected.
 */
public record SpotSpawnTemplate(
        ResourceLocation spotId,
        RustlingSpotFamily visualFamily,
        String pokemonFamily,
        String lootFamily,
        String displayName,
        int priority,
        int weight,
        List<WeightedParticleReference> particles,
        boolean custom
) {
    public SpotSpawnTemplate {
        if (spotId == null) {
            throw new IllegalArgumentException("spotId cannot be null");
        }
        if (visualFamily == null) {
            throw new IllegalArgumentException("visualFamily cannot be null");
        }
        if (pokemonFamily == null || pokemonFamily.isBlank()) {
            throw new IllegalArgumentException("pokemonFamily cannot be null");
        }
        pokemonFamily = pokemonFamily.trim();
        lootFamily = lootFamily != null && !lootFamily.isBlank() ? lootFamily.trim() : null;
        displayName = displayName != null && !displayName.isBlank() ? displayName.trim() : null;
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be positive");
        }
        particles = particles == null ? List.of() : List.copyOf(particles);
    }

    public static SpotSpawnTemplate builtIn(RustlingSpotFamily family) {
        return new SpotSpawnTemplate(
                family.spotId(),
                family,
                family.serializedName(),
                family.serializedName(),
                family.serializedName(),
                0,
                1,
                List.of(),
                false
        );
    }

    public RustlingSpot createSpot(UUID id, ResourceKey<Level> dimension, BlockPos position, long createdTick, int ambientVariant, boolean shiny) {
        return new RustlingSpot(
                id,
                dimension,
                position,
                visualFamily,
                pokemonFamily,
                lootFamily,
                spotId,
                displayName,
                particles,
                createdTick,
                ambientVariant,
                shiny,
                custom
        );
    }
}
