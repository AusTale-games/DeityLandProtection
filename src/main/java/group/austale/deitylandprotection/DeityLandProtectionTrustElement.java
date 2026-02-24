/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.server.core.Message
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction
 *  com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
 *  com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import group.austale.deitylandprotection.DeityLandProtectionTrustCycleInteraction;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.Map;
import java.util.UUID;

public final class DeityLandProtectionTrustElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;
    private final UUID target;
    private final int perms;
    private final boolean header;

    public DeityLandProtectionTrustElement(DeityLandProtectionPlugin plugin, int centerX, int centerZ, UUID target, int perms, boolean header) {
        this.plugin = plugin;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.target = target;
        this.perms = perms;
        this.header = header;
        this.interactions = header || target == null ? new ChoiceInteraction[0] : new ChoiceInteraction[]{new DeityLandProtectionTrustCycleInteraction(plugin, centerX, centerZ, target)};
    }

    public void addButton(UICommandBuilder commands, UIEventBuilder events, String selector, PlayerRef playerRef) {
        DeityLandProtectionLangPreferenceManager.Language lang;
        String iconId;
        if (commands == null || selector == null) {
            return;
        }
        commands.append("#ElementList", ELEMENT_LAYOUT);
        String string = iconId = this.plugin == null ? null : this.plugin.getDeityLandProtectionItemId();
        if (iconId != null && !iconId.isEmpty()) {
            commands.set(selector + " #Icon.ItemId", iconId);
        }
        DeityLandProtectionLangPreferenceManager.Language language = lang = this.plugin == null ? DeityLandProtectionLangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        if (this.header) {
            commands.set(selector + " #Name.TextSpans", Message.raw((String)DeityLandProtectionText.uiFriendsTrust(lang)));
            commands.set(selector + " #Durability.Text", DeityLandProtectionText.uiCyclePermsHint(lang));
            return;
        }
        String who = "<none>";
        if (this.target != null) {
            Map<UUID, String> inClaim;
            String present;
            String cached;
            String string2 = cached = this.plugin == null ? null : this.plugin.getKnownUsername(this.target);
            who = cached != null && !cached.isEmpty() ? cached : (this.plugin != null ? ((present = (inClaim = this.plugin.getPlayersInClaim(this.centerX, this.centerZ)).get(this.target)) != null && !present.isEmpty() ? present : this.target.toString()) : this.target.toString());
        }
        commands.set(selector + " #Name.TextSpans", Message.raw((String)who));
        commands.set(selector + " #Durability.Text", DeityLandProtectionTrustElement.permsToText(this.perms));
    }

    private static String permsToText(int perms) {
        boolean p = (perms & 1) != 0;
        boolean b = (perms & 2) != 0;
        boolean u = (perms & 4) != 0;
        StringBuilder sb = new StringBuilder();
        sb.append("P:").append(p ? "Y" : "N");
        sb.append(" B:").append(b ? "Y" : "N");
        sb.append(" U:").append(u ? "Y" : "N");
        return sb.toString();
    }
}



