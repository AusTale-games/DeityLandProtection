package group.austale.deitylandprotection;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class OpenUpkeepElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/DeityLandProtectionUpkeepButtonElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final Claim claim;

    public OpenUpkeepElement(DeityLandProtectionPlugin plugin, Claim claim, int centerX, int centerZ) {
        this.plugin = plugin;
        this.claim = claim;
        this.interactions = new ChoiceInteraction[]{new OpenUpkeepInteraction(plugin, centerX, centerZ)};
    }

    public void addButton(UICommandBuilder commands, UIEventBuilder events, String selector, PlayerRef playerRef) {
        String iconId;
        if (commands == null || selector == null) {
            return;
        }
        commands.append("#ElementList", ELEMENT_LAYOUT);
        iconId = null;
        if (this.claim != null) {
            iconId = this.claim.getDeityItemId();
        }
        if ((iconId == null || iconId.isEmpty()) && this.plugin != null) {
            iconId = this.plugin.getDeityLandProtectionItemId();
        }
        if (iconId != null && !iconId.isEmpty()) {
            commands.set(selector + " #Icon.ItemId", iconId);
        }
        LangPreferenceManager.Language lang = this.plugin == null ? LangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        commands.set(selector + " #Name.TextSpans", Message.raw(Text.uiFeedUpkeepTitle(lang)));
        commands.set(selector + " #Durability.Text", Text.uiFeedUpkeepSubtitle(lang));
    }
}



