package group.austale.deitylandprotection;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class PlayerClaimOpenInteraction
extends ChoiceInteraction {
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;

    public PlayerClaimOpenInteraction(DeityLandProtectionPlugin plugin, int centerX, int centerZ) {
        this.plugin = plugin;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public void run(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef) {
        if (this.plugin == null) {
            return;
        }
        Player playerEntity = (Player)store.getComponent(ref, Player.getComponentType());
        if (playerEntity == null) {
            return;
        }
        PageManager pages = playerEntity.getPageManager();
        if (pages == null) {
            return;
        }
        pages.openCustomPage(ref, store, (CustomUIPage)new TrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
    }
}



