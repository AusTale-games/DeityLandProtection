package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlayerClaimOpenInteraction;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.UpkeepState;
import group.austale.deitylandprotection.UpkeepStore;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class DeityLandProtectionPlayerClaimElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final Claim claim;

    public DeityLandProtectionPlayerClaimElement(DeityLandProtectionPlugin plugin, Claim claim) {
        this.plugin = plugin;
        this.claim = claim;
        this.interactions = plugin != null && claim != null ? new ChoiceInteraction[]{new DeityLandProtectionPlayerClaimOpenInteraction(plugin, claim.getCenterX(), claim.getCenterZ())} : new ChoiceInteraction[0];
    }

    public void addButton(UICommandBuilder commands, UIEventBuilder events, String selector, PlayerRef playerRef) {
        String iconId;
        if (commands == null || selector == null) {
            return;
        }
        commands.append("#ElementList", ELEMENT_LAYOUT);
        if (this.plugin != null && (iconId = this.plugin.getDeityLandProtectionItemId()) != null && !iconId.isEmpty()) {
            commands.set(selector + " #Icon.ItemId", iconId);
        }
        LangPreferenceManager.Language lang = this.plugin == null ? LangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        String title = this.buildTitle(lang);
        String subtitle = this.buildSubtitle(lang);
        commands.set(selector + " #Name.TextSpans", Message.raw(title));
        commands.set(selector + " #Durability.Text", subtitle);
    }

    private String buildTitle(LangPreferenceManager.Language lang) {
        if (this.claim == null) {
            return "";
        }
        int y = this.claim.getCenterY();
        String yStr = y == Integer.MIN_VALUE ? "?" : String.valueOf(y);
        return "X:" + this.claim.getCenterX() + " Y:" + yStr + " Z:" + this.claim.getCenterZ();
    }

    private String buildSubtitle(LangPreferenceManager.Language lang) {
        if (this.plugin == null || this.claim == null) {
            return "";
        }
        int radius = this.claim.getRadius();
        int members = 0;
        try {
            members = this.claim.getTrusted().size();
        }
        catch (Exception ignored) {
            // Trusted map access can race during reload; treat as zero members.
        }
        String pvp = this.claim.isPvpEnabled() ? Text.borderOnShort(lang) : Text.borderOffShort(lang);
        UpkeepStore upkeep = this.plugin.getUpkeepStore();
        UpkeepState st = upkeep == null ? null : upkeep.get(this.claim.getCenterX(), this.claim.getCenterZ());
        long now = System.currentTimeMillis();
        long until = st == null ? 0L : st.getProtectionUntilMs();
        long totalFeedMs = st == null ? 0L : st.getTotalFeedDurationMs();
        long graceUntil = st == null ? 0L : st.getGraceUntilMs();
        String time = until > now ? DeityLandProtectionPlayerClaimElement.formatDuration(until - now) : "0m";
        String grace = graceUntil > now ? DeityLandProtectionPlayerClaimElement.formatDuration(graceUntil - now) : null;
        String feed = DeityLandProtectionPlayerClaimElement.formatFeedDuration(totalFeedMs);
        StringBuilder sb = new StringBuilder();
        if (lang == LangPreferenceManager.Language.ES) {
            sb.append("Radio: ").append(radius);
            sb.append(" | Esencia: ").append(time);
            sb.append("\n");
            sb.append("PvP: ").append(pvp);
            sb.append(" | Alimentado: ").append(feed);
            sb.append(" | Miembros: ").append(members);
            if (grace != null) {
                sb.append(" | Gracia: ").append(grace);
            }
            return sb.toString();
        }
        sb.append("Radius: ").append(radius);
        sb.append(" | Essence: ").append(time);
        sb.append("\n");
        sb.append("PvP: ").append(pvp);
        sb.append(" | Feed: ").append(feed);
        sb.append(" | Members: ").append(members);
        if (grace != null) {
            sb.append(" | Grace: ").append(grace);
        }
        return sb.toString();
    }

    private static String formatFeedDuration(long ms) {
        if (ms <= 0L) {
            return "0m";
        }
        return DeityLandProtectionPlayerClaimElement.formatDuration(ms);
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



