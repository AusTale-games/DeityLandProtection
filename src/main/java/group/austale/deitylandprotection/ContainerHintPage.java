package group.austale.deitylandprotection;

import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public final class ContainerHintPage
extends BasicCustomUIPage {
    private static final String PAGE_LAYOUT_ES = "Pages/ContainerHintPage.ui";
    private static final String PAGE_LAYOUT_EN = "Pages/DeityLandProtectionContainerHintPage_en.ui";
    private static final String ESSENCE_ITEM_ID = "Ingredient_Life_Essence";
    private final DeityLandProtectionPlugin plugin;
    private final LangPreferenceManager.Language lang;

    public ContainerHintPage(@Nonnull PlayerRef playerRef, DeityLandProtectionPlugin plugin) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction);
        this.plugin = plugin;
        this.lang = plugin == null ? LangPreferenceManager.Language.EN : plugin.getEffectiveLanguage(playerRef);
    }

    public void build(UICommandBuilder commandBuilder) {
        if (commandBuilder == null) {
            return;
        }
        commandBuilder.append(this.lang == LangPreferenceManager.Language.ES ? PAGE_LAYOUT_ES : PAGE_LAYOUT_EN);
        commandBuilder.set("#CopperIcon.ItemId", ESSENCE_ITEM_ID);
        commandBuilder.set("#SilverIcon.ItemId", ESSENCE_ITEM_ID);
    }
}



