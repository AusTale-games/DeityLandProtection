package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.ClaimStore;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
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

public final class DeityLandProtectionTrustAddInteraction
extends ChoiceInteraction {
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;
    private final UUID target;
    private final String username;

    public DeityLandProtectionTrustAddInteraction(DeityLandProtectionPlugin plugin, int centerX, int centerZ, UUID target, String username) {
        this.plugin = plugin;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.target = target;
        this.username = username;
    }

    public void run(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
        if (this.plugin == null || store == null || ref == null || playerRef == null || this.target == null) {
            return;
        }
        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(playerRef);
        ClaimStore claims = this.plugin.getClaimStore();
        if (claims == null) {
            return;
        }
        Claim claim = claims.findClaimByCenter(this.centerX, this.centerZ);
        if (claim == null) {
            return;
        }
        UUID actor = playerRef.getUuid();
        if (actor == null) {
            return;
        }
        if (!this.plugin.isOpBypass(actor) && !claim.getOwner().equals(actor)) {
            this.plugin.sendPlayerMessage(playerRef, DeityLandProtectionText.onlyOwnerAddFriends(lang));
            return;
        }
        if (this.target.equals(claim.getOwner())) {
            this.plugin.sendPlayerMessage(playerRef, DeityLandProtectionText.ownerAlreadyFullAccess(lang));
            return;
        }
        if (this.username != null && !this.username.isEmpty()) {
            this.plugin.rememberUsername(this.target, this.username);
        }
        claim.setTrusted(this.target, 7);
        claims.markDirty();
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



