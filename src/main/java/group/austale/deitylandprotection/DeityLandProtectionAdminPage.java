package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionAdminActionElement;
import group.austale.deitylandprotection.DeityLandProtectionAdminActionInteraction;
import group.austale.deitylandprotection.DeityLandProtectionAdminLangToggleInteraction;
import group.austale.deitylandprotection.DeityLandProtectionHeaderElement;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
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

    private static DeityLandProtectionLangPreferenceManager.Language resolveLang(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        return plugin == null ? DeityLandProtectionLangPreferenceManager.Language.EN : plugin.getEffectiveLanguage(playerRef);
    }

    private static String resolveLayout(DeityLandProtectionLangPreferenceManager.Language lang) {
        return lang == DeityLandProtectionLangPreferenceManager.Language.ES ? PAGE_LAYOUT_ES : PAGE_LAYOUT_EN;
    }

    private static ChoiceElement[] buildElements(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        if (plugin == null) {
            return new ChoiceElement[0];
        }
        DeityLandProtectionLangPreferenceManager.Language lang = DeityLandProtectionAdminPage.resolveLang(plugin, playerRef);
        ArrayList<ChoiceElement> els = new ArrayList<ChoiceElement>();
        els.add(new DeityLandProtectionHeaderElement(DeityLandProtectionText.uiAdminTitle(lang), DeityLandProtectionText.uiAdminSubtitle(lang)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminLanguage(lang), lang.getCode().toUpperCase(), new DeityLandProtectionAdminLangToggleInteraction(plugin)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminCrafting(lang), plugin.isAllowCrafting() ? DeityLandProtectionText.borderOnShort(lang) : DeityLandProtectionText.borderOffShort(lang), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.TOGGLE_CRAFTING)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminReload(lang), "", new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.RELOAD)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminRemoveClaim(lang), "", new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.REMOVE_CLAIM_HERE)));
        els.add(new DeityLandProtectionHeaderElement(DeityLandProtectionText.uiAdminServerSettings(lang), ""));
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminClaimRadius(lang), String.valueOf(plugin.getClaimRadius()), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.CYCLE_RADIUS)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminMaxClaims(lang), String.valueOf(plugin.getMaxClaimsPerPlayer()), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.CYCLE_MAXCLAIMS)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminMapClaimVisual(lang), plugin.isMapClaimVisualEnabled() ? DeityLandProtectionText.borderOnShort(lang) : DeityLandProtectionText.borderOffShort(lang), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.TOGGLE_MAP_CLAIM_VISUAL)));
        els.add(new DeityLandProtectionHeaderElement(DeityLandProtectionText.uiAdminUpkeep(lang), ""));
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminUpkeepEnabled(lang), plugin.isUpkeepEnabled() ? DeityLandProtectionText.borderOnShort(lang) : DeityLandProtectionText.borderOffShort(lang), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.TOGGLE_UPKEEP)));
        int grace = plugin.getUpkeepGraceMinutes();
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminUpkeepGraceMinus(lang), String.valueOf(grace), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.ADJUST_UPKEEP_GRACE, -10)));
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminUpkeepGracePlus(lang), String.valueOf(grace), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.ADJUST_UPKEEP_GRACE, 10)));
        int cost = plugin.getUpkeepEssenceCostPerHour();
        els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiAdminUpkeepEssenceCost(lang), String.valueOf(cost), new DeityLandProtectionAdminActionInteraction(plugin, DeityLandProtectionAdminActionInteraction.Action.CYCLE_UPKEEP_ESSENCE_COST)));
        return (ChoiceElement[])els.toArray(ChoiceElement[]::new);
    }

    public DeityLandProtectionPlugin getPlugin() {
        return this.plugin;
    }
}



