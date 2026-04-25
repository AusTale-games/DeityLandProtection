package group.austale.deitylandprotection;

import group.austale.deitylandprotection.LangPreferenceManager;
import javax.annotation.Nonnull;

public final class DeityLandProtectionText {
    private DeityLandProtectionText() {
    }

    private static String tr(@Nonnull LangPreferenceManager.Language lang, @Nonnull String key, Object ... args) {
        DeityLandProtectionPlugin plugin = DeityLandProtectionPlugin.getInstance();
        if (plugin == null) {
            return new DeityLandProtectionLocalizer().tr(lang, key, args);
        }
        return plugin.tr(lang, key, args);
    }

    @Nonnull
    public static String helpLang(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "help.lang");
    }

    @Nonnull
    public static String noPermission(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "common.no_permission");
    }

    @Nonnull
    public static String langStatus(@Nonnull LangPreferenceManager.Language lang, @Nonnull LangPreferenceManager.Language effective, @Nonnull LangPreferenceManager.Language def, LangPreferenceManager.Language override) {
        String suffix = override != null ? DeityLandProtectionText.tr(lang, "lang.status.override_suffix", override.getCode()) : DeityLandProtectionText.tr(lang, "lang.status.no_override_suffix");
        return DeityLandProtectionText.tr(lang, "lang.status", effective.getCode(), def.getCode(), suffix);
    }

    @Nonnull
    public static String langUpdated(@Nonnull LangPreferenceManager.Language lang, @Nonnull LangPreferenceManager.Language newLang) {
        return DeityLandProtectionText.tr(lang, "lang.updated", newLang.getCode());
    }

    @Nonnull
    public static String langDefaultUpdated(@Nonnull LangPreferenceManager.Language lang, @Nonnull LangPreferenceManager.Language newLang) {
        return DeityLandProtectionText.tr(lang, "lang.default_updated", newLang.getCode());
    }

    @Nonnull
    public static String langOverrideCleared(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "lang.override_cleared");
    }

    @Nonnull
    public static String cannotPlaceClaimLimit(@Nonnull LangPreferenceManager.Language lang, int owned, int max) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_limit", owned, max);
    }

    @Nonnull
    public static String cannotPlaceAreaProtected(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_area_protected");
    }

    @Nonnull
    public static String cannotPlaceOverlap(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_overlap");
    }

    @Nonnull
    public static String protectionCreated(@Nonnull LangPreferenceManager.Language lang, int radius) {
        return DeityLandProtectionText.tr(lang, "claim.protection_created", radius);
    }

    @Nonnull
    public static String cannotPlaceHere(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_here");
    }

    @Nonnull
    public static String cannotPlaceInside(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_place_inside");
    }

    @Nonnull
    public static String protectionRemoved(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.protection_removed");
    }

    @Nonnull
    public static String cannotBreakProtection(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_break_protection");
    }

    @Nonnull
    public static String cannotBreakInside(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_break_inside");
    }

    @Nonnull
    public static String cannotUseInside(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_use_inside");
    }

    @Nonnull
    public static String cannotDamageCrops(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_damage_crops");
    }

    @Nonnull
    public static String cannotHurtAnimals(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_hurt_animals");
    }

    @Nonnull
    public static String cannotInteractAnimals(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "claim.cannot_interact_animals");
    }

    @Nonnull
    public static String enteredArea(@Nonnull LangPreferenceManager.Language lang, @Nonnull String owner) {
        return DeityLandProtectionText.tr(lang, "claim.entered_area", owner);
    }

    @Nonnull
    public static String leftArea(@Nonnull LangPreferenceManager.Language lang, @Nonnull String owner) {
        return DeityLandProtectionText.tr(lang, "claim.left_area", owner);
    }

    @Nonnull
    public static String onlyOwnerAddFriends(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "trust.only_owner_add");
    }

    @Nonnull
    public static String ownerAlreadyFullAccess(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "trust.owner_full_access");
    }

    @Nonnull
    public static String onlyOwnerEditFriends(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "trust.only_owner_edit");
    }

    @Nonnull
    public static String borderOn(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "border.on");
    }

    @Nonnull
    public static String borderOff(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "border.off");
    }

    @Nonnull
    public static String uiTitlePlayersInArea(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.players_in_area.title");
    }

    @Nonnull
    public static String uiSubtitleAddFriendAll(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.players_in_area.add_all");
    }

    @Nonnull
    public static String uiNone(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.none");
    }

    @Nonnull
    public static String uiFriendsTrust(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.friends_trust");
    }

    @Nonnull
    public static String uiCyclePermsHint(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.cycle_perms_hint");
    }

    @Nonnull
    public static String uiClickAddAll(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.click_add_all");
    }

    @Nonnull
    public static String uiShowBorder(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.show_border");
    }

    @Nonnull
    public static String uiPvpInZone(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.pvp_in_zone");
    }

    @Nonnull
    public static String uiSlot0Title(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.essence_life");
    }

    @Nonnull
    public static String uiSlot0Subtitle(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.essence_life_subtitle");
    }

    @Nonnull
    public static String uiFeedUpkeepTitle(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.feed_upkeep.title");
    }

    @Nonnull
    public static String uiFeedUpkeepSubtitle(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.feed_upkeep.subtitle");
    }

    @Nonnull
    public static String DeityLandProtectionDestroyTitlePrimary(@Nonnull LangPreferenceManager.Language lang, int seconds) {
        int s = Math.max(0, seconds);
        return DeityLandProtectionText.tr(lang, "upkeep.destroy_title_primary", s);
    }

    @Nonnull
    public static String DeityLandProtectionDestroyTitleSecondary(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "upkeep.destroy_title_secondary");
    }

    @Nonnull
    public static String uiMyClaimsTitle(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.my_claims.title");
    }

    @Nonnull
    public static String uiMyClaimsSubtitle(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.my_claims.subtitle");
    }

    @Nonnull
    public static String uiMyClaimsNone(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.my_claims.none");
    }

    @Nonnull
    public static String uiAdminTitle(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.title");
    }

    @Nonnull
    public static String uiAdminSubtitle(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.subtitle");
    }

    @Nonnull
    public static String uiAdminLanguage(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.language");
    }

    @Nonnull
    public static String uiAdminCrafting(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.crafting");
    }

    @Nonnull
    public static String uiAdminReload(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.reload");
    }

    @Nonnull
    public static String uiAdminRemoveClaim(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.remove_claim");
    }

    @Nonnull
    public static String uiAdminRemoveClaimNone(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.remove_claim_none");
    }

    @Nonnull
    public static String uiAdminServerSettings(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.server");
    }

    @Nonnull
    public static String uiAdminClaimRadius(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.claim_radius");
    }

    @Nonnull
    public static String uiAdminMaxClaims(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.max_claims");
    }

    @Nonnull
    public static String uiAdminMapClaimVisual(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.map_visual");
    }

    @Nonnull
    public static String uiAdminUpkeep(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep");
    }

    @Nonnull
    public static String uiAdminUpkeepEnabled(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep_enabled");
    }

    @Nonnull
    public static String uiAdminUpkeepGraceMinus(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep_grace_minus");
    }

    @Nonnull
    public static String uiAdminUpkeepGracePlus(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep_grace_plus");
    }

    @Nonnull
    public static String uiAdminUpkeepEssenceCost(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.admin.upkeep_essence_cost");
    }

    @Nonnull
    public static String uiSlot0VoidTitle(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "ui.essence_void");
    }

    @Nonnull
    public static String borderOnShort(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "border.on_short");
    }

    @Nonnull
    public static String borderOffShort(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "border.off_short");
    }

    @Nonnull
    public static String uiPlayerComponentError(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "error.ui.player_component");
    }

    @Nonnull
    public static String pvpDisabledInArea(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "pvp.disabled_in_area");
    }

    @Nonnull
    public static String tierUpgraded(@Nonnull LangPreferenceManager.Language lang, int nextTier, int nextRadius, int upkeepCost) {
        return DeityLandProtectionText.tr(lang, "tier.upgraded", nextTier, nextRadius, upkeepCost);
    }

    @Nonnull
    public static String tierNoUnclaimedTerritory(@Nonnull LangPreferenceManager.Language lang) {
        return DeityLandProtectionText.tr(lang, "tier.no_unclaimed_territory");
    }

}



