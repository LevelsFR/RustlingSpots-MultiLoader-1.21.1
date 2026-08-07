package net.levelscraft7.rustlingspots.spot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

/**
 * Cached custom spot definition loaded from datapacks.
 */
public final class CustomSpotDefinition {
    private final SpotSpawnTemplate template;
    private final Set<ResourceKey<Level>> dimensions;
    private final List<BlockMatcher> blocks;
    private final List<BiomeMatcher> biomes;

    public CustomSpotDefinition(
            SpotSpawnTemplate template,
            Set<ResourceKey<Level>> dimensions,
            List<BlockMatcher> blocks,
            List<BiomeMatcher> biomes
    ) {
        this.template = template;
        this.dimensions = Set.copyOf(dimensions);
        this.blocks = List.copyOf(blocks);
        this.biomes = List.copyOf(biomes);
    }

    public ResourceLocation id() {
        return template.spotId();
    }

    public SpotSpawnTemplate template() {
        return template;
    }

    public boolean matches(Level level, BlockPos groundPos, BlockState groundState) {
        if (!dimensions.contains(level.dimension())) {
            return false;
        }
        if (blocks.stream().noneMatch(matcher -> matcher.matches(groundState, level, groundPos))) {
            return false;
        }
        return biomes.stream().anyMatch(matcher -> matcher.matches(level, groundPos));
    }

    public record BlockMatcher(Block block, TagKey<Block> tag) {
        public static BlockMatcher parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String trimmed = raw.trim();
            if (trimmed.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(trimmed.substring(1));
                if (id == null) {
                    return null;
                }
                return new BlockMatcher(null, TagKey.create(Registries.BLOCK, id));
            }

            ResourceLocation id = ResourceLocation.tryParse(trimmed);
            if (id == null) {
                return null;
            }

            Block resolved = BuiltInRegistries.BLOCK.get(id);
            if (resolved == Blocks.AIR) {
                return null;
            }
            return new BlockMatcher(resolved, null);
        }

        public boolean matches(BlockState state, Level level, BlockPos pos) {
            if (tag != null) {
                return state.is(tag);
            }
            if (block == Blocks.WATER) {
                return state.is(block) && state.getFluidState().isSource() && level.getBlockState(pos.above()).isAir();
            }
            if (block == Blocks.LAVA) {
                return state.is(block) && state.getFluidState().isSource() && level.getBlockState(pos.above()).isAir();
            }
            return state.is(block);
        }
    }

    public record BiomeMatcher(ResourceKey<net.minecraft.world.level.biome.Biome> biome,
                               TagKey<net.minecraft.world.level.biome.Biome> tag) {
        public static BiomeMatcher parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String trimmed = raw.trim();
            if (trimmed.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(trimmed.substring(1));
                if (id == null) {
                    return null;
                }
                return new BiomeMatcher(null, TagKey.create(Registries.BIOME, id));
            }

            ResourceLocation id = ResourceLocation.tryParse(trimmed);
            if (id == null) {
                return null;
            }
            return new BiomeMatcher(ResourceKey.create(Registries.BIOME, id), null);
        }

        public boolean matches(Level level, BlockPos pos) {
            var biomeHolder = level.getBiome(pos);
            if (tag != null) {
                return biomeHolder.is(tag);
            }
            return biome != null && biomeHolder.is(biome);
        }
    }
}
