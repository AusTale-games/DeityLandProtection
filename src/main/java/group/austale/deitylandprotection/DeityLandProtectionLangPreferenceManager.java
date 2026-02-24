/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 */
package group.austale.deitylandprotection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class DeityLandProtectionLangPreferenceManager {
    private static final String FILE_NAME = "DeityLandProtectionlang.properties";
    private static final String KEY_DEFAULT = "default";
    private final ConcurrentHashMap<UUID, Language> overrideByPlayer = new ConcurrentHashMap();
    private volatile Language defaultLanguage = Language.EN;
    private final Path filePath;

    public DeityLandProtectionLangPreferenceManager(Path dataDirectory) {
        if (dataDirectory == null) {
            dataDirectory = Path.of(".", new String[0]);
        }
        this.filePath = dataDirectory.resolve(FILE_NAME);
        this.load();
    }

    @Nonnull
    public Language getDefaultLanguage() {
        return this.defaultLanguage;
    }

    @Nonnull
    public Language setDefaultLanguage(@Nonnull Language language) {
        this.defaultLanguage = language;
        this.save();
        return language;
    }

    @Nullable
    public Language getOverride(@Nullable UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return this.overrideByPlayer.get(playerId);
    }

    @Nonnull
    public Language setOverride(@Nonnull UUID playerId, @Nonnull Language language) {
        this.overrideByPlayer.put(playerId, language);
        this.save();
        return language;
    }

    public boolean clearOverride(@Nonnull UUID playerId) {
        boolean removed;
        boolean bl = removed = this.overrideByPlayer.remove(playerId) != null;
        if (removed) {
            this.save();
        }
        return removed;
    }

    @Nonnull
    public Language getEffectiveLanguage(@Nullable UUID playerId) {
        Language override = this.getOverride(playerId);
        return override != null ? override : this.defaultLanguage;
    }

    public void load() {
        try {
            Files.createDirectories(this.filePath.getParent(), new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (!Files.exists(this.filePath, new LinkOption[0])) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(this.filePath, new OpenOption[0]);){
            props.load(in);
        }
        catch (IOException ignored) {
            return;
        }
        try {
            this.defaultLanguage = Language.fromCode(props.getProperty(KEY_DEFAULT));
        }
        catch (RuntimeException ignored) {
            this.defaultLanguage = Language.EN;
        }
        for (String key : props.stringPropertyNames()) {
            if (KEY_DEFAULT.equalsIgnoreCase(key)) continue;
            try {
                UUID id = UUID.fromString(key);
                Language lang = Language.fromCode(props.getProperty(key));
                this.overrideByPlayer.put(id, lang);
            }
            catch (RuntimeException runtimeException) {}
        }
    }

    public void save() {
        try {
            Files.createDirectories(this.filePath.getParent(), new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        Properties props = new Properties();
        props.setProperty(KEY_DEFAULT, this.defaultLanguage.getCode());
        for (Map.Entry<UUID, Language> entry : this.overrideByPlayer.entrySet()) {
            UUID id = entry.getKey();
            Language lang = entry.getValue();
            if (id == null || lang == null) continue;
            props.setProperty(id.toString(), lang.getCode());
        }
        try (OutputStream out = Files.newOutputStream(this.filePath, new OpenOption[0]);){
            props.store(out, "DeityLandProtection language preferences (default + per-player override)");
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static enum Language {
        EN("en"),
        ES("es");

        private final String code;

        private Language(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

        @Nonnull
        public static Language fromCode(@Nullable String code) {
            if (code == null) {
                return EN;
            }
            String s = code.trim().toLowerCase();
            if (s.equals("es") || s.equals("spa") || s.equals("spanish")) {
                return ES;
            }
            return EN;
        }
    }
}



