package group.austale.deitylandprotection;

import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceBasePage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.ArrayList;

public final class AdminPage
extends ChoiceBasePage {
    private static final String PAGE_LAYOUT_ES = "Pages/AdminPage.ui";
    private static final String PAGE_LAYOUT_EN = "Pages/DeityLandProtectionAdminPage_en.ui";
    private static final String PAGE_LAYOUT_FR = "Pages/DeityLandProtectionAdminPage_fr.ui";
    private final DeityLandProtectionPlugin plugin;

    public AdminPage(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        super(playerRef, AdminPage.buildElements(plugin, playerRef), AdminPage.resolveLayout(AdminPage.resolveLang(plugin, playerRef)));
        this.plugin = plugin;
    }

    private static LangPreferenceManager.Language resolveLang(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        return plugin == null ? LangPreferenceManager.Language.EN : plugin.getEffectiveLanguage(playerRef);
    }

    private static String resolveLayout(LangPreferenceManager.Language lang) {
        if (lang == LangPreferenceManager.Language.ES) {
            return PAGE_LAYOUT_ES;
        }
        if (lang == LangPreferenceManager.Language.FR) {
            return PAGE_LAYOUT_FR;
        }
        return PAGE_LAYOUT_EN;
    }

    private static ChoiceElement[] buildElements(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        if (plugin == null) {
            return new ChoiceElement[0];
        }
        LangPreferenceManager.Language lang = AdminPage.resolveLang(plugin, playerRef);
        ArrayList<ChoiceElement> els = new ArrayList<ChoiceElement>();
        els.add(new HeaderElement(Text.uiAdminTitle(lang), Text.uiAdminSubtitle(lang)));
        els.add(new AdminActionElement(plugin, Text.uiAdminLanguage(lang), lang.getCode().toUpperCase(), new AdminLangToggleInteraction(plugin)));
        els.add(new AdminActionElement(plugin, Text.uiAdminCrafting(lang), plugin.isAllowCrafting() ? Text.borderOnShort(lang) : Text.borderOffShort(lang), new AdminActionInteraction(plugin, AdminActionInteraction.Action.TOGGLE_CRAFTING)));
        els.add(new AdminActionElement(plugin, Text.uiAdminReload(lang), "", new AdminActionInteraction(plugin, AdminActionInteraction.Action.RELOAD)));
        els.add(new AdminActionElement(plugin, Text.uiAdminRemoveClaim(lang), "", new AdminActionInteraction(plugin, AdminActionInteraction.Action.REMOVE_CLAIM_HERE)));
        els.add(new HeaderElement(Text.uiAdminServerSettings(lang), ""));
        els.add(new AdminActionElement(plugin, Text.uiAdminClaimRadius(lang), String.valueOf(plugin.getClaimRadius()), new AdminActionInteraction(plugin, AdminActionInteraction.Action.CYCLE_RADIUS)));
        els.add(new AdminActionElement(plugin, Text.uiAdminMaxClaims(lang), String.valueOf(plugin.getMaxClaimsPerPlayer()), new AdminActionInteraction(plugin, AdminActionInteraction.Action.CYCLE_MAXCLAIMS)));
        els.add(new AdminActionElement(plugin, Text.uiAdminMapClaimVisual(lang), plugin.isMapClaimVisualEnabled() ? Text.borderOnShort(lang) : Text.borderOffShort(lang), new AdminActionInteraction(plugin, AdminActionInteraction.Action.TOGGLE_MAP_CLAIM_VISUAL)));
        els.add(new HeaderElement(Text.uiAdminUpkeep(lang), ""));
        els.add(new AdminActionElement(plugin, Text.uiAdminUpkeepEnabled(lang), plugin.isUpkeepEnabled() ? Text.borderOnShort(lang) : Text.borderOffShort(lang), new AdminActionInteraction(plugin, AdminActionInteraction.Action.TOGGLE_UPKEEP)));
        int grace = plugin.getUpkeepGraceMinutes();
        els.add(new AdminActionElement(plugin, Text.uiAdminUpkeepGraceMinus(lang), String.valueOf(grace), new AdminActionInteraction(plugin, AdminActionInteraction.Action.ADJUST_UPKEEP_GRACE, -10)));
        els.add(new AdminActionElement(plugin, Text.uiAdminUpkeepGracePlus(lang), String.valueOf(grace), new AdminActionInteraction(plugin, AdminActionInteraction.Action.ADJUST_UPKEEP_GRACE, 10)));
        int cost = plugin.getUpkeepEssenceCostPerHour();
        els.add(new AdminActionElement(plugin, Text.uiAdminUpkeepEssenceCost(lang), String.valueOf(cost), new AdminActionInteraction(plugin, AdminActionInteraction.Action.CYCLE_UPKEEP_ESSENCE_COST)));
        return (ChoiceElement[])els.toArray(ChoiceElement[]::new);
    }

    public DeityLandProtectionPlugin getPlugin() {
        return this.plugin;
    }
}



