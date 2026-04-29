package group.austale.deitylandprotection;

import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceBasePage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerOverviewPage
extends ChoiceBasePage {
    private static final String PAGE_LAYOUT_ES = "Pages/PlayerOverviewPage.ui";
    private static final String PAGE_LAYOUT_EN = "Pages/DeityLandProtectionPlayerOverviewPage_en.ui";
    private static final String PAGE_LAYOUT_FR = "Pages/DeityLandProtectionPlayerOverviewPage_fr.ui";
    private final DeityLandProtectionPlugin plugin;

    public PlayerOverviewPage(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        super(playerRef, PlayerOverviewPage.buildElements(plugin, playerRef), PlayerOverviewPage.resolveLayout(PlayerOverviewPage.resolveLang(plugin, playerRef)));
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
        if (plugin == null || playerRef == null) {
            return new ChoiceElement[0];
        }
        UUID playerId = playerRef.getUuid();
        if (playerId == null) {
            return new ChoiceElement[0];
        }
        LangPreferenceManager.Language lang = PlayerOverviewPage.resolveLang(plugin, playerRef);
        ArrayList<ChoiceElement> els = new ArrayList<ChoiceElement>();
        els.add(new HeaderElement(Text.uiMyClaimsTitle(lang), Text.uiMyClaimsSubtitle(lang)));
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
            els.add(new AdminActionElement(plugin, Text.uiMyClaimsNone(lang), "", null));
            return (ChoiceElement[])els.toArray(ChoiceElement[]::new);
        }
        for (Claim c : mine) {
            els.add(new PlayerClaimElement(plugin, c));
        }
        return (ChoiceElement[])els.toArray(ChoiceElement[]::new);
    }

    public DeityLandProtectionPlugin getPlugin() {
        return this.plugin;
    }
}



