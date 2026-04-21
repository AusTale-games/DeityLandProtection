package group.austale.deitylandprotection;

import java.text.MessageFormat;
import java.util.Map;

public final class DeityLandProtectionLocalizer {
    public String tr(DeityLandProtectionLangPreferenceManager.Language language, String key, Object... args) {
        DeityLandProtectionLangPreferenceManager.Language safeLanguage = language == null ? DeityLandProtectionLangPreferenceManager.Language.EN : language;
        Map<String, String> bundle = DeityLandProtectionLocalizationCatalog.getBundle(safeLanguage);
        String template = bundle.get(key);
        if (template == null) {
            template = DeityLandProtectionLocalizationCatalog.getDefaultEnglish(key);
        }
        if (template == null) {
            template = key;
        }
        if (args == null || args.length == 0) {
            return template;
        }
        try {
            return MessageFormat.format(template, args);
        } catch (IllegalArgumentException ignored) {
            return template;
        }
    }
}
