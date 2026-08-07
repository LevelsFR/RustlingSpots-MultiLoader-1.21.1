package net.levelscraft7.rustlingspots.spot;

import net.levelscraft7.rustlingspots.compat.CobblemonRaidDensCompat;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
 * - One spawn attempt per eligible player, only when they move to a new chunk.
 * - Spots can spawn in any dimension, as long as a family matches surface block + biome rules.
 * - One radius value controls both spawn zone and despawn zone.
 */
public class SpotSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotSpawner.class);
    private static final int INTERNAL_MIN_SPAWN_DISTANCE = 20;

    private final Map<UUID, Long> lastPlayerChunkKey = new HashMap<>();

    public void forgetPlayer(UUID playerId) {
        lastPlayerChunkKey.remove(playerId);
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
            Long last = lastPlayerChunkKey.get(player.getUUID());
            if (last != null && last == key) {
                continue;
            }
            lastPlayerChunkKey.put(player.getUUID(), key);

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

    private void trySpawnOneNearPlayer(ServerLevel level, ServerPlayer player) {
        int radius = RustlingSpotsServerConfig.GENERAL.playerSpotRadius();
        int minDistance = Math.min(INTERNAL_MIN_SPAWN_DISTANCE, Math.max(8, radius / 4));

        RandomSource random = level.random;

        for (int attempt = 0; attempt < 14; attempt++) {
            BlockPos candidate = pickRandomSurfaceNear(level, player, random, minDistance, radius);
            if (candidate == null) {
                if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    LOGGER.info("[LeavesDebug] Attempt {}: no candidate surface found near player {}", attempt, player.getUUID());
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

            BlockPos groundPos = candidate.below();
            BlockState below = candidateChunk.getBlockState(groundPos);
            Optional<SpotSpawnTemplate> selectedTemplate = SpotSpawnSelectionService.resolve(level, groundPos, below, random);
            if (selectedTemplate.isEmpty()) {
                if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    LOGGER.info("[LeavesDebug] Candidate {} rejected: no rustling spot definition matched block {}", candidate, blockId(below));
                }
                continue;
            }

            SpotSpawnTemplate template = selectedTemplate.get();

            if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                LOGGER.info("[LeavesDebug] Candidate {} block={} biome={} resolvedSpot={}",
                        candidate,
                        blockId(below),
                        level.getBiome(candidate.below()).unwrapKey().map(key -> key.location().toString()).orElse("<unbound>"),
                        template.spotId());
            }

            double minDist = RustlingSpotsServerConfig.GENERAL.minDistanceBetweenSpots();
            double minDistanceSq = minDist * minDist;
            if (!RustlingSpotService.MANAGER.isFarEnough(level.dimension(), minDistanceSq, candidate)) {
                if (RustlingSpotsServerConfig.GENERAL.loggingEnabled()) {
                    double nearestSq = RustlingSpotService.MANAGER.nearestDistanceSq(level.dimension(), candidate);
                    LOGGER.info("[Spawn] Too close to another spot. nearestSq={}", nearestSq);
                    if (template.visualFamily() == RustlingSpotFamily.LEAVES) {
                        LOGGER.info("[LeavesDebug] Candidate {} resolved to LEAVES but was too close to another spot", candidate);
                    }
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
                LOGGER.info("[Spawn] Spawned spot at {} (spotId={})", candidate, template.spotId());
            }
            return;
        }
    }

    private String blockId(BlockState state) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private BlockPos pickRandomSurfaceNear(ServerLevel level, ServerPlayer player, RandomSource random, int minDistance, int maxDistance) {
        double angle = random.nextDouble() * (Math.PI * 2.0D);
        double dist = minDistance + random.nextDouble() * (maxDistance - minDistance);

        int dx = (int) Math.round(Math.cos(angle) * dist);
        int dz = (int) Math.round(Math.sin(angle) * dist);

        int x = player.blockPosition().getX() + dx;
        int z = player.blockPosition().getZ() + dz;

        BlockPos target = findSurfaceUsingHeightmap(level, x, z);
        if (target == null) {
            if (level.dimension().equals(Level.OVERWORLD)) {
                target = findCaveAirAboveConfiguredBlock(level, x, z);
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

    private BlockPos findCaveAirAboveConfiguredBlock(ServerLevel level, int x, int z) {
        LevelChunk chunk = getLoadedChunk(level, x, z);
        if (chunk == null) {
            return null;
        }

        int minY = level.getMinBuildHeight();
        int seaLevel = level.getSeaLevel();
        int maxY = Math.min(level.getMaxBuildHeight() - 2, seaLevel - 5);

        for (int y = maxY; y >= minY; y--) {
            BlockPos groundPos = new BlockPos(x, y, z);
            BlockPos airPos = groundPos.above();
            if (!chunk.getBlockState(airPos).isAir()) {
                continue;
            }

            BlockState groundState = chunk.getBlockState(groundPos);
            Optional<RustlingSpotFamily> family = RustlingSpotFamily.fromSurfaceBlock(groundState, level, groundPos);
            if (family.isPresent() && family.get() == RustlingSpotFamily.CAVE) {
                return airPos;
            }
        }

        return null;
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

    private LevelChunk getLoadedChunk(ServerLevel level, int blockX, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        // Automatic spot searches must only inspect chunks that are already loaded;
        // never call APIs here that can ticket, block on, load, or generate a chunk.
        return level.getChunkSource().getChunkNow(chunkX, chunkZ);
    }
}
