package net.levelscraft7.rustlingspots.spot;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/**
 * Lightweight server-side representation of an active rustling spot.
 */
public final class RustlingSpot {
    private final UUID id;
    private final ResourceKey<Level> dimension;
    private final BlockPos position;
    private final RustlingSpotFamily family;
    private final String pokemonFamily;
    private final String lootFamily;
    private final ResourceLocation spotId;
    private final String customDisplayName;
    private final List<WeightedParticleReference> particles;
    private final long createdTick;
    private final int ambientVariant;
    private final boolean shiny;
    private final boolean custom;

    public RustlingSpot(
            UUID id,
            ResourceKey<Level> dimension,
            BlockPos position,
            RustlingSpotFamily family,
            String pokemonFamily,
            String lootFamily,
            ResourceLocation spotId,
            String customDisplayName,
            List<WeightedParticleReference> particles,
            long createdTick,
            int ambientVariant,
            boolean shiny,
            boolean custom
    ) {
        this.id = id;
        this.dimension = dimension;
        this.position = position;
        this.family = family;
        this.pokemonFamily = pokemonFamily;
        this.lootFamily = lootFamily;
        this.spotId = spotId;
        this.customDisplayName = customDisplayName;
        this.particles = particles == null ? List.of() : List.copyOf(particles);
        this.createdTick = createdTick;
        this.ambientVariant = ambientVariant;
        this.shiny = shiny;
        this.custom = custom;
    }

    public UUID getId() {
        return id;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public BlockPos getPosition() {
        return position;
    }

    public RustlingSpotFamily getFamily() {
        return family;
    }

    public String getPokemonFamily() {
        return pokemonFamily;
    }

    public String getLootFamily() {
        return lootFamily;
    }

    public ResourceLocation getSpotId() {
        return spotId;
    }

    public List<WeightedParticleReference> getParticles() {
        return particles;
    }

    public long getCreatedTick() {
        return createdTick;
    }

    public int getAmbientVariant() {
        return ambientVariant;
    }

    public boolean isShiny() {
        return shiny;
    }

    public boolean isCustom() {
        return custom;
    }

    public String displayName() {
        if (customDisplayName != null && !customDisplayName.isBlank()) {
            return customDisplayName;
        }
        if (!custom && spotId != null && spotId.equals(family.spotId())) {
            return family.serializedName();
        }
        return spotId != null ? spotId.toString() : family.serializedName();
    }
}
