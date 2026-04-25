package group.austale.deitylandprotection;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class AdminActionElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final String title;
    private final String value;

    public AdminActionElement(DeityLandProtectionPlugin plugin, String title, String value, ChoiceInteraction interaction) {
        ChoiceInteraction[] choiceInteractionArray;
        this.plugin = plugin;
        this.title = title;
        this.value = value;
        if (interaction == null) {
            choiceInteractionArray = new ChoiceInteraction[]{};
        } else {
            ChoiceInteraction[] choiceInteractionArray2 = new ChoiceInteraction[1];
            choiceInteractionArray = choiceInteractionArray2;
            choiceInteractionArray2[0] = interaction;
        }
        this.interactions = choiceInteractionArray;
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
        commands.set(selector + " #Name.TextSpans", Message.raw((this.title == null ? "" : this.title)));
        commands.set(selector + " #Durability.Text", this.value == null ? "" : this.value);
    }
}



