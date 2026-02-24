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

import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionOpenUpkeepInteraction;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class DeityLandProtectionOpenUpkeepElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/DeityLandProtectionUpkeepButtonElement.ui";
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionOpenUpkeepElement(DeityLandProtectionPlugin plugin, int centerX, int centerZ) {
        this.plugin = plugin;
        this.interactions = new ChoiceInteraction[]{new DeityLandProtectionOpenUpkeepInteraction(plugin, centerX, centerZ)};
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
        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin == null ? DeityLandProtectionLangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        commands.set(selector + " #Name.TextSpans", Message.raw((String)DeityLandProtectionText.uiFeedUpkeepTitle(lang)));
        commands.set(selector + " #Durability.Text", DeityLandProtectionText.uiFeedUpkeepSubtitle(lang));
    }
}



