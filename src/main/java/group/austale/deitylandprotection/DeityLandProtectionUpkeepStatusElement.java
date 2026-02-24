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

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import group.austale.deitylandprotection.DeityLandProtectionUpkeepState;
import group.austale.deitylandprotection.DeityLandProtectionUpkeepStore;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class DeityLandProtectionUpkeepStatusElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;

    public DeityLandProtectionUpkeepStatusElement(DeityLandProtectionPlugin plugin, int centerX, int centerZ) {
        this.plugin = plugin;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.interactions = new ChoiceInteraction[0];
    }

    public void addButton(UICommandBuilder commands, UIEventBuilder events, String selector, PlayerRef playerRef) {
        if (commands == null || selector == null) {
            return;
        }
        commands.append("#ElementList", ELEMENT_LAYOUT);
        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin == null ? DeityLandProtectionLangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        String subtitle = this.buildSubtitle(lang);
        commands.set(selector + " #Name.TextSpans", Message.raw((String)""));
        commands.set(selector + " #Durability.Text", subtitle);
    }

    private String buildSubtitle(DeityLandProtectionLangPreferenceManager.Language lang) {
        if (this.plugin == null) {
            return "";
        }
        Claim claim = this.plugin.getClaimStore().findClaimByCenter(this.centerX, this.centerZ);
        if (claim == null) {
            return "";
        }
        DeityLandProtectionUpkeepStore store = this.plugin.getUpkeepStore();
        DeityLandProtectionUpkeepState st = store == null ? null : store.get(this.centerX, this.centerZ);
        long now = System.currentTimeMillis();
        long until = st == null ? 0L : st.getProtectionUntilMs();
        long totalFeedMs = st == null ? 0L : st.getTotalFeedDurationMs();
        long graceUntil = st == null ? 0L : st.getGraceUntilMs();
        StringBuilder sb = new StringBuilder();
        if (lang == DeityLandProtectionLangPreferenceManager.Language.ES) {
            sb.append("Esencia actual: ").append(until > now ? DeityLandProtectionUpkeepStatusElement.formatDuration(until - now) : "0m").append(" restantes");
            sb.append("\n");
            sb.append("Total alimentado: ").append(DeityLandProtectionUpkeepStatusElement.formatFeedDuration(totalFeedMs));
        } else {
            sb.append("Current Essence: ").append(until > now ? DeityLandProtectionUpkeepStatusElement.formatDuration(until - now) : "0m").append(" remaining");
            sb.append("\n");
            sb.append("Total Essence Feed: ").append(DeityLandProtectionUpkeepStatusElement.formatFeedDuration(totalFeedMs));
        }
        if (graceUntil > now) {
            sb.append("\n");
            sb.append(lang == DeityLandProtectionLangPreferenceManager.Language.ES ? "Gracia: " : "Grace: ").append(DeityLandProtectionUpkeepStatusElement.formatDuration(graceUntil - now));
        }
        return sb.toString();
    }

    private static String formatFeedDuration(long ms) {
        if (ms <= 0L) {
            return "0m";
        }
        return DeityLandProtectionUpkeepStatusElement.formatDuration(ms);
    }

    private static String formatDuration(long ms) {
        if (ms <= 0L) {
            return "0m";
        }
        long totalSeconds = ms / 1000L;
        long minutes = totalSeconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long remMinutes = minutes % 60L;
        long remHours = hours % 24L;
        if (days > 0L) {
            return days + "d " + remHours + "h";
        }
        if (hours > 0L) {
            return hours + "h " + remMinutes + "m";
        }
        return minutes + "m";
    }
}



