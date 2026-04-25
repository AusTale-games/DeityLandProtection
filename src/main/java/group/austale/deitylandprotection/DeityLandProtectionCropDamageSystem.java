package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DeityLandProtectionCropDamageSystem
extends EntityEventSystem<EntityStore, DamageBlockEvent> {
    private final DeityLandProtectionPlugin plugin;
    private static final long MSG_COOLDOWN_MS = 1500L;
    private static final ConcurrentHashMap<UUID, Long> LAST_MSG_MS = new ConcurrentHashMap();

    public DeityLandProtectionCropDamageSystem(DeityLandProtectionPlugin plugin) {
        super(DamageBlockEvent.class);
        this.plugin = plugin;
    }

    public Query<EntityStore> getQuery() {
        return Query.and((Query[])new Query[]{PlayerRef.getComponentType()});
    }

    public void handle(int entityIndex, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, DamageBlockEvent event) {
        if (this.plugin == null || chunk == null || event == null) {
            return;
        }
        PlayerRef player = (PlayerRef)chunk.getComponent(entityIndex, PlayerRef.getComponentType());
        if (player == null) {
            return;
        }
        if (event.getBlockType() == null || event.getBlockType().getFarming() == null) {
            return;
        }
        UUID uuid = player.getUuid();
        if (uuid == null) {
            return;
        }
        if (this.plugin.isOpBypass(uuid)) {
            return;
        }
        int x = event.getTargetBlock().x;
        int z = event.getTargetBlock().z;
        Claim claim = this.plugin.getClaimStore().findClaimAt(x, z);
        if (claim == null) {
            return;
        }
        if (claim.getOwner().equals(uuid)) {
            return;
        }
        event.setCancelled(true);
        long now = System.currentTimeMillis();
        Long lastObj = LAST_MSG_MS.get(uuid);
        long last = lastObj == null ? 0L : lastObj;
        if (lastObj == null || now - last >= 1500L) {
            LAST_MSG_MS.put(uuid, now);
            DeityLandProtectionLangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(player);
            this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotDamageCrops(lang));
        }
    }
}



