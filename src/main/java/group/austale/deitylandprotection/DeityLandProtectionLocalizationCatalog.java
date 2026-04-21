package group.austale.deitylandprotection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DeityLandProtectionLocalizationCatalog {
    private static final LinkedHashMap<String, String> EN = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> ES = new LinkedHashMap<>();

    static {
        put("help.lang", "Usage: /DeityLandProtectionlang status | es|en | clear | default es|en", "Uso: /DeityLandProtectionlang status | es|en | clear | default es|en");
        put("common.no_permission", "No permission", "No tienes permiso");
        put("lang.status", "DeityLandProtection language: {0} (default: {1}{2})", "Idioma DeityLandProtection: {0} (predeterminado: {1}{2})");
        put("lang.status.override_suffix", ", your override: {0}", ", tu override: {0}");
        put("lang.status.no_override_suffix", ", no override", ", sin override");
        put("lang.updated", "Language updated: {0}", "Idioma actualizado: {0}");
        put("lang.default_updated", "Server default language updated: {0}", "Idioma predeterminado del servidor actualizado: {0}");
        put("lang.override_cleared", "Your override was cleared (now using server default)", "Tu override fue removido (ahora usas el predeterminado del servidor)");
        put("claim.cannot_place_limit", "Cannot place DeityLandProtection: claim limit reached ({0}/{1})", "No puedes colocar DeityLandProtection: limite de claims alcanzado ({0}/{1})");
        put("claim.cannot_place_area_protected", "Cannot place DeityLandProtection: area is protected", "No puedes colocar DeityLandProtection: el area esta protegida");
        put("claim.cannot_place_overlap", "Cannot place DeityLandProtection: protection would overlap another protection", "No puedes colocar DeityLandProtection: se superpondria con otra proteccion");
        put("claim.protection_created", "Protection created (radius {0})", "Proteccion creada (radio {0})");
        put("claim.cannot_place_here", "Cannot place DeityLandProtection here", "No puedes colocar DeityLandProtection aqui");
        put("claim.cannot_place_inside", "You cannot place blocks inside this protected area", "No puedes colocar bloques dentro de esta zona protegida");
        put("claim.protection_removed", "Protection removed", "Proteccion removida");
        put("claim.cannot_break_protection", "You cannot break this protection", "No puedes romper esta proteccion");
        put("claim.cannot_break_inside", "You cannot break blocks inside this protected area", "No puedes romper bloques dentro de esta zona protegida");
        put("claim.cannot_use_inside", "You cannot use blocks inside this protected area", "No puedes usar bloques dentro de esta zona protegida");
        put("claim.cannot_damage_crops", "You cannot damage or harvest crops inside this protected area", "No puedes dañar o cosechar cultivos dentro de esta zona protegida");
        put("claim.cannot_hurt_animals", "You cannot hurt animals inside this protected area", "No puedes lastimar animales dentro de esta zona protegida");
        put("claim.cannot_interact_animals", "You cannot interact with or capture animals inside this protected area", "No puedes interactuar o capturar animales dentro de esta zona protegida");
        put("claim.entered_area", "You entered a protected area (owner: {0})", "Entraste a una zona protegida (dueño: {0})");
        put("claim.left_area", "You left a protected area (owner: {0})", "Saliste de una zona protegida (dueño: {0})");
        put("trust.only_owner_add", "Only the owner can add friends", "Solo el dueño puede agregar amigos");
        put("trust.owner_full_access", "Owner already has full access", "El dueño ya tiene acceso total");
        put("trust.only_owner_edit", "Only the owner can edit friends/permissions", "Solo el dueño puede editar amigos/permisos");
        put("border.on", "Border: ON", "Borde: ACTIVADO");
        put("border.off", "Border: OFF", "Borde: DESACTIVADO");
        put("border.on_short", "ON", "ACTIVADO");
        put("border.off_short", "OFF", "DESACTIVADO");
        put("ui.players_in_area.title", "Players in area", "Jugadores en el area");
        put("ui.players_in_area.add_all", "Click to add as friend (ALL)", "Click para agregar como amigo (TODOS)");
        put("ui.none", "(none)", "(ninguno)");
        put("ui.friends_trust", "Friends / Trust", "Amigos / Confianza");
        put("ui.cycle_perms_hint", "Click a player to cycle perms", "Click a un jugador para cambiar permisos");
        put("ui.click_add_all", "Click to add (ALL)", "Click para agregar (TODOS)");
        put("ui.show_border", "Show border", "Mostrar borde");
        put("ui.pvp_in_zone", "PvP in zone", "PvP en zona");
        put("ui.essence_life", "Essence of Life", "Esencia de Vida");
        put("ui.essence_void", "Essence of the Void", "Esencia del Vacio");
        put("ui.essence_life_subtitle", "1hr of Divine Protection", "1h de proteccion divina");
        put("ui.feed_upkeep.title", "Make an Offering", "Haz una ofrenda");
        put("ui.feed_upkeep.subtitle", "Open slots to add essence", "Abrir slots para agregar esencia");
        put("upkeep.destroy_title_primary", "DESTROYS IN {0}s", "SE DESTRUYE EN {0}s");
        put("upkeep.destroy_title_secondary", "Add Essence of Life to save your DeityLandProtection", "Agrega Esencia de Vida para salvar tu DeityLandProtection");
        put("ui.my_claims.title", "My DeityLandProtections", "Mis DeityLandProtections");
        put("ui.my_claims.subtitle", "View status of all your protections", "Ver estado de todas tus protecciones");
        put("ui.my_claims.none", "You have no protections", "No tienes protecciones");
        put("ui.admin.title", "DeityLandProtection Admin", "Admin DeityLandProtection");
        put("ui.admin.subtitle", "Operator settings", "Opciones de operador");
        put("ui.admin.language", "Language", "Idioma");
        put("ui.admin.crafting", "DeityLandProtection crafting", "Crafteo DeityLandProtection");
        put("ui.admin.reload", "Reload config/data", "Recargar config/data");
        put("ui.admin.remove_claim", "Remove claim (here)", "Remover claim (aqui)");
        put("ui.admin.remove_claim_none", "No protection here", "No hay proteccion aqui");
        put("ui.admin.server", "Server", "Servidor");
        put("ui.admin.claim_radius", "Default radius", "Radio predeterminado");
        put("ui.admin.max_claims", "Max claims per player", "Max claims por jugador");
        put("ui.admin.map_visual", "Map claim visual", "Visual de claims en mapa");
        put("ui.admin.upkeep", "Upkeep", "Upkeep");
        put("ui.admin.upkeep_enabled", "Upkeep enabled", "Upkeep activado");
        put("ui.admin.upkeep_grace_minus", "Grace -10m", "Gracia -10m");
        put("ui.admin.upkeep_grace_plus", "Grace +10m", "Gracia +10m");
        put("ui.admin.upkeep_essence_cost", "Default Essence per hour", "Esencia predeterminada por hora");
        put("error.ui.player_component", "DeityLandProtection UI error: cannot read Player component", "Error UI DeityLandProtection: no se pudo leer el componente Player");
        put("pvp.disabled_in_area", "PvP is disabled in this protected area", "El PvP esta desactivado en esta zona protegida");
        put("tier.upgraded", "Deity upgraded to Tier {0} (radius {1}, essence/hr {2})", "Deidad mejorada a Tier {0} (radio {1}, esencia/h {2})");
        put("tier.no_unclaimed_territory", "No unclaimed territory to expand into", "No hay territorio sin reclamar para expandir");
    }

    private DeityLandProtectionLocalizationCatalog() {
    }

    private static void put(String key, String en, String es) {
        EN.put(key, en);
        ES.put(key, es);
    }

    public static Map<String, String> getBundle(DeityLandProtectionLangPreferenceManager.Language language) {
        if (language == DeityLandProtectionLangPreferenceManager.Language.ES) {
            return ES;
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
