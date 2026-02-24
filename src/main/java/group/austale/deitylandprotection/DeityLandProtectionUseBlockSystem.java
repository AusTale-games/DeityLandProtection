/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.builtin.crafting.state.BenchState
 *  com.hypixel.hytale.component.ArchetypeChunk
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.query.Query
 *  com.hypixel.hytale.component.system.EntityEventSystem
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.protocol.InteractionType
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager
 *  com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
 *  com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent$Pre
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.meta.BlockState
 *  com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerBlockState
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.ClaimStore;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import group.austale.deitylandprotection.DeityLandProtectionTrustListPage;
import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerBlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.logging.Level;

public final class DeityLandProtectionUseBlockSystem
extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionUseBlockSystem(DeityLandProtectionPlugin plugin) {
        super(UseBlockEvent.Pre.class);
        this.plugin = plugin;
    }

    public Query<EntityStore> getQuery() {
        return Query.and((Query[])new Query[]{PlayerRef.getComponentType(), MovementStatesComponent.getComponentType()});
    }

    public void handle(int entityIndex, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, UseBlockEvent.Pre event) {
        boolean isCenter;
        boolean crouching;
        PlayerRef player = (PlayerRef)chunk.getComponent(entityIndex, PlayerRef.getComponentType());
        if (player == null) {
            return;
        }
        if (event == null) {
            return;
        }
        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(player);
        UUID uuid = player.getUuid();
        boolean bypass = this.plugin.isOpBypass(uuid);
        int x = event.getTargetBlock().x;
        int y = event.getTargetBlock().y;
        int z = event.getTargetBlock().z;
        ClaimStore claims = this.plugin.getClaimStore();
        Claim claim = claims.findClaimAt(x, z);
        if (claim == null) {
            return;
        }
        if (!bypass && event.getBlockType().getFarming() != null && !claim.getOwner().equals(uuid)) {
            event.setCancelled(true);
            this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotDamageCrops(lang));
            return;
        }
        MovementStatesComponent msComponent = (MovementStatesComponent)chunk.getComponent(entityIndex, MovementStatesComponent.getComponentType());
        boolean bl = crouching = msComponent != null && msComponent.getMovementStates() != null && msComponent.getMovementStates().crouching;
        if (crouching && !bypass && !claim.getOwner().equals(uuid) && !claim.hasPermission(uuid, 2)) {
            BlockState state = null;
            try {
                state = ((EntityStore)store.getExternalData()).getWorld().getState(x, y, z, true);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (state instanceof ItemContainerBlockState) {
                event.setCancelled(true);
                this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotBreakInside(lang));
                return;
            }
            if (state instanceof BenchState) {
                event.setCancelled(true);
                this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotBreakInside(lang));
                return;
            }
        }
        if (claim.getCenterY() == Integer.MIN_VALUE) {
            claims.updateClaimCenterYIfUnknown(claim.getCenterX(), claim.getCenterZ(), y);
            claim = claims.findClaimByCenter(claim.getCenterX(), claim.getCenterZ());
            if (claim == null) {
                return;
            }
        }
        boolean bl2 = isCenter = claim.getCenterX() == x && claim.getCenterZ() == z && claim.getCenterY() == y;
        if (event.getInteractionType() == InteractionType.Use && isCenter && (bypass || claim.getOwner().equals(uuid))) {
            Player playerEntity;
            event.setCancelled(true);
            Ref ref = null;
            try {
                ref = player.getReference();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (ref == null) {
                ref = chunk.getReferenceTo(entityIndex);
            }
            try {
                playerEntity = (Player)store.getComponent(ref, Player.getComponentType());
            }
            catch (Exception e) {
                ((HytaleLogger.Api)this.plugin.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection UI: failed to get Player component");
                this.plugin.sendPlayerMessage(player, "DeityLandProtection UI error: cannot read Player component");
                return;
            }
            PageManager pages = playerEntity.getPageManager();
            try {
                pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, player, claim.getCenterX(), claim.getCenterZ()));
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return;
        }
        if (bypass) {
            return;
        }
        if (!claim.getOwner().equals(uuid) && !claim.hasPermission(uuid, 4)) {
            event.setCancelled(true);
            this.plugin.sendPlayerMessage(player, DeityLandProtectionText.cannotUseInside(lang));
        }
    }
}



