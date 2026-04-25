package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.ClaimStore;
import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.Text;
import group.austale.deitylandprotection.UpkeepStore;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;

public final class BreakSystem
extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private final DeityLandProtectionPlugin plugin;

    public BreakSystem(DeityLandProtectionPlugin plugin) {
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
        LangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(player);
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
                catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
                claims.removeClaimAt(x, z);
                UpkeepStore upkeep = this.plugin.getUpkeepStore();
                if (upkeep != null) {
                    upkeep.remove(x, z);
                    upkeep.markDirty();
                }
                this.plugin.clearBorderForClaim(x, z);
                this.plugin.sendPlayerMessage(player, Text.protectionRemoved(lang));
            }
            return;
        }
        if (isCenter) {
            if (!claim.getOwner().equals(uuid)) {
                event.setCancelled(true);
                this.plugin.sendPlayerMessage(player, Text.cannotBreakProtection(lang));
                return;
            }
            try {
                this.plugin.queueMapUpdateForClaim(((EntityStore)store.getExternalData()).getWorld().getName(), claim);
            }
            catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
            claims.removeClaimAt(x, z);
            UpkeepStore upkeep = this.plugin.getUpkeepStore();
            if (upkeep != null) {
                upkeep.remove(x, z);
                upkeep.markDirty();
            }
            this.plugin.clearBorderForClaim(x, z);
            this.plugin.sendPlayerMessage(player, Text.protectionRemoved(lang));
            return;
        }
        if (event.getBlockType() != null && event.getBlockType().getFarming() != null) {
            if (!claim.getOwner().equals(uuid) && !claim.hasPermission(uuid, 2)) {
                event.setCancelled(true);
                this.plugin.sendPlayerMessage(player, Text.cannotDamageCrops(lang));
            }
            return;
        }
        if (!claim.getOwner().equals(uuid) && !claim.hasPermission(uuid, 2)) {
            event.setCancelled(true);
            this.plugin.sendPlayerMessage(player, Text.cannotBreakInside(lang));
        }
    }
}



