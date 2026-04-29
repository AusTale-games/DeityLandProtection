package group.austale.deitylandprotection;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class UpkeepStatusElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;

    public UpkeepStatusElement(DeityLandProtectionPlugin plugin, int centerX, int centerZ) {
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
        commands.set(selector + " #Durability.Text", this.buildValue(lang));
    }

    private String buildTitle(LangPreferenceManager.Language lang) {
        if (lang == LangPreferenceManager.Language.ES) {
            return "Tiempo restante:";
        }
        if (lang == LangPreferenceManager.Language.FR) {
            return "Temps restant :";
        }
        return "Time Remaining:";
    }

    private String buildValue(LangPreferenceManager.Language lang) {
        if (this.plugin == null) {
            return "0d 0h 0m";
        }
        Claim claim = this.plugin.getClaimStore().findClaimByCenter(this.centerX, this.centerZ);
        if (claim == null) {
            return "0d 0h 0m";
        }
        UpkeepStore store = this.plugin.getUpkeepStore();
        UpkeepState st = store == null ? null : store.get(this.centerX, this.centerZ);
        long now = System.currentTimeMillis();
        long until = st == null ? 0L : st.getProtectionUntilMs();
        long remainingMs = until > now ? until - now : 0L;
        return UpkeepStatusElement.formatDuration(remainingMs);
    }

    private static String formatDuration(long ms) {
        if (ms <= 0L) {
            return "0d 0h 0m";
        }
        long totalMinutes = ms / 60000L;
        long days = totalMinutes / 1440L;
        long hours = totalMinutes % 1440L / 60L;
        long minutes = totalMinutes % 60L;
        return days + "d " + hours + "h " + minutes + "m";
    }
}



