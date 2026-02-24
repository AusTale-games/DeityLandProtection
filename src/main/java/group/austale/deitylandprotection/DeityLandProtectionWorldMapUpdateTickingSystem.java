/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.component.system.DelayedSystem
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.longs.LongSet;

public final class DeityLandProtectionWorldMapUpdateTickingSystem
extends DelayedSystem<ChunkStore> {
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionWorldMapUpdateTickingSystem(DeityLandProtectionPlugin plugin) {
        super(3.0f);
        this.plugin = plugin;
    }

    public void delayedTick(float v, int i, Store<ChunkStore> store) {
        if (store == null) {
            return;
        }
        World world = ((ChunkStore)store.getExternalData()).getWorld();
        LongSet chunks = this.plugin.pollMapUpdateChunks(world.getName());
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        world.execute(() -> {
            world.getWorldMapManager().clearImagesInChunks(chunks);
            for (PlayerRef playerRef : world.getPlayerRefs()) {
                Player player;
                Ref ref;
                if (playerRef == null || (ref = playerRef.getReference()) == null || (player = (Player)world.getEntityStore().getStore().getComponent(ref, Player.getComponentType())) == null) continue;
                player.getWorldMapTracker().clearChunks(chunks);
            }
        });
    }
}



