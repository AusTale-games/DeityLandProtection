package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionTrustListPage;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.builtin.crafting.window.ProcessingBenchWindow;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;

public final class DeityLandProtectionOpenUpkeepInteraction
extends ChoiceInteraction {
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;

    public DeityLandProtectionOpenUpkeepInteraction(DeityLandProtectionPlugin plugin, int centerX, int centerZ) {
        this.plugin = plugin;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public void run(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
        if (this.plugin == null || store == null || ref == null || playerRef == null) {
            return;
        }
        Claim claim = this.plugin.getClaimStore().findClaimByCenter(this.centerX, this.centerZ);
        if (claim == null || claim.getCenterY() == Integer.MIN_VALUE) {
            return;
        }
        String allowedEssenceItemId = this.plugin.getUpkeepEssenceItemIdForClaim(claim);
        int x = claim.getCenterX();
        int y = claim.getCenterY();
        int z = claim.getCenterZ();
        Player playerEntity = (Player)store.getComponent(ref, Player.getComponentType());
        if (playerEntity == null) {
            return;
        }
        PageManager pages = playerEntity.getPageManager();
        if (pages == null) {
            return;
        }
        if (!this.plugin.isUpkeepEnabled()) {
            pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
            return;
        }
        World world = ((EntityStore)store.getExternalData()).getWorld();
        if (world == null) {
            return;
        }
        try {
            ChunkStore chunkStore = world.getChunkStore();
            Ref chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
            if (chunkRef == null || !chunkRef.isValid()) {
                pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                return;
            }
            Store chunkStoreStore = chunkStore.getStore();
            BlockComponentChunk bcc = (BlockComponentChunk)chunkStoreStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
            if (bcc == null) {
                pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                return;
            }
            Ref entityRef = bcc.getEntityReference(ChunkUtil.indexBlockInColumn(x, y, z));
            if (entityRef == null || !entityRef.isValid()) {
                pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                return;
            }
            ProcessingBenchBlock processingBenchBlock = (ProcessingBenchBlock)chunkStoreStore.getComponent(entityRef, ProcessingBenchBlock.getComponentType());
            if (processingBenchBlock == null) {
                pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                return;
            }
            BenchBlock benchBlock = (BenchBlock)chunkStoreStore.getComponent(entityRef, BenchBlock.getComponentType());
            if (benchBlock == null) {
                pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                return;
            }
            BlockModule.BlockStateInfo blockStateInfo = (BlockModule.BlockStateInfo)chunkStoreStore.getComponent(entityRef, BlockModule.BlockStateInfo.getComponentType());
            BlockType blockType = world.getBlockType(x, y, z);
            BlockAccessor blockAccessor = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
            if (blockAccessor == null) {
                pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                return;
            }
            int rotationIndex = blockAccessor.getRotationIndex(x, y, z);
            try {
                ItemContainer c2 = processingBenchBlock.getItemContainer();
                if (c2 != null && c2.getCapacity() > 0) {
                    short cap = c2.getCapacity();
                    short inputCapacity = (short)Math.max(0, cap - 1);
                    short feedSlotCount = (short)Math.min((int)inputCapacity, 2);
                    for (short slot = 0; slot < feedSlotCount; slot = (short)(slot + 1)) {
                        short targetSlot = slot;
                        c2.setSlotFilter(FilterActionType.ADD, targetSlot, (actionType, container, slotArg, itemStack) -> {
                            if (itemStack == null || itemStack.isEmpty() || !itemStack.isValid()) {
                                return true;
                            }
                            String id = itemStack.getItemId();
                            return allowedEssenceItemId.equals(id) || this.plugin.isUpgradeMaterialItemId(id);
                        });
                    }
                }
            }
            catch (Throwable innerIgnored) {
                // empty catch block
            }
            ProcessingBenchWindow window = new ProcessingBenchWindow(processingBenchBlock, benchBlock, blockStateInfo, x, y, z, rotationIndex, blockType);
            UUID uuid = playerRef.getUuid();
            Map windows = benchBlock.getWindows();
            if (windows.putIfAbsent(uuid, window) == null) {
                processingBenchBlock.updateFuelValues(benchBlock.getWindows());
                boolean ok = pages.setPageWithWindows(ref, store, Page.Bench, true, new Window[]{window});
                if (ok) {
                    window.registerCloseEvent(ev -> {
                        windows.remove(uuid, window);
                        try {
                            pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    });
                } else {
                    windows.remove(uuid, window);
                    pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                }
            }
        }
        catch (Throwable ignored) {
            try {
                pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }
}



