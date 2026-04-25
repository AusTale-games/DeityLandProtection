package group.austale.deitylandprotection;

import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.Text;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class DeityLandProtectionUpkeepSlotHintElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final Claim claim;

    public DeityLandProtectionUpkeepSlotHintElement(DeityLandProtectionPlugin plugin, Claim claim) {
        this.plugin = plugin;
        this.claim = claim;
        this.interactions = new ChoiceInteraction[0];
    }

    public void addButton(UICommandBuilder commands, UIEventBuilder events, String selector, PlayerRef playerRef) {
        String iconId;
        if (commands == null || selector == null) {
            return;
        }
        commands.append("#ElementList", ELEMENT_LAYOUT);
        iconId = this.plugin == null ? null : this.plugin.getUpkeepEssenceItemIdForClaim(this.claim);
        if (iconId != null && !iconId.isEmpty()) {
            commands.set(selector + " #Icon.ItemId", iconId);
        }
        LangPreferenceManager.Language lang = this.plugin == null ? LangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        commands.set(selector + " #Name.TextSpans", Message.raw(this.buildTitle(lang)));
        commands.set(selector + " #Durability.Text", this.buildSubtitle(lang));
    }

    private String buildTitle(LangPreferenceManager.Language lang) {
        String baseTitle = this.plugin == null ? Text.uiSlot0Title(lang) : this.plugin.getUpkeepEssenceTitleForClaim(this.claim, lang);
        if (baseTitle.isEmpty()) {
            return "";
        }
        int cost = this.plugin == null ? 1 : this.plugin.getUpkeepEssenceCostPerHourForClaim(this.claim);
        return cost + " x " + baseTitle;
    }

    private String buildSubtitle(LangPreferenceManager.Language lang) {
        return Text.uiSlot0Subtitle(lang);
    }
}



