package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DeityLandProtectionEnterExitTickSystem
extends EntityTickingSystem<EntityStore> {
    private static final long CHECK_COOLDOWN_MS = 250L;
    private final DeityLandProtectionPlugin plugin;
    private final ConcurrentHashMap<UUID, Long> lastCheckMsByPlayer = new ConcurrentHashMap();

    public DeityLandProtectionEnterExitTickSystem(DeityLandProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    public Query<EntityStore> getQuery() {
        return Query.and((Query[])new Query[]{PlayerRef.getComponentType()});
    }

    public void tick(float deltaSeconds, int entityIndex, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        Transform t;
        if (this.plugin == null || chunk == null) {
            return;
        }
        PlayerRef player = (PlayerRef)chunk.getComponent(entityIndex, PlayerRef.getComponentType());
        if (player == null) {
            return;
        }
        LangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(player);
        UUID uuid = player.getUuid();
        if (uuid == null) {
            return;
        }
        long nowMs = System.currentTimeMillis();
        Long lastCheck = this.lastCheckMsByPlayer.get(uuid);
        if (lastCheck != null && nowMs - lastCheck < 250L) {
            return;
        }
        this.lastCheckMsByPlayer.put(uuid, nowMs);
        try {
            t = player.getTransform();
        }
        catch (Exception ignored) {
            t = null;
        }
        if (t == null || t.getPosition() == null) {
            return;
        }
        Vector3d pos = t.getPosition();
        try {
            this.plugin.rememberUsername(uuid, player.getUsername());
        }
        catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        int x = (int)Math.floor(pos.x);
        int z = (int)Math.floor(pos.z);
        Claim claim = this.plugin.getClaimStore().findClaimAt(x, z);
        Long prev = this.plugin.getLastZoneKey(uuid);
        Long now = null;
        if (claim != null) {
            now = DeityLandProtectionPlugin.centerKey(claim.getCenterX(), claim.getCenterZ());
        }
        if (prev == null && now == null) {
            return;
        }
        if (prev != null && now != null && prev.longValue() == now.longValue()) {
            return;
        }
        try {
            this.plugin.updatePlayerClaimMembership(uuid, prev, now, player.getUsername());
        }
        catch (Exception ignored) {
            this.plugin.updatePlayerClaimMembership(uuid, prev, now, null);
        }
        this.plugin.setLastZoneKey(uuid, now);
        if (prev != null && now == null) {
            Claim prevClaim = DeityLandProtectionEnterExitTickSystem.resolveClaim(this.plugin, prev);
            String owner = DeityLandProtectionEnterExitTickSystem.resolveOwnerName(this.plugin, prevClaim);
            this.plugin.sendPlayerMessage(player, DeityLandProtectionText.leftArea(lang, owner));
            return;
        }
        if (prev == null && now != null) {
            String owner = DeityLandProtectionEnterExitTickSystem.resolveOwnerName(this.plugin, claim);
            this.plugin.sendPlayerMessage(player, DeityLandProtectionText.enteredArea(lang, owner));
            return;
        }
        String owner = DeityLandProtectionEnterExitTickSystem.resolveOwnerName(this.plugin, claim);
        this.plugin.sendPlayerMessage(player, DeityLandProtectionText.enteredArea(lang, owner));
    }

    private static String resolveOwnerName(DeityLandProtectionPlugin plugin, Claim claim) {
        String cached;
        if (claim == null) {
            return "?";
        }
        String n = claim.getOwnerName();
        if (n != null && !n.isEmpty()) {
            return n;
        }
        if (plugin != null && claim.getOwner() != null && (cached = plugin.getKnownUsername(claim.getOwner())) != null && !cached.isEmpty()) {
            return cached;
        }
        return claim.getOwner() == null ? "?" : claim.getOwner().toString();
    }

    private static Claim resolveClaim(DeityLandProtectionPlugin plugin, long centerKey) {
        if (plugin == null) {
            return null;
        }
        int centerX = (int)(centerKey >> 32);
        int centerZ = (int)centerKey;
        return plugin.getClaimStore().findClaimByCenter(centerX, centerZ);
    }
}



