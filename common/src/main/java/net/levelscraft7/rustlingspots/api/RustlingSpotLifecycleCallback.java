package net.levelscraft7.rustlingspots.api;

@FunctionalInterface
public interface RustlingSpotLifecycleCallback {
    void onSpotEvent(RustlingSpotLifecycleEvent event);
}
