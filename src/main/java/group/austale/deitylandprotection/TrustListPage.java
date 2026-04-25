package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.BorderToggleElement;
import group.austale.deitylandprotection.HeaderElement;
import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.OpenUpkeepElement;
import group.austale.deitylandprotection.PlayerCandidateElement;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.Text;
import group.austale.deitylandprotection.TrustElement;
import group.austale.deitylandprotection.UpkeepSlotHintElement;
import group.austale.deitylandprotection.UpkeepStatusElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceBasePage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public final class TrustListPage
extends ChoiceBasePage {
    private static final String PAGE_LAYOUT_ES = "Pages/DeityLandProtectionZoneConfigPage.ui";
    private static final String PAGE_LAYOUT_EN = "Pages/DeityLandProtectionZoneConfigPage_en.ui";
    private final DeityLandProtectionPlugin plugin;
    private final int centerX;
    private final int centerZ;

    public TrustListPage(DeityLandProtectionPlugin plugin, PlayerRef playerRef, int centerX, int centerZ) {
        super(playerRef, TrustListPage.buildElements(plugin, centerX, centerZ, TrustListPage.resolveLang(plugin, playerRef)), TrustListPage.resolveLayout(TrustListPage.resolveLang(plugin, playerRef)));
        this.plugin = plugin;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    private static LangPreferenceManager.Language resolveLang(DeityLandProtectionPlugin plugin, PlayerRef playerRef) {
        return plugin == null ? LangPreferenceManager.Language.EN : plugin.getEffectiveLanguage(playerRef);
    }

    private static String resolveLayout(LangPreferenceManager.Language lang) {
        return lang == LangPreferenceManager.Language.ES ? PAGE_LAYOUT_ES : PAGE_LAYOUT_EN;
    }

    private static ChoiceElement[] buildElements(DeityLandProtectionPlugin plugin, int centerX, int centerZ, LangPreferenceManager.Language lang) {
        if (plugin == null) {
            return new ChoiceElement[0];
        }
        Claim claim = plugin.getClaimStore().findClaimByCenter(centerX, centerZ);
        if (claim == null) {
            return new ChoiceElement[0];
        }
        Map<UUID, Integer> trusted = claim.getTrusted();
        ArrayList<ChoiceElement> els = new ArrayList<ChoiceElement>();
        if (plugin.isUpkeepEnabled()) {
            els.add(new OpenUpkeepElement(plugin, claim, centerX, centerZ));
            els.add(new UpkeepSlotHintElement(plugin, claim));
            els.add(new UpkeepStatusElement(plugin, centerX, centerZ));
            UpkeepStore upkeepStore = plugin.getUpkeepStore();
            UpkeepState st = upkeepStore == null ? null : upkeepStore.get(centerX, centerZ);
            if (st != null && st.getGraceUntilMs() > System.currentTimeMillis()) {
                els.add(new UpkeepGraceElement(plugin, centerX, centerZ));
            }
        }
        els.add(new BorderToggleElement(plugin, centerX, centerZ));
        els.add(new TrustElement(plugin, centerX, centerZ, null, 0, true));
        if (!trusted.isEmpty()) {
            ArrayList<Map.Entry<UUID, Integer>> entries = new ArrayList<Map.Entry<UUID, Integer>>(trusted.entrySet());
            Collections.sort(entries, Comparator.comparing(e -> ((UUID)e.getKey()).toString()));
            for (Map.Entry entry : entries) {
                UUID uUID = (UUID)entry.getKey();
                Integer value = (Integer)entry.getValue();
                int perms = value == null ? 0 : value;
                els.add(new TrustElement(plugin, centerX, centerZ, uUID, perms, false));
            }
        }
        els.add(new HeaderElement(Text.uiTitlePlayersInArea(lang), Text.uiSubtitleAddFriendAll(lang)));
        Map<UUID, String> inClaim = plugin.getPlayersInClaim(centerX, centerZ);
        if (inClaim.isEmpty()) {
            els.add(new HeaderElement(Text.uiNone(lang), ""));
        } else {
            ArrayList<Map.Entry<UUID, String>> entries = new ArrayList<Map.Entry<UUID, String>>(inClaim.entrySet());
            Collections.sort(entries, Comparator.comparing(e -> e.getValue() == null ? "" : ((String)e.getValue()).toLowerCase()));
            for (Map.Entry entry : entries) {
                UUID u = (UUID)entry.getKey();
                if (u == null || u.equals(claim.getOwner()) || trusted.containsKey(u)) continue;
                String name = (String)entry.getValue();
                els.add(new PlayerCandidateElement(plugin, centerX, centerZ, u, name));
            }
        }
        return (ChoiceElement[])els.toArray(ChoiceElement[]::new);
    }

    public DeityLandProtectionPlugin getPlugin() {
        return this.plugin;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public int getCenterZ() {
        return this.centerZ;
    }
}



