package group.austale.deitylandprotection;

import com.hypixel.hytale.logger.HytaleLogger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Holds all values that come from {@code config.json} and centralizes
 * loading, persisting, and the small set of normalization helpers (radius
 * snapping, max-claims clamping, item-id matching).
 *
 * <p>The on-disk JSON keys are kept stable for backwards compatibility.</p>
 */
final class PluginConfig {
    static final int DEFAULT_RADIUS = 16;
    static final String DEFAULT_DEITY_ITEM_ID = "SlumberingDeity_Block";
    static final String CONFIG_DEITY_ITEM_ID_KEY = "DeityLandProtectionItemId";
    static final String CONFIG_OUTLANDER_DEITY_ITEM_ID_KEY = "OutlanderDeityItemId";
    static final String OUTLANDER_DEITY_ITEM_ID = "OutlanderDeity_Block";
    static final String OUTLANDER_DEITY_BLOCK_ITEM_ID = "Furniture_Temple_Dark_Statue_Gaia";
    static final String ESSENCE_OF_LIFE_ITEM_ID = "Ingredient_Life_Essence";
    static final String ESSENCE_OF_VOID_ITEM_ID = "Ingredient_Void_Essence";
    static final int DEFAULT_MAX_CLAIMS_PER_PLAYER = 1;
    static final int MAX_CLAIMS_PER_PLAYER_CAP = 5;
    static final boolean DEFAULT_ALLOW_CRAFTING = true;
    static final boolean DEFAULT_MAP_CLAIM_VISUAL_ENABLED = true;
    static final int DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR = 1;
    static final int DEFAULT_UPKEEP_GRACE_MINUTES = 30;
    static final int DEFAULT_SLUMBERING_RECIPE_COBBLE_COST = 20;
    static final int DEFAULT_SLUMBERING_RECIPE_ESSENCE_COST = 10;
    static final int DEFAULT_OUTLANDER_RECIPE_COBBLE_COST = 20;
    static final int DEFAULT_OUTLANDER_RECIPE_ESSENCE_COST = 10;
    static final String DEFAULT_UPGRADE_TIER_2_ITEM_ID = "Ingredient_Bar_Adamantite";
    static final int DEFAULT_UPGRADE_TIER_2_ITEM_QUANTITY = 25;
    static final String DEFAULT_UPGRADE_TIER_3_ITEM_ID = "Wood_Crystal_Trunk";
    static final int DEFAULT_UPGRADE_TIER_3_ITEM_QUANTITY = 100;
    static final String DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID = "Rock_Gem_Ruby";
    static final int DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY = 1;
    static final String DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID = "Rock_Gem_Sapphire";
    static final int DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY = 1;
    static final String CONFIG_SLUMBERING_RECIPE_COBBLE_COST_KEY = "slumberingRecipeCobbleCost";
    static final String CONFIG_SLUMBERING_RECIPE_ESSENCE_COST_KEY = "slumberingRecipeEssenceCost";
    static final String CONFIG_OUTLANDER_RECIPE_COBBLE_COST_KEY = "outlanderRecipeCobbleCost";
    static final String CONFIG_OUTLANDER_RECIPE_ESSENCE_COST_KEY = "outlanderRecipeEssenceCost";
    static final String CONFIG_UPGRADE_TIER_2_ITEM_ID_KEY = "upgradeTier2ItemId";
    static final String CONFIG_UPGRADE_TIER_2_ITEM_QUANTITY_KEY = "upgradeTier2ItemQuantity";
    static final String CONFIG_UPGRADE_TIER_3_ITEM_ID_KEY = "upgradeTier3ItemId";
    static final String CONFIG_UPGRADE_TIER_3_ITEM_QUANTITY_KEY = "upgradeTier3ItemQuantity";
    static final String CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_ID_KEY = "upgradeTier4PrimaryItemId";
    static final String CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY_KEY = "upgradeTier4PrimaryItemQuantity";
    static final String CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_ID_KEY = "upgradeTier4SecondaryItemId";
    static final String CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY_KEY = "upgradeTier4SecondaryItemQuantity";
    static final int[] ALLOWED_RADII = new int[]{16, 32, 64, 128};

    private final HytaleLogger logger;

    String deityItemId = DEFAULT_DEITY_ITEM_ID;
    String outlanderDeityItemId = OUTLANDER_DEITY_ITEM_ID;
    int claimRadius = DEFAULT_RADIUS;
    int maxClaimsPerPlayer = DEFAULT_MAX_CLAIMS_PER_PLAYER;
    boolean allowCrafting = DEFAULT_ALLOW_CRAFTING;
    boolean mapClaimVisualEnabled = DEFAULT_MAP_CLAIM_VISUAL_ENABLED;
    boolean upkeepEnabled = true;
    int upkeepGraceMinutes = DEFAULT_UPKEEP_GRACE_MINUTES;
    int upkeepEssenceCostPerHour = DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR;
    int slumberingRecipeCobbleCost = DEFAULT_SLUMBERING_RECIPE_COBBLE_COST;
    int slumberingRecipeEssenceCost = DEFAULT_SLUMBERING_RECIPE_ESSENCE_COST;
    int outlanderRecipeCobbleCost = DEFAULT_OUTLANDER_RECIPE_COBBLE_COST;
    int outlanderRecipeEssenceCost = DEFAULT_OUTLANDER_RECIPE_ESSENCE_COST;
    String upgradeTier2ItemId = DEFAULT_UPGRADE_TIER_2_ITEM_ID;
    int upgradeTier2ItemQuantity = DEFAULT_UPGRADE_TIER_2_ITEM_QUANTITY;
    String upgradeTier3ItemId = DEFAULT_UPGRADE_TIER_3_ITEM_ID;
    int upgradeTier3ItemQuantity = DEFAULT_UPGRADE_TIER_3_ITEM_QUANTITY;
    String upgradeTier4PrimaryItemId = DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID;
    int upgradeTier4PrimaryItemQuantity = DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY;
    String upgradeTier4SecondaryItemId = DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID;
    int upgradeTier4SecondaryItemQuantity = DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY;

    PluginConfig(HytaleLogger logger) {
        this.logger = logger;
    }

    /** Resolves the directory holding {@code config.json} relative to the plugin data dir. */
    static Path resolveConfigDir(Path dataDir) {
        Path baseDir = dataDir != null ? dataDir : Path.of(".");
        Path serverRoot = resolveServerRoot(baseDir);
        Path configDir = serverRoot.resolve("config").resolve("DeityLandProtection");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (Exception ignored) {
            // best-effort: directory may already exist or be created on demand later.
        }
        return configDir;
    }

    static Path resolveConfigPath(Path dataDir) {
        return resolveConfigDir(dataDir).resolve("config.json");
    }

    private static Path resolveServerRoot(Path dataDir) {
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

    /**
     * Loads {@code config.json}; if the file does not yet exist, writes a fresh
     * file populated with defaults so admins have something to edit.
     */
    void load(Path dataDir) {
        Path cfg = resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            this.save(dataDir);
            return;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            this.deityItemId = readItemIdOrDefault(json, CONFIG_DEITY_ITEM_ID_KEY, DEFAULT_DEITY_ITEM_ID);
            this.outlanderDeityItemId = readItemIdOrDefault(json, CONFIG_OUTLANDER_DEITY_ITEM_ID_KEY, OUTLANDER_DEITY_ITEM_ID);
            Integer rawRadius = JsonReader.readInt(json, "claimRadius");
            int radius = normalizeRadius(rawRadius == null ? DEFAULT_RADIUS : rawRadius);
            this.claimRadius = radius > 0 ? radius : DEFAULT_RADIUS;
            Integer maxClaims = JsonReader.readInt(json, "maxClaimsPerPlayer");
            this.maxClaimsPerPlayer = clampMaxClaimsPerPlayer(maxClaims == null ? DEFAULT_MAX_CLAIMS_PER_PLAYER : maxClaims);
            Boolean allow = JsonReader.readBoolean(json, "allowCrafting");
            this.allowCrafting = allow == null ? DEFAULT_ALLOW_CRAFTING : allow;
            Boolean mapVisual = JsonReader.readBoolean(json, "mapClaimVisualEnabled");
            this.mapClaimVisualEnabled = mapVisual == null ? DEFAULT_MAP_CLAIM_VISUAL_ENABLED : mapVisual;
            Boolean upkeep = JsonReader.readBoolean(json, "upkeepEnabled");
            this.upkeepEnabled = upkeep == null ? true : upkeep;
            Integer grace = JsonReader.readInt(json, "upkeepGraceMinutes");
            this.upkeepGraceMinutes = grace == null ? DEFAULT_UPKEEP_GRACE_MINUTES : Math.max(0, grace);
            Integer cost = JsonReader.readInt(json, "upkeepEssenceCostPerHour");
            this.upkeepEssenceCostPerHour = cost == null ? DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR : Math.max(1, cost);
            this.slumberingRecipeCobbleCost = readNonNegativeInt(json, CONFIG_SLUMBERING_RECIPE_COBBLE_COST_KEY, DEFAULT_SLUMBERING_RECIPE_COBBLE_COST);
            this.slumberingRecipeEssenceCost = readNonNegativeInt(json, CONFIG_SLUMBERING_RECIPE_ESSENCE_COST_KEY, DEFAULT_SLUMBERING_RECIPE_ESSENCE_COST);
            this.outlanderRecipeCobbleCost = readNonNegativeInt(json, CONFIG_OUTLANDER_RECIPE_COBBLE_COST_KEY, DEFAULT_OUTLANDER_RECIPE_COBBLE_COST);
            this.outlanderRecipeEssenceCost = readNonNegativeInt(json, CONFIG_OUTLANDER_RECIPE_ESSENCE_COST_KEY, DEFAULT_OUTLANDER_RECIPE_ESSENCE_COST);
            this.upgradeTier2ItemId = readItemIdOrDefault(json, CONFIG_UPGRADE_TIER_2_ITEM_ID_KEY, DEFAULT_UPGRADE_TIER_2_ITEM_ID);
            this.upgradeTier2ItemQuantity = readNonNegativeInt(json, CONFIG_UPGRADE_TIER_2_ITEM_QUANTITY_KEY, DEFAULT_UPGRADE_TIER_2_ITEM_QUANTITY);
            this.upgradeTier3ItemId = readItemIdOrDefault(json, CONFIG_UPGRADE_TIER_3_ITEM_ID_KEY, DEFAULT_UPGRADE_TIER_3_ITEM_ID);
            this.upgradeTier3ItemQuantity = readNonNegativeInt(json, CONFIG_UPGRADE_TIER_3_ITEM_QUANTITY_KEY, DEFAULT_UPGRADE_TIER_3_ITEM_QUANTITY);
            this.upgradeTier4PrimaryItemId = readItemIdOrDefault(json, CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_ID_KEY, DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID);
            this.upgradeTier4PrimaryItemQuantity = readNonNegativeInt(json, CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY_KEY, DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY);
            this.upgradeTier4SecondaryItemId = readItemIdOrDefault(json, CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_ID_KEY, DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID);
            this.upgradeTier4SecondaryItemQuantity = readNonNegativeInt(json, CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY_KEY, DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY);
        }
        catch (IOException e) {
            ((HytaleLogger.Api) this.logger.at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read config.json");
        }
    }

    /** Persists the current values to {@code config.json}, creating directories as needed. */
    void save(Path dataDir) {
        if (dataDir == null) {
            return;
        }
        Path cfg = resolveConfigPath(dataDir);
        try {
            Path cfgDir = cfg.getParent();
            if (cfgDir != null) {
                Files.createDirectories(cfgDir, new FileAttribute[0]);
            }
            String content = "{\"" + CONFIG_DEITY_ITEM_ID_KEY + "\":\"" + this.deityItemId
                    + "\",\"" + CONFIG_OUTLANDER_DEITY_ITEM_ID_KEY + "\":\"" + this.outlanderDeityItemId
                    + "\",\"claimRadius\":" + this.claimRadius
                    + ",\"maxClaimsPerPlayer\":" + this.maxClaimsPerPlayer
                    + ",\"allowCrafting\":" + this.allowCrafting
                    + ",\"mapClaimVisualEnabled\":" + this.mapClaimVisualEnabled
                    + ",\"upkeepEnabled\":" + this.upkeepEnabled
                    + ",\"upkeepGraceMinutes\":" + this.upkeepGraceMinutes
                    + ",\"upkeepEssenceCostPerHour\":" + this.upkeepEssenceCostPerHour
                    + ",\"" + CONFIG_SLUMBERING_RECIPE_COBBLE_COST_KEY + "\":" + Math.max(0, this.slumberingRecipeCobbleCost)
                    + ",\"" + CONFIG_SLUMBERING_RECIPE_ESSENCE_COST_KEY + "\":" + Math.max(0, this.slumberingRecipeEssenceCost)
                    + ",\"" + CONFIG_OUTLANDER_RECIPE_COBBLE_COST_KEY + "\":" + Math.max(0, this.outlanderRecipeCobbleCost)
                    + ",\"" + CONFIG_OUTLANDER_RECIPE_ESSENCE_COST_KEY + "\":" + Math.max(0, this.outlanderRecipeEssenceCost)
                    + ",\"" + CONFIG_UPGRADE_TIER_2_ITEM_ID_KEY + "\":\"" + this.upgradeTier2ItemId + "\""
                    + ",\"" + CONFIG_UPGRADE_TIER_2_ITEM_QUANTITY_KEY + "\":" + Math.max(0, this.upgradeTier2ItemQuantity)
                    + ",\"" + CONFIG_UPGRADE_TIER_3_ITEM_ID_KEY + "\":\"" + this.upgradeTier3ItemId + "\""
                    + ",\"" + CONFIG_UPGRADE_TIER_3_ITEM_QUANTITY_KEY + "\":" + Math.max(0, this.upgradeTier3ItemQuantity)
                    + ",\"" + CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_ID_KEY + "\":\"" + this.upgradeTier4PrimaryItemId + "\""
                    + ",\"" + CONFIG_UPGRADE_TIER_4_PRIMARY_ITEM_QUANTITY_KEY + "\":" + Math.max(0, this.upgradeTier4PrimaryItemQuantity)
                    + ",\"" + CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_ID_KEY + "\":\"" + this.upgradeTier4SecondaryItemId + "\""
                    + ",\"" + CONFIG_UPGRADE_TIER_4_SECONDARY_ITEM_QUANTITY_KEY + "\":" + Math.max(0, this.upgradeTier4SecondaryItemQuantity)
                    + "}";
            Files.writeString(cfg, content, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException e) {
            ((HytaleLogger.Api) this.logger.at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to write config.json");
        }
    }

    private static String readItemIdOrDefault(String json, String key, String defaultValue) {
        String value = JsonReader.readString(json, key);
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }

    private static int readNonNegativeInt(String json, String key, int defaultValue) {
        Integer value = JsonReader.readInt(json, key);
        if (value == null) {
            return defaultValue;
        }
        return Math.max(0, value);
    }

    static int clampMaxClaimsPerPlayer(int value) {
        if (value < 1) {
            return 1;
        }
        if (value > MAX_CLAIMS_PER_PLAYER_CAP) {
            return MAX_CLAIMS_PER_PLAYER_CAP;
        }
        return value;
    }

    static int normalizeRadius(int radius) {
        for (int allowed : ALLOWED_RADII) {
            if (allowed == radius) {
                return allowed;
            }
        }
        return -1;
    }

    /**
     * Loose item-id matching: equal, ends with {@code ":configured"}, or simply contains it.
     * Used when the configured id has been seen with optional namespace prefixes at runtime.
     */
    static boolean itemIdMatches(String itemId, String configuredItemId) {
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

    int resolvedClaimRadius() {
        return this.claimRadius > 0 ? this.claimRadius : DEFAULT_RADIUS;
    }

    int resolvedMaxClaimsPerPlayer() {
        return clampMaxClaimsPerPlayer(this.maxClaimsPerPlayer <= 0 ? DEFAULT_MAX_CLAIMS_PER_PLAYER : this.maxClaimsPerPlayer);
    }

    int resolvedUpkeepGraceMinutes() {
        return Math.max(0, this.upkeepGraceMinutes);
    }

    int resolvedUpkeepEssenceCostPerHour() {
        return Math.max(1, this.upkeepEssenceCostPerHour);
    }

    String resolvedUpgradeTier2ItemId() {
        return resolvedItemId(this.upgradeTier2ItemId, DEFAULT_UPGRADE_TIER_2_ITEM_ID);
    }

    String resolvedUpgradeTier3ItemId() {
        return resolvedItemId(this.upgradeTier3ItemId, DEFAULT_UPGRADE_TIER_3_ITEM_ID);
    }

    String resolvedUpgradeTier4PrimaryItemId() {
        return resolvedItemId(this.upgradeTier4PrimaryItemId, DEFAULT_UPGRADE_TIER_4_PRIMARY_ITEM_ID);
    }

    String resolvedUpgradeTier4SecondaryItemId() {
        return resolvedItemId(this.upgradeTier4SecondaryItemId, DEFAULT_UPGRADE_TIER_4_SECONDARY_ITEM_ID);
    }

    private static String resolvedItemId(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }

    boolean isClaimItemId(String itemId) {
        return itemIdMatches(itemId, this.deityItemId)
                || itemIdMatches(itemId, DEFAULT_DEITY_ITEM_ID)
                || isOutlanderClaimItemId(itemId);
    }

    boolean isOutlanderClaimItemId(String itemId) {
        return itemIdMatches(itemId, this.outlanderDeityItemId)
                || itemIdMatches(itemId, OUTLANDER_DEITY_ITEM_ID)
                || itemIdMatches(itemId, OUTLANDER_DEITY_BLOCK_ITEM_ID);
    }

    boolean isTier2UpgradeItemId(String itemId) {
        return itemIdEqualsConfigured(itemId, resolvedUpgradeTier2ItemId());
    }

    boolean isTier3UpgradeItemId(String itemId) {
        return itemIdEqualsConfigured(itemId, resolvedUpgradeTier3ItemId());
    }

    boolean isTier4PrimaryUpgradeItemId(String itemId) {
        return itemIdEqualsConfigured(itemId, resolvedUpgradeTier4PrimaryItemId());
    }

    boolean isTier4SecondaryUpgradeItemId(String itemId) {
        return itemIdEqualsConfigured(itemId, resolvedUpgradeTier4SecondaryItemId());
    }

    boolean areTier4UpgradeItemsSameItem() {
        return itemIdEqualsConfigured(resolvedUpgradeTier4PrimaryItemId(), resolvedUpgradeTier4SecondaryItemId());
    }

    /** Strict item-id matching: equal or {@code "namespace:configured"}, no substring fallback. */
    static boolean itemIdEqualsConfigured(String itemId, String configuredItemId) {
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
}
