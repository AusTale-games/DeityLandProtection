package group.austale.deitylandprotection;

import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.Text;
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
        if (commands == null || selector == null) {
            return;
        }
        commands.append("#ElementList", ELEMENT_LAYOUT);
        String iconId = this.plugin == null ? null : this.plugin.getDeityLandProtectionItemId();
        if (iconId != null && !iconId.isEmpty()) {
            commands.set(selector + " #Icon.ItemId", iconId);
        }
        LangPreferenceManager.Language lang = this.plugin == null
                ? LangPreferenceManager.Language.EN
                : this.plugin.getEffectiveLanguage(playerRef);
        if (this.header) {
            commands.set(selector + " #Name.TextSpans", Message.raw(Text.uiFriendsTrust(lang)));
            commands.set(selector + " #Durability.Text", Text.uiCyclePermsHint(lang));
            return;
        }
        String who = "<none>";
        if (this.target != null) {
            who = resolveDisplayName(this.target);
        }
        commands.set(selector + " #Name.TextSpans", Message.raw(who));
        commands.set(selector + " #Durability.Text", DeityLandProtectionTrustElement.permsToText(this.perms));
    }

    private String resolveDisplayName(UUID targetId) {
        if (this.plugin == null) {
            return targetId.toString();
        }
        String cached = this.plugin.getKnownUsername(targetId);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        Map<UUID, String> inClaim = this.plugin.getPlayersInClaim(this.centerX, this.centerZ);
        String present = inClaim.get(targetId);
        if (present != null && !present.isEmpty()) {
            return present;
        }
        return targetId.toString();
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



