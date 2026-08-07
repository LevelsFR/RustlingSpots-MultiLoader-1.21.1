package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.config.RustlingSpotsFamilySpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves the final rustling spot template for a spawn attempt.
 */
public final class SpotSpawnSelectionService {
    private SpotSpawnSelectionService() {
    }

    public static Optional<SpotSpawnTemplate> resolve(ServerLevel level, BlockPos groundPos, BlockState groundState, RandomSource random) {
        List<SpotSpawnTemplate> candidates = new ArrayList<>();

        RustlingSpotFamily.fromSurfaceBlock(groundState, level, groundPos)
                .filter(family -> random.nextDouble() <= RustlingSpotsFamilySpawnConfig.FAMILIES.spawnRate(family))
                .map(SpotSpawnTemplate::builtIn)
                .ifPresent(candidates::add);

        candidates.addAll(CustomSpotDefinitionRegistry.matching(level, groundPos, groundState));
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        int highestPriority = candidates.stream()
                .mapToInt(SpotSpawnTemplate::priority)
                .max()
                .orElse(0);

        List<SpotSpawnTemplate> filtered = candidates.stream()
                .filter(candidate -> candidate.priority() == highestPriority)
                .toList();

        int totalWeight = filtered.stream().mapToInt(SpotSpawnTemplate::weight).sum();
        if (totalWeight <= 0) {
            return Optional.empty();
        }

        int roll = random.nextInt(totalWeight);
        int cursor = 0;
        for (SpotSpawnTemplate candidate : filtered) {
            cursor += candidate.weight();
            if (roll < cursor) {
                return Optional.of(candidate);
            }
        }
        return Optional.of(filtered.get(filtered.size() - 1));
    }

    public static Optional<SpotSpawnTemplate> resolveCommandTarget(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String normalized = input.trim();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        Optional<RustlingSpotFamily> builtinFamily = RustlingSpotFamily.fromSerializedName(normalized);
        if (builtinFamily.isPresent()) {
            return Optional.of(SpotSpawnTemplate.builtIn(builtinFamily.get()));
        }

        ResourceLocation id = ResourceLocation.tryParse(normalized);
        if (id == null) {
            return Optional.empty();
        }

        Optional<RustlingSpotFamily> familyByPath = RustlingSpotFamily.fromSerializedName(id.getPath());
        if (familyByPath.isPresent()) {
            return Optional.of(SpotSpawnTemplate.builtIn(familyByPath.get()));
        }

        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            if (family.spotId().equals(id)) {
                return Optional.of(SpotSpawnTemplate.builtIn(family));
            }
        }

        return CustomSpotDefinitionRegistry.get(id).map(CustomSpotDefinition::template);
    }

    public static List<String> commandSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for (RustlingSpotFamily family : RustlingSpotFamily.values()) {
            suggestions.add(family.serializedName());
        }
        suggestions.addAll(CustomSpotDefinitionRegistry.idsForSuggestions());
        suggestions.sort(String::compareTo);
        return suggestions;
    }
}
