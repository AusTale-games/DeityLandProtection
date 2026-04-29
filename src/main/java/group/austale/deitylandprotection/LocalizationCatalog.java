package group.austale.deitylandprotection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LocalizationCatalog {
    private static final LinkedHashMap<String, String> EN = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> ES = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> FR = new LinkedHashMap<>();

    static {
        put("help.lang", "Usage: /DeityLandProtectionlang status | es|en|fr | clear | default es|en|fr", "Uso: /DeityLandProtectionlang status | es|en|fr | clear | default es|en|fr", "Usage : /DeityLandProtectionlang status | es|en|fr | clear | default es|en|fr");
        put("common.no_permission", "No permission", "No tienes permiso", "Permission refusée");
        put("lang.status", "DeityLandProtection language: {0} (default: {1}{2})", "Idioma DeityLandProtection: {0} (predeterminado: {1}{2})", "Langue DeityLandProtection : {0} (par défaut : {1}{2})");
        put("lang.status.override_suffix", ", your override: {0}", ", tu override: {0}", ", votre préférence : {0}");
        put("lang.status.no_override_suffix", ", no override", ", sin override", ", aucune préférence personnelle");
        put("lang.updated", "Language updated: {0}", "Idioma actualizado: {0}", "Langue mise à jour : {0}");
        put("lang.default_updated", "Server default language updated: {0}", "Idioma predeterminado del servidor actualizado: {0}", "Langue par défaut du serveur mise à jour : {0}");
        put("lang.override_cleared", "Your override was cleared (now using server default)", "Tu override fue removido (ahora usas el predeterminado del servidor)", "Votre préférence a été réinitialisée (langue du serveur appliquée)");
        put("claim.cannot_place_limit", "Cannot place DeityLandProtection: claim limit reached ({0}/{1})", "No puedes colocar DeityLandProtection: limite de claims alcanzado ({0}/{1})", "Impossible de placer DeityLandProtection : limite de revendications atteinte ({0}/{1})");
        put("claim.cannot_place_area_protected", "Cannot place DeityLandProtection: area is protected", "No puedes colocar DeityLandProtection: el area esta protegida", "Impossible de placer DeityLandProtection : la zone est protégée");
        put("claim.cannot_place_overlap", "Cannot place DeityLandProtection: protection would overlap another protection", "No puedes colocar DeityLandProtection: se superpondria con otra proteccion", "Impossible de placer DeityLandProtection : chevauchement avec une autre protection");
        put("claim.protection_created", "Protection created (radius {0})", "Proteccion creada (radio {0})", "Protection créée (rayon {0})");
        put("claim.cannot_place_here", "Cannot place DeityLandProtection here", "No puedes colocar DeityLandProtection aqui", "Impossible de placer DeityLandProtection ici");
        put("claim.cannot_place_inside", "You cannot place blocks inside this protected area", "No puedes colocar bloques dentro de esta zona protegida", "Vous ne pouvez pas placer de blocs dans cette zone protégée");
        put("claim.protection_removed", "Protection removed", "Proteccion removida", "Protection retirée");
        put("claim.cannot_break_protection", "You cannot break this protection", "No puedes romper esta proteccion", "Vous ne pouvez pas casser cette protection");
        put("claim.cannot_break_inside", "You cannot break blocks inside this protected area", "No puedes romper bloques dentro de esta zona protegida", "Vous ne pouvez pas casser de blocs dans cette zone protégée");
        put("claim.cannot_use_inside", "You cannot use blocks inside this protected area", "No puedes usar bloques dentro de esta zona protegida", "Vous ne pouvez pas utiliser de blocs dans cette zone protégée");
        put("claim.cannot_damage_crops", "You cannot damage or harvest crops inside this protected area", "No puedes dañar o cosechar cultivos dentro de esta zona protegida", "Vous ne pouvez pas endommager ou récolter dans cette zone protégée");
        put("claim.cannot_hurt_animals", "You cannot hurt animals inside this protected area", "No puedes lastimar animales dentro de esta zona protegida", "Vous ne pouvez pas blesser les animaux dans cette zone protégée");
        put("claim.cannot_interact_animals", "You cannot interact with or capture animals inside this protected area", "No puedes interactuar o capturar animales dentro de esta zona protegida", "Vous ne pouvez pas interagir avec ou capturer les animaux dans cette zone protégée");
        put("claim.entered_area", "You entered a protected area (owner: {0})", "Entraste a una zona protegida (dueño: {0})", "Vous entrez dans une zone protégée (propriétaire : {0})");
        put("claim.left_area", "You left a protected area (owner: {0})", "Saliste de una zona protegida (dueño: {0})", "Vous quittez une zone protégée (propriétaire : {0})");
        put("trust.only_owner_add", "Only the owner can add friends", "Solo el dueño puede agregar amigos", "Seul le propriétaire peut ajouter des amis");
        put("trust.owner_full_access", "Owner already has full access", "El dueño ya tiene acceso total", "Le propriétaire a déjà tous les droits");
        put("trust.only_owner_edit", "Only the owner can edit friends/permissions", "Solo el dueño puede editar amigos/permisos", "Seul le propriétaire peut modifier les amis et les permissions");
        put("border.on", "Border: ON", "Borde: ACTIVADO", "Bordure : activée");
        put("border.off", "Border: OFF", "Borde: DESACTIVADO", "Bordure : désactivée");
        put("border.on_short", "ON", "ACTIVADO", "Oui");
        put("border.off_short", "OFF", "DESACTIVADO", "Non");
        put("ui.players_in_area.title", "Players in area", "Jugadores en el area", "Joueurs dans la zone");
        put("ui.players_in_area.add_all", "Click to add as friend (ALL)", "Click para agregar como amigo (TODOS)", "Cliquer pour ajouter en ami (TOUS)");
        put("ui.none", "(none)", "(ninguno)", "(aucun)");
        put("ui.friends_trust", "Friends / Trust", "Amigos / Confianza", "Amis / confiance");
        put("ui.cycle_perms_hint", "Click a player to cycle perms", "Click a un jugador para cambiar permisos", "Cliquez sur un joueur pour faire défiler les permissions");
        put("ui.click_add_all", "Click to add (ALL)", "Click para agregar (TODOS)", "Cliquer pour ajouter (TOUS)");
        put("ui.show_border", "Show border", "Mostrar borde", "Afficher la bordure");
        put("ui.pvp_in_zone", "PvP in zone", "PvP en zona", "PvP dans la zone");
        put("ui.essence_life", "Essence of Life", "Esencia de Vida", "Essence de vie");
        put("ui.essence_void", "Essence of the Void", "Esencia del Vacio", "Essence du vide");
        put("ui.essence_life_subtitle", "1hr of Divine Protection", "1h de proteccion divina", "1 h de protection divine");
        put("ui.feed_upkeep.title", "Make an Offering", "Haz una ofrenda", "Faire une offrande");
        put("ui.feed_upkeep.subtitle", "Open slots to add essence", "Abrir slots para agregar esencia", "Ouvrez les emplacements pour ajouter de l’essence");
        put("upkeep.destroy_title_primary", "DESTROYS IN {0}s", "SE DESTRUYE EN {0}s", "DESTRUCTION DANS {0} s");
        put("upkeep.destroy_title_secondary", "Add Essence of Life to save your DeityLandProtection", "Agrega Esencia de Vida para salvar tu DeityLandProtection", "Ajoutez de l’essence de vie pour sauver votre DeityLandProtection");
        put("ui.my_claims.title", "My DeityLandProtections", "Mis DeityLandProtections", "Mes DeityLandProtections");
        put("ui.my_claims.subtitle", "View status of all your protections", "Ver estado de todas tus protecciones", "Voir l’état de toutes vos protections");
        put("ui.my_claims.none", "You have no protections", "No tienes protecciones", "Vous n’avez aucune protection");
        put("ui.admin.title", "DeityLandProtection Admin", "Admin DeityLandProtection", "Administration DeityLandProtection");
        put("ui.admin.subtitle", "Operator settings", "Opciones de operador", "Paramètres opérateur");
        put("ui.admin.language", "Language", "Idioma", "Langue");
        put("ui.admin.crafting", "DeityLandProtection crafting", "Crafteo DeityLandProtection", "Fabrication DeityLandProtection");
        put("ui.admin.reload", "Reload config/data", "Recargar config/data", "Recharger config / données");
        put("ui.admin.remove_claim", "Remove claim (here)", "Remover claim (aqui)", "Retirer la revendication (ici)");
        put("ui.admin.remove_claim_none", "No protection here", "No hay proteccion aqui", "Aucune protection ici");
        put("ui.admin.server", "Server", "Servidor", "Serveur");
        put("ui.admin.claim_radius", "Default radius", "Radio predeterminado", "Rayon par défaut");
        put("ui.admin.max_claims", "Max claims per player", "Max claims por jugador", "Revendications max par joueur");
        put("ui.admin.map_visual", "Map claim visual", "Visual de claims en mapa", "Affichage des revendications sur la carte");
        put("ui.admin.upkeep", "Upkeep", "Upkeep", "Entretien");
        put("ui.admin.upkeep_enabled", "Upkeep enabled", "Upkeep activado", "Entretien activé");
        put("ui.admin.upkeep_grace_minus", "Grace -10m", "Gracia -10m", "Grâce -10 min");
        put("ui.admin.upkeep_grace_plus", "Grace +10m", "Gracia +10m", "Grâce +10 min");
        put("ui.admin.upkeep_essence_cost", "Default Essence per hour", "Esencia predeterminada por hora", "Essence par heure par défaut");
        put("error.ui.player_component", "DeityLandProtection UI error: cannot read Player component", "Error UI DeityLandProtection: no se pudo leer el componente Player", "Erreur d’interface DeityLandProtection : impossible de lire le composant joueur");
        put("pvp.disabled_in_area", "PvP is disabled in this protected area", "El PvP esta desactivado en esta zona protegida", "Le JcJ est désactivé dans cette zone protégée");
        put("tier.upgraded", "Deity upgraded to Tier {0} (radius {1}, essence/hr {2})", "Deidad mejorada a Tier {0} (radio {1}, esencia/h {2})", "Divinité passée au palier {0} (rayon {1}, essence/h {2})");
        put("tier.no_unclaimed_territory", "No unclaimed territory to expand into", "No hay territorio sin reclamar para expandir", "Aucun terrain libre pour l’extension");
    }

    private LocalizationCatalog() {
    }

    private static void put(String key, String en, String es, String fr) {
        EN.put(key, en);
        ES.put(key, es);
        FR.put(key, fr);
    }

    public static Map<String, String> getBundle(LangPreferenceManager.Language language) {
        if (language == LangPreferenceManager.Language.ES) {
            return ES;
        }
        if (language == LangPreferenceManager.Language.FR) {
            return FR;
        }
        return EN;
    }

    public static String getDefaultEnglish(String key) {
        return EN.get(key);
    }

    public static void writeGeneratedLanguageFiles(Path dataDir) {
        if (dataDir == null) {
            return;
        }
        writeBundle(dataDir.resolve("Server/Languages/en-US/DeityLandProtection.lang"), EN);
        writeBundle(dataDir.resolve("Server/Languages/es-ES/DeityLandProtection.lang"), ES);
        writeBundle(dataDir.resolve("Server/Languages/fr-FR/DeityLandProtection.lang"), FR);
    }

    private static void writeBundle(Path filePath, LinkedHashMap<String, String> bundle) {
        if (filePath == null || bundle == null) {
            return;
        }
        StringBuilder content = new StringBuilder();
        content.append("# Auto-generated by DeityLandProtection at startup.\n");
        content.append("# You can translate values; keep keys unchanged.\n\n");
        for (Map.Entry<String, String> entry : bundle.entrySet()) {
            content.append(entry.getKey()).append(" = ").append(escapeValue(entry.getValue())).append('\n');
        }
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            Files.writeString(filePath, content.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static String escapeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\n", "\\n");
    }
}
