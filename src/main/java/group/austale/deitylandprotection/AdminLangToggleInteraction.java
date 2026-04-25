package group.austale.deitylandprotection;

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

public final class AdminLangToggleInteraction
extends ChoiceInteraction {
    private final DeityLandProtectionPlugin plugin;

    public AdminLangToggleInteraction(DeityLandProtectionPlugin plugin) {
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
        LangPreferenceManager mgr = this.plugin.getLangPreferenceManager();
        if (mgr == null) {
            return;
        }
        LangPreferenceManager.Language current = mgr.getEffectiveLanguage(actor);
        LangPreferenceManager.Language next = current == LangPreferenceManager.Language.ES ? LangPreferenceManager.Language.EN : LangPreferenceManager.Language.ES;
        mgr.setOverride(actor, next);
        this.plugin.sendPlayerMessageImmediate(playerRef, Text.langUpdated(next, next));
        Player playerEntity = (Player)store.getComponent(ref, Player.getComponentType());
        if (playerEntity == null) {
            return;
        }
        PageManager pages = playerEntity.getPageManager();
        if (pages == null) {
            return;
        }
        pages.openCustomPage(ref, store, (CustomUIPage)new AdminPage(this.plugin, playerRef));
    }
}



