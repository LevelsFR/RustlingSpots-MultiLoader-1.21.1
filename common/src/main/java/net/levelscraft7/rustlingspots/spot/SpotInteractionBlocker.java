package net.levelscraft7.rustlingspots.spot;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks temporary interaction locks for players.
 */
public class SpotInteractionBlocker {
    private final Map<UUID, Long> blockedUntil = new ConcurrentHashMap<>();

    public void blockForTicks(UUID playerId, long currentTick, long delayTicks) {
        blockedUntil.put(playerId, currentTick + delayTicks);
    }

    public boolean isBlocked(UUID playerId, long currentTick) {
        Long until = blockedUntil.get(playerId);
        if (until == null) {
            return false;
        }

        if (currentTick >= until) {
            blockedUntil.remove(playerId);
            return false;
        }
        return true;
    }
}