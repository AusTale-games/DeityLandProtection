package group.austale.deitylandprotection;

import com.hypixel.hytale.math.util.ChunkUtil;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the per-world set of chunk indices whose world-map tile needs to be
 * regenerated because a claim was placed, removed, or resized inside it.
 *
 * <p>{@link DeityLandProtectionWorldMapUpdateTickingSystem} drains the queue
 * one world at a time on its own schedule.</p>
 */
final class DeityLandProtectionMapUpdateQueue {

    private final ConcurrentHashMap<String, LongSet> queueByWorld = new ConcurrentHashMap<>();
    private final ClaimStore claimStore;

    DeityLandProtectionMapUpdateQueue(ClaimStore claimStore) {
        this.claimStore = claimStore;
    }

    void queueForClaim(String worldName, Claim claim) {
        if (worldName == null || worldName.isEmpty() || claim == null) {
            return;
        }
        int minX, maxX, minZ, maxZ;
        int[] bounds = this.claimStore == null ? null : this.claimStore.getClaimCellBounds(claim.getCenterX(), claim.getCenterZ());
        if (bounds != null && bounds.length >= 4) {
            minX = bounds[0];
            maxX = bounds[1];
            minZ = bounds[2];
            maxZ = bounds[3];
        } else {
            int centerX = claim.getCenterX();
            int centerZ = claim.getCenterZ();
            int r = claim.getRadius();
            minX = centerX - r;
            maxX = centerX + r;
            minZ = centerZ - r;
            maxZ = centerZ + r;
        }
        int minChunkX = ChunkUtil.chunkCoordinate(minX);
        int maxChunkX = ChunkUtil.chunkCoordinate(maxX);
        int minChunkZ = ChunkUtil.chunkCoordinate(minZ);
        int maxChunkZ = ChunkUtil.chunkCoordinate(maxZ);
        LongOpenHashSet toAdd = new LongOpenHashSet();
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                toAdd.add(ChunkUtil.indexChunk(cx, cz));
            }
        }
        this.queueByWorld.compute(worldName, (k, existing) -> {
            LongSet set = existing != null ? existing : new LongOpenHashSet();
            LongIterator it = toAdd.iterator();
            while (it.hasNext()) {
                set.add(it.nextLong());
            }
            return set;
        });
    }

    void queueForAllClaims(String worldName) {
        if (worldName == null || worldName.isEmpty() || this.claimStore == null) {
            return;
        }
        List<Claim> claims = this.claimStore.getClaims();
        for (Claim claim : claims) {
            queueForClaim(worldName, claim);
        }
    }

    LongSet poll(String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return null;
        }
        return this.queueByWorld.remove(worldName);
    }

    void clear() {
        this.queueByWorld.clear();
    }
}
