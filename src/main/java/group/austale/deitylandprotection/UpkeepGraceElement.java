package group.austale.deitylandprotection;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class UpkeepGraceElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;

    public UpkeepGraceElement(DeityLandProtectionPlugin plugin, int centerX, int centerZ) {
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
        LangPreferenceManager.Language lang = this.plugin == null ? LangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        commands.set(selector + " #Name.TextSpans", Message.raw(this.buildTitle(lang)));
        commands.set(selector + " #Durability.Text", this.buildValue());
    }

    private String buildTitle(LangPreferenceManager.Language lang) {
        if (lang == LangPreferenceManager.Language.ES) {
            return "Gracia";
        }
        if (lang == LangPreferenceManager.Language.FR) {
            return "Grâce";
        }
        return "Grace";
    }

    private String buildValue() {
        if (this.plugin == null) {
            return "0m";
        }
        UpkeepStore store = this.plugin.getUpkeepStore();
        UpkeepState st = store == null ? null : store.get(this.centerX, this.centerZ);
        long now = System.currentTimeMillis();
        long graceUntil = st == null ? 0L : st.getGraceUntilMs();
        long remainingMs = graceUntil > now ? graceUntil - now : 0L;
        return UpkeepGraceElement.formatGraceDuration(remainingMs);
    }

    private static String formatGraceDuration(long ms) {
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
