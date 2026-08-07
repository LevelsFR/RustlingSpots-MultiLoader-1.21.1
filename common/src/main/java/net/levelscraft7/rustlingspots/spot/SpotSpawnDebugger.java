package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.compat.CobblemonRaidDensCompat;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Simulates automatic spawn attempts for admin diagnostics without creating spots.
 */
public final class SpotSpawnDebugger {
    public static final int DEFAULT_ATTEMPTS = 14;

    private SpotSpawnDebugger() {
    }

    public static Report inspect(ServerLevel level, ServerPlayer player, int attempts) {
        int radius = RustlingSpotsServerConfig.GENERAL.playerSpotRadius();
        int minDistance = Math.min(20, Math.max(8, radius / 4));

        if (!RustlingSpotsServerConfig.GENERAL.enabled()) {
            return Report.blocked("Rustling Spots is disabled.", attempts, radius, minDistance);
        }
        if (player.isSpectator()) {
            return Report.blocked("Player is spectator.", attempts, radius, minDistance);
        }
        if (!CobblemonRaidDensCompat.shouldAllowSpots(level)) {
            return Report.blocked("Rustling Spots is disabled in this dimension by the Cobblemon Raid Dens config.", attempts, radius, minDistance);
        }
        int maxTotal = RustlingSpotsServerConfig.GENERAL.maxSpotsTotal();
        if (RustlingSpotService.MANAGER.totalCount() >= maxTotal) {
            return Report.blocked("Max total spots reached (" + maxTotal + ").", attempts, radius, minDistance);
        }

        int perPlayer = RustlingSpotsServerConfig.GENERAL.maxSpotsPerPlayer();
        long nearby = RustlingSpotService.MANAGER.countWithin(level.dimension(), player.blockPosition(), radius);
        if (nearby >= perPlayer) {
            return Report.blocked("Player already has " + nearby + " nearby spot(s), max is " + perPlayer + ".", attempts, radius, minDistance);
        }

        RandomSource random = RandomSource.create(level.getSeed() ^ player.getUUID().getMostSignificantBits() ^ level.getGameTime());
        EnumMap<Reason, Integer> counts = new EnumMap<>(Reason.class);
        List<Attempt> details = new ArrayList<>();
        Attempt firstSpawnable = null;

        for (int attempt = 0; attempt < attempts; attempt++) {
            Attempt result = inspectAttempt(level, player, random, minDistance, radius, attempt);
            counts.merge(result.reason(), 1, Integer::sum);
            details.add(result);
            if (firstSpawnable == null && result.reason() == Reason.SPAWNABLE) {
                firstSpawnable = result;
            }
        }

        return new Report(false, null, attempts, radius, minDistance, Map.copyOf(counts), List.copyOf(details), firstSpawnable);
    }

    private static Attempt inspectAttempt(ServerLevel level, ServerPlayer player, RandomSource random, int minDistance, int maxDistance, int attempt) {
        double angle = random.nextDouble() * (Math.PI * 2.0D);
        double dist = minDistance + random.nextDouble() * (maxDistance - minDistance);

        int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * dist);
        int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * dist);

        Candidate candidate = findCandidate(level, x, z);
        if (candidate.pos() == null) {
            return new Attempt(attempt, null, candidate.reason(), candidate.detail());
        }

        double distance = player.position().distanceTo(Vec3.atCenterOf(candidate.pos()));
        if (distance < minDistance || distance > maxDistance) {
            return new Attempt(attempt, candidate.pos(), Reason.OUTSIDE_DISTANCE_BAND,
                    "distance=" + format(distance) + ", expected " + minDistance + "-" + maxDistance);
        }

        ChunkPos chunkPos = new ChunkPos(candidate.pos());
        if (RustlingSpotService.MANAGER.hasSpotInChunk(level.dimension(), chunkPos)) {
            return new Attempt(attempt, candidate.pos(), Reason.SPOT_ALREADY_IN_CHUNK,
                    "chunk=" + chunkPos.x + "," + chunkPos.z);
        }

        LevelChunk chunk = getLoadedChunk(level, candidate.pos().getX(), candidate.pos().getZ());
        if (chunk == null) {
            return new Attempt(attempt, candidate.pos(), Reason.UNLOADED_CHUNK, "candidate chunk became unloaded before template resolution");
        }

        BlockPos groundPos = candidate.pos().below();
        BlockState groundState = chunk.getBlockState(groundPos);
        Optional<RustlingSpotFamily> builtInFamily = RustlingSpotFamily.fromSurfaceBlock(groundState, level, groundPos);
        boolean hasCustomMatch = !CustomSpotDefinitionRegistry.matching(level, groundPos, groundState).isEmpty();
        Optional<SpotSpawnTemplate> selected = SpotSpawnSelectionService.resolve(level, groundPos, groundState, random);
        if (selected.isEmpty()) {
            Reason reason = builtInFamily.isPresent() || hasCustomMatch
                    ? Reason.SPAWN_RATE_ROLL_FAILED
                    : Reason.NO_SPOT_DEFINITION;
            return new Attempt(attempt, candidate.pos(), reason,
                    "block=" + blockId(groundState) + ", biome=" + biomeId(level, groundPos));
        }

        double minDist = RustlingSpotsServerConfig.GENERAL.minDistanceBetweenSpots();
        double minDistanceSq = minDist * minDist;
        if (!RustlingSpotService.MANAGER.isFarEnough(level.dimension(), minDistanceSq, candidate.pos())) {
            double nearestSq = RustlingSpotService.MANAGER.nearestDistanceSq(level.dimension(), candidate.pos());
            return new Attempt(attempt, candidate.pos(), Reason.TOO_CLOSE_TO_SPOT,
                    "nearestSq=" + format(nearestSq) + ", requiredSq=" + format(minDistanceSq));
        }

        return new Attempt(attempt, candidate.pos(), Reason.SPAWNABLE,
                "spotId=" + selected.get().spotId() + ", block=" + blockId(groundState) + ", biome=" + biomeId(level, groundPos));
    }

    private static Candidate findCandidate(ServerLevel level, int x, int z) {
        Candidate surface = findSurfaceUsingHeightmap(level, x, z);
        if (surface.pos() != null || surface.reason() == Reason.UNLOADED_CHUNK) {
            return surface;
        }

        return level.dimension().equals(Level.OVERWORLD)
                ? findCaveAirAboveConfiguredBlock(level, x, z)
                : findAnyValidAirAboveConfiguredBlock(level, x, z);
    }

    private static Candidate findSurfaceUsingHeightmap(ServerLevel level, int x, int z) {
        LevelChunk chunk = getLoadedChunk(level, x, z);
        if (chunk == null) {
            return Candidate.rejected(Reason.UNLOADED_CHUNK, "chunk=" + (x >> 4) + "," + (z >> 4));
        }

        int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15) + 1;
        if (y <= level.getMinBuildHeight() + 1 || y >= level.getMaxBuildHeight() - 1) {
            return Candidate.rejected(Reason.HEIGHTMAP_OUT_OF_BOUNDS, "y=" + y);
        }

        BlockPos airPos = new BlockPos(x, y, z);
        BlockPos groundPos = airPos.below();
        if (!chunk.getBlockState(airPos).isAir()) {
            return Candidate.rejected(Reason.SURFACE_AIR_BLOCKED, "airPos=" + posText(airPos));
        }

        BlockState groundState = chunk.getBlockState(groundPos);
        if (RustlingSpotFamily.fromSurfaceBlock(groundState, level, groundPos).isPresent()) {
            return Candidate.found(airPos);
        }

        return Candidate.rejected(Reason.NO_SURFACE_OR_CAVE_MATCH, "surface block=" + blockId(groundState));
    }

    private static Candidate findCaveAirAboveConfiguredBlock(ServerLevel level, int x, int z) {
        LevelChunk chunk = getLoadedChunk(level, x, z);
        if (chunk == null) {
            return Candidate.rejected(Reason.UNLOADED_CHUNK, "chunk=" + (x >> 4) + "," + (z >> 4));
        }

        int minY = level.getMinBuildHeight();
        int maxY = Math.min(level.getMaxBuildHeight() - 2, level.getSeaLevel() - 5);
        for (int y = maxY; y >= minY; y--) {
            BlockPos groundPos = new BlockPos(x, y, z);
            BlockPos airPos = groundPos.above();
            if (!chunk.getBlockState(airPos).isAir()) {
                continue;
            }

            BlockState groundState = chunk.getBlockState(groundPos);
            Optional<RustlingSpotFamily> family = RustlingSpotFamily.fromSurfaceBlock(groundState, level, groundPos);
            if (family.isPresent() && family.get() == RustlingSpotFamily.CAVE) {
                return Candidate.found(airPos);
            }
        }
        return Candidate.rejected(Reason.NO_SURFACE_OR_CAVE_MATCH, "no cave air above configured block");
    }

    private static Candidate findAnyValidAirAboveConfiguredBlock(ServerLevel level, int x, int z) {
        LevelChunk chunk = getLoadedChunk(level, x, z);
        if (chunk == null) {
            return Candidate.rejected(Reason.UNLOADED_CHUNK, "chunk=" + (x >> 4) + "," + (z >> 4));
        }

        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 2;
        for (int y = maxY; y >= minY; y--) {
            BlockPos groundPos = new BlockPos(x, y, z);
            BlockPos airPos = groundPos.above();
            if (!chunk.getBlockState(airPos).isAir()) {
                continue;
            }

            BlockState groundState = chunk.getBlockState(groundPos);
            if (RustlingSpotFamily.fromSurfaceBlock(groundState, level, groundPos).isPresent()) {
                return Candidate.found(airPos);
            }
        }
        return Candidate.rejected(Reason.NO_SURFACE_OR_CAVE_MATCH, "no valid air above configured block");
    }

    private static LevelChunk getLoadedChunk(ServerLevel level, int blockX, int blockZ) {
        return level.getChunkSource().getChunkNow(blockX >> 4, blockZ >> 4);
    }

    private static String blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private static String biomeId(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos).unwrapKey().map(key -> key.location().toString()).orElse("<unbound>");
    }

    private static String posText(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private record Candidate(BlockPos pos, Reason reason, String detail) {
        static Candidate found(BlockPos pos) {
            return new Candidate(pos, Reason.SPAWNABLE, "");
        }

        static Candidate rejected(Reason reason, String detail) {
            return new Candidate(null, reason, detail);
        }
    }

    public record Attempt(int index, BlockPos pos, Reason reason, String detail) {
    }

    public record Report(
            boolean blocked,
            String blockedReason,
            int attempts,
            int radius,
            int minDistance,
            Map<Reason, Integer> counts,
            List<Attempt> attemptsDetails,
            Attempt firstSpawnable
    ) {
        static Report blocked(String reason, int attempts, int radius, int minDistance) {
            return new Report(true, reason, attempts, radius, minDistance, Map.of(), List.of(), null);
        }
    }

    public enum Reason {
        SPAWNABLE("spawnable"),
        UNLOADED_CHUNK("unloaded chunk"),
        HEIGHTMAP_OUT_OF_BOUNDS("heightmap out of bounds"),
        SURFACE_AIR_BLOCKED("surface air blocked"),
        NO_SURFACE_OR_CAVE_MATCH("no matching surface/cave column"),
        OUTSIDE_DISTANCE_BAND("outside distance band"),
        SPOT_ALREADY_IN_CHUNK("spot already in chunk"),
        NO_SPOT_DEFINITION("no matching spot definition"),
        SPAWN_RATE_ROLL_FAILED("spawn-rate roll failed"),
        TOO_CLOSE_TO_SPOT("too close to another spot");

        private final String label;

        Reason(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }
}
