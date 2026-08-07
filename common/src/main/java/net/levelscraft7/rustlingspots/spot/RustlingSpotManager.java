package net.levelscraft7.rustlingspots.spot;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central registry for active rustling spots per dimension.
 */
public class RustlingSpotManager {
    private final Map<ResourceKey<Level>, DimensionIndex> perDimension = new ConcurrentHashMap<>();
    private final AtomicInteger totalSpots = new AtomicInteger();

    public Collection<RustlingSpot> getAll(ResourceKey<Level> dimension) {
        return perDimension.getOrDefault(dimension, DimensionIndex.EMPTY).allSpots();
    }

    public Optional<RustlingSpot> get(ResourceKey<Level> dimension, UUID id) {
        return Optional.ofNullable(perDimension.getOrDefault(dimension, DimensionIndex.EMPTY).get(id));
    }

    public void add(RustlingSpot spot) {
        DimensionIndex index = perDimension.computeIfAbsent(spot.getDimension(), ignored -> new DimensionIndex());
        RustlingSpot previous = index.put(spot);
        if (previous == null) {
            totalSpots.incrementAndGet();
        }
    }

    public void remove(RustlingSpot spot) {
        DimensionIndex index = perDimension.get(spot.getDimension());
        if (index == null) {
            return;
        }

        RustlingSpot removed = index.remove(spot.getId());
        if (removed != null) {
            totalSpots.decrementAndGet();
        }

        if (index.isEmpty()) {
            perDimension.remove(spot.getDimension(), index);
        }
    }

    public boolean hasSpotInChunk(ResourceKey<Level> dimension, ChunkPos chunkPos) {
        return perDimension.getOrDefault(dimension, DimensionIndex.EMPTY).hasSpotInChunk(chunkPos);
    }

    public int totalCount() {
        return totalSpots.get();
    }

    public boolean isFarEnough(ResourceKey<Level> dimension, double minDistanceSq, BlockPos position) {
        double searchRadius = Math.sqrt(minDistanceSq);
        for (RustlingSpot existing : nearbySpots(dimension, position, searchRadius)) {
            if (existing.getPosition().distSqr(position) < minDistanceSq) {
                return false;
            }
        }
        return true;
    }

    public double nearestDistanceSq(ResourceKey<Level> dimension, BlockPos position) {
        DimensionIndex index = perDimension.get(dimension);
        if (index == null || index.isEmpty()) {
            return -1D;
        }

        double nearest = Double.MAX_VALUE;
        boolean found = false;
        for (RustlingSpot existing : nearbySpots(dimension, position, Double.MAX_VALUE)) {
            double distanceSq = existing.getPosition().distSqr(position);
            if (distanceSq < nearest) {
                nearest = distanceSq;
                found = true;
            }
        }
        return found ? nearest : -1D;
    }

    public long countWithin(ResourceKey<Level> dimension, BlockPos position, double radius) {
        double radiusSq = radius * radius;
        long count = 0L;
        for (RustlingSpot existing : nearbySpots(dimension, position, radius)) {
            if (existing.getPosition().distSqr(position) <= radiusSq) {
                count++;
            }
        }
        return count;
    }

    private Iterable<RustlingSpot> nearbySpots(ResourceKey<Level> dimension, BlockPos position, double radius) {
        DimensionIndex index = perDimension.get(dimension);
        if (index == null || index.isEmpty()) {
            return List.of();
        }

        if (radius == Double.MAX_VALUE) {
            return index.allSpots();
        }

        int chunkRadius = Math.max(0, (int) Math.ceil(radius / 16.0D));
        ChunkPos origin = new ChunkPos(position);
        List<RustlingSpot> candidates = new ArrayList<>();
        for (int chunkX = origin.x - chunkRadius; chunkX <= origin.x + chunkRadius; chunkX++) {
            for (int chunkZ = origin.z - chunkRadius; chunkZ <= origin.z + chunkRadius; chunkZ++) {
                candidates.addAll(index.spotsInChunk(chunkX, chunkZ));
            }
        }
        return candidates;
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL);
    }

    private static final class DimensionIndex {
        private static final DimensionIndex EMPTY = new DimensionIndex(true);

        private final Map<UUID, RustlingSpot> byId;
        private final Map<Long, Map<UUID, RustlingSpot>> byChunk;
        private final boolean readOnly;

        private DimensionIndex() {
            this(false);
        }

        private DimensionIndex(boolean readOnly) {
            this.byId = new ConcurrentHashMap<>();
            this.byChunk = new ConcurrentHashMap<>();
            this.readOnly = readOnly;
        }

        Collection<RustlingSpot> allSpots() {
            return byId.values();
        }

        RustlingSpot get(UUID id) {
            return byId.get(id);
        }

        RustlingSpot put(RustlingSpot spot) {
            if (readOnly) {
                throw new UnsupportedOperationException("Cannot mutate empty spot index");
            }

            RustlingSpot previous = byId.put(spot.getId(), spot);
            if (previous != null) {
                removeFromChunk(previous);
            }

            byChunk.computeIfAbsent(chunkKey(new ChunkPos(spot.getPosition()).x, new ChunkPos(spot.getPosition()).z),
                            ignored -> new ConcurrentHashMap<>())
                    .put(spot.getId(), spot);
            return previous;
        }

        RustlingSpot remove(UUID id) {
            if (readOnly) {
                return null;
            }

            RustlingSpot removed = byId.remove(id);
            if (removed != null) {
                removeFromChunk(removed);
            }
            return removed;
        }

        boolean hasSpotInChunk(ChunkPos chunkPos) {
            Map<UUID, RustlingSpot> spots = byChunk.get(chunkKey(chunkPos.x, chunkPos.z));
            return spots != null && !spots.isEmpty();
        }

        Collection<RustlingSpot> spotsInChunk(int chunkX, int chunkZ) {
            Map<UUID, RustlingSpot> spots = byChunk.get(chunkKey(chunkX, chunkZ));
            return spots != null ? spots.values() : List.of();
        }

        boolean isEmpty() {
            return byId.isEmpty();
        }

        private void removeFromChunk(RustlingSpot spot) {
            ChunkPos chunkPos = new ChunkPos(spot.getPosition());
            long key = chunkKey(chunkPos.x, chunkPos.z);
            Map<UUID, RustlingSpot> spots = byChunk.get(key);
            if (spots == null) {
                return;
            }

            spots.remove(spot.getId());
            if (spots.isEmpty()) {
                byChunk.remove(key, spots);
            }
        }
    }
}
