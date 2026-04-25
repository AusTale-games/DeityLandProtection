package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.ClaimStore;
import group.austale.deitylandprotection.DeityLandProtectionAdminActionElement;
import group.austale.deitylandprotection.DeityLandProtectionHeaderElement;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlayerClaimElement;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceBasePage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class DeityLandProtectionPlayerOverviewPage
extends ChoiceBasePage {
    private static final String PAGE_LAYOUT_ES = "Pages/DeityLandProtectionPlayerOverviewPage.ui";
    private static final String PAGE_LAYOUT_EN = "Pages/DeityLandProtectionPlayerOverviewPage_en.ui";
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionPlayerOverviewPage(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        super(playerRef, DeityLandProtectionPlayerOverviewPage.buildElements(plugin, playerRef), DeityLandProtectionPlayerOverviewPage.resolveLayout(DeityLandProtectionPlayerOverviewPage.resolveLang(plugin, playerRef)));
        this.plugin = plugin;
    }

    private static DeityLandProtectionLangPreferenceManager.Language resolveLang(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        return plugin == null ? DeityLandProtectionLangPreferenceManager.Language.EN : plugin.getEffectiveLanguage(playerRef);
    }

    private static String resolveLayout(DeityLandProtectionLangPreferenceManager.Language lang) {
        return lang == DeityLandProtectionLangPreferenceManager.Language.ES ? PAGE_LAYOUT_ES : PAGE_LAYOUT_EN;
    }

    private static ChoiceElement[] buildElements(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        if (plugin == null || playerRef == null) {
            return new ChoiceElement[0];
        }
        UUID playerId = playerRef.getUuid();
        if (playerId == null) {
            return new ChoiceElement[0];
        }
        DeityLandProtectionLangPreferenceManager.Language lang = DeityLandProtectionPlayerOverviewPage.resolveLang(plugin, playerRef);
        ArrayList<ChoiceElement> els = new ArrayList<ChoiceElement>();
        els.add(new DeityLandProtectionHeaderElement(DeityLandProtectionText.uiMyClaimsTitle(lang), DeityLandProtectionText.uiMyClaimsSubtitle(lang)));
        ClaimStore store = plugin.getClaimStore();
        List<Claim> claims = store == null ? null : store.getClaims();
        ArrayList<Claim> mine = new ArrayList<Claim>();
        if (claims != null) {
            for (Claim c : claims) {
                if (c == null || !playerId.equals(c.getOwner())) continue;
                mine.add(c);
            }
        }
        mine.sort(Comparator.comparingInt(Claim::getCenterX).thenComparingInt(Claim::getCenterZ));
        if (mine.isEmpty()) {
            els.add(new DeityLandProtectionAdminActionElement(plugin, DeityLandProtectionText.uiMyClaimsNone(lang), "", null));
            return (ChoiceElement[])els.toArray(ChoiceElement[]::new);
        }
        for (Claim c : mine) {
            els.add(new DeityLandProtectionPlayerClaimElement(plugin, c));
        }
        return (ChoiceElement[])els.toArray(ChoiceElement[]::new);
    }

    public DeityLandProtectionPlugin getPlugin() {
        return this.plugin;
    }
}



