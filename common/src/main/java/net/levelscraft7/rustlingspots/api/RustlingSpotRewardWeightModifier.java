package net.levelscraft7.rustlingspots.api;

/** Returns the adjusted weight for a loot entry; zero excludes the entry for this roll. */
@FunctionalInterface
public interface RustlingSpotRewardWeightModifier {
    int modifyWeight(RustlingSpotRewardWeightEvent event);
}
