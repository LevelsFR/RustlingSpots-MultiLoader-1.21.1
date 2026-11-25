package net.levelscraft7.rustlingspots.spot;

/** Controls whether a datapack definition participates in natural spot selection. */
public enum SpotSpawnMode {
    NATURAL,
    EXTERNAL_ONLY;

    public static SpotSpawnMode fromSerializedName(String value) {
        if (value == null) {
            return NATURAL;
        }
        return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "natural" -> NATURAL;
            case "external_only" -> EXTERNAL_ONLY;
            default -> null;
        };
    }
}
