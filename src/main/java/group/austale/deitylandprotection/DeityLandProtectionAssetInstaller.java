package group.austale.deitylandprotection;

import com.hypixel.hytale.logger.HytaleLogger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Owns the on-disk asset pack lifecycle for the plugin's data directory:
 * - writes/refreshes the asset pack manifest,
 * - strips legacy duplicate Custom UI from the data folder,
 * - writes the per-server custom Deity item resources (with recipe cost overrides applied).
 */
final class DeityLandProtectionAssetInstaller {
    private static final String MANIFEST_CONTENT = "{\"Group\":\"games.Austale\",\"Name\":\"DeityLandProtectionData\",\"Version\":\"1.2.1\",\"ServerVersion\":\"2026.03.26-89796e57b\"}";

    private final HytaleLogger logger;
    private final DeityLandProtectionRecipeOverrider recipeOverrider;

    DeityLandProtectionAssetInstaller(HytaleLogger logger, DeityLandProtectionRecipeOverrider recipeOverrider) {
        this.logger = logger;
        this.recipeOverrider = recipeOverrider;
    }

    void ensureAssetPackManifest(Path dataDir) {
        try {
            Files.createDirectories(dataDir, new FileAttribute[0]);
            Path manifest = dataDir.resolve("manifest.json");
            if (Files.exists(manifest, new LinkOption[0])) {
                try {
                    String existing = Files.readString(manifest, StandardCharsets.UTF_8);
                    if (existing != null && existing.equals(MANIFEST_CONTENT)) {
                        return;
                    }
                }
                catch (IOException ignored) {
                    // Manifest is unreadable; fall through and overwrite it.
                }
            }
            Files.writeString(manifest, MANIFEST_CONTENT, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException e) {
            ((HytaleLogger.Api) this.logger.at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to write asset pack manifest");
        }
    }

    void removeDuplicateCustomUiFromDataPack(Path dataDir) {
        if (dataDir == null) {
            return;
        }
        String[] roots = new String[]{"Common/UI/Custom", "Assets/Common/UI/Custom"};
        try {
            boolean removed = false;
            for (String rel : roots) {
                Path root = dataDir.resolve(rel);
                if (!Files.isDirectory(root)) {
                    continue;
                }
                removed = true;
                try (Stream<Path> walk = Files.walk(root)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        }
                        catch (IOException ignored) {
                            // best-effort cleanup; leave stragglers in place.
                        }
                    });
                }
            }
            if (removed) {
                ((HytaleLogger.Api) this.logger.at(Level.INFO)).log("DeityLandProtection removed duplicate Custom UI from plugin data folder (UI stays in the main asset pack only).");
            }
        }
        catch (Exception e) {
            ((HytaleLogger.Api) this.logger.at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to strip duplicate UI from data pack");
        }
    }

    void ensureCustomDeityItem(Path dataDir) {
        try {
            this.writeCustomDeityItemResource(dataDir.resolve("Server/Item/Items/DeityLandProtection/SlumberingDeity_Block.json"), "Server/Item/Items/DeityLandProtection/SlumberingDeity_Block.json");
            this.writeCustomDeityItemResource(dataDir.resolve("Assets/Server/Item/Items/DeityLandProtection/SlumberingDeity_Block.json"), "Assets/Server/Item/Items/DeityLandProtection/SlumberingDeity_Block.json");
            this.writeCustomDeityItemResource(dataDir.resolve("Server/Item/Items/DeityLandProtection/OutlanderDeity_Block.json"), "Server/Item/Items/DeityLandProtection/OutlanderDeity_Block.json");
            this.writeCustomDeityItemResource(dataDir.resolve("Assets/Server/Item/Items/DeityLandProtection/OutlanderDeity_Block.json"), "Assets/Server/Item/Items/DeityLandProtection/OutlanderDeity_Block.json");
        }
        catch (Exception e) {
            ((HytaleLogger.Api) this.logger.at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to write custom item asset");
        }
    }

    private void writeCustomDeityItemResource(Path dest, String resourcePath) throws IOException {
        if (dest == null || resourcePath == null) {
            return;
        }
        Files.createDirectories(dest.getParent(), new FileAttribute[0]);
        ClassLoader classLoader = DeityLandProtectionAssetInstaller.class.getClassLoader();
        if (classLoader == null) {
            return;
        }
        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return;
            }
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            String overridden = this.recipeOverrider.applyOverrides(resourcePath, content);
            if (Files.exists(dest, new LinkOption[0])) {
                try {
                    String existing = Files.readString(dest, StandardCharsets.UTF_8);
                    if (existing.equals(overridden)) {
                        return;
                    }
                }
                catch (Exception ignored) {
                    // Fall through and overwrite if the file cannot be read cleanly.
                }
            }
            Files.writeString(dest, overridden, StandardCharsets.UTF_8, new OpenOption[0]);
        }
    }
}
