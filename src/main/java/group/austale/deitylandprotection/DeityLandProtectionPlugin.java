package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.ClaimStore;
import group.austale.deitylandprotection.DeityLandProtectionBorderTickSystem;
import group.austale.deitylandprotection.DeityLandProtectionBreakSystem;
import group.austale.deitylandprotection.DeityLandProtectionCommand;
import group.austale.deitylandprotection.DeityLandProtectionCraftSystem;
import group.austale.deitylandprotection.DeityLandProtectionEnterExitTickSystem;
import group.austale.deitylandprotection.DeityLandProtectionLangCommand;
import group.austale.deitylandprotection.DeityLandProtectionLangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionLocalizationCatalog;
import group.austale.deitylandprotection.DeityLandProtectionLocalizer;
import group.austale.deitylandprotection.DeityLandProtectionPlaceSystem;
import group.austale.deitylandprotection.DeityLandProtectionUpkeepStore;
import group.austale.deitylandprotection.DeityLandProtectionUseBlockSystem;
import group.austale.deitylandprotection.DeityLandProtectionBorderSurfaceRefreshSystem;
import group.austale.deitylandprotection.DeityLandProtectionWorldMapProvider;
import group.austale.deitylandprotection.DeityLandProtectionWorldMapUpdateTickingSystem;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk.WorldGenWorldMapProvider;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.stream.Stream;

public class DeityLandProtectionPlugin
extends JavaPlugin {
    private static final int DEFAULT_RADIUS = 16;
    private static final String DEFAULT_DeityLandProtection_ITEM_ID = "SlumberingDeity_Block";
    private static final String CONFIG_DEITY_ITEM_ID_KEY = "DeityLandProtectionItemId";
    private static final String CONFIG_OUTLANDER_DEITY_ITEM_ID_KEY = "OutlanderDeityItemId";
    private static final String OUTLANDER_DEITY_ITEM_ID = "OutlanderDeity_Block";
    private static final String OUTLANDER_DEITY_BLOCK_ITEM_ID = "Furniture_Temple_Dark_Statue_Gaia";
    private static final String ESSENCE_OF_LIFE_ITEM_ID = "Ingredient_Life_Essence";
    private static final String ESSENCE_OF_VOID_ITEM_ID = "Ingredient_Void_Essence";
    private static final int DEFAULT_MAX_CLAIMS_PER_PLAYER = 1;
    private static final int MAX_CLAIMS_PER_PLAYER_CAP = 5;
    private static final boolean DEFAULT_ALLOW_CRAFTING = true;
    private static final boolean DEFAULT_MAP_CLAIM_VISUAL_ENABLED = true;
    private static final int DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR = 1;
    private static final int DEFAULT_UPKEEP_GRACE_MINUTES = 30;
    private static final int DEFAULT_SLUMBERING_RECIPE_COBBLE_COST = 20;
    private static final int DEFAULT_SLUMBERING_RECIPE_ESSENCE_COST = 10;
    private static final int DEFAULT_OUTLANDER_RECIPE_COBBLE_COST = 20;
    private static final int DEFAULT_OUTLANDER_RECIPE_ESSENCE_COST = 10;
    private static final String DEFAULT_UPGRADE_TIER_2_ITEM_ID = "Ingredient_Bar_Adamantite";
    private static final int DEFAULT_UPGRADE_TIER_2_ITEM_QUANTITY = 25;
    private static final String DEFAULT_UPGRADE_TIER_3_ITEM_ID = "Wood_Crystal_Trunk";
    private static final int DEFAULT_UPGRADE_TIER_3_ITEM_QUANTITY = 100;
    private static final String DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID = "Rock_Gem_Ruby";
    private static final int DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY = 1;
    private static final String DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID = "Rock_Gem_Sapphire";
    private static final int DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY = 1;
    private static final String CONFIG_SLUMBERING_RECIPE_COBBLE_COST_KEY = "slumberingRecipeCobbleCost";
    private static final String CONFIG_SLUMBERING_RECIPE_ESSENCE_COST_KEY = "slumberingRecipeEssenceCost";
    private static final String CONFIG_OUTLANDER_RECIPE_COBBLE_COST_KEY = "outlanderRecipeCobbleCost";
    private static final String CONFIG_OUTLANDER_RECIPE_ESSENCE_COST_KEY = "outlanderRecipeEssenceCost";
    private static final String CONFIG_UPGRADE_TIER_2_ITEM_ID_KEY = "upgradeTier2ItemId";
    private static final String CONFIG_UPGRADE_TIER_2_ITEM_QUANTITY_KEY = "upgradeTier2ItemQuantity";
    private static final String CONFIG_UPGRADE_TIER_3_ITEM_ID_KEY = "upgradeTier3ItemId";
    private static final String CONFIG_UPGRADE_TIER_3_ITEM_QUANTITY_KEY = "upgradeTier3ItemQuantity";
    private static final String CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_ID_KEY = "upgradeTier4PrimaryItemId";
    private static final String CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY_KEY = "upgradeTier4PrimaryItemQuantity";
    private static final String CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_ID_KEY = "upgradeTier4SecondaryItemId";
    private static final String CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY_KEY = "upgradeTier4SecondaryItemQuantity";
    private static final int[] ALLOWED_RADII = new int[]{16, 32, 64, 128};
    private static final long FLUSH_PERIOD_SECONDS = 10L;
    private static final long RECENT_PLACEMENT_IGNORE_BREAK_MS = 2000L;
    private static final long PLAYER_MESSAGE_COOLDOWN_MS = 1200L;
    private static volatile DeityLandProtectionPlugin instance;
    private ClaimStore claimStore;
    private String DeityLandProtectionItemId;
    private String outlanderDeityItemId;
    private int claimRadius;
    private int maxClaimsPerPlayer;
    private boolean allowCrafting;
    private boolean mapClaimVisualEnabled;
    private ScheduledExecutorService flushExecutor;
    private DeityLandProtectionUpkeepStore upkeepStore;
    private boolean upkeepEnabled;
    private int upkeepGraceMinutes;
    private int upkeepEssenceCostPerHour;
    private int slumberingRecipeCobbleCost;
    private int slumberingRecipeEssenceCost;
    private int outlanderRecipeCobbleCost;
    private int outlanderRecipeEssenceCost;
    private String upgradeTier2ItemId;
    private int upgradeTier2ItemQuantity;
    private String upgradeTier3ItemId;
    private int upgradeTier3ItemQuantity;
    private String upgradeTier4PrimaryItemId;
    private int upgradeTier4PrimaryItemQuantity;
    private String upgradeTier4SecondaryItemId;
    private int upgradeTier4SecondaryItemQuantity;
    private Path absDataDir;
    private DeityLandProtectionLangPreferenceManager langPreferenceManager;
    private DeityLandProtectionLocalizer localizer;
    private final ConcurrentHashMap<Long, Long> recentClaimPlacements = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, Long> lastPlayerMessageMs = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, Long> borderCenterByPlayer = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, Long> lastBorderSpawnMsByPlayer = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, String> borderWorldByPlayer = new ConcurrentHashMap();
    private final DeityLandProtectionBorderSurfaceCache borderSurfaceCache = new DeityLandProtectionBorderSurfaceCache();
    private final ConcurrentHashMap<String, Integer> borderSurfaceScanBaseYByWorldCenter = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, Long> lastZoneKeyByPlayer = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, String> knownUsernameByUuid = new ConcurrentHashMap();
    private final ConcurrentHashMap<String, UUID> knownUuidByUsername = new ConcurrentHashMap();
    private final ConcurrentHashMap<Long, ConcurrentHashMap<UUID, String>> playersInClaim = new ConcurrentHashMap();
    private final ConcurrentHashMap<String, LongSet> mapUpdateQueueByWorld = new ConcurrentHashMap();

    public DeityLandProtectionPlugin(JavaPluginInit init) {
        super(init);
        try {
            Path dataDir = this.getDataDirectory();
            this.absDataDir = dataDir.toAbsolutePath().normalize();
            this.ensureAssetPackManifest(this.absDataDir);
            this.removeDuplicateCustomUiFromDataPack(this.absDataDir);
            this.loadRecipeCostConfig(this.absDataDir);
            this.ensureCustomDeityLandProtectionItem(this.absDataDir);
        }
        catch (Exception e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed during plugin construction; setup() will retry");
        }
    }

    public static DeityLandProtectionPlugin getInstance() {
        return instance;
    }

    private Path resolveConfigPath(Path dataDir) {
        Path baseDir = dataDir;
        if (baseDir == null) {
            try {
                baseDir = this.getDataDirectory();
            }
            catch (Exception ignored) {
                baseDir = null;
            }
        }
        if (baseDir == null) {
            baseDir = Path.of(".");
        }
        Path serverRoot = this.resolveServerRoot(baseDir);
        Path configDir = serverRoot.resolve("config").resolve("DeityLandProtection");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        return configDir.resolve("config.json");
    }

    private Path resolveServerRoot(Path dataDir) {
        if (dataDir == null) {
            return Path.of(".");
        }
        Path current = dataDir.toAbsolutePath().normalize();
        while (current != null) {
            Path name = current.getFileName();
            if (name != null && name.toString().equalsIgnoreCase("mods")) {
                Path parent = current.getParent();
                return parent == null ? current : parent;
            }
            current = current.getParent();
        }
        return dataDir.toAbsolutePath().normalize();
    }

    private int loadClaimRadius(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return DEFAULT_RADIUS;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Integer r = JsonReader.readInt(json, "claimRadius");
            int normalized = DeityLandProtectionPlugin.normalizeRadius(r == null ? DEFAULT_RADIUS : r);
            return normalized > 0 ? normalized : DEFAULT_RADIUS;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
            return DEFAULT_RADIUS;
        }
    }

    private int loadMaxClaimsPerPlayer(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return 1;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Integer v = JsonReader.readInt(json, "maxClaimsPerPlayer");
            int requested = v == null ? 1 : v;
            return DeityLandProtectionPlugin.clampMaxClaimsPerPlayer(requested);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
            return 1;
        }
    }

    private boolean loadAllowCrafting(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return true;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Boolean v = JsonReader.readBoolean(json, "allowCrafting");
            return v == null ? true : v;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
            return true;
        }
    }

    private boolean loadMapClaimVisualEnabled(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return DEFAULT_MAP_CLAIM_VISUAL_ENABLED;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Boolean v = JsonReader.readBoolean(json, "mapClaimVisualEnabled");
            return v == null ? DEFAULT_MAP_CLAIM_VISUAL_ENABLED : v;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
            return DEFAULT_MAP_CLAIM_VISUAL_ENABLED;
        }
    }

    private void persistConfig() {
        if (this.absDataDir == null) {
            return;
        }
        Path cfg = this.resolveConfigPath(this.absDataDir);
        try {
            Path cfgDir = cfg.getParent();
            if (cfgDir != null) {
                Files.createDirectories(cfgDir, new FileAttribute[0]);
            }
            String content = "{\"" + CONFIG_DEITY_ITEM_ID_KEY + "\":\"" + (this.DeityLandProtectionItemId == null ? DEFAULT_DeityLandProtection_ITEM_ID : this.DeityLandProtectionItemId) + "\",\"" + CONFIG_OUTLANDER_DEITY_ITEM_ID_KEY + "\":\"" + (this.outlanderDeityItemId == null ? OUTLANDER_DEITY_ITEM_ID : this.outlanderDeityItemId) + "\",\"claimRadius\":" + this.getDefaultRadius() + ",\"maxClaimsPerPlayer\":" + this.getMaxClaimsPerPlayer() + ",\"allowCrafting\":" + this.isAllowCrafting() + ",\"mapClaimVisualEnabled\":" + this.isMapClaimVisualEnabled() + ",\"upkeepEnabled\":" + this.isUpkeepEnabled() + ",\"upkeepGraceMinutes\":" + this.getUpkeepGraceMinutes() + ",\"upkeepEssenceCostPerHour\":" + this.getUpkeepEssenceCostPerHour() + ",\"" + CONFIG_SLUMBERING_RECIPE_COBBLE_COST_KEY + "\":" + Math.max(0, this.slumberingRecipeCobbleCost) + ",\"" + CONFIG_SLUMBERING_RECIPE_ESSENCE_COST_KEY + "\":" + Math.max(0, this.slumberingRecipeEssenceCost) + ",\"" + CONFIG_OUTLANDER_RECIPE_COBBLE_COST_KEY + "\":" + Math.max(0, this.outlanderRecipeCobbleCost) + ",\"" + CONFIG_OUTLANDER_RECIPE_ESSENCE_COST_KEY + "\":" + Math.max(0, this.outlanderRecipeEssenceCost) + ",\"" + CONFIG_UPGRADE_TIER_2_ITEM_ID_KEY + "\":\"" + this.getUpgradeTier2ItemId() + "\",\"" + CONFIG_UPGRADE_TIER_2_ITEM_QUANTITY_KEY + "\":" + this.getUpgradeTier2ItemQuantity() + ",\"" + CONFIG_UPGRADE_TIER_3_ITEM_ID_KEY + "\":\"" + this.getUpgradeTier3ItemId() + "\",\"" + CONFIG_UPGRADE_TIER_3_ITEM_QUANTITY_KEY + "\":" + this.getUpgradeTier3ItemQuantity() + ",\"" + CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_ID_KEY + "\":\"" + this.getUpgradeTier4PrimaryItemId() + "\",\"" + CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY_KEY + "\":" + this.getUpgradeTier4PrimaryItemQuantity() + ",\"" + CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_ID_KEY + "\":\"" + this.getUpgradeTier4SecondaryItemId() + "\",\"" + CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY_KEY + "\":" + this.getUpgradeTier4SecondaryItemQuantity() + "}";
            Files.writeString(cfg, content, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to write config.json");
        }
    }

    private boolean loadUpkeepEnabled(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return true;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Boolean v = JsonReader.readBoolean(json, "upkeepEnabled");
            return v == null ? true : v;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
            return true;
        }
    }

    private int loadUpkeepGraceMinutes(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return DEFAULT_UPKEEP_GRACE_MINUTES;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Integer v = JsonReader.readInt(json, "upkeepGraceMinutes");
            return v == null ? DEFAULT_UPKEEP_GRACE_MINUTES : Math.max(0, v);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
            return DEFAULT_UPKEEP_GRACE_MINUTES;
        }
    }

    private int loadUpkeepEssenceCostPerHour(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Integer v = JsonReader.readInt(json, "upkeepEssenceCostPerHour");
            return v == null ? DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR : Math.max(1, v);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
            return DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR;
        }
    }

    private void loadRecipeCostConfig(Path dataDir) {
        this.slumberingRecipeCobbleCost = DEFAULT_SLUMBERING_RECIPE_COBBLE_COST;
        this.slumberingRecipeEssenceCost = DEFAULT_SLUMBERING_RECIPE_ESSENCE_COST;
        this.outlanderRecipeCobbleCost = DEFAULT_OUTLANDER_RECIPE_COBBLE_COST;
        this.outlanderRecipeEssenceCost = DEFAULT_OUTLANDER_RECIPE_ESSENCE_COST;
        this.upgradeTier2ItemId = DEFAULT_UPGRADE_TIER_2_ITEM_ID;
        this.upgradeTier2ItemQuantity = DEFAULT_UPGRADE_TIER_2_ITEM_QUANTITY;
        this.upgradeTier3ItemId = DEFAULT_UPGRADE_TIER_3_ITEM_ID;
        this.upgradeTier3ItemQuantity = DEFAULT_UPGRADE_TIER_3_ITEM_QUANTITY;
        this.upgradeTier4PrimaryItemId = DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID;
        this.upgradeTier4PrimaryItemQuantity = DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY;
        this.upgradeTier4SecondaryItemId = DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID;
        this.upgradeTier4SecondaryItemQuantity = DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY;
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            this.slumberingRecipeCobbleCost = DeityLandProtectionPlugin.readConfiguredRecipeCost(json, CONFIG_SLUMBERING_RECIPE_COBBLE_COST_KEY, DEFAULT_SLUMBERING_RECIPE_COBBLE_COST);
            this.slumberingRecipeEssenceCost = DeityLandProtectionPlugin.readConfiguredRecipeCost(json, CONFIG_SLUMBERING_RECIPE_ESSENCE_COST_KEY, DEFAULT_SLUMBERING_RECIPE_ESSENCE_COST);
            this.outlanderRecipeCobbleCost = DeityLandProtectionPlugin.readConfiguredRecipeCost(json, CONFIG_OUTLANDER_RECIPE_COBBLE_COST_KEY, DEFAULT_OUTLANDER_RECIPE_COBBLE_COST);
            this.outlanderRecipeEssenceCost = DeityLandProtectionPlugin.readConfiguredRecipeCost(json, CONFIG_OUTLANDER_RECIPE_ESSENCE_COST_KEY, DEFAULT_OUTLANDER_RECIPE_ESSENCE_COST);
            this.upgradeTier2ItemId = DeityLandProtectionPlugin.readConfiguredItemId(json, CONFIG_UPGRADE_TIER_2_ITEM_ID_KEY, DEFAULT_UPGRADE_TIER_2_ITEM_ID);
            this.upgradeTier2ItemQuantity = DeityLandProtectionPlugin.readConfiguredRecipeCost(json, CONFIG_UPGRADE_TIER_2_ITEM_QUANTITY_KEY, DEFAULT_UPGRADE_TIER_2_ITEM_QUANTITY);
            this.upgradeTier3ItemId = DeityLandProtectionPlugin.readConfiguredItemId(json, CONFIG_UPGRADE_TIER_3_ITEM_ID_KEY, DEFAULT_UPGRADE_TIER_3_ITEM_ID);
            this.upgradeTier3ItemQuantity = DeityLandProtectionPlugin.readConfiguredRecipeCost(json, CONFIG_UPGRADE_TIER_3_ITEM_QUANTITY_KEY, DEFAULT_UPGRADE_TIER_3_ITEM_QUANTITY);
            this.upgradeTier4PrimaryItemId = DeityLandProtectionPlugin.readConfiguredItemId(json, CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_ID_KEY, DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID);
            this.upgradeTier4PrimaryItemQuantity = DeityLandProtectionPlugin.readConfiguredRecipeCost(json, CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY_KEY, DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY);
            this.upgradeTier4SecondaryItemId = DeityLandProtectionPlugin.readConfiguredItemId(json, CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_ID_KEY, DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID);
            this.upgradeTier4SecondaryItemQuantity = DeityLandProtectionPlugin.readConfiguredRecipeCost(json, CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY_KEY, DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
        }
    }

    private static int readConfiguredRecipeCost(String json, String key, int defaultValue) {
        Integer value = JsonReader.readInt(json, key);
        if (value == null) {
            return defaultValue;
        }
        return Math.max(0, value);
    }

    private static String readConfiguredItemId(String json, String key, String defaultValue) {
        String value = JsonReader.readString(json, key);
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }

    private static int clampMaxClaimsPerPlayer(int value) {
        int v = value;
        if (v < 1) {
            v = 1;
        }
        if (v > 5) {
            v = 5;
        }
        return v;
    }

    private static int normalizeRadius(int radius) {
        int[] nArray = ALLOWED_RADII;
        int n = ALLOWED_RADII.length;
        int n2 = 0;
        while (n2 < n) {
            int r = nArray[n2];
            if (r == radius) {
                return r;
            }
            ++n2;
        }
        return -1;
    }

    protected void setup() {
        instance = this;
        Path dataDir = this.getDataDirectory();
        this.absDataDir = dataDir.toAbsolutePath().normalize();
        this.ensureAssetPackManifest(this.absDataDir);
        this.removeDuplicateCustomUiFromDataPack(this.absDataDir);
        this.loadRecipeCostConfig(this.absDataDir);
        this.ensureCustomDeityLandProtectionItem(this.absDataDir);
        DeityLandProtectionLocalizationCatalog.writeGeneratedLanguageFiles(this.absDataDir);
        this.langPreferenceManager = new DeityLandProtectionLangPreferenceManager(this.getDataDirectory());
        this.localizer = new DeityLandProtectionLocalizer();
        this.DeityLandProtectionItemId = this.loadDeityLandProtectionItemId(this.absDataDir);
        this.outlanderDeityItemId = this.loadOutlanderDeityItemId(this.absDataDir);
        this.claimRadius = this.loadClaimRadius(this.absDataDir);
        this.maxClaimsPerPlayer = this.loadMaxClaimsPerPlayer(this.absDataDir);
        this.allowCrafting = this.loadAllowCrafting(this.absDataDir);
        this.mapClaimVisualEnabled = this.loadMapClaimVisualEnabled(this.absDataDir);
        this.upkeepEnabled = this.loadUpkeepEnabled(this.absDataDir);
        this.upkeepGraceMinutes = this.loadUpkeepGraceMinutes(this.absDataDir);
        this.upkeepEssenceCostPerHour = this.loadUpkeepEssenceCostPerHour(this.absDataDir);
        this.claimStore = new ClaimStore(this.absDataDir.resolve("claims.json"), this.getLogger());
        this.claimStore.load();
        this.upkeepStore = new DeityLandProtectionUpkeepStore(this.absDataDir.resolve("upkeep.json"), this.getLogger());
        this.upkeepStore.load();
        this.getEntityStoreRegistry().registerSystem((ISystem)new DeityLandProtectionPlaceSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new DeityLandProtectionBreakSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new DeityLandProtectionUseBlockSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new DeityLandProtectionCraftSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new DeityLandProtectionBorderTickSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new DeityLandProtectionEnterExitTickSystem(this));
        this.getChunkStoreRegistry().registerSystem((ISystem)new DeityLandProtectionUpkeepTickingSystem(this));
        this.getChunkStoreRegistry().registerSystem((ISystem)new DeityLandProtectionBorderSurfaceRefreshSystem(this));
        this.getChunkStoreRegistry().registerSystem((ISystem)new DeityLandProtectionWorldMapUpdateTickingSystem(this));
        IWorldMapProvider.CODEC.register("DeityLandProtection", DeityLandProtectionWorldMapProvider.class, DeityLandProtectionWorldMapProvider.CODEC);
        this.getEventRegistry().registerGlobal(AddWorldEvent.class, event -> {
            if (event.getWorld().getWorldConfig().isDeleteOnRemove()) {
                event.getWorld().getWorldConfig().setWorldMapProvider((IWorldMapProvider)new WorldGenWorldMapProvider());
                return;
            }
            event.getWorld().getWorldConfig().setWorldMapProvider((IWorldMapProvider)new DeityLandProtectionWorldMapProvider());
        });
        this.flushExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DeityLandProtection-DataFlush");
            t.setDaemon(true);
            return t;
        });
        this.flushExecutor.scheduleAtFixedRate(() -> {
            if (this.claimStore != null) {
                try {
                    this.claimStore.flushIfDirty();
                }
                catch (Exception e) {
                    ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection claims flush failed");
                }
            }
            if (this.upkeepStore != null) {
                try {
                    this.upkeepStore.flushIfDirty();
                }
                catch (Exception e) {
                    ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection upkeep flush failed");
                }
            }
        }, 10L, 10L, TimeUnit.SECONDS);
        this.getLogger().at(Level.INFO).log("DeityLandProtection dataDir=" + String.valueOf(this.absDataDir) + ", DeityLandProtectionItemId=" + this.DeityLandProtectionItemId + ", OutlanderDeityItemId=" + this.outlanderDeityItemId + ", claimRadius=" + this.claimRadius + ", maxClaimsPerPlayer=" + this.maxClaimsPerPlayer + ", allowCrafting=" + this.allowCrafting + ", claimsLoaded=" + this.claimStore.getClaims().size());
        this.getLogger().at(Level.INFO).log("DeityLandProtectionPlugin setup");
    }

    protected void start() {
        try {
            CommandManager.get().register((AbstractCommand)new DeityLandProtectionCommand(this));
            CommandManager.get().register((AbstractCommand)new DeityLandProtectionLangCommand(this, this.langPreferenceManager));
        }
        catch (Exception e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to register commands");
        }
        this.getLogger().at(Level.INFO).log("DeityLandProtectionPlugin start");
    }

    protected void shutdown() {
        if (this.flushExecutor != null) {
            try {
                this.flushExecutor.shutdown();
                this.flushExecutor.awaitTermination(2L, TimeUnit.SECONDS);
            }
            catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            catch (Exception ignored) {
                // best-effort: shutdown is on a path where logging is unavailable.
            }
            this.flushExecutor = null;
        }
        if (this.claimStore != null) {
            this.claimStore.flushIfDirty();
        }
        if (this.upkeepStore != null) {
            this.upkeepStore.flushIfDirty();
        }
        this.mapUpdateQueueByWorld.clear();
        if (instance == this) {
            instance = null;
        }
        this.getLogger().at(Level.INFO).log("DeityLandProtectionPlugin shutdown");
    }

    public void queueMapUpdateForClaim(String worldName, Claim claim) {
        if (worldName == null || worldName.isEmpty() || claim == null) {
            return;
        }
        int minX;
        int maxX;
        int minZ;
        int maxZ;
        int[] bounds = this.claimStore == null ? null : this.claimStore.getClaimCellBounds(claim.getCenterX(), claim.getCenterZ());
        if (bounds != null && bounds.length >= 4) {
            minX = bounds[0];
            maxX = bounds[1];
            minZ = bounds[2];
            maxZ = bounds[3];
        } else {
            int centerX = claim.getCenterX();
            int centerZ = claim.getCenterZ();
            int r = claim.getRadius();
            minX = centerX - r;
            maxX = centerX + r;
            minZ = centerZ - r;
            maxZ = centerZ + r;
        }
        int minChunkX = ChunkUtil.chunkCoordinate((int)minX);
        int maxChunkX = ChunkUtil.chunkCoordinate((int)maxX);
        int minChunkZ = ChunkUtil.chunkCoordinate((int)minZ);
        int maxChunkZ = ChunkUtil.chunkCoordinate((int)maxZ);
        LongOpenHashSet toAdd = new LongOpenHashSet();
        int cx = minChunkX;
        while (cx <= maxChunkX) {
            int cz = minChunkZ;
            while (cz <= maxChunkZ) {
                toAdd.add(ChunkUtil.indexChunk((int)cx, (int)cz));
                ++cz;
            }
            ++cx;
        }
        this.mapUpdateQueueByWorld.compute(worldName, (k, existing) -> {
            LongSet set = existing;
            if (set == null) {
                set = new LongOpenHashSet();
            }
            LongIterator it = toAdd.iterator();
            while (it.hasNext()) {
                set.add(it.nextLong());
            }
            return set;
        });
    }

    public void queueMapUpdateForAllClaims(String worldName) {
        if (worldName == null || worldName.isEmpty() || this.claimStore == null) {
            return;
        }
        List<Claim> claims = this.claimStore.getClaims();
        for (Claim claim : claims) {
            this.queueMapUpdateForClaim(worldName, claim);
        }
    }

    public LongSet pollMapUpdateChunks(String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return null;
        }
        return this.mapUpdateQueueByWorld.remove(worldName);
    }

    public ClaimStore getClaimStore() {
        return this.claimStore;
    }

    public int getDefaultRadius() {
        return this.claimRadius > 0 ? this.claimRadius : 16;
    }

    public int getClaimRadius() {
        return this.getDefaultRadius();
    }

    public boolean setClaimRadius(int radius) {
        int normalized = DeityLandProtectionPlugin.normalizeRadius(radius);
        if (normalized <= 0) {
            return false;
        }
        this.claimRadius = normalized;
        this.persistConfig();
        return true;
    }

    public int getMaxClaimsPerPlayer() {
        return DeityLandProtectionPlugin.clampMaxClaimsPerPlayer(this.maxClaimsPerPlayer <= 0 ? 1 : this.maxClaimsPerPlayer);
    }

    public boolean setMaxClaimsPerPlayer(int maxClaimsPerPlayer) {
        this.maxClaimsPerPlayer = DeityLandProtectionPlugin.clampMaxClaimsPerPlayer(maxClaimsPerPlayer);
        this.persistConfig();
        return true;
    }

    public boolean isAllowCrafting() {
        return this.allowCrafting;
    }

    public boolean setAllowCrafting(boolean allowCrafting) {
        this.allowCrafting = allowCrafting;
        this.persistConfig();
        return true;
    }

    public boolean isMapClaimVisualEnabled() {
        return this.mapClaimVisualEnabled;
    }

    public boolean setMapClaimVisualEnabled(boolean enabled) {
        this.mapClaimVisualEnabled = enabled;
        this.persistConfig();
        return true;
    }

    public String getDeityLandProtectionItemId() {
        return this.DeityLandProtectionItemId;
    }

    public boolean isClaimItemId(String itemId) {
        return DeityLandProtectionPlugin.itemIdMatches(itemId, this.DeityLandProtectionItemId) || DeityLandProtectionPlugin.itemIdMatches(itemId, DEFAULT_DeityLandProtection_ITEM_ID) || this.isOutlanderClaimItemId(itemId);
    }

    public boolean isOutlanderClaimItemId(String itemId) {
        return DeityLandProtectionPlugin.itemIdMatches(itemId, this.outlanderDeityItemId) || DeityLandProtectionPlugin.itemIdMatches(itemId, OUTLANDER_DEITY_ITEM_ID) || DeityLandProtectionPlugin.itemIdMatches(itemId, OUTLANDER_DEITY_BLOCK_ITEM_ID);
    }

    public String getUpkeepEssenceItemIdForClaim(Claim claim) {
        if (claim != null && this.isOutlanderClaimItemId(claim.getDeityItemId())) {
            return ESSENCE_OF_VOID_ITEM_ID;
        }
        return ESSENCE_OF_LIFE_ITEM_ID;
    }

    public int getUpkeepTierForClaim(Claim claim) {
        return DeityLandProtectionTierSystem.getUpkeepTierForClaim(this.upkeepStore, claim);
    }

    public int getClaimRadiusForTier(int tier) {
        return DeityLandProtectionTierSystem.getClaimRadiusForTier(this.getClaimRadius(), tier);
    }

    public int getUpkeepEssenceCostPerHourForTier(int tier) {
        return DeityLandProtectionTierSystem.getUpkeepEssenceCostPerHourForTier(this.getUpkeepEssenceCostPerHour(), tier);
    }

    public int getUpkeepEssenceCostPerHourForClaim(Claim claim) {
        return this.getUpkeepEssenceCostPerHourForTier(this.getUpkeepTierForClaim(claim));
    }

    public int getTierMultiplier(int tier) {
        return DeityLandProtectionTierSystem.getTierMultiplier(tier);
    }

    public boolean isUpgradeMaterialItemId(String itemId) {
        return this.isTier2UpgradeItemId(itemId) || this.isTier3UpgradeItemId(itemId) || this.isTier4PrimaryUpgradeItemId(itemId) || this.isTier4SecondaryUpgradeItemId(itemId);
    }

    public boolean isAdamantiteIngotItemId(String itemId) {
        return this.isTier2UpgradeItemId(itemId);
    }

    public boolean isCrystalwoodLogItemId(String itemId) {
        return this.isTier3UpgradeItemId(itemId);
    }

    public boolean isRubyItemId(String itemId) {
        return this.isTier4PrimaryUpgradeItemId(itemId);
    }

    public boolean isSapphireItemId(String itemId) {
        return this.isTier4SecondaryUpgradeItemId(itemId);
    }

    public String getUpgradeTier2ItemId() {
        String value = this.upgradeTier2ItemId;
        if (value == null) {
            return DEFAULT_UPGRADE_TIER_2_ITEM_ID;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? DEFAULT_UPGRADE_TIER_2_ITEM_ID : trimmed;
    }

    public int getUpgradeTier2ItemQuantity() {
        return Math.max(0, this.upgradeTier2ItemQuantity);
    }

    public String getUpgradeTier3ItemId() {
        String value = this.upgradeTier3ItemId;
        if (value == null) {
            return DEFAULT_UPGRADE_TIER_3_ITEM_ID;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? DEFAULT_UPGRADE_TIER_3_ITEM_ID : trimmed;
    }

    public int getUpgradeTier3ItemQuantity() {
        return Math.max(0, this.upgradeTier3ItemQuantity);
    }

    public String getUpgradeTier4PrimaryItemId() {
        String value = this.upgradeTier4PrimaryItemId;
        if (value == null) {
            return DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID : trimmed;
    }

    public int getUpgradeTier4PrimaryItemQuantity() {
        return Math.max(0, this.upgradeTier4PrimaryItemQuantity);
    }

    public String getUpgradeTier4SecondaryItemId() {
        String value = this.upgradeTier4SecondaryItemId;
        if (value == null) {
            return DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID : trimmed;
    }

    public int getUpgradeTier4SecondaryItemQuantity() {
        return Math.max(0, this.upgradeTier4SecondaryItemQuantity);
    }

    public boolean isTier2UpgradeItemId(String itemId) {
        return DeityLandProtectionPlugin.itemIdEqualsConfigured(itemId, this.getUpgradeTier2ItemId());
    }

    public boolean isTier3UpgradeItemId(String itemId) {
        return DeityLandProtectionPlugin.itemIdEqualsConfigured(itemId, this.getUpgradeTier3ItemId());
    }

    public boolean isTier4PrimaryUpgradeItemId(String itemId) {
        return DeityLandProtectionPlugin.itemIdEqualsConfigured(itemId, this.getUpgradeTier4PrimaryItemId());
    }

    public boolean isTier4SecondaryUpgradeItemId(String itemId) {
        return DeityLandProtectionPlugin.itemIdEqualsConfigured(itemId, this.getUpgradeTier4SecondaryItemId());
    }

    public boolean areTier4UpgradeItemsSameItem() {
        return DeityLandProtectionPlugin.itemIdEqualsConfigured(this.getUpgradeTier4PrimaryItemId(), this.getUpgradeTier4SecondaryItemId());
    }

    public String getUpkeepEssenceTitleForClaim(Claim claim, DeityLandProtectionLangPreferenceManager.Language lang) {
        if (claim != null && this.isOutlanderClaimItemId(claim.getDeityItemId())) {
            return DeityLandProtectionText.uiSlot0VoidTitle(lang);
        }
        return DeityLandProtectionText.uiSlot0Title(lang);
    }

    public Path getAbsDataDir() {
        return this.absDataDir;
    }

    public DeityLandProtectionLangPreferenceManager getLangPreferenceManager() {
        return this.langPreferenceManager;
    }

    public DeityLandProtectionLangPreferenceManager.Language getEffectiveLanguage(UUID playerUuid) {
        if (this.langPreferenceManager == null) {
            return DeityLandProtectionLangPreferenceManager.Language.EN;
        }
        return this.langPreferenceManager.getEffectiveLanguage(playerUuid);
    }

    public DeityLandProtectionLangPreferenceManager.Language getEffectiveLanguage(PlayerRef playerRef) {
        if (playerRef == null) {
            return DeityLandProtectionLangPreferenceManager.Language.EN;
        }
        return this.getEffectiveLanguage(playerRef.getUuid());
    }

    public String tr(DeityLandProtectionLangPreferenceManager.Language language, String key, Object ... args) {
        DeityLandProtectionLocalizer l = this.localizer;
        if (l == null) {
            l = new DeityLandProtectionLocalizer();
            this.localizer = l;
        }
        return l.tr(language, key, args);
    }

    public void reloadData() {
        if (this.absDataDir == null) {
            return;
        }
        this.loadRecipeCostConfig(this.absDataDir);
        this.ensureCustomDeityLandProtectionItem(this.absDataDir);
        this.DeityLandProtectionItemId = this.loadDeityLandProtectionItemId(this.absDataDir);
        this.outlanderDeityItemId = this.loadOutlanderDeityItemId(this.absDataDir);
        this.claimRadius = this.loadClaimRadius(this.absDataDir);
        this.maxClaimsPerPlayer = this.loadMaxClaimsPerPlayer(this.absDataDir);
        this.allowCrafting = this.loadAllowCrafting(this.absDataDir);
        this.mapClaimVisualEnabled = this.loadMapClaimVisualEnabled(this.absDataDir);
        this.upkeepEnabled = this.loadUpkeepEnabled(this.absDataDir);
        this.upkeepGraceMinutes = this.loadUpkeepGraceMinutes(this.absDataDir);
        this.upkeepEssenceCostPerHour = this.loadUpkeepEssenceCostPerHour(this.absDataDir);
        if (this.claimStore != null) {
            this.claimStore.load();
        }
        if (this.upkeepStore != null) {
            this.upkeepStore.load();
        }
    }

    public DeityLandProtectionUpkeepStore getUpkeepStore() {
        return this.upkeepStore;
    }

    public boolean isUpkeepEnabled() {
        return this.upkeepEnabled;
    }

    public void setUpkeepEnabled(boolean enabled) {
        this.upkeepEnabled = enabled;
        this.persistConfig();
    }

    public int getUpkeepGraceMinutes() {
        return Math.max(0, this.upkeepGraceMinutes);
    }

    public void setUpkeepGraceMinutes(int minutes) {
        this.upkeepGraceMinutes = Math.max(0, minutes);
        this.persistConfig();
    }

    public long getUpkeepGraceMs() {
        return (long)this.getUpkeepGraceMinutes() * 60000L;
    }

    public int getUpkeepEssenceCostPerHour() {
        return Math.max(1, this.upkeepEssenceCostPerHour);
    }

    public void setUpkeepEssenceCostPerHour(int cost) {
        this.upkeepEssenceCostPerHour = Math.max(1, cost);
        this.persistConfig();
    }

    public void cycleUpkeepEssenceCostPerHour() {
        int current = this.getUpkeepEssenceCostPerHour();
        int next;
        if (current < 1) {
            next = 1;
        } else if (current < 3) {
            next = 3;
        } else if (current < 5) {
            next = 5;
        } else if (current < 10) {
            next = 10;
        } else {
            next = 1;
        }
        this.setUpkeepEssenceCostPerHour(next);
    }

    private String loadDeityLandProtectionItemId(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            try {
                Path cfgDir = cfg.getParent();
                if (cfgDir != null) {
                    Files.createDirectories(cfgDir, new FileAttribute[0]);
                }
                String content = "{\"" + CONFIG_DEITY_ITEM_ID_KEY + "\":\"" + DEFAULT_DeityLandProtection_ITEM_ID + "\",\"" + CONFIG_OUTLANDER_DEITY_ITEM_ID_KEY + "\":\"" + OUTLANDER_DEITY_ITEM_ID + "\",\"claimRadius\":16,\"maxClaimsPerPlayer\":1,\"allowCrafting\":true,\"mapClaimVisualEnabled\":true,\"upkeepEnabled\":true,\"upkeepGraceMinutes\":30,\"upkeepEssenceCostPerHour\":1,\"" + CONFIG_SLUMBERING_RECIPE_COBBLE_COST_KEY + "\":" + DEFAULT_SLUMBERING_RECIPE_COBBLE_COST + ",\"" + CONFIG_SLUMBERING_RECIPE_ESSENCE_COST_KEY + "\":" + DEFAULT_SLUMBERING_RECIPE_ESSENCE_COST + ",\"" + CONFIG_OUTLANDER_RECIPE_COBBLE_COST_KEY + "\":" + DEFAULT_OUTLANDER_RECIPE_COBBLE_COST + ",\"" + CONFIG_OUTLANDER_RECIPE_ESSENCE_COST_KEY + "\":" + DEFAULT_OUTLANDER_RECIPE_ESSENCE_COST + ",\"" + CONFIG_UPGRADE_TIER_2_ITEM_ID_KEY + "\":\"" + DEFAULT_UPGRADE_TIER_2_ITEM_ID + "\",\"" + CONFIG_UPGRADE_TIER_2_ITEM_QUANTITY_KEY + "\":" + DEFAULT_UPGRADE_TIER_2_ITEM_QUANTITY + ",\"" + CONFIG_UPGRADE_TIER_3_ITEM_ID_KEY + "\":\"" + DEFAULT_UPGRADE_TIER_3_ITEM_ID + "\",\"" + CONFIG_UPGRADE_TIER_3_ITEM_QUANTITY_KEY + "\":" + DEFAULT_UPGRADE_TIER_3_ITEM_QUANTITY + ",\"" + CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_ID_KEY + "\":\"" + DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID + "\",\"" + CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY_KEY + "\":" + DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY + ",\"" + CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_ID_KEY + "\":\"" + DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID + "\",\"" + CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY_KEY + "\":" + DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY + "}";
                Files.writeString(cfg, content, StandardCharsets.UTF_8, new OpenOption[0]);
            }
            catch (IOException e) {
                ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to write config.json");
            }
            return DEFAULT_DeityLandProtection_ITEM_ID;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            String value = JsonReader.readString(json, CONFIG_DEITY_ITEM_ID_KEY);
            if (value == null) {
                return DEFAULT_DeityLandProtection_ITEM_ID;
            }
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return DEFAULT_DeityLandProtection_ITEM_ID;
            }
            if (DeityLandProtectionPlugin.itemIdMatches(trimmed, "DeityLandProtection_Block")) {
                return DEFAULT_DeityLandProtection_ITEM_ID;
            }
            return trimmed;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
            return DEFAULT_DeityLandProtection_ITEM_ID;
        }
    }

    private String loadOutlanderDeityItemId(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return OUTLANDER_DEITY_ITEM_ID;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            String value = JsonReader.readString(json, CONFIG_OUTLANDER_DEITY_ITEM_ID_KEY);
            if (value == null) {
                return OUTLANDER_DEITY_ITEM_ID;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? OUTLANDER_DEITY_ITEM_ID : trimmed;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
            return OUTLANDER_DEITY_ITEM_ID;
        }
    }

    public boolean isOpBypass(UUID playerUuid) {
        PermissionsModule perms = PermissionsModule.get();
        if (perms == null) {
            return false;
        }
        try {
            for (Object raw : perms.getGroupsForUser(playerUuid)) {
                if (!(raw instanceof String)) continue;
                String group = ((String) raw).trim().toLowerCase(Locale.ROOT);
                if (group.equals("op") || group.equals("admin") || group.equals("operator")) {
                    return true;
                }
            }
        }
        catch (Exception ignored) {
            // Permissions API unavailable; fall through and deny bypass.
        }
        return false;
    }

    public void markRecentClaimPlacement(int x, int z) {
        this.recentClaimPlacements.put(DeityLandProtectionPlugin.centerKey(x, z), System.currentTimeMillis());
    }

    public void enableBorder(UUID playerUuid, int centerX, int centerZ) {
        if (playerUuid == null) {
            return;
        }
        this.borderCenterByPlayer.put(playerUuid, DeityLandProtectionPlugin.centerKey(centerX, centerZ));
    }

    public void disableBorder(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        this.borderCenterByPlayer.remove(playerUuid);
        this.lastBorderSpawnMsByPlayer.remove(playerUuid);
        this.borderWorldByPlayer.remove(playerUuid);
    }

    public void clearBorderForClaim(int centerX, int centerZ) {
        long key = DeityLandProtectionPlugin.centerKey(centerX, centerZ);
        for (Map.Entry<UUID, Long> e : this.borderCenterByPlayer.entrySet()) {
            if (e == null) continue;
            UUID u = e.getKey();
            Long v = e.getValue();
            if (u == null || v == null || v != key) continue;
            this.disableBorder(u);
        }
    }

    public boolean isBorderEnabled(UUID playerUuid, long centerKey) {
        if (playerUuid == null) {
            return false;
        }
        Long v = this.borderCenterByPlayer.get(playerUuid);
        return v != null && v == centerKey;
    }

    public Long getBorderCenterKey(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        return this.borderCenterByPlayer.get(playerUuid);
    }

    public boolean shouldSpawnBorderNow(UUID playerUuid, long nowMs, long cooldownMs) {
        if (playerUuid == null) {
            return false;
        }
        Long last = this.lastBorderSpawnMsByPlayer.get(playerUuid);
        if (last != null && nowMs - last < cooldownMs) {
            return false;
        }
        this.lastBorderSpawnMsByPlayer.put(playerUuid, nowMs);
        return true;
    }

    public void recordBorderPlayerWorld(UUID playerUuid, String worldName) {
        if (playerUuid == null || worldName == null || worldName.isEmpty()) {
            return;
        }
        if (this.borderCenterByPlayer.containsKey(playerUuid)) {
            this.borderWorldByPlayer.put(playerUuid, worldName);
        }
    }

    private static String borderSurfaceCompositeKey(String worldName, long centerKey) {
        return worldName + "\0" + centerKey;
    }

    public void updateBorderSurfaceScanBaseY(String worldName, long centerKey, int baseY) {
        if (worldName == null || worldName.isEmpty()) {
            return;
        }
        this.borderSurfaceScanBaseYByWorldCenter.put(DeityLandProtectionPlugin.borderSurfaceCompositeKey(worldName, centerKey), baseY);
    }

    public int getBorderSurfaceScanBaseY(String worldName, long centerKey, int fallbackSeed) {
        if (worldName == null || worldName.isEmpty()) {
            return fallbackSeed;
        }
        Integer v = this.borderSurfaceScanBaseYByWorldCenter.get(DeityLandProtectionPlugin.borderSurfaceCompositeKey(worldName, centerKey));
        return v != null ? v : fallbackSeed;
    }

    public DeityLandProtectionBorderSurfaceCache getBorderSurfaceCache() {
        return this.borderSurfaceCache;
    }

    public void collectActiveBorderCenterKeysForWorld(String worldName, LongOpenHashSet out) {
        if (worldName == null || worldName.isEmpty() || out == null) {
            return;
        }
        out.clear();
        for (Map.Entry<UUID, Long> e : this.borderCenterByPlayer.entrySet()) {
            if (e == null) {
                continue;
            }
            UUID u = e.getKey();
            Long ck = e.getValue();
            if (u == null || ck == null) {
                continue;
            }
            String w = this.borderWorldByPlayer.get(u);
            if (worldName.equals(w)) {
                out.add(ck.longValue());
            }
        }
    }

    public boolean hasAnyBorderSessionForWorld(String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return false;
        }
        for (String w : this.borderWorldByPlayer.values()) {
            if (w != null && worldName.equals(w)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldIgnoreCenterBreak(int x, int z) {
        long key = DeityLandProtectionPlugin.centerKey(x, z);
        Long ts = this.recentClaimPlacements.get(key);
        if (ts == null) {
            return false;
        }
        long age = System.currentTimeMillis() - ts;
        if (age >= 0L && age < RECENT_PLACEMENT_IGNORE_BREAK_MS) {
            return true;
        }
        this.recentClaimPlacements.remove(key, ts);
        return false;
    }

    public void sendPlayerMessage(PlayerRef player, String text) {
        if (player == null || text == null || text.isEmpty()) {
            return;
        }
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();
        Long last = this.lastPlayerMessageMs.get(uuid);
        if (last != null && now - last < PLAYER_MESSAGE_COOLDOWN_MS) {
            return;
        }
        this.lastPlayerMessageMs.put(uuid, now);
        try {
            player.sendMessage(Message.raw(text));
        }
        catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
    }

    public void sendPlayerMessageImmediate(PlayerRef player, String text) {
        if (player == null || text == null || text.isEmpty()) {
            return;
        }
        try {
            player.sendMessage(Message.raw(text));
        }
        catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
    }

    public Long getLastZoneKey(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        return this.lastZoneKeyByPlayer.get(playerUuid);
    }

    public void setLastZoneKey(UUID playerUuid, Long zoneKey) {
        if (playerUuid == null) {
            return;
        }
        if (zoneKey == null) {
            this.lastZoneKeyByPlayer.remove(playerUuid);
            return;
        }
        this.lastZoneKeyByPlayer.put(playerUuid, zoneKey);
    }

    public void rememberUsername(UUID playerUuid, String username) {
        if (playerUuid == null || username == null) {
            return;
        }
        String u = username.trim();
        if (u.isEmpty()) {
            return;
        }
        this.knownUsernameByUuid.put(playerUuid, u);
        this.knownUuidByUsername.put(u.toLowerCase(), playerUuid);
    }

    public String getKnownUsername(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        return this.knownUsernameByUuid.get(playerUuid);
    }

    public UUID getKnownUuidForUsername(String username) {
        if (username == null) {
            return null;
        }
        String u = username.trim().toLowerCase();
        if (u.isEmpty()) {
            return null;
        }
        return this.knownUuidByUsername.get(u);
    }

    public Map<UUID, String> getPlayersInClaim(int centerX, int centerZ) {
        long key = DeityLandProtectionPlugin.centerKey(centerX, centerZ);
        Map m = this.playersInClaim.get(key);
        if (m == null || m.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(m);
    }

    public void updatePlayerClaimMembership(UUID playerUuid, Long prevCenterKey, Long nowCenterKey, String username) {
        ConcurrentHashMap<UUID, String> prev;
        if (playerUuid == null) {
            return;
        }
        if (prevCenterKey != null && (prev = this.playersInClaim.get(prevCenterKey)) != null) {
            prev.remove(playerUuid);
            if (prev.isEmpty()) {
                this.playersInClaim.remove(prevCenterKey, prev);
            }
        }
        if (nowCenterKey != null) {
            String u = username == null ? null : username.trim();
            if (u == null || u.isEmpty()) {
                u = this.knownUsernameByUuid.get(playerUuid);
            }
            if (u == null || u.isEmpty()) {
                u = playerUuid.toString();
            }
            this.playersInClaim.computeIfAbsent(nowCenterKey, k -> new ConcurrentHashMap()).put(playerUuid, u);
        }
    }

    public static long centerKey(int x, int z) {
        return ChunkKeys.pack(x, z);
    }

    private static boolean itemIdMatches(String itemId, String configuredItemId) {
        if (itemId == null || configuredItemId == null) {
            return false;
        }
        String itemLower = itemId.trim().toLowerCase(Locale.ROOT);
        String configLower = configuredItemId.trim().toLowerCase(Locale.ROOT);
        if (itemLower.isEmpty() || configLower.isEmpty()) {
            return false;
        }
        if (itemLower.equals(configLower)) {
            return true;
        }
        if (itemLower.endsWith(":" + configLower)) {
            return true;
        }
        return itemLower.contains(configLower);
    }

    private static boolean itemIdEqualsConfigured(String itemId, String configuredItemId) {
        if (itemId == null || configuredItemId == null) {
            return false;
        }
        String itemLower = itemId.trim().toLowerCase(Locale.ROOT);
        String configLower = configuredItemId.trim().toLowerCase(Locale.ROOT);
        if (itemLower.isEmpty() || configLower.isEmpty()) {
            return false;
        }
        if (itemLower.equals(configLower)) {
            return true;
        }
        return itemLower.endsWith(":" + configLower);
    }

    private void ensureAssetPackManifest(Path dataDir) {
        try {
            Files.createDirectories(dataDir, new FileAttribute[0]);
            Path manifest = dataDir.resolve("manifest.json");
            String desired = "{\"Group\":\"games.Austale\",\"Name\":\"DeityLandProtectionData\",\"Version\":\"1.2.1\",\"ServerVersion\":\"2026.03.26-89796e57b\"}";
            if (Files.exists(manifest, new LinkOption[0])) {
                try {
                    String existing = Files.readString(manifest, StandardCharsets.UTF_8);
                    if (existing != null && existing.equals(desired)) {
                        return;
                    }
                }
                catch (IOException ignored) {
                    // Manifest is unreadable; fall through and overwrite it.
                }
            }
            Files.writeString(manifest, desired, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to write asset pack manifest");
        }
    }

    private void ensureModsAssetPack() {
    }

    private void removeDuplicateCustomUiFromDataPack(Path dataDir) {
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
                        }
                    });
                }
            }
            if (removed) {
                ((HytaleLogger.Api)this.getLogger().at(Level.INFO)).log("DeityLandProtection removed duplicate Custom UI from plugin data folder (UI stays in the main asset pack only).");
            }
        }
        catch (Exception e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to strip duplicate UI from data pack");
        }
    }

    private void writeResourceIfMissingOrDifferent(Path dest, String resourcePath) throws IOException {
        if (dest == null || resourcePath == null) {
            return;
        }
        Files.createDirectories(dest.getParent(), new FileAttribute[0]);
        ClassLoader classLoader = DeityLandProtectionPlugin.class.getClassLoader();
        if (classLoader == null) {
            return;
        }
        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return;
            }
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (Files.exists(dest, new LinkOption[0])) {
                try {
                    String existing = Files.readString(dest, StandardCharsets.UTF_8);
                    if (existing.equals(content)) {
                        return;
                    }
                } catch (Exception ignored) {
                    // Fall through and overwrite if the file cannot be read cleanly.
                }
            }
            Files.writeString(dest, content, StandardCharsets.UTF_8, new OpenOption[0]);
        }
    }

    private void writeCustomDeityItemResource(Path dest, String resourcePath) throws IOException {
        if (dest == null || resourcePath == null) {
            return;
        }
        Files.createDirectories(dest.getParent(), new FileAttribute[0]);
        ClassLoader classLoader = DeityLandProtectionPlugin.class.getClassLoader();
        if (classLoader == null) {
            return;
        }
        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return;
            }
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            String overridden = this.applyRecipeCostOverrides(resourcePath, content);
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

    private String applyRecipeCostOverrides(String resourcePath, String content) {
        if (content == null || content.isEmpty() || resourcePath == null) {
            return content;
        }
        String adjusted = content;
        boolean isSlumberingDeity = resourcePath.endsWith("SlumberingDeity_Block.json");
        boolean isOutlanderDeity = resourcePath.endsWith("OutlanderDeity_Block.json");
        if (!isSlumberingDeity && !isOutlanderDeity) {
            return adjusted;
        }
        if (isSlumberingDeity) {
            adjusted = DeityLandProtectionPlugin.replaceRecipeQuantity(adjusted, "Rock_Stone_Cobble", this.slumberingRecipeCobbleCost);
            adjusted = DeityLandProtectionPlugin.replaceRecipeQuantity(adjusted, "Ingredient_Life_Essence", this.slumberingRecipeEssenceCost);
        }
        if (isOutlanderDeity) {
            adjusted = DeityLandProtectionPlugin.replaceRecipeQuantity(adjusted, "Rock_Slate_Cobble", this.outlanderRecipeCobbleCost);
            adjusted = DeityLandProtectionPlugin.replaceRecipeQuantity(adjusted, "Ingredient_Void_Essence", this.outlanderRecipeEssenceCost);
        }
        adjusted = this.applyUpgradeRequirementOverrides(adjusted);
        return adjusted;
    }

    private static String replaceRecipeQuantity(String content, String itemId, int quantity) {
        if (content == null || content.isEmpty() || itemId == null || itemId.isEmpty()) {
            return content;
        }
        int safeQuantity = Math.max(0, quantity);
        Pattern pattern = Pattern.compile("(\\\"ItemId\\\"\\s*:\\s*\\\"" + Pattern.quote(itemId) + "\\\"\\s*,\\s*\\\"Quantity\\\"\\s*:\\s*)\\d+");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean replaced = false;
        while (matcher.find()) {
            replaced = true;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + safeQuantity));
        }
        if (!replaced) {
            return content;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String applyUpgradeRequirementOverrides(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String adjusted = content;
        String tier2Materials = DeityLandProtectionPlugin.buildUpgradeMaterialsJson(this.getUpgradeTier2ItemId(), this.getUpgradeTier2ItemQuantity(), null, 0);
        String tier3Materials = DeityLandProtectionPlugin.buildUpgradeMaterialsJson(this.getUpgradeTier3ItemId(), this.getUpgradeTier3ItemQuantity(), null, 0);
        String tier4Materials = DeityLandProtectionPlugin.buildUpgradeMaterialsJson(this.getUpgradeTier4PrimaryItemId(), this.getUpgradeTier4PrimaryItemQuantity(), this.getUpgradeTier4SecondaryItemId(), this.getUpgradeTier4SecondaryItemQuantity());
        adjusted = DeityLandProtectionPlugin.replaceUpgradeRequirementMaterials(adjusted, 1, tier2Materials);
        adjusted = DeityLandProtectionPlugin.replaceUpgradeRequirementMaterials(adjusted, 2, tier3Materials);
        adjusted = DeityLandProtectionPlugin.replaceUpgradeRequirementMaterials(adjusted, 3, tier4Materials);
        return adjusted;
    }

    private static String buildUpgradeMaterialsJson(String firstItemId, int firstQuantity, String secondItemId, int secondQuantity) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n              {\n                \"ItemId\": \"").append(DeityLandProtectionPlugin.escapeJson(firstItemId)).append("\",\n                \"Quantity\": ").append(Math.max(0, firstQuantity)).append("\n              }");
        if (secondItemId != null && !secondItemId.trim().isEmpty()) {
            sb.append(",\n              {\n                \"ItemId\": \"").append(DeityLandProtectionPlugin.escapeJson(secondItemId)).append("\",\n                \"Quantity\": ").append(Math.max(0, secondQuantity)).append("\n              }");
        }
        sb.append("\n            ");
        return sb.toString();
    }

    private static String replaceUpgradeRequirementMaterials(String content, int requirementIndex, String replacementMaterialsJson) {
        if (content == null || content.isEmpty() || requirementIndex < 1 || replacementMaterialsJson == null) {
            return content;
        }
        Pattern pattern = Pattern.compile("(\\\"UpgradeRequirement\\\"\\s*:\\s*\\{\\s*\\\"Material\\\"\\s*:\\s*\\[)(.*?)(\\]\\s*,\\s*\\\"TimeSeconds\\\"\\s*:\\s*\\d+\\s*\\})", 32);
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        int index = 0;
        boolean replaced = false;
        while (matcher.find()) {
            ++index;
            if (index == requirementIndex) {
                replaced = true;
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + replacementMaterialsJson + matcher.group(3)));
                continue;
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
        }
        if (!replaced) {
            return content;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void ensureCustomDeityLandProtectionItem(Path dataDir) {
        try {
            this.writeCustomDeityItemResource(dataDir.resolve("Server/Item/Items/DeityLandProtection/SlumberingDeity_Block.json"), "Server/Item/Items/DeityLandProtection/SlumberingDeity_Block.json");
            this.writeCustomDeityItemResource(dataDir.resolve("Assets/Server/Item/Items/DeityLandProtection/SlumberingDeity_Block.json"), "Assets/Server/Item/Items/DeityLandProtection/SlumberingDeity_Block.json");
            this.writeCustomDeityItemResource(dataDir.resolve("Server/Item/Items/DeityLandProtection/OutlanderDeity_Block.json"), "Server/Item/Items/DeityLandProtection/OutlanderDeity_Block.json");
            this.writeCustomDeityItemResource(dataDir.resolve("Assets/Server/Item/Items/DeityLandProtection/OutlanderDeity_Block.json"), "Assets/Server/Item/Items/DeityLandProtection/OutlanderDeity_Block.json");
        }
        catch (Exception e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to write custom item asset");
        }
    }

}



