/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.math.vector.Transform
 *  com.hypixel.hytale.math.vector.Vector3d
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.ParticleUtil
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import group.austale.deitylandprotection.DeityLandProtectionTrustListPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
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
        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(playerRef);
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
            try {
                Transform t = playerRef.getTransform();
                if (t != null && t.getPosition() != null) {
                    Vector3d pos = t.getPosition();
                    ParticleUtil.spawnParticleEffect((String)"Impact_Critical", (Vector3d)new Vector3d(pos.x, pos.y + 1.0, pos.z), Collections.singletonList(playerRef.getReference()), store);
                }
            }
            catch (Exception t) {
                // empty catch block
            }
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



