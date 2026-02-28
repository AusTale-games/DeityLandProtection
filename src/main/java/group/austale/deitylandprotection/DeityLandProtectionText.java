/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import javax.annotation.Nonnull;

public final class DeityLandProtectionText {
    private DeityLandProtectionText() {
    }

    @Nonnull
    public static String helpLang(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Uso: /DeityLandProtectionlang status | es|en | clear | default es|en";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Usage: /DeityLandProtectionlang status | es|en | clear | default es|en";
        };
    }

    @Nonnull
    public static String noPermission(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No tienes permiso";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "No permission";
        };
    }

    @Nonnull
    public static String langStatus(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull DeityLandProtectionLangPreferenceManager.Language effective, @Nonnull DeityLandProtectionLangPreferenceManager.Language def, DeityLandProtectionLangPreferenceManager.Language override) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Idioma DeityLandProtection: " + effective.getCode() + " (default: " + def.getCode() + (String)(override != null ? ", tu override: " + override.getCode() : ", sin override") + ")";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "DeityLandProtection language: " + effective.getCode() + " (default: " + def.getCode() + (String)(override != null ? ", your override: " + override.getCode() : ", no override") + ")";
        };
    }

    @Nonnull
    public static String langUpdated(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull DeityLandProtectionLangPreferenceManager.Language newLang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Idioma actualizado: " + newLang.getCode();
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Language updated: " + newLang.getCode();
        };
    }

    @Nonnull
    public static String langDefaultUpdated(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull DeityLandProtectionLangPreferenceManager.Language newLang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Idioma default del servidor actualizado: " + newLang.getCode();
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Server default language updated: " + newLang.getCode();
        };
    }

    @Nonnull
    public static String langOverrideCleared(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Tu override fue removido (ahora usas el default del servidor)";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Your override was cleared (now using server default)";
        };
    }

    @Nonnull
    public static String cannotPlaceClaimLimit(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, int owned, int max) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes poner DeityLandProtection: l\u00edmite de claims alcanzado (" + owned + "/" + max + ")";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Cannot place DeityLandProtection: claim limit reached (" + owned + "/" + max + ")";
        };
    }

    @Nonnull
    public static String cannotPlaceAreaProtected(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes poner DeityLandProtection: el \u00e1rea est\u00e1 protegida";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Cannot place DeityLandProtection: area is protected";
        };
    }

    @Nonnull
    public static String cannotPlaceOverlap(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes poner DeityLandProtection: se superpondr\u00eda con otra protecci\u00f3n";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Cannot place DeityLandProtection: protection would overlap another protection";
        };
    }

    @Nonnull
    public static String protectionCreated(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, int radius) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Protecci\u00f3n creada (radio " + radius + ")";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Protection created (radius " + radius + ")";
        };
    }

    @Nonnull
    public static String cannotPlaceHere(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes poner DeityLandProtection aqu\u00ed";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Cannot place DeityLandProtection here";
        };
    }

    @Nonnull
    public static String cannotPlaceInside(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes poner bloques dentro de esta zona protegida";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You cannot place blocks inside this protected area";
        };
    }

    @Nonnull
    public static String protectionRemoved(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Protecci\u00f3n removida";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Protection removed";
        };
    }

    @Nonnull
    public static String cannotBreakProtection(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes romper esta protecci\u00f3n";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You cannot break this protection";
        };
    }

    @Nonnull
    public static String cannotBreakInside(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes romper bloques dentro de esta zona protegida";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You cannot break blocks inside this protected area";
        };
    }

    @Nonnull
    public static String cannotUseInside(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes usar bloques dentro de esta zona protegida";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You cannot use blocks inside this protected area";
        };
    }

    @Nonnull
    public static String cannotDamageCrops(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes da\u00f1ar o cosechar cultivos dentro de esta zona protegida";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You cannot damage or harvest crops inside this protected area";
        };
    }

    @Nonnull
    public static String cannotHurtAnimals(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes lastimar animales dentro de esta zona protegida";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You cannot hurt animals inside this protected area";
        };
    }

    @Nonnull
    public static String cannotInteractAnimals(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No puedes interactuar o capturar animales dentro de esta zona protegida";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You cannot interact with or capture animals inside this protected area";
        };
    }

    @Nonnull
    public static String enteredArea(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull String owner) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Entraste a una zona protegida (due\u00f1o: " + owner + ")";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You entered a protected area (owner: " + owner + ")";
        };
    }

    @Nonnull
    public static String leftArea(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, @Nonnull String owner) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Saliste de una zona protegida (due\u00f1o: " + owner + ")";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You left a protected area (owner: " + owner + ")";
        };
    }

    @Nonnull
    public static String onlyOwnerAddFriends(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Solo el due\u00f1o puede agregar amigos";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Only the owner can add friends";
        };
    }

    @Nonnull
    public static String ownerAlreadyFullAccess(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "El due\u00f1o ya tiene acceso total";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Owner already has full access";
        };
    }

    @Nonnull
    public static String onlyOwnerEditFriends(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Solo el due\u00f1o puede editar amigos/permisos";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Only the owner can edit friends/permissions";
        };
    }

    @Nonnull
    public static String borderOn(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Borde: ACTIVADO";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Border: ON";
        };
    }

    @Nonnull
    public static String borderOff(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Borde: DESACTIVADO";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Border: OFF";
        };
    }

    @Nonnull
    public static String uiTitlePlayersInArea(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Jugadores en el \u00e1rea";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Players in area";
        };
    }

    @Nonnull
    public static String uiSubtitleAddFriendAll(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Click para agregar como amigo (TODO)";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Click to add as friend (ALL)";
        };
    }

    @Nonnull
    public static String uiNone(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "(ninguno)";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "(none)";
        };
    }

    @Nonnull
    public static String uiFriendsTrust(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Amigos / Trust";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Friends / Trust";
        };
    }

    @Nonnull
    public static String uiCyclePermsHint(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Click a un jugador para cambiar permisos";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Click a player to cycle perms";
        };
    }

    @Nonnull
    public static String uiClickAddAll(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Click para agregar (TODO)";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Click to add (ALL)";
        };
    }

    @Nonnull
    public static String uiShowBorder(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Mostrar borde";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Show border";
        };
    }

    @Nonnull
    public static String uiPvpInZone(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "PvP en zona";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "PvP in zone";
        };
    }

    @Nonnull
    public static String uiSlot0Title(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Esencia de Vida";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Essence of Life";
        };
    }

    @Nonnull
    public static String uiSlot0Subtitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "1h de proteccion divina";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "1hr of Divine Protection";
        };
    }

    @Nonnull
    public static String uiFeedUpkeepTitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Haz una ofrenda";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Make an Offering";
        };
    }

    @Nonnull
    public static String uiFeedUpkeepSubtitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Abrir slots para agregar esencia";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Open slots to add essence";
        };
    }

    @Nonnull
    public static String DeityLandProtectionDestroyTitlePrimary(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang, int seconds) {
        int s = Math.max(0, seconds);
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "SE DESTRUYE EN " + s + "s";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "DESTROYS IN " + s + "s";
        };
    }

    @Nonnull
    public static String DeityLandProtectionDestroyTitleSecondary(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Agrega Esencia de Vida para salvar tu DeityLandProtection";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Add Essence of Life to save your DeityLandProtection";
        };
    }

    @Nonnull
    public static String uiMyClaimsTitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Mis DeityLandProtections";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "My DeityLandProtections";
        };
    }

    @Nonnull
    public static String uiMyClaimsSubtitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Ver estado de todas tus protecciones";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "View status of all your protections";
        };
    }

    @Nonnull
    public static String uiMyClaimsNone(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No tienes protecciones";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "You have no protections";
        };
    }

    @Nonnull
    public static String uiAdminTitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Admin DeityLandProtection";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "DeityLandProtection Admin";
        };
    }

    @Nonnull
    public static String uiAdminSubtitle(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Opciones de operador";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Operator settings";
        };
    }

    @Nonnull
    public static String uiAdminLanguage(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Idioma";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Language";
        };
    }

    @Nonnull
    public static String uiAdminCrafting(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Crafteo DeityLandProtection";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "DeityLandProtection crafting";
        };
    }

    @Nonnull
    public static String uiAdminReload(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Recargar config/data";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Reload config/data";
        };
    }

    @Nonnull
    public static String uiAdminRemoveClaim(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Remover claim (aqui)";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Remove claim (here)";
        };
    }

    @Nonnull
    public static String uiAdminRemoveClaimNone(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "No hay proteccion aqui";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "No protection here";
        };
    }

    @Nonnull
    public static String uiAdminServerSettings(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Servidor";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Server";
        };
    }

    @Nonnull
    public static String uiAdminClaimRadius(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Radio default";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Default radius";
        };
    }

    @Nonnull
    public static String uiAdminMaxClaims(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Max claims por jugador";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Max claims per player";
        };
    }

    @Nonnull
    public static String uiAdminMapClaimVisual(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Visual de claims en mapa";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Map claim visual";
        };
    }

    @Nonnull
    public static String uiAdminUpkeep(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Upkeep";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Upkeep";
        };
    }

    @Nonnull
    public static String uiAdminUpkeepEnabled(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Upkeep activado";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Upkeep enabled";
        };
    }

    @Nonnull
    public static String uiAdminUpkeepGraceMinus(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Grace -10m";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Grace -10m";
        };
    }

    @Nonnull
    public static String uiAdminUpkeepGracePlus(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Grace +10m";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Grace +10m";
        };
    }

    @Nonnull
    public static String uiAdminUpkeepEssenceCost(@Nonnull DeityLandProtectionLangPreferenceManager.Language lang) {
        return switch (lang) {
            default -> throw new MatchException(null, null);
            case DeityLandProtectionLangPreferenceManager.Language.ES -> "Defecto Esencia por hora";
            case DeityLandProtectionLangPreferenceManager.Language.EN -> "Default Essence per hour";
        };
    }

}



