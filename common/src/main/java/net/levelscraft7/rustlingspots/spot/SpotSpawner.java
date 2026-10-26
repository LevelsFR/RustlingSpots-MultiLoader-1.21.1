package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.compat.CobblemonRaidDensCompat;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Responsible for spawning new rustling spots during server ticks.
 *
 * New model (v1.8):
 * - One spawn attempt per eligible player when they enter a new chunk, plus when they submerge in the current chunk.
 * - Underwater candidates are preferred while submerged and sampled rarely from outside the water.
 * - Spots can spawn in any dimension, as long as a family matches surface block + biome rules.
 * - One radius value controls both spawn zone and despawn zone.
 */
public class SpotSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotSpawner.class);
    private static final int INTERNAL_MIN_SPAWN_DISTANCE = 20;
    private static final double CANOPY_SEARCH_CHANCE = 0.35D;
    private static final double GEODE_SEARCH_CHANCE = 0.2D;
    private static final double UNDERGROUND_SURFACE_FALLBACK_CHANCE = 0.1D;
    private static final int CANOPY_SEARCH_DEPTH = 32;
    private static final int GEODE_SEARCH_COLUMN_SAMPLES = 256;
    private static final int GEODE_SEARCH_MAX_COLUMN_ATTEMPTS = 1_024;

    private final Map<UUID, PlayerSpawnTrigger> lastPlayerSpawnTrigger = new HashMap<>();

    public void forgetPlayer(UUID playerId) {
        lastPlayerSpawnTrigger.remove(playerId);
    }

    public void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) {
            return;
        }

        if (!RustlingSpotsServerConfig.GENERAL.enabled()) {
            return;
        }

        int maxTotal = RustlingSpotsServerConfig.GENERAL.maxSpotsTotal();
        if (RustlingSpotService.MANAGER.totalCount() >= maxTotal) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (RustlingSpotService.MANAGER.totalCount() >= maxTotal) {
                return;
            }

            if (player.isSpectator()) {
                continue;
            }

            ServerLevel level = player.serverLevel();
            if (!CobblemonRaidDensCompat.shouldAllowSpots(level)) {
                continue;
            }

            long key = chunkKey(level.dimension().location().toString(), player.chunkPosition());
            boolean underwater = player.isEyeInFluid(FluidTags.WATER);
            PlayerSpawnTrigger last = lastPlayerSpawnTrigger.get(player.getUUID());
            if (last != null && last.chunkKey() == key) {
                boolean justSubmerged = underwater && !last.underwater();
                lastPlayerSpawnTrigger.put(player.getUUID(), new PlayerSpawnTrigger(key, underwater));
                if (!justSubmerged) {
                    continue;
                }
            } else {
                lastPlayerSpawnTrigger.put(player.getUUID(), new PlayerSpawnTrigger(key, underwater));
            }

            int radius = RustlingSpotsServerConfig.GENERAL.playerSpotRadius();
            int perPlayer = RustlingSpotsServerConfig.GENERAL.maxSpotsPerPlayer();

            long nearby = RustlingSpotService.MANAGER.countWithin(level.dimension(), player.blockPosition(), radius);
            if (nearby >= perPlayer) {
                continue;
            }

            trySpawnOneNearPlayer(level, player);
        }
    }

    private static long chunkKey(String dimensionId, ChunkPos pos) {
        long packed = (((long) pos.x) & 0xffffffffL) << 32 | (((long) pos.z) & 0xffffffffL);
        return packed ^ (long) dimensionId.hashCode();
    }

    private record PlayerSpawnTrigger(long chunkKey, boolean underwater) {
    }

    record GeodeSearchResult(BlockPos position, int searchRadius, int sampledColumns, int loadedColumns,
                             int buddingBlocks, int openSurfaces, int inRangeCandidates) {
        String summary() {
            return "geodeSearch={searchRadius=" + searchRadius
                    + ", targetLoadedColumns=" + GEODE_SEARCH_COLUMN_SAMPLES
                    + ", sampledColumns=" + sampledColumns
                    + ", loadedColumns=" + loadedColumns
                    + ", buddingBlocks=" + buddingBlocks
                    + ", openSurfaces=" + openSurfaces
                    + ", inRangeCandidates=" + inRangeCandidates + "}";
        }
    }

    private void trySpawnOneNearPlayer(ServerLevel level, ServerPlayer player) {
        int radius = RustlingSpotsServerConfig.GENERAL.playerSpotRadius();
        int minDistance = Math.min(INTERNAL_MIN_SPAWN_DISTANCE, Math.max(8, radius / 4));
        boolean playerSubmerged = player.isEyeInFluid(FluidTags.WATER);
        boolean playerUnderground = isUndergroundPlayer(level, player);

        RandomSource random = level.random;
        boolean underwaterSearch = playerSubmerged
                || random.nextDouble() < RustlingSpotsServerConfig.GENERAL.outsideWaterUnderwaterChance();
        boolean allowUndergroundSurfaceFallback = playerUnderground && shouldAllowUndergroundSurfaceFallback(random);

        for (int attempt = 0; attempt < 14; attempt++) {
            BlockPos candidate = pickRandomSurfaceNear(level, player, random, minDistance, radius,
                    playerSubmerged, underwaterSearch, playerUnderground, allowUndergroundSurfaceFallback);
            if (candidate == null) {
                if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    LOGGER.info("[SpotSpawnDebug] Attempt {}: no candidate found near player {} (playerSubmerged={}, playerUnderground={}, underwaterSearch={})",
                            attempt, player.getUUID(), playerSubmerged, playerUnderground, underwaterSearch);
                }
                continue;
            }

            ChunkPos chunkPos = new ChunkPos(candidate);
            if (RustlingSpotService.MANAGER.hasSpotInChunk(level.dimension(), chunkPos)) {
                continue;
            }

            LevelChunk candidateChunk = getLoadedChunk(level, candidate.getX(), candidate.getZ());
            if (candidateChunk == null) {
                continue;
            }

            boolean underwater = candidateChunk.getBlockState(candidate).is(Blocks.WATER);
            BlockPos definitionPos = underwater ? candidate : candidate.below();
            BlockState definitionBlock = candidateChunk.getBlockState(definitionPos);
            Optional<RustlingSpotFamily> builtInFamily = RustlingSpotFamily.fromSurfaceBlock(definitionBlock, level, definitionPos);
            boolean customDefinitionMatched = !CustomSpotDefinitionRegistry.matching(level, definitionPos, definitionBlock).isEmpty();
            Optional<SpotSpawnTemplate> selectedTemplate = SpotSpawnSelectionService.resolve(level, definitionPos, definitionBlock, random);
            if (selectedTemplate.isEmpty()) {
                if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    String reason = builtInFamily.isPresent() || customDefinitionMatched
                            ? "no template was selected after spawn-rate/weight checks"
                            : "no built-in family or custom definition matched";
                    LOGGER.info("[SpotSpawnDebug] Candidate {} not selected: {} (block={}, family={}, customDefinitionMatched={})",
                            candidate, reason, blockId(definitionBlock),
                            builtInFamily.map(RustlingSpotFamily::serializedName).orElse("none"), customDefinitionMatched);
                }
                continue;
            }

            SpotSpawnTemplate template = selectedTemplate.get();

            double minDist = RustlingSpotsServerConfig.GENERAL.minDistanceBetweenSpots();
            double minDistanceSq = minDist * minDist;
            if (!RustlingSpotService.MANAGER.isFarEnough(level.dimension(), minDistanceSq, candidate)) {
                if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    double nearestSq = RustlingSpotService.MANAGER.nearestDistanceSq(level.dimension(), candidate);
                    LOGGER.info("[SpotSpawnDebug] Candidate {} resolved to {} but was too close to another spot (nearestSq={})",
                            candidate, template.spotId(), nearestSq);
                }
                continue;
            }

            UUID id = UUID.randomUUID();
            int variant = RustlingSoundResolver.indexForFamily(template.visualFamily());
            boolean shiny = random.nextDouble() < RustlingSpotsServerConfig.GENERAL.shinySpotChance();
            RustlingSpot spot = template.createSpot(id, level.dimension(), candidate, level.getGameTime(), variant, shiny);

            RustlingSpotService.MANAGER.add(spot);
            RustlingSpotSyncService.broadcastSpawn(level, spot);

            if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                LOGGER.info("[Spawn] Spawned spot at {} (spotId={}, family={}, block={}, biome={}, playerSubmerged={}, playerUnderground={}, underwaterSearch={})",
                        candidate,
                        template.spotId(),
                        template.visualFamily().serializedName(),
                        blockId(definitionBlock),
                        level.getBiome(definitionPos).unwrapKey().map(key -> key.location().toString()).orElse("<unbound>"),
                        playerSubmerged,
                        playerUnderground,
                        underwaterSearch);
            }
            return;
        }
    }

    private String blockId(BlockState state) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private BlockPos pickRandomSurfaceNear(ServerLevel level, ServerPlayer player, RandomSource random,
                                           int minDistance, int maxDistance, boolean playerSubmerged, boolean underwaterSearch,
                                           boolean playerUnderground, boolean allowUndergroundSurfaceFallback) {
        double angle = random.nextDouble() * (Math.PI * 2.0D);
        double dist = minDistance + random.nextDouble() * (maxDistance - minDistance);

        int dx = (int) Math.round(Math.cos(angle) * dist);
        int dz = (int) Math.round(Math.sin(angle) * dist);

        int x = player.blockPosition().getX() + dx;
        int z = player.blockPosition().getZ() + dz;

        BlockPos target = underwaterSearch
                ? findRandomUnderwaterCandidate(level, player, x, z, random, minDistance, maxDistance)
                : null;
        if (target == null && !playerSubmerged) {
            if (playerUnderground || shouldSearchGeode(random)) {
                GeodeSearchResult geodeSearch = searchRandomGeodeCandidates(level, player, random, minDistance, maxDistance);
                target = geodeSearch.position();
                if (target == null && level.dimension().equals(Level.OVERWORLD)
                        && RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    LOGGER.info("[SpotSpawnDebug] Geode scan found no valid position near x={}, z={}: {}",
                            x, z, geodeSearch.summary());
                }
            }
        }
        if (target == null && !playerSubmerged && playerUnderground) {
            target = findRandomCaveCandidate(level, player, x, z, random, minDistance, maxDistance);
        }
        if (target == null && !playerSubmerged && !playerUnderground) {
            if (shouldSearchCanopy(random)) {
                target = findRandomCanopyCandidate(level, x, z, random);
            }
        }
        if (target == null && !playerSubmerged && playerUnderground && allowUndergroundSurfaceFallback
                && shouldSearchCanopy(random)) {
            target = findRandomCanopyCandidate(level, x, z, random);
        }
        if (target == null && !playerSubmerged && (!playerUnderground || allowUndergroundSurfaceFallback)) {
            target = findSurfaceUsingHeightmap(level, x, z);
        }
        if (target == null && !playerSubmerged && !playerUnderground) {
            if (level.dimension().equals(Level.OVERWORLD)) {
                target = findRandomCaveCandidate(level, player, x, z, random, minDistance, maxDistance);
            } else {
                target = findAnyValidAirAboveConfiguredBlock(level, x, z);
            }
        }

        if (target == null) {
            return null;
        }

        double distance = player.position().distanceTo(Vec3.atCenterOf(target));
        if (distance < minDistance || distance > maxDistance) {
            return null;
        }

        return target;
    }

    static boolean shouldSearchCanopy(RandomSource random) {
        return random.nextDouble() < CANOPY_SEARCH_CHANCE;
    }

    static boolean shouldSearchGeode(RandomSource random) {
        return random.nextDouble() < GEODE_SEARCH_CHANCE;
    }

    static boolean shouldAllowUndergroundSurfaceFallback(RandomSource random) {
        return random.nextDouble() < UNDERGROUND_SURFACE_FALLBACK_CHANCE;
    }

    static boolean isUndergroundPlayer(ServerLevel level, ServerPlayer player) {
        if (!level.dimension().equals(Level.OVERWORLD) || player.isEyeInFluid(FluidTags.WATER)) {
            return false;
        }

        BlockPos playerPos = player.blockPosition();
        if (level.canSeeSkyFromBelowWater(playerPos)) {
            return false;
        }

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, playerPos.getX(), playerPos.getZ());
        while (surfaceY > level.getMinBuildHeight()) {
            BlockState surfaceState = level.getBlockState(new BlockPos(playerPos.getX(), surfaceY, playerPos.getZ()));
            if (!surfaceState.is(BlockTags.LEAVES) && !surfaceState.is(BlockTags.LOGS)) {
                break;
            }
            surfaceY--;
        }
        int depthBelowSurface = surfaceY - playerPos.getY();
        return depthBelowSurface > 24
                || (playerPos.getY() < level.getSeaLevel() - 5 && depthBelowSurface > 8);
    }

    static BlockPos findRandomCanopyCandidate(ServerLevel level, int x, int z, RandomSource random) {
        LevelChunk chunk = getLoadedChunk(level, x, z);
        if (chunk == null) {
            return null;
        }

        int surfaceY = Math.min(level.getMaxBuildHeight() - 3,
                chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15));
        int minY = Math.max(level.getMinBuildHeight(), surfaceY - CANOPY_SEARCH_DEPTH);
        BlockPos selected = null;
        int matchingPositions = 0;
        for (int supportY = surfaceY; supportY >= minY; supportY--) {
            BlockPos supportPos = new BlockPos(x, supportY, z);
            BlockPos spotPos = supportPos.above();
            if (!chunk.getBlockState(spotPos).isAir() || !chunk.getBlockState(spotPos.above()).isAir()) {
                continue;
            }

            BlockState supportState = chunk.getBlockState(supportPos);
            Optional<RustlingSpotFamily> family = RustlingSpotFamily.fromSurfaceBlock(supportState, level, supportPos);
            if (family.isEmpty() || family.get() != RustlingSpotFamily.LEAVES || countNearbyLeaves(chunk, supportPos) < 2) {
                continue;
            }

            matchingPositions++;
            if (random.nextInt(matchingPositions) == 0) {
                selected = spotPos;
            }
        }
        return selected;
    }

    private static int countNearbyLeaves(LevelChunk chunk, BlockPos supportPos) {
        int count = 0;
        int chunkX = supportPos.getX() >> 4;
        int chunkZ = supportPos.getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if ((dx == 0 && dz == 0)
                        || ((supportPos.getX() + dx) >> 4) != chunkX
                        || ((supportPos.getZ() + dz) >> 4) != chunkZ) {
                    continue;
                }
                if (chunk.getBlockState(supportPos.offset(dx, 0, dz)).is(BlockTags.LEAVES)) {
                    count++;
                }
            }
        }
        return count;
    }

    static BlockPos findRandomUnderwaterCandidate(ServerLevel level, ServerPlayer player, int x, int z,
                                                   RandomSource random, int minDistance, int maxDistance) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        if (chunk == null) {
            return null;
        }

        BlockPos selected = null;
        int matchingWaterBlocks = 0;
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight() - 1; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = chunk.getBlockState(pos);
            BlockState above = chunk.getBlockState(pos.above());
            if (!state.is(Blocks.WATER) || !state.getFluidState().isSource()
                    || !above.is(Blocks.WATER) || !above.getFluidState().isSource()) {
                continue;
            }

            double distance = player.position().distanceTo(Vec3.atCenterOf(pos));
            if (distance < minDistance || distance > maxDistance) {
                continue;
            }

            matchingWaterBlocks++;
            if (random.nextInt(matchingWaterBlocks) == 0) {
                selected = pos;
            }
        }
        return selected;
    }

    static GeodeSearchResult searchRandomGeodeCandidates(ServerLevel level, ServerPlayer player, RandomSource random,
                                                          int minDistance, int maxDistance) {
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return new GeodeSearchResult(null, 0, 0, 0, 0, 0, 0);
        }

        BlockPos selected = null;
        int matchingPositions = 0;
        int sampledColumns = 0;
        int loadedColumns = 0;
        int buddingBlocks = 0;
        int openSurfaces = 0;
        int inRangeCandidates = 0;
        int minY = level.getMinBuildHeight();
        int maxY = Math.min(level.getMaxBuildHeight() - 2, level.getSeaLevel() - 5);
        BlockPos playerPos = player.blockPosition();
        for (int sample = 0; sample < GEODE_SEARCH_MAX_COLUMN_ATTEMPTS && loadedColumns < GEODE_SEARCH_COLUMN_SAMPLES; sample++) {
            double angle = random.nextDouble() * (Math.PI * 2.0D);
            double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
            int sampleX = playerPos.getX() + (int) Math.round(Math.cos(angle) * distance);
            int sampleZ = playerPos.getZ() + (int) Math.round(Math.sin(angle) * distance);
            sampledColumns++;

            LevelChunk chunk = getLoadedChunk(level, sampleX, sampleZ);
            if (chunk == null) {
                continue;
            }

            loadedColumns++;
            for (int y = minY; y <= maxY; y++) {
                BlockPos supportPos = new BlockPos(sampleX, y, sampleZ);
                BlockState support = chunk.getBlockState(supportPos);
                if (!support.is(Blocks.BUDDING_AMETHYST)) {
                    continue;
                }
                buddingBlocks++;

                BlockPos spotPos = supportPos.above();
                if (!chunk.getBlockState(spotPos).isAir() || !chunk.getBlockState(spotPos.above()).isAir()) {
                    continue;
                }

                Optional<RustlingSpotFamily> family = RustlingSpotFamily.fromSurfaceBlock(support, level, supportPos);
                if (family.isEmpty() || family.get() != RustlingSpotFamily.GEODE) {
                    continue;
                }
                openSurfaces++;

                double candidateDistance = player.position().distanceTo(Vec3.atCenterOf(spotPos));
                if (candidateDistance < minDistance || candidateDistance > maxDistance) {
                    continue;
                }
                inRangeCandidates++;

                matchingPositions++;
                if (random.nextInt(matchingPositions) == 0) {
                    selected = spotPos;
                }
            }
        }
        return new GeodeSearchResult(selected, maxDistance, sampledColumns, loadedColumns,
                buddingBlocks, openSurfaces, inRangeCandidates);
    }

    static BlockPos findRandomCaveCandidate(ServerLevel level, ServerPlayer player, int x, int z,
                                            RandomSource random, int minDistance, int maxDistance) {
        LevelChunk chunk = getLoadedChunk(level, x, z);
        if (chunk == null) {
            return null;
        }

        int minY = level.getMinBuildHeight();
        int seaLevel = level.getSeaLevel();
        int maxY = Math.min(level.getMaxBuildHeight() - 2, seaLevel - 5);
        BlockPos selected = null;
        int matchingPositions = 0;

        for (int y = maxY; y >= minY; y--) {
            BlockPos groundPos = new BlockPos(x, y, z);
            BlockPos airPos = groundPos.above();
            if (!chunk.getBlockState(airPos).isAir()) {
                continue;
            }

            BlockState groundState = chunk.getBlockState(groundPos);
            Optional<RustlingSpotFamily> family = RustlingSpotFamily.fromSurfaceBlock(groundState, level, groundPos);
            if (family.isEmpty() || family.get() != RustlingSpotFamily.CAVE) {
                continue;
            }

            double distance = player.position().distanceTo(Vec3.atCenterOf(airPos));
            if (distance < minDistance || distance > maxDistance) {
                continue;
            }

            matchingPositions++;
            if (random.nextInt(matchingPositions) == 0) {
                selected = airPos;
            }
        }

        return selected;
    }

    private BlockPos findAnyValidAirAboveConfiguredBlock(ServerLevel level, int x, int z) {
        LevelChunk chunk = getLoadedChunk(level, x, z);
        if (chunk == null) {
            return null;
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
                return airPos;
            }
        }

        return null;
    }

    private BlockPos findSurfaceUsingHeightmap(ServerLevel level, int x, int z) {
        LevelChunk chunk = getLoadedChunk(level, x, z);
        if (chunk == null) {
            return null;
        }

        // ChunkAccess#getHeight returns the top blocking block Y in 1.21.1; the spot belongs in the first free block above it.
        int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15) + 1;
        if (y <= level.getMinBuildHeight() + 1 || y >= level.getMaxBuildHeight() - 1) {
            return null;
        }

        BlockPos airPos = new BlockPos(x, y, z);
        BlockPos groundPos = airPos.below();

        if (!chunk.getBlockState(airPos).isAir()) {
            return null;
        }

        BlockState groundState = chunk.getBlockState(groundPos);
        if (RustlingSpotFamily.fromSurfaceBlock(groundState, level, groundPos).isPresent()) {
            return airPos;
        }

        return null;
    }

    private static LevelChunk getLoadedChunk(ServerLevel level, int blockX, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        // Automatic spot searches must only inspect chunks that are already loaded;
        // never call APIs here that can ticket, block on, load, or generate a chunk.
        return level.getChunkSource().getChunkNow(chunkX, chunkZ);
    }
}
