package group.austale.deitylandprotection;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class BorderToggleElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;

    public BorderToggleElement(DeityLandProtectionPlugin plugin, int centerX, int centerZ) {
        this.plugin = plugin;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.interactions = new ChoiceInteraction[]{new BorderToggleInteraction(plugin, centerX, centerZ)};
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
        boolean on = false;
        if (this.plugin != null && playerRef != null && playerRef.getUuid() != null) {
            long key = DeityLandProtectionPlugin.centerKey(this.centerX, this.centerZ);
            on = this.plugin.isBorderEnabled(playerRef.getUuid(), key);
        }
        LangPreferenceManager.Language lang = this.plugin == null ? LangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        commands.set(selector + " #Name.TextSpans", Message.raw(Text.uiShowBorder(lang)));
        commands.set(selector + " #Durability.Text", on ? Text.borderOnShort(lang) : Text.borderOffShort(lang));
    }
}



