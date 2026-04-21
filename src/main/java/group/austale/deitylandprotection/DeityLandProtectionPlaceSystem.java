/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.EntityEventSystem
 *  com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent
 *  com.hypixel.hytale.server.core.inventory.ItemStack
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
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.logging.Level;

public final class DeityLandProtectionPlaceSystem
extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionPlaceSystem(DeityLandProtectionPlugin plugin) {
        super(PlaceBlockEvent.class);
        this.plugin = plugin;
    }

    public Query<EntityStore> getQuery() {
        return Query.and((Query[])new Query[]{PlayerRef.getComponentType()});
    }

    public void handle(int entityIndex, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, PlaceBlockEvent event) {
        PlayerRef player = (PlayerRef)chunk.getComponent(entityIndex, PlayerRef.getComponentType());
        if (player == null) {
            return;
        }
        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(player);
        UUID uuid = player.getUuid();
        boolean bypass = this.plugin.isOpBypass(uuid);
        int x = event.getTargetBlock().x;
        int y = event.getTargetBlock().y;
        int z = event.getTargetBlock().z;
        ClaimStore claims = this.plugin.getClaimStore();
        ItemStack inHand = event.getItemInHand();
        if (inHand != null && this.plugin.isClaimItemId(inHand.getItemId())) {
            if (!bypass) {
                int owned = claims.countClaimsForOwner(uuid);
                if (owned >= this.plugin.getMaxClaimsPerPlayer()) {
                    event.setCancelled(true);
                    this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotPlaceClaimLimit(lang, owned, this.plugin.getMaxClaimsPerPlayer()));
                    return;
                }
                Claim existing = claims.findClaimAt(x, z);
                if (existing != null && !existing.getOwner().equals(uuid)) {
                    event.setCancelled(true);
                    this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotPlaceAreaProtected(lang));
                    return;
                }
            }
            if (claims.intersectsAny(x, z, this.plugin.getClaimRadius())) {
                event.setCancelled(true);
                this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotPlaceOverlap(lang));
                return;
            }
            String ownerName = null;
            try {
                ownerName = player.getUsername();
            }
            catch (Exception existing) {
                // empty catch block
            }
            Claim newClaim = new Claim(uuid, ownerName, x, y, z, this.plugin.getClaimRadius(), inHand.getItemId());
            boolean added = claims.addClaim(newClaim);
            if (added) {
                DeityLandProtectionUpkeepStore upkeep = this.plugin.getUpkeepStore();
                if (upkeep != null) {
                    upkeep.getOrCreate(x, z);
                    upkeep.markDirty();
                }
                this.plugin.markRecentClaimPlacement(x, z);
                try {
                    this.plugin.queueMapUpdateForClaim(((EntityStore)store.getExternalData()).getWorld().getName(), newClaim);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.plugin.sendPlayerMessage(player, DeityLandProtectionText.protectionCreated(lang, this.plugin.getClaimRadius()));
                this.plugin.getLogger().at(Level.INFO).log("DeityLandProtection claim created owner=" + String.valueOf(uuid) + " center=" + x + "," + z + " radius=" + this.plugin.getClaimRadius() + " itemId=" + inHand.getItemId());
            } else {
                this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotPlaceHere(lang));
            }
            return;
        }
        Claim claim = claims.findClaimAt(x, z);
        if (claim == null) {
            return;
        }
        if (bypass) {
            return;
        }
        if (!claim.getOwner().equals(uuid) && !claim.hasPermission(uuid, 1)) {
            event.setCancelled(true);
            this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotPlaceInside(lang));
        }
    }
}



