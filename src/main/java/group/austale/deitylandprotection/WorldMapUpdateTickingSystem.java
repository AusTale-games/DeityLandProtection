package group.austale.deitylandprotection;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldMapUpdateTickingSystem
extends DelayedSystem<ChunkStore> {
    private final DeityLandProtectionPlugin plugin;
    private final ConcurrentHashMap<String, Boolean> lastVisualStateByWorld = new ConcurrentHashMap<String, Boolean>();

    public WorldMapUpdateTickingSystem(DeityLandProtectionPlugin plugin) {
        super(3.0f);
        this.plugin = plugin;
    }

    public void delayedTick(float v, int i, Store<ChunkStore> store) {
        if (store == null) {
            return;
        }
        World world = ((ChunkStore)store.getExternalData()).getWorld();
        if (world == null) {
            return;
        }
        String worldName = world.getName();
        if (worldName != null && !worldName.isEmpty()) {
            boolean currentVisualEnabled = this.plugin != null && this.plugin.isMapClaimVisualEnabled();
            Boolean previous = this.lastVisualStateByWorld.put(worldName, currentVisualEnabled);
            if (previous == null || previous.booleanValue() != currentVisualEnabled) {
                this.plugin.queueMapUpdateForAllClaims(worldName);
            }
        }
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



