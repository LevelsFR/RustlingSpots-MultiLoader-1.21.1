package net.levelscraft7.rustlingspots.config;

/**
 * Small helper used by in-game config screens to show where the shared JSON
 * files live for the multi-loader build.
 */
public final class RustlingSpotsConfigPaths {
    private RustlingSpotsConfigPaths() {}

    public static String client() {
        return "config/rustlingspots/rustlingspots-client.json";
    }

    public static String server() {
        return "config/rustlingspots/rustlingspots-server.json";
    }

    public static String pokemon() {
        return "config/rustlingspots/rustlingspots-pokemon.json";
    }

    public static String sound() {
        return "config/rustlingspots/rustlingspots-sound.json";
    }

    public static String familyRates() {
        return "config/rustlingspots/rustlingspots-families.json";
    }

    public static String familyOverridesFolder() {
        return "config/rustlingspots/families + biome_tags";
    }

    public static String legacyTomlFolder() {
        return "run/config/rustlingspots/*.toml";
    }
}
