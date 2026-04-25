package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public final class DeityLandProtectionContainerHintPage
extends BasicCustomUIPage {
    private static final String PAGE_LAYOUT_ES = "Pages/DeityLandProtectionContainerHintPage.ui";
    private static final String PAGE_LAYOUT_EN = "Pages/DeityLandProtectionContainerHintPage_en.ui";
    private static final String ESSENCE_ITEM_ID = "Ingredient_Life_Essence";
    private final DeityLandProtectionPlugin plugin;
    private final DeityLandProtectionLangPreferenceManager.Language lang;

    public DeityLandProtectionContainerHintPage(@Nonnull PlayerRef playerRef, DeityLandProtectionPlugin plugin) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction);
        this.plugin = plugin;
        this.lang = plugin == null ? DeityLandProtectionLangPreferenceManager.Language.EN : plugin.getEffectiveLanguage(playerRef);
    }

    public void build(UICommandBuilder commandBuilder) {
        if (commandBuilder == null) {
            return;
        }
        commandBuilder.append(this.lang == DeityLandProtectionLangPreferenceManager.Language.ES ? PAGE_LAYOUT_ES : PAGE_LAYOUT_EN);
        commandBuilder.set("#CopperIcon.ItemId", ESSENCE_ITEM_ID);
        commandBuilder.set("#SilverIcon.ItemId", ESSENCE_ITEM_ID);
    }
}



