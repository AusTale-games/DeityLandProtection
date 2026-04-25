package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionAdminActionElement;
import group.austale.deitylandprotection.DeityLandProtectionAdminActionInteraction;
import group.austale.deitylandprotection.DeityLandProtectionAdminLangToggleInteraction;
import group.austale.deitylandprotection.DeityLandProtectionHeaderElement;
import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.Text;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceBasePage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.ArrayList;

public final class DeityLandProtectionAdminPage
extends ChoiceBasePage {
    private static final String PAGE_LAYOUT_ES = "Pages/DeityLandProtectionAdminPage.ui";
    private static final String PAGE_LAYOUT_EN = "Pages/DeityLandProtectionAdminPage_en.ui";
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionAdminPage(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        super(playerRef, DeityLandProtectionAdminPage.buildElements(plugin, playerRef), DeityLandProtectionAdminPage.resolveLayout(DeityLandProtectionAdminPage.resolveLang(plugin, playerRef)));
        this.plugin = plugin;
    }

    private static LangPreferenceManager.Language resolveLang(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        return plugin == null ? LangPreferenceManager.Language.EN : plugin.getEffectiveLanguage(playerRef);
    }

    private static String resolveLayout(LangPreferenceManager.Language lang) {
        return lang == LangPreferenceManager.Language.ES ? PAGE_LAYOUT_ES : PAGE_LAYOUT_EN;
    }

    private static ChoiceElement[] buildElements(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        if (plugin == null) {
            return new ChoiceElement[0];
        }
        LangPreferenceManager.Language lang = DeityLandProtectionAdminPage.resolveLang(plugin, playerRef);
        ArrayList<ChoiceElement> els = new ArrayList<ChoiceElement>();
        els.add(new DeityLandProtectionHeaderElement(Text.uiAdminTitle(lang), Text.uiAdminSubtitle(lang)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminLanguage(lang), lang.getCode().toUpperCase(), new DeityLandProtectionAdminLangToggleInteraction(plugin)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminCrafting(lang), plugin.isAllowCrafting() ? Text.borderOnShort(lang) : Text.borderOffShort(lang), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.TOGGLE_CRAFTING)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminReload(lang), "", new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.RELOAD)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminRemoveClaim(lang), "", new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.REMOVE_CLAIM_HERE)));
        els.add(new DeityLandProtectionHeaderElement(Text.uiAdminServerSettings(lang), ""));
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminClaimRadius(lang), String.valueOf(plugin.getClaimRadius()), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.CYCLE_RADIUS)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminMaxClaims(lang), String.valueOf(plugin.getMaxClaimsPerPlayer()), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.CYCLE_MAXCLAIMS)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminMapClaimVisual(lang), plugin.isMapClaimVisualEnabled() ? Text.borderOnShort(lang) : Text.borderOffShort(lang), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.TOGGLE_MAP_CLAIM_VISUAL)));
        els.add(new DeityLandProtectionHeaderElement(Text.uiAdminUpkeep(lang), ""));
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminUpkeepEnabled(lang), plugin.isUpkeepEnabled() ? Text.borderOnShort(lang) : Text.borderOffShort(lang), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.TOGGLE_UPKEEP)));
        int grace = plugin.getUpkeepGraceMinutes();
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminUpkeepGraceMinus(lang), String.valueOf(grace), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.ADJUST_UPKEEP_GRACE, -10)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminUpkeepGracePlus(lang), String.valueOf(grace), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.ADJUST_UPKEEP_GRACE, 10)));
        int cost = plugin.getUpkeepEssenceCostPerHour();
        els.add(new DeityLandProtectionAdminActionElement(plugin, Text.uiAdminUpkeepEssenceCost(lang), String.valueOf(cost), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.CYCLE_UPKEEP_ESSENCE_COST)));
        return (ChoiceElement[])els.toArray(ChoiceElement[]::new);
    }

    public DeityLandProtectionPlugin getPlugin() {
        return this.plugin;
    }
}



