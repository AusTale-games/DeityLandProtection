/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  javax.annotation.Nonnull
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionAdminPage;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public final class DeityLandProtectionAdminLangToggleInteraction
extends ChoiceInteraction {
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionAdminLangToggleInteraction(DeityLandProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    public void run(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef) {
        if (this.plugin == null) {
            return;
        }
        UUID actor = playerRef.getUuid();
        if (actor == null) {
            return;
        }
        DeityLandProtectionLangPreferenceManager mgr = this.plugin.getLangPreferenceManager();
        if (mgr == null) {
            return;
        }
        DeityLandProtectionLangPreferenceManager.Language current = mgr.getEffectiveLanguage(actor);
        DeityLandProtectionLangPreferenceManager.Language next = current == DeityLandProtectionLangPreferenceManager.Language.ES ? DeityLandProtectionLangPreferenceManager.Language.EN : DeityLandProtectionLangPreferenceManager.Language.ES;
        mgr.setOverride(actor, next);
        this.plugin.sendPlayerMessageImmediate(playerRef, DeityLandProtectionText.langUpdated(next, next));
        Player playerEntity = (Player)store.getComponent(ref, Player.getComponentType());
        if (playerEntity == null) {
            return;
        }
        PageManager pages = playerEntity.getPageManager();
        if (pages == null) {
            return;
        }
        pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionAdminPage(this.plugin, playerRef));
    }
}



