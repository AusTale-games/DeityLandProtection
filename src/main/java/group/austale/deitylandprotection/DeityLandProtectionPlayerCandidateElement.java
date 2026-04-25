package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import group.austale.deitylandprotection.DeityLandProtectionTrustAddInteraction;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.UUID;

public final class DeityLandProtectionPlayerCandidateElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final UUID target;
    private final String username;

    public DeityLandProtectionPlayerCandidateElement(DeityLandProtectionPlugin plugin, int centerX, int centerZ, UUID target, String username) {
        ChoiceInteraction[] choiceInteractionArray;
        this.plugin = plugin;
        this.target = target;
        this.username = username;
        if (target == null) {
            choiceInteractionArray = new ChoiceInteraction[]{};
        } else {
            ChoiceInteraction[] choiceInteractionArray2 = new ChoiceInteraction[1];
            choiceInteractionArray = choiceInteractionArray2;
            choiceInteractionArray2[0] = new DeityLandProtectionTrustAddInteraction(plugin, centerX, centerZ, target, username);
        }
        this.interactions = choiceInteractionArray;
    }

    public void addButton(UICommandBuilder commands, UIEventBuilder events, String selector, PlayerRef playerRef) {
        String who;
        String iconId;
        if (commands == null || selector == null) {
            return;
        }
        commands.append("#ElementList", ELEMENT_LAYOUT);
        if (this.plugin != null && (iconId = this.plugin.getDeityLandProtectionItemId()) != null && !iconId.isEmpty()) {
            commands.set(selector + " #Icon.ItemId", iconId);
        }
        if ((who = this.username) == null || who.isEmpty()) {
            who = this.target == null ? "<unknown>" : this.target.toString();
        }
        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin == null ? DeityLandProtectionLangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        commands.set(selector + " #Name.TextSpans", Message.raw(who));
        commands.set(selector + " #Durability.Text", DeityLandProtectionText.uiClickAddAll(lang));
    }
}



