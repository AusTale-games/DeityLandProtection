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
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class DeityLandProtectionUpkeepSlotHintElement
extends ChoiceElement {
    private static final String ELEMENT_LAYOUT = "Pages/ItemRepairElement.ui";
    private static final String ESSENCE_ITEM_ID = "Ingredient_Life_Essence";
    private final DeityLandProtectionPlugin plugin;
    private final String title;

    public DeityLandProtectionUpkeepSlotHintElement(DeityLandProtectionPlugin plugin, String title) {
        this.plugin = plugin;
        this.title = title;
        this.interactions = new ChoiceInteraction[0];
    }

    public void addButton(UICommandBuilder commands, UIEventBuilder events, String selector, PlayerRef playerRef) {
        String iconId;
        if (commands == null || selector == null) {
            return;
        }
        commands.append("#ElementList", ELEMENT_LAYOUT);
        if ((iconId = ESSENCE_ITEM_ID) != null && !iconId.isEmpty()) {
            commands.set(selector + " #Icon.ItemId", iconId);
        }
        DeityLandProtectionLangPreferenceManager.Language lang = this.plugin == null ? DeityLandProtectionLangPreferenceManager.Language.EN : this.plugin.getEffectiveLanguage(playerRef);
        commands.set(selector + " #Name.TextSpans", Message.raw((String)this.buildTitle(lang)));
        commands.set(selector + " #Durability.Text", this.buildSubtitle(lang));
    }

    private String buildTitle(DeityLandProtectionLangPreferenceManager.Language lang) {
        String baseTitle = this.title == null ? "" : this.title;
        if (baseTitle.isEmpty()) {
            return "";
        }
        int cost = this.plugin == null ? 1 : this.plugin.getUpkeepEssenceCostPerHour();
        return cost + " x " + baseTitle;
    }

    private String buildSubtitle(DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.uiSlot0Subtitle(lang);
    }
}



