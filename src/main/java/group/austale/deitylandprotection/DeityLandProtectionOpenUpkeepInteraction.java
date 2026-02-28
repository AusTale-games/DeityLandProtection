/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.event.EventPriority
 *  com.hypixel.hytale.math.util.ChunkUtil
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.protocol.packets.interface_.Page
 *  com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction
 *  com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow
 *  com.hypixel.hytale.server.core.entity.entities.player.windows.Window
 *  com.hypixel.hytale.server.core.inventory.Inventory
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer
 *  com.hypixel.hytale.server.core.inventory.container.ItemContainer
 *  com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer
 *  com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk
 *  com.hypixel.hytale.server.core.universe.world.meta.BlockState
 *  com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionTrustListPage;
import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.builtin.crafting.window.ProcessingBenchWindow;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
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
            ItemContainer c2;
            ProcessingBenchState benchState;
            Vector3i pos = new Vector3i(x, y, z);
            BlockType actualBlockType = null;
            try {
                actualBlockType = world.getBlockType(pos);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            BlockState state = world.getState(x, y, z, true);
            if (!(state instanceof ProcessingBenchState)) {
                try {
                    WorldChunk chunkWorld = world.getChunk(ChunkUtil.indexChunkFromBlock((int)x, (int)z));
                    if (chunkWorld != null) {
                        BlockType bt;
                        BlockType blockType = bt = actualBlockType != null ? actualBlockType : world.getBlockType(pos);
                        if (bt != null) {
                            chunkWorld.setBlock(x, y, z, bt);
                            state = world.getState(x, y, z, true);
                        }
                    }
                }
                catch (Throwable chunkWorld) {
                    // empty catch block
                }
                if (!(state instanceof ProcessingBenchState)) {
                    pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                    return;
                }
            }
            benchState = (ProcessingBenchState)state;
            if (actualBlockType == null) {
                actualBlockType = world.getBlockType(pos);
            }
            if (actualBlockType == null) {
                pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                return;
            }
            try {
                if (actualBlockType.getBench() == null || !actualBlockType.getBench().equals(benchState.getBench())) {
                    if (!benchState.initialize(actualBlockType)) {
                        pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
                        return;
                    }
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                c2 = benchState.getItemContainer();
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
            catch (Throwable c3) {
                // empty catch block
            }
            ProcessingBenchWindow window = new ProcessingBenchWindow(benchState);
            UUID uuid = playerRef.getUuid();
            Map windows = benchState.getWindows();
            if (windows.putIfAbsent(uuid, window) == null) {
                benchState.updateFuelValues();
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



