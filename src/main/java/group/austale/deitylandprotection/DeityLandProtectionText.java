package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import javax.annotation.Nonnull;

public final class DeityLandProtectionText {
    private DeityLandProtectionText() {
    }

    private static String tr(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull String key, Object ... args) {
        DeityLandProtectionPlugin plugin = DeityLandProtectionPlugin.getInstance();
        if (plugin == null) {
            return new DeityLandProtectionLocalizer().tr(lang, key, args);
        }
        return plugin.tr(lang, key, args);
    }

    @Nonnull
    public static String helpLang(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "help.lang");
    }

    @Nonnull
    public static String noPermission(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "common.no_permission");
    }

    @Nonnull
    public static String langStatus(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull DeityLandProtectionLangPreferenceManager.Language effective, @Nonnull DeityLandProtectionLangPreferenceManager.Language def, DeityLandProtectionLangPreferenceManager.Language override) {
        String suffix = override != null ? DeityLandProtectionText.tr(lang, "lang.status.override_suffix", override.getCode()) : DeityLandProtectionText.tr(lang, "lang.status.no_override_suffix");
        return DeityLandProtectionText.tr(lang, "lang.status", effective.getCode(), def.getCode(), suffix);
    }

    @Nonnull
    public static String langUpdated(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull DeityLandProtectionLangPreferenceManager.Language newLang) {
        return DeityLandProtectionText.tr(lang, "lang.updated", newLang.getCode());
    }

    @Nonnull
    public static String langDefaultUpdated(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull DeityLandProtectionLangPreferenceManager.Language newLang) {
        return DeityLandProtectionText.tr(lang, "lang.default_updated", newLang.getCode());
    }

    @Nonnull
    public static String langOverrideCleared(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "lang.override_cleared");
    }

    @Nonnull
    public static String cannotPlaceClaimLimit(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, int owned, int max) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_limit", owned, max);
    }

    @Nonnull
    public static String cannotPlaceAreaProtected(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_area_protected");
    }

    @Nonnull
    public static String cannotPlaceOverlap(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_overlap");
    }

    @Nonnull
    public static String protectionCreated(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, int radius) {
        return DeityLandProtectionText.tr(lang, "claim.protection_created", radius);
    }

    @Nonnull
    public static String cannotPlaceHere(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_here");
    }

    @Nonnull
    public static String cannotPlaceInside(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_inside");
    }

    @Nonnull
    public static String protectionRemoved(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.protection_removed");
    }

    @Nonnull
    public static String cannotBreakProtection(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_break_protection");
    }

    @Nonnull
    public static String cannotBreakInside(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_break_inside");
    }

    @Nonnull
    public static String cannotUseInside(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_use_inside");
    }

    @Nonnull
    public static String cannotDamageCrops(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_damage_crops");
    }

    @Nonnull
    public static String cannotHurtAnimals(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_hurt_animals");
    }

    @Nonnull
    public static String cannotInteractAnimals(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_interact_animals");
    }

    @Nonnull
    public static String enteredArea(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull String owner) {
        return DeityLandProtectionText.tr(lang, "claim.entered_area", owner);
    }

    @Nonnull
    public static String leftArea(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull String owner) {
        return DeityLandProtectionText.tr(lang, "claim.left_area", owner);
    }

    @Nonnull
    public static String onlyOwnerAddFriends(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "trust.only_owner_add");
    }

    @Nonnull
    public static String ownerAlreadyFullAccess(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "trust.owner_full_access");
    }

    @Nonnull
    public static String onlyOwnerEditFriends(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "trust.only_owner_edit");
    }

    @Nonnull
    public static String borderOn(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "border.on");
    }

    @Nonnull
    public static String borderOff(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "border.off");
    }

    @Nonnull
    public static String uiTitlePlayersInArea(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.players_in_area.title");
    }

    @Nonnull
    public static String uiSubtitleAddFriendAll(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.players_in_area.add_all");
    }

    @Nonnull
    public static String uiNone(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.none");
    }

    @Nonnull
    public static String uiFriendsTrust(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.friends_trust");
    }

    @Nonnull
    public static String uiCyclePermsHint(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.cycle_perms_hint");
    }

    @Nonnull
    public static String uiClickAddAll(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.click_add_all");
    }

    @Nonnull
    public static String uiShowBorder(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.show_border");
    }

    @Nonnull
    public static String uiPvpInZone(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.pvp_in_zone");
    }

    @Nonnull
    public static String uiSlot0Title(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.essence_life");
    }

    @Nonnull
    public static String uiSlot0Subtitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.essence_life_subtitle");
    }

    @Nonnull
    public static String uiFeedUpkeepTitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.feed_upkeep.title");
    }

    @Nonnull
    public static String uiFeedUpkeepSubtitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.feed_upkeep.subtitle");
    }

    @Nonnull
    public static String DeityLandProtectionDestroyTitlePrimary(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, int seconds) {
        int s = Math.max(0, seconds);
        return DeityLandProtectionText.tr(lang, "upkeep.destroy_title_primary", s);
    }

    @Nonnull
    public static String DeityLandProtectionDestroyTitleSecondary(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "upkeep.destroy_title_secondary");
    }

    @Nonnull
    public static String uiMyClaimsTitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.my_claims.title");
    }

    @Nonnull
    public static String uiMyClaimsSubtitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.my_claims.subtitle");
    }

    @Nonnull
    public static String uiMyClaimsNone(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.my_claims.none");
    }

    @Nonnull
    public static String uiAdminTitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.title");
    }

    @Nonnull
    public static String uiAdminSubtitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.subtitle");
    }

    @Nonnull
    public static String uiAdminLanguage(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.language");
    }

    @Nonnull
    public static String uiAdminCrafting(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.crafting");
    }

    @Nonnull
    public static String uiAdminReload(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.reload");
    }

    @Nonnull
    public static String uiAdminRemoveClaim(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.remove_claim");
    }

    @Nonnull
    public static String uiAdminRemoveClaimNone(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.remove_claim_none");
    }

    @Nonnull
    public static String uiAdminServerSettings(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.server");
    }

    @Nonnull
    public static String uiAdminClaimRadius(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.claim_radius");
    }

    @Nonnull
    public static String uiAdminMaxClaims(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.max_claims");
    }

    @Nonnull
    public static String uiAdminMapClaimVisual(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.map_visual");
    }

    @Nonnull
    public static String uiAdminUpkeep(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep");
    }

    @Nonnull
    public static String uiAdminUpkeepEnabled(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep_enabled");
    }

    @Nonnull
    public static String uiAdminUpkeepGraceMinus(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep_grace_minus");
    }

    @Nonnull
    public static String uiAdminUpkeepGracePlus(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep_grace_plus");
    }

    @Nonnull
    public static String uiAdminUpkeepEssenceCost(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep_essence_cost");
    }

    @Nonnull
    public static String uiSlot0VoidTitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.essence_void");
    }

    @Nonnull
    public static String borderOnShort(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "border.on_short");
    }

    @Nonnull
    public static String borderOffShort(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "border.off_short");
    }

    @Nonnull
    public static String uiPlayerComponentError(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "error.ui.player_component");
    }

    @Nonnull
    public static String pvpDisabledInArea(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "pvp.disabled_in_area");
    }

    @Nonnull
    public static String tierUpgraded(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, int nextTier, int nextRadius, int upkeepCost) {
        return DeityLandProtectionText.tr(lang, "tier.upgraded", nextTier, nextRadius, upkeepCost);
    }

    @Nonnull
    public static String tierNoUnclaimedTerritory(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "tier.no_unclaimed_territory");
    }

}



