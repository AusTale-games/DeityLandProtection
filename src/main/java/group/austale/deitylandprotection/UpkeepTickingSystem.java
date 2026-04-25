package group.austale.deitylandprotection;

import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import java.util.Iterator;
import java.util.UUID;

public final class UpkeepTickingSystem
extends DelayedSystem<ChunkStore> {
    private static final long ONE_HOUR_MS = 3600000L;
    private static final String SLUMBERING_OUTPUT_ITEM_ID = "Plant_Fruit_Apple";
    private static final String OUTLANDER_OUTPUT_ITEM_ID = "Plant_Fruit_Poison";

    private final DeityLandProtectionPlugin plugin;

    public UpkeepTickingSystem(DeityLandProtectionPlugin plugin) {
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
        UpkeepStore upkeep = this.plugin.getUpkeepStore();
        if (claims == null || upkeep == null) {
            return;
        }
        boolean upkeepEnabled = this.plugin.isUpkeepEnabled();
        int configuredBaseRadius = this.plugin.getClaimRadius();
        long now = System.currentTimeMillis();
        long graceMs = this.plugin.getUpkeepGraceMs();
        boolean reconcilePending = claims.consumeTerritoryReconcilePending();
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
            if (liveClaim == null) {
                continue;
            }
            if (reconcilePending && claims.reconcileClaimTerritory(centerX, centerZ)) {
                try {
                    this.plugin.queueMapUpdateForClaim(world.getName(), liveClaim);
                }
                catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
            }
            if (centerY == Integer.MIN_VALUE) {
                continue;
            }
            UpkeepState st = upkeep.getOrCreate(centerX, centerZ);
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
                if (st.getProcessEndsAtMs() > 0L) {
                    st.setProcessEndsAtMs(0L);
                    changed = true;
                }
                if (st.getProcessedEssenceCarryCount() > 0) {
                    st.setProcessedEssenceCarryCount(0);
                    changed = true;
                }
                if (st.getObservedOutputQuantity() != -1) {
                    st.setObservedOutputQuantity(-1);
                    changed = true;
                }
                if (configuredBaseRadius > 0 && liveClaim.getRadius() > configuredBaseRadius) {
                    Claim updated;
                    try {
                        this.plugin.queueMapUpdateForClaim(world.getName(), liveClaim);
                    }
                    catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
                    if (claims.updateClaimRadius(centerX, centerZ, configuredBaseRadius) && (updated = claims.findClaimByCenter(centerX, centerZ)) != null) {
                        try {
                            this.plugin.queueMapUpdateForClaim(world.getName(), updated);
                        }
                        catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
                    }
                }
                if (changed) {
                    upkeep.markDirty();
                }
                continue;
            }
            if (st.isPendingRemoveBlock() && UpkeepTickingSystem.tryRemoveDeityLandProtectionBlock(world, centerX, centerY, centerZ)) {
                st.setPendingRemoveBlock(false);
                upkeep.markDirty();
            }
            ItemContainer container = null;
            BenchBlock benchBlock = null;
            try {
                long chunkIndex = ChunkUtil.indexChunkFromBlock((int)centerX, (int)centerZ);
                WorldChunk chunk = world.getChunkIfInMemory(chunkIndex);
                if (chunk == null) {
                    continue;
                }
                ChunkStore chunkStoreObj = world.getChunkStore();
                Ref chunkRef = chunkStoreObj.getChunkReference(chunkIndex);
                if (chunkRef != null && chunkRef.isValid()) {
                    Store chunkStore = chunkStoreObj.getStore();
                    BlockComponentChunk bcc = (BlockComponentChunk)chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
                    if (bcc != null) {
                        Ref entityRef = bcc.getEntityReference(ChunkUtil.indexBlockInColumn(centerX, centerY, centerZ));
                        if (entityRef != null && entityRef.isValid()) {
                            ProcessingBenchBlock pbb = (ProcessingBenchBlock)chunkStore.getComponent(entityRef, ProcessingBenchBlock.getComponentType());
                            benchBlock = (BenchBlock)chunkStore.getComponent(entityRef, BenchBlock.getComponentType());
                            if (pbb != null) {
                                container = pbb.getItemContainer();
                            } else {
                                ItemContainerBlock icb = (ItemContainerBlock)chunkStore.getComponent(entityRef, ItemContainerBlock.getComponentType());
                                if (icb != null) {
                                    container = icb.getItemContainer();
                                }
                            }
                        }
                    }
                }
            }
            catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
            boolean changed = false;
            if (benchBlock != null) {
                int benchTier = benchBlock.getTierLevel();
                if (benchTier < 1) {
                    benchTier = 1;
                } else if (benchTier > 4) {
                    benchTier = 4;
                }
                if (st.getUpgradeTier() != benchTier) {
                    st.setUpgradeTier(benchTier);
                    changed = true;
                }
            } else if (TierSystem.tryUpgradeClaimTier(this.plugin, claims, world, liveClaim, container, st)) {
                changed = true;
            }
            String upkeepOutputItemId = this.plugin.isOutlanderClaimItemId(liveClaim.getDeityItemId()) ? OUTLANDER_OUTPUT_ITEM_ID : SLUMBERING_OUTPUT_ITEM_ID;
            int baseEssenceCostPerHour = Math.max(1, this.plugin.getUpkeepEssenceCostPerHour());
            int effectiveEssenceCostPerHour = this.plugin.getUpkeepEssenceCostPerHourForTier(st.getUpgradeTier());
            int processedRequiredPerHour = UpkeepTickingSystem.divideCeil(Math.max(1, effectiveEssenceCostPerHour), baseEssenceCostPerHour);
            if (st.getProcessEndsAtMs() != 0L) {
                st.setProcessEndsAtMs(0L);
                changed = true;
            }
            if (UpkeepTickingSystem.trackNativeProcessedOutput(container, st, upkeepOutputItemId, processedRequiredPerHour, now)) {
                changed = true;
            }
            boolean active = st.getProtectionUntilMs() > now;
            int targetRadius = this.plugin.getClaimRadiusForTier(st.getUpgradeTier());
            if (targetRadius <= 0) {
                targetRadius = configuredBaseRadius > 0 ? configuredBaseRadius : liveClaim.getRadius();
            }
            int currentRadius = liveClaim.getRadius();
            if (targetRadius != currentRadius) {
                Claim updated;
                try {
                    this.plugin.queueMapUpdateForClaim(world.getName(), liveClaim);
                }
                catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
                if (claims.updateClaimRadius(centerX, centerZ, targetRadius) && (updated = claims.findClaimByCenter(centerX, centerZ)) != null) {
                    try {
                        this.plugin.queueMapUpdateForClaim(world.getName(), updated);
                    }
                    catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
                    liveClaim = updated;
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
                    PlayerRef ownerRef = UpkeepTickingSystem.findPlayer(world, liveClaim.getOwner());
                    if (ownerRef != null) {
                        LangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(ownerRef);
                        EventTitleUtil.showEventTitleToPlayer((PlayerRef)ownerRef, Message.raw(Text.DeityLandProtectionDestroyTitlePrimary(lang, (int)secondsRemaining)), Message.raw(Text.DeityLandProtectionDestroyTitleSecondary(lang)), true, null, 1.1f, 0.0f, 0.2f);
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

    private static int divideCeil(int numerator, int denominator) {
        int safeNumerator = Math.max(0, numerator);
        int safeDenominator = Math.max(1, denominator);
        return (safeNumerator + safeDenominator - 1) / safeDenominator;
    }

    private static short getOutputSlot(ItemContainer container) {
        if (container == null) {
            return -1;
        }
        short capacity = container.getCapacity();
        if (capacity <= 0) {
            return -1;
        }
        return (short)(capacity - 1);
    }

    private static int getOutputQuantity(ItemContainer container, short outputSlot, String outputItemId) {
        if (container == null || outputItemId == null || outputItemId.isEmpty()) {
            return 0;
        }
        if (outputSlot < 0 || outputSlot >= container.getCapacity()) {
            return 0;
        }
        ItemStack stack = container.getItemStack(outputSlot);
        if (stack == null || stack.isEmpty() || !stack.isValid()) {
            return 0;
        }
        if (!outputItemId.equals(stack.getItemId())) {
            return 0;
        }
        return Math.max(0, stack.getQuantity());
    }

    private static boolean applyProcessedEssenceCredits(UpkeepState state, int processedDelta, int processedRequiredPerHour, long now) {
        if (state == null || processedDelta <= 0) {
            return false;
        }
        int required = Math.max(1, processedRequiredPerHour);
        int carry = Math.max(0, state.getProcessedEssenceCarryCount()) + processedDelta;
        int creditedHours = carry / required;
        state.setProcessedEssenceCarryCount(carry % required);
        if (creditedHours <= 0) {
            return true;
        }
        long addedFeedMs = (long)creditedHours * ONE_HOUR_MS;
        state.setProtectionUntilMs(Math.max(now, state.getProtectionUntilMs()) + addedFeedMs);
        state.setTotalFeedDurationMs(state.getTotalFeedDurationMs() + addedFeedMs);
        state.setGraceUntilMs(0L);
        state.setGraceCountdownLastSecond(0L);
        return true;
    }

    private static boolean trackNativeProcessedOutput(ItemContainer container, UpkeepState state, String outputItemId, int processedRequiredPerHour, long now) {
        if (container == null || state == null || outputItemId == null || outputItemId.isEmpty()) {
            return false;
        }
        short outputSlot = UpkeepTickingSystem.getOutputSlot(container);
        if (outputSlot < 0) {
            return false;
        }
        int currentOutputQuantity = UpkeepTickingSystem.getOutputQuantity(container, outputSlot, outputItemId);
        int observedOutputQuantity = state.getObservedOutputQuantity();
        boolean changed = false;
        if (observedOutputQuantity < 0) {
            state.setObservedOutputQuantity(currentOutputQuantity);
            return true;
        }
        if (currentOutputQuantity > observedOutputQuantity && UpkeepTickingSystem.applyProcessedEssenceCredits(state, currentOutputQuantity - observedOutputQuantity, processedRequiredPerHour, now)) {
            changed = true;
        }
        if (currentOutputQuantity != observedOutputQuantity) {
            state.setObservedOutputQuantity(currentOutputQuantity);
            changed = true;
        }
        return changed;
    }

    private void expireClaim(World world, Claim claim, UpkeepState st, UpkeepStore upkeep) {
        if (world == null || claim == null || st == null || upkeep == null) {
            return;
        }
        try {
            this.plugin.queueMapUpdateForClaim(world.getName(), claim);
        }
        catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        this.plugin.getClaimStore().removeClaimAt(claim.getCenterX(), claim.getCenterZ());
        this.plugin.clearBorderForClaim(claim.getCenterX(), claim.getCenterZ());
        if (!UpkeepTickingSystem.tryRemoveDeityLandProtectionBlock(world, claim.getCenterX(), claim.getCenterY(), claim.getCenterZ())) {
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
        catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        return null;
    }

    private static boolean tryRemoveDeityLandProtectionBlock(World world, int x, int y, int z) {
        if (world == null) {
            return false;
        }
        long chunkIndex = ChunkUtil.indexChunkFromBlock((int)x, (int)z);
        WorldChunk chunk = world.getChunk(chunkIndex);
        if (chunk == null) {
            return false;
        }
        chunk.setBlock(x, y, z, BlockType.EMPTY);
        return true;
    }
}



