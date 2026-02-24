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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.UUID;

public final class DeityLandProtectionBorderTickSystem
extends EntityTickingSystem<EntityStore> {
    private static final long SPAWN_COOLDOWN_MS = 650L;
    private static final int STEP = 2;
    private static final String PARTICLE_SYSTEM_ID = "Impact_Critical";
    private static final float PARTICLE_SCALE = 0.25f;
    private static final Color PARTICLE_COLOR = new Color((byte)0, (byte)96, (byte)127);
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
        UUID uuid = player.getUuid();
        if (uuid == null) {
            return;
        }
        Long centerKey = this.plugin.getBorderCenterKey(uuid);
        if (centerKey == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!this.plugin.shouldSpawnBorderNow(uuid, now, 650L)) {
            return;
        }
        int centerX = (int)(centerKey >> 32);
        int centerZ = (int)centerKey.longValue();
        Claim claim = this.plugin.getClaimStore().findClaimByCenter(centerX, centerZ);
        if (claim == null) {
            this.plugin.disableBorder(uuid);
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
        double yBase = claim.getCenterY() != Integer.MIN_VALUE ? (double)claim.getCenterY() + 0.15 : Math.floor(p.y) + 0.15;
        Ref ref = player.getReference();
        if (ref == null || !ref.isValid()) {
            return;
        }
        int r = claim.getRadius();
        int minX = centerX - r;
        int maxX = centerX + r;
        int minZ = centerZ - r;
        int maxZ = centerZ + r;
        for (int x = minX; x <= maxX; x += 2) {
            DeityLandProtectionBorderTickSystem.spawnPoint(store, (Ref<EntityStore>)ref, x, yBase, minZ);
            DeityLandProtectionBorderTickSystem.spawnPoint(store, (Ref<EntityStore>)ref, x, yBase, maxZ);
        }
        for (int z = minZ; z <= maxZ; z += 2) {
            DeityLandProtectionBorderTickSystem.spawnPoint(store, (Ref<EntityStore>)ref, minX, yBase, z);
            DeityLandProtectionBorderTickSystem.spawnPoint(store, (Ref<EntityStore>)ref, maxX, yBase, z);
        }
    }

    private static void spawnPoint(Store<EntityStore> store, Ref<EntityStore> ref, int x, double y, int z) {
        Vector3d pos = new Vector3d((double)x + 0.5, y, (double)z + 0.5);
        ParticleUtil.spawnParticleEffect((String)PARTICLE_SYSTEM_ID, (Vector3d)pos, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.25f, (Color)PARTICLE_COLOR, Collections.singletonList(ref), store);
    }
}



