package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.BorderSurfaceCache;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class BorderTickSystem
extends EntityTickingSystem<EntityStore> {
    private static final long SPAWN_COOLDOWN_MS = 6000L;
    private static final int STEP = 1;
    private static final String OUTLANDER_PARTICLE_SYSTEM_ID = "Fire_Teal";
    private static final String SLUMBERING_PARTICLE_SYSTEM_ID = "Fire_Green";
    private static final float PARTICLE_SCALE = 2f;
    private static final Color PARTICLE_COLOR = new Color((byte)0, (byte)96, (byte)127);
    private final DeityLandProtectionPlugin plugin;

    public BorderTickSystem(DeityLandProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    public Query<EntityStore> getQuery() {
        return Query.and((Query[])new Query[]{PlayerRef.getComponentType()});
    }

    public void tick(float deltaSeconds, int entityIndex, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        Transform t;
        if (this.plugin == null || chunk == null || store == null) {
            return;
        }
        PlayerRef player = (PlayerRef)chunk.getComponent(entityIndex, PlayerRef.getComponentType());
        if (player == null) {
            return;
        }
        UUID uuid = player.getUuid();
        if (uuid == null) {
            return;
        }
        Long centerKey = this.plugin.getBorderCenterKey(uuid);
        if (centerKey == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!this.plugin.shouldSpawnBorderNow(uuid, now, SPAWN_COOLDOWN_MS)) {
            return;
        }
        int centerX = (int)(centerKey >> 32);
        int centerZ = (int)centerKey.longValue();
        ClaimStore claimStore = this.plugin.getClaimStore();
        if (claimStore == null) {
            return;
        }
        Claim claim = claimStore.findClaimByCenter(centerX, centerZ);
        if (claim == null) {
            this.plugin.disableBorder(uuid);
            return;
        }
        String particleSystemId = BorderTickSystem.resolveParticleSystemId(this.plugin, claim);
        World world = ((EntityStore)store.getExternalData()).getWorld();
        if (world == null) {
            return;
        }
        String worldName = world.getName();
        if (worldName == null || worldName.isEmpty()) {
            return;
        }
        this.plugin.recordBorderPlayerWorld(uuid, worldName);
        try {
            t = player.getTransform();
        }
        catch (Exception ignored) {
            t = null;
        }
        if (t == null || t.getPosition() == null) {
            return;
        }
        Vector3d p = t.getPosition();
        int playerY = (int)Math.floor(p.y);
        int baseY = claim.getCenterY() != Integer.MIN_VALUE ? claim.getCenterY() : playerY;
        this.plugin.updateBorderSurfaceScanBaseY(worldName, DeityLandProtectionPlugin.centerKey(centerX, centerZ), baseY);
        Ref ref = player.getReference();
        if (ref == null || !ref.isValid()) {
            return;
        }
        List<Ref<EntityStore>> borderViewers = BorderTickSystem.collectViewerRefs(world);
        if (borderViewers.isEmpty()) {
            return;
        }
        long claimKey = DeityLandProtectionPlugin.centerKey(centerX, centerZ);
        BorderSurfaceCache surfaceCache = this.plugin.getBorderSurfaceCache();
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
        for (int z = minZ; z <= maxZ; z += STEP) {
            for (int x = minX; x <= maxX; x += STEP) {
                Claim at = claimStore.findClaimAt(x, z);
                if (!BorderGeometry.isSameClaim(at, centerX, centerZ)) {
                    continue;
                }
                if (!BorderGeometry.isBorderCell(claimStore, centerX, centerZ, x, z)) {
                    continue;
                }
                Integer cachedY = surfaceCache.getSurfaceYSampledOrNull(worldName, claimKey, x, z);
                int surfaceY;
                if (cachedY != null) {
                    surfaceY = cachedY;
                } else {
                    long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
                    if (world.getChunkIfInMemory(chunkIndex) == null) {
                        continue;
                    }
                    surfaceY = BorderSurfaceUtil.resolveSurfaceY(world, x, baseY, z);
                }
                BorderTickSystem.spawnPoint(store, borderViewers, particleSystemId, x, (double)surfaceY + 0.15, z);
            }
        }
    }

    private static List<Ref<EntityStore>> collectViewerRefs(World world) {
        if (world == null) {
            return Collections.emptyList();
        }
        Iterable<PlayerRef> playerRefs = world.getPlayerRefs();
        if (playerRefs == null) {
            return Collections.emptyList();
        }
        ArrayList<Ref<EntityStore>> viewers = new ArrayList<>();
        for (PlayerRef pr : playerRefs) {
            if (pr == null) {
                continue;
            }
            Ref<EntityStore> r = pr.getReference();
            if (r != null && r.isValid()) {
                viewers.add(r);
            }
        }
        return viewers;
    }

    private static String resolveParticleSystemId(DeityLandProtectionPlugin plugin, Claim claim) {
        if (plugin != null && claim != null && plugin.isOutlanderClaimItemId(claim.getDeityItemId())) {
            return OUTLANDER_PARTICLE_SYSTEM_ID;
        }
        return SLUMBERING_PARTICLE_SYSTEM_ID;
    }

    private static void spawnPoint(Store<EntityStore> store, List<Ref<EntityStore>> viewers, String particleSystemId, int x, double y, int z) {
        if (viewers == null || viewers.isEmpty()) {
            return;
        }
        Vector3d pos = new Vector3d((double)x + 0.5, y, (double)z + 0.5);
        String id = particleSystemId == null || particleSystemId.isEmpty() ? SLUMBERING_PARTICLE_SYSTEM_ID : particleSystemId;
        ParticleUtil.spawnParticleEffect((String)id, (Vector3d)pos, 0.0f, 0.0f, 0.0f, PARTICLE_SCALE, PARTICLE_COLOR, viewers, store);
    }
}
