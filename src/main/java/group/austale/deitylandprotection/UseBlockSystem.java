package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.ClaimStore;
import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.Text;
import group.austale.deitylandprotection.TrustListPage;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
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
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.logging.Level;

public final class UseBlockSystem
extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
    private final DeityLandProtectionPlugin plugin;

    public UseBlockSystem(DeityLandProtectionPlugin plugin) {
        super(UseBlockEvent.Pre.class);
        this.plugin = plugin;
    }

    public Query<EntityStore> getQuery() {
        return Query.and((Query[])new Query[]{PlayerRef.getComponentType(), MovementStatesComponent.getComponentType()});
    }

    public void handle(int entityIndex, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, UseBlockEvent.Pre event) {
        PlayerRef player = (PlayerRef)chunk.getComponent(entityIndex, PlayerRef.getComponentType());
        if (player == null) {
            return;
        }
        if (event == null) {
            return;
        }
        LangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(player);
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
        if (!bypass && event.getBlockType().getFarming() != null && !claim.getOwner().equals(uuid) && !claim.hasPermission(uuid, 4)) {
            event.setCancelled(true);
            this.plugin.sendPlayerMessage(player, Text.cannotDamageCrops(lang));
            return;
        }
        MovementStatesComponent msComponent = (MovementStatesComponent)chunk.getComponent(entityIndex, MovementStatesComponent.getComponentType());
        boolean crouching = msComponent != null && msComponent.getMovementStates() != null && msComponent.getMovementStates().crouching;
        if (crouching && !bypass && !claim.getOwner().equals(uuid) && !claim.hasPermission(uuid, 2)) {
            if (UseBlockSystem.isContainerOrBenchBlock(((EntityStore)store.getExternalData()).getWorld(), x, y, z)) {
                event.setCancelled(true);
                this.plugin.sendPlayerMessage(player, Text.cannotBreakInside(lang));
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
        boolean isCenter = claim.getCenterX() == x && claim.getCenterZ() == z && claim.getCenterY() == y;
        if (event.getInteractionType() == InteractionType.Use && isCenter && (bypass || claim.getOwner().equals(uuid))) {
            Player playerEntity;
            event.setCancelled(true);
            Ref ref = null;
            try {
                ref = player.getReference();
            }
            catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
            if (ref == null) {
                ref = chunk.getReferenceTo(entityIndex);
            }
            try {
                playerEntity = (Player)store.getComponent(ref, Player.getComponentType());
            }
            catch (Exception e) {
                ((HytaleLogger.Api)this.plugin.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection UI: failed to get Player component");
                this.plugin.sendPlayerMessage(player, Text.uiPlayerComponentError(lang));
                return;
            }
            PageManager pages = playerEntity.getPageManager();
            try {
                pages.openCustomPage(ref, store, (CustomUIPage)new TrustListPage(this.plugin, player, claim.getCenterX(), claim.getCenterZ()));
            }
            catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
            return;
        }
        if (bypass) {
            return;
        }
        if (!claim.getOwner().equals(uuid) && !claim.hasPermission(uuid, 4) && !claim.hasPermission(uuid, 2)) {
            event.setCancelled(true);
            this.plugin.sendPlayerMessage(player, Text.cannotUseInside(lang));
        }
    }

    private static boolean isContainerOrBenchBlock(World world, int x, int y, int z) {
        if (world == null) {
            return false;
        }
        try {
            long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
            ChunkStore chunkStore = world.getChunkStore();
            if (chunkStore == null) {
                return false;
            }
            Ref chunkRef = chunkStore.getChunkReference(chunkIndex);
            if (chunkRef == null || !chunkRef.isValid()) {
                return false;
            }
            Store chunkStoreStore = chunkStore.getStore();
            BlockComponentChunk bcc = (BlockComponentChunk)chunkStoreStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
            if (bcc == null) {
                return false;
            }
            Ref entityRef = bcc.getEntityReference(ChunkUtil.indexBlockInColumn(x, y, z));
            if (entityRef == null || !entityRef.isValid()) {
                return false;
            }
            if (chunkStoreStore.getComponent(entityRef, ProcessingBenchBlock.getComponentType()) != null) {
                return true;
            }
            if (chunkStoreStore.getComponent(entityRef, BenchBlock.getComponentType()) != null) {
                return true;
            }
            return chunkStoreStore.getComponent(entityRef, ItemContainerBlock.getComponentType()) != null;
        }
        catch (Throwable ignored) {
            return false;
        }
    }
}



