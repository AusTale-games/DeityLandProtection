/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.tick.EntityTickingSystem
 *  com.hypixel.hytale.math.vector.Transform
 *  com.hypixel.hytale.math.vector.Vector3d
 *  com.hypixel.hytale.protocol.Color
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.ParticleUtil
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.UUID;

public final class DeityLandProtectionBorderTickSystem
extends EntityTickingSystem<EntityStore> {
    // Tunable particle/border visuals.
    private static final long SPAWN_COOLDOWN_MS = 6000L;
    private static final int STEP = 1;
    private static final String OUTLANDER_PARTICLE_SYSTEM_ID = "Fire_Teal";
    private static final String SLUMBERING_PARTICLE_SYSTEM_ID = "Fire_Green";
    private static final float PARTICLE_SCALE = 2f;
    private static final Color PARTICLE_COLOR = new Color((byte)0, (byte)96, (byte)127);
    // Vertical scan range around the claim center/player Y used to find the surface.
    private static final int SURFACE_SCAN_RANGE = 10;
    private static final int SURFACE_SCAN_FALLBACK_RANGE = 64;
    private static final String AIR_TYPE_ID = "Air";
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionBorderTickSystem(DeityLandProtectionPlugin plugin) {
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
        // Border visuals are per-player and gated by a cooldown.
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
        String particleSystemId = DeityLandProtectionBorderTickSystem.resolveParticleSystemId(this.plugin, claim);
        World world = ((EntityStore)store.getExternalData()).getWorld();
        if (world == null) {
            return;
        }
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
        // Use claim-center Y when available so border height stays stable regardless of player altitude.
        int baseY = claim.getCenterY() != Integer.MIN_VALUE ? claim.getCenterY() : playerY;
        Ref ref = player.getReference();
        if (ref == null || !ref.isValid()) {
            return;
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
        // Scan claim perimeter cells and emit particles only on true borders.
        for (int z = minZ; z <= maxZ; z += STEP) {
            for (int x = minX; x <= maxX; x += STEP) {
                Claim at = claimStore.findClaimAt(x, z);
                if (!DeityLandProtectionBorderTickSystem.isSameClaim(at, centerX, centerZ)) {
                    continue;
                }
                if (!DeityLandProtectionBorderTickSystem.isBorderCell(claimStore, centerX, centerZ, x, z)) {
                    continue;
                }
                // Resolve the topmost solid block and spawn just above it.
                int surfaceY = DeityLandProtectionBorderTickSystem.resolveSurfaceY(world, x, baseY, z);
                DeityLandProtectionBorderTickSystem.spawnPoint(store, (Ref<EntityStore>)ref, particleSystemId, x, (double)surfaceY + 0.15, z);
            }
        }
    }

    private static boolean isBorderCell(ClaimStore claimStore, int centerX, int centerZ, int x, int z) {
        return !DeityLandProtectionBorderTickSystem.isSameClaim(claimStore.findClaimAt(x - 1, z), centerX, centerZ) || !DeityLandProtectionBorderTickSystem.isSameClaim(claimStore.findClaimAt(x + 1, z), centerX, centerZ) || !DeityLandProtectionBorderTickSystem.isSameClaim(claimStore.findClaimAt(x, z - 1), centerX, centerZ) || !DeityLandProtectionBorderTickSystem.isSameClaim(claimStore.findClaimAt(x, z + 1), centerX, centerZ);
    }

    private static boolean isSameClaim(Claim claim, int centerX, int centerZ) {
        return claim != null && claim.getCenterX() == centerX && claim.getCenterZ() == centerZ;
    }

    private static String resolveParticleSystemId(DeityLandProtectionPlugin plugin, Claim claim) {
        if (plugin != null && claim != null && plugin.isOutlanderClaimItemId(claim.getDeityItemId())) {
            return OUTLANDER_PARTICLE_SYSTEM_ID;
        }
        return SLUMBERING_PARTICLE_SYSTEM_ID;
    }

    private static void spawnPoint(Store<EntityStore> store, Ref<EntityStore> ref, String particleSystemId, int x, double y, int z) {
        Vector3d pos = new Vector3d((double)x + 0.5, y, (double)z + 0.5);
        String id = particleSystemId == null || particleSystemId.isEmpty() ? SLUMBERING_PARTICLE_SYSTEM_ID : particleSystemId;
        ParticleUtil.spawnParticleEffect((String)id, (Vector3d)pos, (float)0.0f, (float)0.0f, (float)0.0f, PARTICLE_SCALE, PARTICLE_COLOR, Collections.singletonList(ref), store);
    }

    private static int resolveSurfaceY(World world, int x, int baseY, int z) {
        int surfaceY = baseY;
        BlockType baseType = DeityLandProtectionBorderTickSystem.safeGetBlockType(world, x, baseY, z);
        if (baseType == null) {
            return surfaceY;
        }
        if (DeityLandProtectionBorderTickSystem.isAirType(baseType)) {
            for (int dy = 1; dy <= SURFACE_SCAN_FALLBACK_RANGE; dy++) {
                int y = baseY - dy;
                BlockType type = DeityLandProtectionBorderTickSystem.safeGetBlockType(world, x, y, z);
                if (DeityLandProtectionBorderTickSystem.isAirType(type)) {
                    continue;
                }
                if (type != null) {
                    surfaceY = y;
                    break;
                }
            }
            return surfaceY;
        }
        int lastSolidY = baseY;
        for (int dy = 1; dy <= SURFACE_SCAN_RANGE; dy++) {
            int y = baseY + dy;
            BlockType type = DeityLandProtectionBorderTickSystem.safeGetBlockType(world, x, y, z);
            if (type == null || DeityLandProtectionBorderTickSystem.isAirType(type)) {
                return lastSolidY;
            }
            lastSolidY = y;
        }
        for (int dy = SURFACE_SCAN_RANGE + 1; dy <= SURFACE_SCAN_FALLBACK_RANGE; dy++) {
            int y = baseY + dy;
            BlockType type = DeityLandProtectionBorderTickSystem.safeGetBlockType(world, x, y, z);
            if (type == null || DeityLandProtectionBorderTickSystem.isAirType(type)) {
                return lastSolidY;
            }
            lastSolidY = y;
        }
        return lastSolidY;
    }

    private static BlockType safeGetBlockType(World world, int x, int y, int z) {
        try {
            return world.getBlockType(x, y, z);
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isAirType(BlockType type) {
        if (type == null) {
            return true;
        }
        if (type == BlockType.EMPTY) {
            return true;
        }
        String id = DeityLandProtectionBorderTickSystem.blockTypeId(type);
        if (id == null || id.isEmpty()) {
            return false;
        }
        if (AIR_TYPE_ID == null || AIR_TYPE_ID.isEmpty()) {
            return false;
        }
        return id.equalsIgnoreCase(AIR_TYPE_ID);
    }

    private static String blockTypeId(BlockType type) {
        if (type == null) {
            return null;
        }
        if (type == BlockType.EMPTY) {
            return "EMPTY";
        }
        String text = String.valueOf(type);
        int idIndex = text.indexOf("id=");
        if (idIndex < 0) {
            return text;
        }
        int start = idIndex + 3;
        int end = text.indexOf(',', start);
        if (end <= start) {
            return text.substring(start).trim();
        }
        return text.substring(start, end).trim();
    }
}



