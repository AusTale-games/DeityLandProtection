/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.system.DelayedSystem
 *  com.hypixel.hytale.math.util.ChunkUtil
 *  com.hypixel.hytale.server.core.Message
 *  com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.inventory.container.ItemContainer
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk
 *  com.hypixel.hytale.server.core.universe.world.meta.BlockState
 *  com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.util.EventTitleUtil
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.ClaimStore;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import group.austale.deitylandprotection.DeityLandProtectionUpkeepState;
import group.austale.deitylandprotection.DeityLandProtectionUpkeepStore;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import java.util.Iterator;
import java.util.UUID;

public final class DeityLandProtectionUpkeepTickingSystem
extends DelayedSystem<ChunkStore> {
    private static final long ONE_HOUR_MS = 3600000L;
    private static final String ESSENCE_ITEM_ID = "Ingredient_Life_Essence";

    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionUpkeepTickingSystem(DeityLandProtectionPlugin plugin) {
        super(1.0f);
        this.plugin = plugin;
    }

    public void delayedTick(float v, int i, Store<ChunkStore> store) {
        if (store == null || this.plugin == null) {
            return;
        }
        World world = ((ChunkStore)store.getExternalData()).getWorld();
        if (world == null) {
            return;
        }
        world.execute(() -> this.tickWorld(world));
    }

    private void tickWorld(World world) {
        if (world == null) {
            return;
        }
        ClaimStore claims = this.plugin.getClaimStore();
        DeityLandProtectionUpkeepStore upkeep = this.plugin.getUpkeepStore();
        if (claims == null || upkeep == null) {
            return;
        }
        boolean upkeepEnabled = this.plugin.isUpkeepEnabled();
        int configuredBaseRadius = this.plugin.getClaimRadius();
        int essenceCostPerHour = this.plugin.getUpkeepEssenceCostPerHour();
        long essenceFeedDurationMs = Math.max(1L, ONE_HOUR_MS / (long)Math.max(1, essenceCostPerHour));
        long now = System.currentTimeMillis();
        long graceMs = this.plugin.getUpkeepGraceMs();
        Iterator<Claim> iterator = claims.getClaims().iterator();
        while (iterator.hasNext()) {
            Claim claim = iterator.next();
            if (claim == null) {
                continue;
            }
            int centerX = claim.getCenterX();
            int centerZ = claim.getCenterZ();
            int centerY = claim.getCenterY();
            Claim liveClaim = claims.findClaimByCenter(centerX, centerZ);
            if (centerY == Integer.MIN_VALUE || liveClaim == null) {
                continue;
            }
            DeityLandProtectionUpkeepState st = upkeep.getOrCreate(centerX, centerZ);
            if (!upkeepEnabled) {
                boolean changed = false;
                if (st.isPendingRemoveBlock()) {
                    st.setPendingRemoveBlock(false);
                    changed = true;
                }
                if (st.getGraceUntilMs() > 0L || st.getGraceCountdownLastSecond() > 0L) {
                    st.setGraceUntilMs(0L);
                    st.setGraceCountdownLastSecond(0L);
                    changed = true;
                }
                if (st.getProtectionUntilMs() > 0L) {
                    st.setProtectionUntilMs(0L);
                    changed = true;
                }
                if (st.getTotalFeedDurationMs() > 0L) {
                    st.setTotalFeedDurationMs(0L);
                    changed = true;
                }
                if (configuredBaseRadius > 0 && liveClaim.getRadius() > configuredBaseRadius) {
                    Claim updated;
                    try {
                        this.plugin.queueMapUpdateForClaim(world.getName(), liveClaim);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (claims.updateClaimRadius(centerX, centerZ, configuredBaseRadius) && (updated = claims.findClaimByCenter(centerX, centerZ)) != null) {
                        try {
                            this.plugin.queueMapUpdateForClaim(world.getName(), updated);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                if (changed) {
                    upkeep.markDirty();
                }
                continue;
            }
            if (st.isPendingRemoveBlock() && DeityLandProtectionUpkeepTickingSystem.tryRemoveDeityLandProtectionBlock(world, centerX, centerY, centerZ)) {
                st.setPendingRemoveBlock(false);
                upkeep.markDirty();
            }
            ItemContainer container = null;
            try {
                long chunkIndex = ChunkUtil.indexChunkFromBlock((int)centerX, (int)centerZ);
                WorldChunk chunk = world.getChunkIfInMemory(chunkIndex);
                if (chunk == null) {
                    continue;
                }
                BlockState blockState = world.getState(centerX, centerY, centerZ, true);
                if (blockState instanceof ItemContainerState) {
                    container = ((ItemContainerState)blockState).getItemContainer();
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            boolean changed = false;
            boolean active = st.getProtectionUntilMs() > now;
            if (!active && DeityLandProtectionUpkeepTickingSystem.consumeOneFromAnySlot(container, ESSENCE_ITEM_ID)) {
                st.setProtectionUntilMs(now + essenceFeedDurationMs);
                st.setGraceUntilMs(0L);
                st.setGraceCountdownLastSecond(0L);
                active = true;
                changed = true;
            }
            // Remaining feed is computed from current container essence and shown in upkeep UI.
            long totalFeedMs = (long)DeityLandProtectionUpkeepTickingSystem.countItemTotal(container, ESSENCE_ITEM_ID) * essenceFeedDurationMs;
            if (st.getTotalFeedDurationMs() != totalFeedMs) {
                st.setTotalFeedDurationMs(totalFeedMs);
                changed = true;
            }
            int targetRadius = configuredBaseRadius > 0 ? configuredBaseRadius : liveClaim.getRadius();
            int currentRadius = liveClaim.getRadius();
            if (targetRadius != currentRadius) {
                boolean canResize = targetRadius < currentRadius || !claims.intersectsAnyExceptOwner(liveClaim.getOwner(), centerX, centerZ, targetRadius);
                if (canResize) {
                    Claim updated;
                    try {
                        this.plugin.queueMapUpdateForClaim(world.getName(), liveClaim);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (claims.updateClaimRadius(centerX, centerZ, targetRadius) && (updated = claims.findClaimByCenter(centerX, centerZ)) != null) {
                        try {
                            this.plugin.queueMapUpdateForClaim(world.getName(), updated);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        liveClaim = updated;
                    }
                }
            }
            if (active) {
                if (st.getGraceUntilMs() > 0L || st.getGraceCountdownLastSecond() > 0L) {
                    st.setGraceUntilMs(0L);
                    st.setGraceCountdownLastSecond(0L);
                    changed = true;
                }
                if (changed) {
                    upkeep.markDirty();
                }
                continue;
            }
            if (st.getGraceUntilMs() <= 0L) {
                st.setGraceUntilMs(now + graceMs);
                st.setGraceCountdownLastSecond(0L);
                changed = true;
                this.plugin.clearBorderForClaim(centerX, centerZ);
            }
            long graceUntil = st.getGraceUntilMs();
            if (graceUntil > now) {
                long secondsRemaining = (graceUntil - now + 999L) / 1000L;
                long lastSent = st.getGraceCountdownLastSecond();
                if (secondsRemaining <= 30L && lastSent != secondsRemaining) {
                    st.setGraceCountdownLastSecond(secondsRemaining);
                    changed = true;
                    PlayerRef ownerRef = DeityLandProtectionUpkeepTickingSystem.findPlayer(world, liveClaim.getOwner());
                    if (ownerRef != null) {
                        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(ownerRef);
                        EventTitleUtil.showEventTitleToPlayer((PlayerRef)ownerRef, (Message)Message.raw((String)DeityLandProtectionText.DeityLandProtectionDestroyTitlePrimary(lang, (int)secondsRemaining)), (Message)Message.raw((String)DeityLandProtectionText.DeityLandProtectionDestroyTitleSecondary(lang)), (boolean)true, null, (float)1.1f, (float)0.0f, (float)0.2f);
                    }
                }
                if (changed) {
                    upkeep.markDirty();
                }
                continue;
            }
            if (changed) {
                upkeep.markDirty();
            }
            this.expireClaim(world, liveClaim, st, upkeep);
        }
    }

    private static int countItemTotal(ItemContainer container, String itemId) {
        if (container == null || itemId == null || itemId.isEmpty()) {
            return 0;
        }
        int total = 0;
        short cap = container.getCapacity();
        short slot = 0;
        while (slot < cap) {
            ItemStack st = container.getItemStack(slot);
            if (st != null && !st.isEmpty() && st.isValid() && itemId.equals(st.getItemId())) {
                total += Math.max(0, st.getQuantity());
            }
            slot = (short)(slot + 1);
        }
        return total;
    }

    private static boolean consumeOneFromAnySlot(ItemContainer container, String itemId) {
        if (container == null || itemId == null || itemId.isEmpty()) {
            return false;
        }
        short cap = container.getCapacity();
        short slot = 0;
        while (slot < cap) {
            if (DeityLandProtectionUpkeepTickingSystem.consumeOneFromSlot(container, slot, itemId)) {
                return true;
            }
            slot = (short)(slot + 1);
        }
        return false;
    }

    private static boolean consumeOneFromSlot(ItemContainer container, short slot, String itemId) {
        if (container == null || itemId == null || itemId.isEmpty()) {
            return false;
        }
        if (slot < 0 || slot >= container.getCapacity()) {
            return false;
        }
        ItemStack st = container.getItemStack(slot);
        if (st == null || st.isEmpty() || !st.isValid()) {
            return false;
        }
        if (!itemId.equals(st.getItemId())) {
            return false;
        }
        int qty = st.getQuantity();
        if (qty <= 0) {
            return false;
        }
        ItemStack replacement = st.withQuantity(qty - 1);
        container.setItemStackForSlot(slot, replacement);
        return true;
    }

    private void expireClaim(World world, Claim claim, DeityLandProtectionUpkeepState st, DeityLandProtectionUpkeepStore upkeep) {
        if (world == null || claim == null || st == null || upkeep == null) {
            return;
        }
        try {
            this.plugin.queueMapUpdateForClaim(world.getName(), claim);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.plugin.getClaimStore().removeClaimAt(claim.getCenterX(), claim.getCenterZ());
        this.plugin.clearBorderForClaim(claim.getCenterX(), claim.getCenterZ());
        if (!DeityLandProtectionUpkeepTickingSystem.tryRemoveDeityLandProtectionBlock(world, claim.getCenterX(), claim.getCenterY(), claim.getCenterZ())) {
            st.setPendingRemoveBlock(true);
        }
        upkeep.remove(claim.getCenterX(), claim.getCenterZ());
        upkeep.markDirty();
    }

    private static PlayerRef findPlayer(World world, UUID playerUuid) {
        if (world == null || playerUuid == null) {
            return null;
        }
        try {
            for (PlayerRef pr : world.getPlayerRefs()) {
                UUID u;
                if (pr == null || !playerUuid.equals(u = pr.getUuid())) continue;
                return pr;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static boolean tryRemoveDeityLandProtectionBlock(World world, int x, int y, int z) {
        if (world == null) {
            return false;
        }
        long chunkIndex = ChunkUtil.indexChunkFromBlock((int)x, (int)z);
        WorldChunk chunk = world.getChunkIfInMemory(chunkIndex);
        if (chunk == null) {
            return false;
        }
        chunk.setBlock(x, y, z, BlockType.EMPTY);
        return true;
    }
}



