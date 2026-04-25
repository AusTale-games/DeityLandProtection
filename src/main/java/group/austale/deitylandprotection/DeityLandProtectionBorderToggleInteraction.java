package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import group.austale.deitylandprotection.DeityLandProtectionTrustListPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;

public final class DeityLandProtectionBorderToggleInteraction
extends ChoiceInteraction {
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;

    public DeityLandProtectionBorderToggleInteraction(DeityLandProtectionPlugin plugin, int centerX, int centerZ) {
        this.plugin = plugin;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public void run(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
        if (this.plugin == null || store == null || ref == null || playerRef == null) {
            return;
        }
        LangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(playerRef);
        UUID actor = playerRef.getUuid();
        if (actor == null) {
            return;
        }
        Claim claim = this.plugin.getClaimStore().findClaimByCenter(this.centerX, this.centerZ);
        if (claim == null) {
            return;
        }
        long key = DeityLandProtectionPlugin.centerKey(this.centerX, this.centerZ);
        boolean currentlyOn = this.plugin.isBorderEnabled(actor, key);
        if (currentlyOn) {
            this.plugin.disableBorder(actor);
            this.plugin.sendPlayerMessageImmediate(playerRef, DeityLandProtectionText.borderOff(lang));
        } else {
            this.plugin.enableBorder(actor, this.centerX, this.centerZ);
            this.plugin.sendPlayerMessageImmediate(playerRef, DeityLandProtectionText.borderOn(lang));
        }
        Player playerEntity = (Player)store.getComponent(ref, Player.getComponentType());
        if (playerEntity == null) {
            return;
        }
        PageManager pages = playerEntity.getPageManager();
        if (pages == null) {
            return;
        }
        pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionTrustListPage(this.plugin, playerRef, this.centerX, this.centerZ));
    }
}



