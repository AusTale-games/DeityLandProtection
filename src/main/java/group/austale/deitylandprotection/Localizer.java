package group.austale.deitylandprotection;

import java.text.MessageFormat;
import java.util.Map;

public final class Localizer {
    public String tr(LangPreferenceManager.Language language, String key, Object... args) {
        LangPreferenceManager.Language safeLanguage = language == null ? LangPreferenceManager.Language.EN : language;
        Map<String, String> bundle = LocalizationCatalog.getBundle(safeLanguage);
        String template = bundle.get(key);
        if (template == null) {
            template = LocalizationCatalog.getDefaultEnglish(key);
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
