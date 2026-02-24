/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.EntityEventSystem
 *  com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.ClaimStore;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import group.austale.deitylandprotection.DeityLandProtectionUpkeepStore;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;

public final class DeityLandProtectionBreakSystem
extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionBreakSystem(DeityLandProtectionPlugin plugin) {
        super(BreakBlockEvent.class);
        this.plugin = plugin;
    }

    public Query<EntityStore> getQuery() {
        return Query.and((Query[])new Query[]{PlayerRef.getComponentType()});
    }

    public void handle(int entityIndex, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, BreakBlockEvent event) {
        PlayerRef player = (PlayerRef)chunk.getComponent(entityIndex, PlayerRef.getComponentType());
        if (player == null) {
            return;
        }
        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(player);
        UUID uuid = player.getUuid();
        boolean bypass = this.plugin.isOpBypass(uuid);
        int x = event.getTargetBlock().x;
        int z = event.getTargetBlock().z;
        ClaimStore claims = this.plugin.getClaimStore();
        Claim claim = claims.findClaimAt(x, z);
        if (claim == null) {
            return;
        }
        boolean isCenter = claim.getCenterX() == x && claim.getCenterZ() == z;
        if (isCenter && this.plugin.shouldIgnoreCenterBreak(x, z)) {
            return;
        }
        if (bypass) {
            if (isCenter) {
                try {
                    this.plugin.queueMapUpdateForClaim(((EntityStore)store.getExternalData()).getWorld().getName(), claim);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                claims.removeClaimAt(x, z);
                DeityLandProtectionUpkeepStore upkeep = this.plugin.getUpkeepStore();
                if (upkeep != null) {
                    upkeep.remove(x, z);
                    upkeep.markDirty();
                }
                this.plugin.clearBorderForClaim(x, z);
                this.plugin.sendPlayerMessage(player, DeityLandProtectionText.protectionRemoved(lang));
            }
            return;
        }
        if (isCenter) {
            if (!claim.getOwner().equals(uuid)) {
                event.setCancelled(true);
                this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotBreakProtection(lang));
                return;
            }
            try {
                this.plugin.queueMapUpdateForClaim(((EntityStore)store.getExternalData()).getWorld().getName(), claim);
            }
            catch (Exception upkeep) {
                // empty catch block
            }
            claims.removeClaimAt(x, z);
            DeityLandProtectionUpkeepStore upkeep = this.plugin.getUpkeepStore();
            if (upkeep != null) {
                upkeep.remove(x, z);
                upkeep.markDirty();
            }
            this.plugin.clearBorderForClaim(x, z);
            this.plugin.sendPlayerMessage(player, DeityLandProtectionText.protectionRemoved(lang));
            return;
        }
        if (event.getBlockType() != null && event.getBlockType().getFarming() != null) {
            if (!claim.getOwner().equals(uuid)) {
                event.setCancelled(true);
                this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotDamageCrops(lang));
            }
            return;
        }
        if (!claim.getOwner().equals(uuid) && !claim.hasPermission(uuid, 2)) {
            event.setCancelled(true);
            this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotBreakInside(lang));
        }
    }
}



