/*
 * Per-world, per-claim packed XZ -> surface Y for border particles. Thread-safe.
 */
package group.austale.deitylandprotection;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.concurrent.ConcurrentHashMap;

public final class DeityLandProtectionBorderSurfaceCache {
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, Long2IntOpenHashMap>> byWorldThenClaim = new ConcurrentHashMap<>();

    public DeityLandProtectionBorderSurfaceCache() {
    }

    public static long packXz(int x, int z) {
        return (long)x << 32 | (long)z & 0xffffffffL;
    }

    /**
     * @return cached surface Y for this column, or {@code null} if never sampled (e.g. chunk was unloaded during refresh).
     */
    public Integer getSurfaceYSampledOrNull(String worldName, long centerKey, int x, int z) {
        if (worldName == null || worldName.isEmpty()) {
            return null;
        }
        ConcurrentHashMap<Long, Long2IntOpenHashMap> w = this.byWorldThenClaim.get(worldName);
        if (w == null) {
            return null;
        }
        Long2IntOpenHashMap m = w.get(centerKey);
        if (m == null) {
            return null;
        }
        long k = DeityLandProtectionBorderSurfaceCache.packXz(x, z);
        if (!m.containsKey(k)) {
            return null;
        }
        return m.get(k);
    }

    public void putReplace(String worldName, long centerKey, Long2IntOpenHashMap xzToSurfaceY) {
        if (worldName == null || worldName.isEmpty() || xzToSurfaceY == null) {
            return;
        }
        this.byWorldThenClaim.computeIfAbsent(worldName, n -> new ConcurrentHashMap<>()).put(centerKey, xzToSurfaceY);
    }

    /**
     * Copy of the current xz→surface map for this claim, or empty. Used so refresh can merge
     * new column samples without dropping cells that were valid when other chunks were loaded.
     */
    public Long2IntOpenHashMap copyClaimMapOrEmpty(String worldName, long centerKey) {
        if (worldName == null || worldName.isEmpty()) {
            return new Long2IntOpenHashMap();
        }
        ConcurrentHashMap<Long, Long2IntOpenHashMap> w = this.byWorldThenClaim.get(worldName);
        if (w == null) {
            return new Long2IntOpenHashMap();
        }
        Long2IntOpenHashMap m = w.get(centerKey);
        if (m == null || m.isEmpty()) {
            return new Long2IntOpenHashMap();
        }
        return new Long2IntOpenHashMap(m);
    }

    /**
     * Drops cache entries for this world whose claim center keys are not in the active set.
     */
    public void pruneWorldClaims(String worldName, LongOpenHashSet activeCenterKeys) {
        if (worldName == null || worldName.isEmpty()) {
            return;
        }
        ConcurrentHashMap<Long, Long2IntOpenHashMap> w = this.byWorldThenClaim.get(worldName);
        if (w == null) {
            return;
        }
        if (activeCenterKeys == null || activeCenterKeys.isEmpty()) {
            w.clear();
            if (w.isEmpty()) {
                this.byWorldThenClaim.remove(worldName, w);
            }
            return;
        }
        for (Long ckObj : w.keySet()) {
            if (ckObj == null) {
                continue;
            }
            long ck = ckObj;
            if (!activeCenterKeys.contains(ck)) {
                w.remove(ck);
            }
        }
    }

    /**
     * Removes all cached surface data for a world (e.g. world unload).
     */
    public void removeWorld(String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return;
        }
        this.byWorldThenClaim.remove(worldName);
    }
}
