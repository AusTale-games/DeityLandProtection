package group.austale.deitylandprotection;

import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
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
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DeityLandProtectionPlugin
extends JavaPlugin {
    private static final long FLUSH_PERIOD_SECONDS = 10L;
    private static final long RECENT_PLACEMENT_IGNORE_BREAK_MS = 2000L;
    private static final long PLAYER_MESSAGE_COOLDOWN_MS = 1200L;
    private static volatile DeityLandProtectionPlugin instance;
    private ClaimStore claimStore;
    private PluginConfig config;
    private ScheduledExecutorService flushExecutor;
    private UpkeepStore upkeepStore;
    private Path absDataDir;
    private LangPreferenceManager langPreferenceManager;
    private Localizer localizer;
    private final PlayerStateRegistry playerState = new PlayerStateRegistry();
    private final BorderSurfaceCache borderSurfaceCache = new BorderSurfaceCache();
    private final ConcurrentHashMap<String, Integer> borderSurfaceScanBaseYByWorldCenter = new ConcurrentHashMap();
    private MapUpdateQueue mapUpdateQueue;
    private final RecipeOverrider recipeOverrider = new RecipeOverrider(this);
    private AssetInstaller assetInstaller;

    public DeityLandProtectionPlugin(JavaPluginInit init) {
        super(init);
        try {
            Path dataDir = this.getDataDirectory();
            this.absDataDir = dataDir.toAbsolutePath().normalize();
            this.config = new PluginConfig(this.getLogger());
            this.assetInstaller = new AssetInstaller(this.getLogger(), this.recipeOverrider);
            this.assetInstaller.ensureAssetPackManifest(this.absDataDir);
            this.assetInstaller.removeDuplicateCustomUiFromDataPack(this.absDataDir);
            this.config.load(this.absDataDir);
            this.assetInstaller.ensureCustomDeityItem(this.absDataDir);
        }
        catch (Exception e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("DeityLandProtection failed during plugin construction; setup() will retry");
        }
    }

    public static DeityLandProtectionPlugin getInstance() {
        return instance;
    }

    protected void setup() {
        instance = this;
        Path dataDir = this.getDataDirectory();
        this.absDataDir = dataDir.toAbsolutePath().normalize();
        if (this.config == null) {
            this.config = new PluginConfig(this.getLogger());
        }
        if (this.assetInstaller == null) {
            this.assetInstaller = new AssetInstaller(this.getLogger(), this.recipeOverrider);
        }
        this.assetInstaller.ensureAssetPackManifest(this.absDataDir);
        this.assetInstaller.removeDuplicateCustomUiFromDataPack(this.absDataDir);
        this.config.load(this.absDataDir);
        this.assetInstaller.ensureCustomDeityItem(this.absDataDir);
        LocalizationCatalog.writeGeneratedLanguageFiles(this.absDataDir);
        this.langPreferenceManager = new LangPreferenceManager(this.getDataDirectory());
        this.localizer = new Localizer();
        this.claimStore = new ClaimStore(this.absDataDir.resolve("claims.json"), this.getLogger());
        this.claimStore.load();
        this.upkeepStore = new UpkeepStore(this.absDataDir.resolve("upkeep.json"), this.getLogger());
        this.upkeepStore.load();
        this.mapUpdateQueue = new MapUpdateQueue(this.claimStore);
        this.getEntityStoreRegistry().registerSystem((ISystem)new PlaceSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new BreakSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new UseBlockSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new CraftSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new BorderTickSystem(this));
        this.getEntityStoreRegistry().registerSystem((ISystem)new EnterExitTickSystem(this));
        this.getChunkStoreRegistry().registerSystem((ISystem)new UpkeepTickingSystem(this));
        this.getChunkStoreRegistry().registerSystem((ISystem)new BorderSurfaceRefreshSystem(this));
        this.getChunkStoreRegistry().registerSystem((ISystem)new WorldMapUpdateTickingSystem(this));
        IWorldMapProvider.CODEC.register("DeityLandProtection", WorldMapProvider.class, WorldMapProvider.CODEC);
        this.getEventRegistry().registerGlobal(AddWorldEvent.class, event -> {
            if (event.getWorld().getWorldConfig().isDeleteOnRemove()) {
                event.getWorld().getWorldConfig().setWorldMapProvider((IWorldMapProvider)new WorldGenWorldMapProvider());
                return;
            }
            event.getWorld().getWorldConfig().setWorldMapProvider((IWorldMapProvider)new WorldMapProvider());
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
        this.getLogger().at(Level.INFO).log("DeityLandProtection dataDir=" + String.valueOf(this.absDataDir) + ", DeityLandProtectionItemId=" + this.config.deityItemId + ", OutlanderDeityItemId=" + this.config.outlanderDeityItemId + ", claimRadius=" + this.config.claimRadius + ", maxClaimsPerPlayer=" + this.config.maxClaimsPerPlayer + ", allowCrafting=" + this.config.allowCrafting + ", claimsLoaded=" + this.claimStore.getClaims().size());
        this.getLogger().at(Level.INFO).log("DeityLandProtectionPlugin setup");
    }

    protected void start() {
        try {
            CommandManager.get().register((AbstractCommand)new DeityCommand(this));
            CommandManager.get().register((AbstractCommand)new LangCommand(this, this.langPreferenceManager));
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
        if (this.mapUpdateQueue != null) {
            this.mapUpdateQueue.clear();
        }
        if (instance == this) {
            instance = null;
        }
        this.getLogger().at(Level.INFO).log("DeityLandProtectionPlugin shutdown");
    }

    public void queueMapUpdateForClaim(String worldName, Claim claim) {
        if (this.mapUpdateQueue != null) {
            this.mapUpdateQueue.queueForClaim(worldName, claim);
        }
    }

    public void queueMapUpdateForAllClaims(String worldName) {
        if (this.mapUpdateQueue != null) {
            this.mapUpdateQueue.queueForAllClaims(worldName);
        }
    }

    public LongSet pollMapUpdateChunks(String worldName) {
        return this.mapUpdateQueue == null ? null : this.mapUpdateQueue.poll(worldName);
    }

    public ClaimStore getClaimStore() {
        return this.claimStore;
    }

    public int getDefaultRadius() {
        return this.config.resolvedClaimRadius();
    }

    public int getClaimRadius() {
        return this.getDefaultRadius();
    }

    public boolean setClaimRadius(int radius) {
        int normalized = PluginConfig.normalizeRadius(radius);
        if (normalized <= 0) {
            return false;
        }
        this.config.claimRadius = normalized;
        this.config.save(this.absDataDir);
        return true;
    }

    public int getMaxClaimsPerPlayer() {
        return this.config.resolvedMaxClaimsPerPlayer();
    }

    public boolean setMaxClaimsPerPlayer(int maxClaimsPerPlayer) {
        this.config.maxClaimsPerPlayer = PluginConfig.clampMaxClaimsPerPlayer(maxClaimsPerPlayer);
        this.config.save(this.absDataDir);
        return true;
    }

    public boolean isAllowCrafting() {
        return this.config.allowCrafting;
    }

    public boolean setAllowCrafting(boolean allowCrafting) {
        this.config.allowCrafting = allowCrafting;
        this.config.save(this.absDataDir);
        return true;
    }

    public boolean isMapClaimVisualEnabled() {
        return this.config.mapClaimVisualEnabled;
    }

    public boolean setMapClaimVisualEnabled(boolean enabled) {
        this.config.mapClaimVisualEnabled = enabled;
        this.config.save(this.absDataDir);
        return true;
    }

    public String getDeityLandProtectionItemId() {
        return this.config.deityItemId;
    }

    public boolean isClaimItemId(String itemId) {
        return this.config.isClaimItemId(itemId);
    }

    public boolean isOutlanderClaimItemId(String itemId) {
        return this.config.isOutlanderClaimItemId(itemId);
    }

    public String getUpkeepEssenceItemIdForClaim(Claim claim) {
        if (claim != null && this.isOutlanderClaimItemId(claim.getDeityItemId())) {
            return PluginConfig.ESSENCE_OF_VOID_ITEM_ID;
        }
        return PluginConfig.ESSENCE_OF_LIFE_ITEM_ID;
    }

    public int getUpkeepTierForClaim(Claim claim) {
        return TierSystem.getUpkeepTierForClaim(this.upkeepStore, claim);
    }

    public int getClaimRadiusForTier(int tier) {
        return TierSystem.getClaimRadiusForTier(this.getClaimRadius(), tier);
    }

    public int getUpkeepEssenceCostPerHourForTier(int tier) {
        return TierSystem.getUpkeepEssenceCostPerHourForTier(this.getUpkeepEssenceCostPerHour(), tier);
    }

    public int getUpkeepEssenceCostPerHourForClaim(Claim claim) {
        return this.getUpkeepEssenceCostPerHourForTier(this.getUpkeepTierForClaim(claim));
    }

    public int getTierMultiplier(int tier) {
        return TierSystem.getTierMultiplier(tier);
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
        return this.config.resolvedUpgradeTier2ItemId();
    }

    public int getUpgradeTier2ItemQuantity() {
        return Math.max(0, this.config.upgradeTier2ItemQuantity);
    }

    public String getUpgradeTier3ItemId() {
        return this.config.resolvedUpgradeTier3ItemId();
    }

    public int getUpgradeTier3ItemQuantity() {
        return Math.max(0, this.config.upgradeTier3ItemQuantity);
    }

    public String getUpgradeTier4PrimaryItemId() {
        return this.config.resolvedUpgradeTier4PrimaryItemId();
    }

    public int getUpgradeTier4PrimaryItemQuantity() {
        return Math.max(0, this.config.upgradeTier4PrimaryItemQuantity);
    }

    public String getUpgradeTier4SecondaryItemId() {
        return this.config.resolvedUpgradeTier4SecondaryItemId();
    }

    public int getUpgradeTier4SecondaryItemQuantity() {
        return Math.max(0, this.config.upgradeTier4SecondaryItemQuantity);
    }

    public boolean isTier2UpgradeItemId(String itemId) {
        return this.config.isTier2UpgradeItemId(itemId);
    }

    public boolean isTier3UpgradeItemId(String itemId) {
        return this.config.isTier3UpgradeItemId(itemId);
    }

    public boolean isTier4PrimaryUpgradeItemId(String itemId) {
        return this.config.isTier4PrimaryUpgradeItemId(itemId);
    }

    public boolean isTier4SecondaryUpgradeItemId(String itemId) {
        return this.config.isTier4SecondaryUpgradeItemId(itemId);
    }

    public boolean areTier4UpgradeItemsSameItem() {
        return this.config.areTier4UpgradeItemsSameItem();
    }

    public String getUpkeepEssenceTitleForClaim(Claim claim, LangPreferenceManager.Language lang) {
        if (claim != null && this.isOutlanderClaimItemId(claim.getDeityItemId())) {
            return Text.uiSlot0VoidTitle(lang);
        }
        return Text.uiSlot0Title(lang);
    }

    public Path getAbsDataDir() {
        return this.absDataDir;
    }

    public LangPreferenceManager getLangPreferenceManager() {
        return this.langPreferenceManager;
    }

    public LangPreferenceManager.Language getEffectiveLanguage(UUID playerUuid) {
        if (this.langPreferenceManager == null) {
            return LangPreferenceManager.Language.EN;
        }
        return this.langPreferenceManager.getEffectiveLanguage(playerUuid);
    }

    public LangPreferenceManager.Language getEffectiveLanguage(PlayerRef playerRef) {
        if (playerRef == null) {
            return LangPreferenceManager.Language.EN;
        }
        return this.getEffectiveLanguage(playerRef.getUuid());
    }

    public String tr(LangPreferenceManager.Language language, String key, Object ... args) {
        Localizer l = this.localizer;
        if (l == null) {
            l = new Localizer();
            this.localizer = l;
        }
        return l.tr(language, key, args);
    }

    public void reloadData() {
        if (this.absDataDir == null) {
            return;
        }
        this.config.load(this.absDataDir);
        if (this.assetInstaller != null) {
            this.assetInstaller.ensureCustomDeityItem(this.absDataDir);
        }
        if (this.claimStore != null) {
            this.claimStore.load();
        }
        if (this.upkeepStore != null) {
            this.upkeepStore.load();
        }
    }

    public UpkeepStore getUpkeepStore() {
        return this.upkeepStore;
    }

    public boolean isUpkeepEnabled() {
        return this.config.upkeepEnabled;
    }

    public void setUpkeepEnabled(boolean enabled) {
        this.config.upkeepEnabled = enabled;
        this.config.save(this.absDataDir);
    }

    public int getUpkeepGraceMinutes() {
        return this.config.resolvedUpkeepGraceMinutes();
    }

    public void setUpkeepGraceMinutes(int minutes) {
        this.config.upkeepGraceMinutes = Math.max(0, minutes);
        this.config.save(this.absDataDir);
    }

    public long getUpkeepGraceMs() {
        return (long)this.getUpkeepGraceMinutes() * 60000L;
    }

    public int getUpkeepEssenceCostPerHour() {
        return this.config.resolvedUpkeepEssenceCostPerHour();
    }

    public void setUpkeepEssenceCostPerHour(int cost) {
        this.config.upkeepEssenceCostPerHour = Math.max(1, cost);
        this.config.save(this.absDataDir);
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
        this.playerState.markRecentClaimPlacement(x, z);
    }

    public void enableBorder(UUID playerUuid, int centerX, int centerZ) {
        this.playerState.enableBorder(playerUuid, centerX, centerZ);
    }

    public void disableBorder(UUID playerUuid) {
        this.playerState.disableBorder(playerUuid);
    }

    public void clearBorderForClaim(int centerX, int centerZ) {
        this.playerState.clearBorderForClaim(centerX, centerZ);
    }

    public boolean isBorderEnabled(UUID playerUuid, long centerKey) {
        return this.playerState.isBorderEnabled(playerUuid, centerKey);
    }

    public Long getBorderCenterKey(UUID playerUuid) {
        return this.playerState.getBorderCenterKey(playerUuid);
    }

    public boolean shouldSpawnBorderNow(UUID playerUuid, long nowMs, long cooldownMs) {
        return this.playerState.shouldSpawnBorderNow(playerUuid, nowMs, cooldownMs);
    }

    public void recordBorderPlayerWorld(UUID playerUuid, String worldName) {
        this.playerState.recordBorderPlayerWorld(playerUuid, worldName);
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

    public BorderSurfaceCache getBorderSurfaceCache() {
        return this.borderSurfaceCache;
    }

    public void collectActiveBorderCenterKeysForWorld(String worldName, LongOpenHashSet out) {
        this.playerState.collectActiveBorderCenterKeysForWorld(worldName, out);
    }

    public boolean hasAnyBorderSessionForWorld(String worldName) {
        return this.playerState.hasAnyBorderSessionForWorld(worldName);
    }

    public boolean shouldIgnoreCenterBreak(int x, int z) {
        return this.playerState.shouldIgnoreCenterBreak(x, z, RECENT_PLACEMENT_IGNORE_BREAK_MS);
    }

    public void sendPlayerMessage(PlayerRef player, String text) {
        if (player == null || text == null || text.isEmpty()) {
            return;
        }
        if (!this.playerState.shouldSendRateLimitedMessage(player.getUuid(), System.currentTimeMillis(), PLAYER_MESSAGE_COOLDOWN_MS)) {
            return;
        }
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
        return this.playerState.getLastZoneKey(playerUuid);
    }

    public void setLastZoneKey(UUID playerUuid, Long zoneKey) {
        this.playerState.setLastZoneKey(playerUuid, zoneKey);
    }

    public void rememberUsername(UUID playerUuid, String username) {
        this.playerState.rememberUsername(playerUuid, username);
    }

    public String getKnownUsername(UUID playerUuid) {
        return this.playerState.getKnownUsername(playerUuid);
    }

    public UUID getKnownUuidForUsername(String username) {
        return this.playerState.getKnownUuidForUsername(username);
    }

    public Map<UUID, String> getPlayersInClaim(int centerX, int centerZ) {
        return this.playerState.getPlayersInClaim(centerX, centerZ);
    }

    public void updatePlayerClaimMembership(UUID playerUuid, Long prevCenterKey, Long nowCenterKey, String username) {
        this.playerState.updatePlayerClaimMembership(playerUuid, prevCenterKey, nowCenterKey, username);
    }

    public static long centerKey(int x, int z) {
        return ChunkKeys.pack(x, z);
    }

    int getSlumberingRecipeCobbleCost() {
        return Math.max(0, this.config.slumberingRecipeCobbleCost);
    }

    int getSlumberingRecipeEssenceCost() {
        return Math.max(0, this.config.slumberingRecipeEssenceCost);
    }

    int getOutlanderRecipeCobbleCost() {
        return Math.max(0, this.config.outlanderRecipeCobbleCost);
    }

    int getOutlanderRecipeEssenceCost() {
        return Math.max(0, this.config.outlanderRecipeEssenceCost);
    }

}



