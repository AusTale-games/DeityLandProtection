package group.austale.deitylandprotection;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player runtime state owned by {@link DeityLandProtectionPlugin}.
 *
 * <p>This was previously a sea of {@code ConcurrentHashMap} fields embedded in
 * the plugin god class. Grouping the maps together makes the lifecycle and
 * concurrency model explicit: every map is concurrent, written from any
 * system thread, and cleared only on plugin shutdown.</p>
 */
final class DeityLandProtectionPlayerStateRegistry {

    /** centerKey(x,z) -> wall-clock millis when the deity block was placed. */
    private final ConcurrentHashMap<Long, Long> recentClaimPlacements = new ConcurrentHashMap<>();
    /** UUID -> last time we sent a rate-limited message to this player. */
    private final ConcurrentHashMap<UUID, Long> lastPlayerMessageMs = new ConcurrentHashMap<>();
    /** UUID -> centerKey of the claim whose border the player has toggled on. */
    private final ConcurrentHashMap<UUID, Long> borderCenterByPlayer = new ConcurrentHashMap<>();
    /** UUID -> last time we re-spawned border particles for this player. */
    private final ConcurrentHashMap<UUID, Long> lastBorderSpawnMsByPlayer = new ConcurrentHashMap<>();
    /** UUID -> world the player was in the last time we drew their border. */
    private final ConcurrentHashMap<UUID, String> borderWorldByPlayer = new ConcurrentHashMap<>();
    /** UUID -> last claim center the player was inside (used for enter/exit edge detection). */
    private final ConcurrentHashMap<UUID, Long> lastZoneKeyByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> knownUsernameByUuid = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UUID> knownUuidByUsername = new ConcurrentHashMap<>();
    /** centerKey -> {playerUuid -> displayName} for everyone currently inside that claim. */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<UUID, String>> playersInClaim = new ConcurrentHashMap<>();

    void markRecentClaimPlacement(int x, int z) {
        this.recentClaimPlacements.put(ChunkKeys.pack(x, z), System.currentTimeMillis());
    }

    boolean shouldIgnoreCenterBreak(int x, int z, long ignoreWindowMs) {
        long key = ChunkKeys.pack(x, z);
        Long ts = this.recentClaimPlacements.get(key);
        if (ts == null) {
            return false;
        }
        long age = System.currentTimeMillis() - ts;
        if (age >= 0L && age < ignoreWindowMs) {
            return true;
        }
        this.recentClaimPlacements.remove(key, ts);
        return false;
    }

    boolean shouldSendRateLimitedMessage(UUID playerUuid, long nowMs, long cooldownMs) {
        Long last = this.lastPlayerMessageMs.get(playerUuid);
        if (last != null && nowMs - last < cooldownMs) {
            return false;
        }
        this.lastPlayerMessageMs.put(playerUuid, nowMs);
        return true;
    }

    void enableBorder(UUID playerUuid, int centerX, int centerZ) {
        if (playerUuid == null) {
            return;
        }
        this.borderCenterByPlayer.put(playerUuid, ChunkKeys.pack(centerX, centerZ));
    }

    void disableBorder(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        this.borderCenterByPlayer.remove(playerUuid);
        this.lastBorderSpawnMsByPlayer.remove(playerUuid);
        this.borderWorldByPlayer.remove(playerUuid);
    }

    void clearBorderForClaim(int centerX, int centerZ) {
        long key = ChunkKeys.pack(centerX, centerZ);
        for (Map.Entry<UUID, Long> e : this.borderCenterByPlayer.entrySet()) {
            if (e == null) continue;
            UUID u = e.getKey();
            Long v = e.getValue();
            if (u == null || v == null || v != key) continue;
            disableBorder(u);
        }
    }

    boolean isBorderEnabled(UUID playerUuid, long centerKey) {
        if (playerUuid == null) {
            return false;
        }
        Long v = this.borderCenterByPlayer.get(playerUuid);
        return v != null && v == centerKey;
    }

    Long getBorderCenterKey(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        return this.borderCenterByPlayer.get(playerUuid);
    }

    boolean shouldSpawnBorderNow(UUID playerUuid, long nowMs, long cooldownMs) {
        if (playerUuid == null) {
            return false;
        }
        Long last = this.lastBorderSpawnMsByPlayer.get(playerUuid);
        if (last != null && nowMs - last < cooldownMs) {
            return false;
        }
        this.lastBorderSpawnMsByPlayer.put(playerUuid, nowMs);
        return true;
    }

    void recordBorderPlayerWorld(UUID playerUuid, String worldName) {
        if (playerUuid == null || worldName == null || worldName.isEmpty()) {
            return;
        }
        if (this.borderCenterByPlayer.containsKey(playerUuid)) {
            this.borderWorldByPlayer.put(playerUuid, worldName);
        }
    }

    void collectActiveBorderCenterKeysForWorld(String worldName, LongOpenHashSet out) {
        if (worldName == null || worldName.isEmpty() || out == null) {
            return;
        }
        out.clear();
        for (Map.Entry<UUID, Long> e : this.borderCenterByPlayer.entrySet()) {
            if (e == null) continue;
            UUID u = e.getKey();
            Long ck = e.getValue();
            if (u == null || ck == null) continue;
            String w = this.borderWorldByPlayer.get(u);
            if (worldName.equals(w)) {
                out.add(ck.longValue());
            }
        }
    }

    boolean hasAnyBorderSessionForWorld(String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return false;
        }
        for (String w : this.borderWorldByPlayer.values()) {
            if (w != null && worldName.equals(w)) {
                return true;
            }
        }
        return false;
    }

    Long getLastZoneKey(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        return this.lastZoneKeyByPlayer.get(playerUuid);
    }

    void setLastZoneKey(UUID playerUuid, Long zoneKey) {
        if (playerUuid == null) {
            return;
        }
        if (zoneKey == null) {
            this.lastZoneKeyByPlayer.remove(playerUuid);
            return;
        }
        this.lastZoneKeyByPlayer.put(playerUuid, zoneKey);
    }

    void rememberUsername(UUID playerUuid, String username) {
        if (playerUuid == null || username == null) {
            return;
        }
        String u = username.trim();
        if (u.isEmpty()) {
            return;
        }
        this.knownUsernameByUuid.put(playerUuid, u);
        this.knownUuidByUsername.put(u.toLowerCase(), playerUuid);
    }

    String getKnownUsername(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        return this.knownUsernameByUuid.get(playerUuid);
    }

    UUID getKnownUuidForUsername(String username) {
        if (username == null) {
            return null;
        }
        String u = username.trim().toLowerCase();
        if (u.isEmpty()) {
            return null;
        }
        return this.knownUuidByUsername.get(u);
    }

    Map<UUID, String> getPlayersInClaim(int centerX, int centerZ) {
        long key = ChunkKeys.pack(centerX, centerZ);
        Map<UUID, String> m = this.playersInClaim.get(key);
        if (m == null || m.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(m);
    }

    void updatePlayerClaimMembership(UUID playerUuid, Long prevCenterKey, Long nowCenterKey, String username) {
        if (playerUuid == null) {
            return;
        }
        if (prevCenterKey != null) {
            ConcurrentHashMap<UUID, String> prev = this.playersInClaim.get(prevCenterKey);
            if (prev != null) {
                prev.remove(playerUuid);
                if (prev.isEmpty()) {
                    this.playersInClaim.remove(prevCenterKey, prev);
                }
            }
        }
        if (nowCenterKey != null) {
            String u = username == null ? null : username.trim();
            if (u == null || u.isEmpty()) {
                u = this.knownUsernameByUuid.get(playerUuid);
            }
            if (u == null || u.isEmpty()) {
                u = playerUuid.toString();
            }
            this.playersInClaim.computeIfAbsent(nowCenterKey, k -> new ConcurrentHashMap<>()).put(playerUuid, u);
        }
    }
}
