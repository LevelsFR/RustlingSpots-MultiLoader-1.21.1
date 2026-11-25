package net.levelscraft7.rustlingspots.spot;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.levelscraft7.rustlingspots.api.RustlingSpotRewardChances;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final ResourceLocation ambientSoundId;
    private final int lifetimeTicks;
    private final ResourceLocation sourceId;
    private final boolean external;
    private final double sameSourceSpacingBlocks;
    private final boolean despawnWhenNoPlayers;
    private final RustlingSpotRewardChances rewardChances;
    private final AtomicBoolean interactionClaimed = new AtomicBoolean();

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
        this(id, dimension, position, family, pokemonFamily, lootFamily, spotId, customDisplayName, particles,
                createdTick, ambientVariant, shiny, custom, null, 0, null);
    }

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
            boolean custom,
            ResourceLocation ambientSoundId,
            int lifetimeTicks,
            ResourceLocation sourceId
    ) {
        this(id, dimension, position, family, pokemonFamily, lootFamily, spotId, customDisplayName, particles,
                createdTick, ambientVariant, shiny, custom, ambientSoundId, lifetimeTicks, sourceId,
                false, 0.0D, true, null);
    }

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
            boolean custom,
            ResourceLocation ambientSoundId,
            int lifetimeTicks,
            ResourceLocation sourceId,
            boolean external,
            double sameSourceSpacingBlocks,
            boolean despawnWhenNoPlayers,
            RustlingSpotRewardChances rewardChances
    ) {
        this.id = java.util.Objects.requireNonNull(id, "id");
        this.dimension = java.util.Objects.requireNonNull(dimension, "dimension");
        this.position = java.util.Objects.requireNonNull(position, "position").immutable();
        this.family = java.util.Objects.requireNonNull(family, "family");
        this.pokemonFamily = java.util.Objects.requireNonNull(pokemonFamily, "pokemonFamily");
        this.lootFamily = lootFamily;
        this.spotId = java.util.Objects.requireNonNull(spotId, "spotId");
        this.customDisplayName = customDisplayName;
        this.particles = particles == null ? List.of() : List.copyOf(particles);
        this.createdTick = createdTick;
        this.ambientVariant = ambientVariant;
        this.shiny = shiny;
        this.custom = custom;
        this.ambientSoundId = ambientSoundId;
        this.lifetimeTicks = Math.max(0, lifetimeTicks);
        this.sourceId = sourceId;
        this.external = external;
        this.sameSourceSpacingBlocks = sameSourceSpacingBlocks;
        this.despawnWhenNoPlayers = despawnWhenNoPlayers;
        this.rewardChances = rewardChances;
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

    public Optional<ResourceLocation> getAmbientSoundId() {
        return Optional.ofNullable(ambientSoundId);
    }

    /** Returns zero when the configured server lifetime should be used. */
    public int getLifetimeTicks() {
        return lifetimeTicks;
    }

    public Optional<ResourceLocation> getSourceId() {
        return Optional.ofNullable(sourceId);
    }

    public boolean isExternal() {
        return external;
    }

    public double getSameSourceSpacingBlocks() {
        return sameSourceSpacingBlocks;
    }

    public boolean despawnWhenNoPlayers() {
        return despawnWhenNoPlayers;
    }

    public Optional<RustlingSpotRewardChances> getRewardChances() {
        return Optional.ofNullable(rewardChances);
    }

    public boolean tryClaimInteraction() {
        return interactionClaimed.compareAndSet(false, true);
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
