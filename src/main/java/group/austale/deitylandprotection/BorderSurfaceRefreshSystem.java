package group.austale.deitylandprotection;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public final class BorderSurfaceRefreshSystem
extends DelayedSystem<ChunkStore> {
    private static final int STEP = 1;
    private static final int DEFAULT_BASE_Y_SEED = 100;
    private final DeityLandProtectionPlugin plugin;

    public BorderSurfaceRefreshSystem(DeityLandProtectionPlugin plugin) {
        super(1.0f);
        this.plugin = plugin;
    }

    public void delayedTick(float v, int i, Store<ChunkStore> store) {
        if (this.plugin == null || store == null) {
            return;
        }
        World world = ((ChunkStore)store.getExternalData()).getWorld();
        if (world == null) {
            return;
        }
        String worldName = world.getName();
        if (worldName == null || worldName.isEmpty()) {
            return;
        }
        ClaimStore claimStore = this.plugin.getClaimStore();
        if (claimStore == null) {
            return;
        }
        LongOpenHashSet centerKeys = new LongOpenHashSet();
        this.plugin.collectActiveBorderCenterKeysForWorld(worldName, centerKeys);
        if (centerKeys.isEmpty()) {
            if (!this.plugin.hasAnyBorderSessionForWorld(worldName)) {
                this.plugin.getBorderSurfaceCache().removeWorld(worldName);
            }
            return;
        }
        world.execute(() -> {
            LongIterator cit = centerKeys.iterator();
            while (cit.hasNext()) {
                long centerKey = cit.nextLong();
                int centerX = (int)(centerKey >> 32);
                int centerZ = (int)centerKey;
                Claim claim = claimStore.findClaimByCenter(centerX, centerZ);
                if (claim == null) {
                    continue;
                }
                int minX;
                int maxX;
                int minZ;
                int maxZ;
                int[] bounds = claimStore.getClaimCellBounds(centerX, centerZ);
                if (bounds != null && bounds.length >= 4) {
                    minX = bounds[0];
                    maxX = bounds[1];
                    minZ = bounds[2];
                    maxZ = bounds[3];
                } else {
                    int r = claim.getRadius();
                    minX = centerX - r;
                    maxX = centerX + r;
                    minZ = centerZ - r;
                    maxZ = centerZ + r;
                }
                int scanBaseY = claim.getCenterY() != Integer.MIN_VALUE
                    ? claim.getCenterY()
                    : this.plugin.getBorderSurfaceScanBaseY(worldName, centerKey, DEFAULT_BASE_Y_SEED);
                Long2IntOpenHashMap surface = this.plugin.getBorderSurfaceCache().copyClaimMapOrEmpty(worldName, centerKey);
                for (int zz = minZ; zz <= maxZ; zz += STEP) {
                    for (int xx = minX; xx <= maxX; xx += STEP) {
                        Claim at = claimStore.findClaimAt(xx, zz);
                        if (!BorderGeometry.isSameClaim(at, centerX, centerZ)) {
                            continue;
                        }
                        if (!BorderGeometry.isBorderCell(claimStore, centerX, centerZ, xx, zz)) {
                            continue;
                        }
                        long chunkIndex = ChunkUtil.indexChunkFromBlock(xx, zz);
                        if (world.getChunkIfInMemory(chunkIndex) == null) {
                            continue;
                        }
                        int sy = BorderSurfaceUtil.resolveSurfaceY(world, xx, scanBaseY, zz);
                        surface.put(BorderSurfaceCache.packXz(xx, zz), sy);
                    }
                }
                this.plugin.getBorderSurfaceCache().putReplace(worldName, centerKey, surface);
            }
            this.plugin.getBorderSurfaceCache().pruneWorldClaims(worldName, centerKeys);
        });
    }
}
