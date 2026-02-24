/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 *  com.hypixel.hytale.math.util.ChunkUtil
 *  com.hypixel.hytale.server.core.Message
 *  com.hypixel.hytale.server.core.command.system.AbstractCommand
 *  com.hypixel.hytale.server.core.command.system.CommandManager
 *  com.hypixel.hytale.server.core.permissions.PermissionsModule
 *  com.hypixel.hytale.server.core.plugin.JavaPlugin
 *  com.hypixel.hytale.server.core.plugin.JavaPluginInit
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent
 *  com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider
 *  com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk.WorldGenWorldMapProvider
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
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
import group.austale.deitylandprotection.DeityLandProtectionPlaceSystem;
import group.austale.deitylandprotection.DeityLandProtectionUpkeepStore;
import group.austale.deitylandprotection.DeityLandProtectionUseBlockSystem;
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
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DeityLandProtectionPlugin
extends JavaPlugin {
    private static final int DEFAULT_RADIUS = 16;
    private static final String DEFAULT_DeityLandProtection_ITEM_ID = "DeityLandProtection_Block";
    private static final int DEFAULT_MAX_CLAIMS_PER_PLAYER = 1;
    private static final int MAX_CLAIMS_PER_PLAYER_CAP = 5;
    private static final boolean DEFAULT_ALLOW_CRAFTING = true;
    private static final int DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR = 1;
    private static final int[] ALLOWED_RADII = new int[]{16, 32, 64, 128};
    private static final long FLUSH_PERIOD_SECONDS = 10L;
    private static final long RECENT_PLACEMENT_IGNORE_BREAK_MS = 2000L;
    private static final long PLAYER_MESSAGE_COOLDOWN_MS = 1200L;
    private static volatile DeityLandProtectionPlugin instance;
    private ClaimStore claimStore;
    private String DeityLandProtectionItemId;
    private int claimRadius;
    private int maxClaimsPerPlayer;
    private boolean allowCrafting;
    private ScheduledExecutorService flushExecutor;
    private DeityLandProtectionUpkeepStore upkeepStore;
    private boolean upkeepEnabled;
    private int upkeepGraceMinutes;
    private int upkeepEssenceCostPerHour;
    private Path absDataDir;
    private DeityLandProtectionLangPreferenceManager langPreferenceManager;
    private final ConcurrentHashMap<Long, Long> recentClaimPlacements = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, Long> lastPlayerMessageMs = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, Long> borderCenterByPlayer = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, Long> lastBorderSpawnMsByPlayer = new ConcurrentHashMap();
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
            this.ensureDeityLandProtectionUiAssets(this.absDataDir);
            this.ensureZoneConfigPageUi(this.absDataDir);
            this.ensureCustomDeityLandProtectionItem(this.absDataDir);
        }
        catch (Exception exception) {
            // empty catch block
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
            // empty catch block
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
            return 16;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Integer r = DeityLandProtectionPlugin.readJsonInt(json, "claimRadius");
            int normalized = DeityLandProtectionPlugin.normalizeRadius(r == null ? 16 : r);
            return normalized > 0 ? normalized : 16;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to read config.json");
            return 16;
        }
    }

    private int loadMaxClaimsPerPlayer(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return 1;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Integer v = DeityLandProtectionPlugin.readJsonInt(json, "maxClaimsPerPlayer");
            int requested = v == null ? 1 : v;
            return DeityLandProtectionPlugin.clampMaxClaimsPerPlayer(requested);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to read config.json");
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
            Boolean v = DeityLandProtectionPlugin.readJsonBoolean(json, "allowCrafting");
            return v == null ? true : v;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to read config.json");
            return true;
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
            String content = "{\"DeityLandProtectionItemId\":\"" + (this.DeityLandProtectionItemId == null ? DEFAULT_DeityLandProtection_ITEM_ID : this.DeityLandProtectionItemId) + "\",\"claimRadius\":" + this.getDefaultRadius() + ",\"maxClaimsPerPlayer\":" + this.getMaxClaimsPerPlayer() + ",\"allowCrafting\":" + this.isAllowCrafting() + ",\"upkeepEnabled\":" + this.isUpkeepEnabled() + ",\"upkeepGraceMinutes\":" + this.getUpkeepGraceMinutes() + ",\"upkeepEssenceCostPerHour\":" + this.getUpkeepEssenceCostPerHour() + "}";
            Files.writeString(cfg, (CharSequence)content, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to write config.json");
        }
    }

    private boolean loadUpkeepEnabled(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return true;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Boolean v = DeityLandProtectionPlugin.readJsonBoolean(json, "upkeepEnabled");
            return v == null ? true : v;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to read config.json");
            return true;
        }
    }

    private int loadUpkeepGraceMinutes(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return 30;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Integer v = DeityLandProtectionPlugin.readJsonInt(json, "upkeepGraceMinutes");
            return v == null ? 30 : Math.max(0, v);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to read config.json");
            return 30;
        }
    }

    private int loadUpkeepEssenceCostPerHour(Path dataDir) {
        Path cfg = this.resolveConfigPath(dataDir);
        if (!Files.exists(cfg, new LinkOption[0])) {
            return DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR;
        }
        try {
            String json = Files.readString(cfg, StandardCharsets.UTF_8);
            Integer v = DeityLandProtectionPlugin.readJsonInt(json, "upkeepEssenceCostPerHour");
            return v == null ? DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR : Math.max(1, v);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to read config.json");
            return DEFAULT_UPKEEP_ESSENCE_COST_PER_HOUR;
        }
    }

    private static Integer readJsonInt(String obj, String key) {
        String pattern = "\"" + key + "\"";
        int k = obj.indexOf(pattern);
        if (k < 0) {
            return null;
        }
        int colon = obj.indexOf(58, k + pattern.length());
        if (colon < 0) {
            return null;
        }
        int i = colon + 1;
        while (i < obj.length() && Character.isWhitespace(obj.charAt(i))) {
            ++i;
        }
        int j = i;
        while (j < obj.length() && (obj.charAt(j) == '-' || Character.isDigit(obj.charAt(j)))) {
            ++j;
        }
        if (j == i) {
            return null;
        }
        try {
            return Integer.parseInt(obj, i, j, 10);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Boolean readJsonBoolean(String obj, String key) {
        String pattern = "\"" + key + "\"";
        int k = obj.indexOf(pattern);
        if (k < 0) {
            return null;
        }
        int colon = obj.indexOf(58, k + pattern.length());
        if (colon < 0) {
            return null;
        }
        int i = colon + 1;
        while (i < obj.length() && Character.isWhitespace(obj.charAt(i))) {
            ++i;
        }
        if (i >= obj.length()) {
            return null;
        }
        if (obj.startsWith("true", i)) {
            return Boolean.TRUE;
        }
        if (obj.startsWith("false", i)) {
            return Boolean.FALSE;
        }
        return null;
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
        this.ensureDeityLandProtectionUiAssets(this.absDataDir);
        this.ensureZoneConfigPageUi(this.absDataDir);
        this.ensureCustomDeityLandProtectionItem(this.absDataDir);
        this.langPreferenceManager = new DeityLandProtectionLangPreferenceManager(this.getDataDirectory());
        this.DeityLandProtectionItemId = this.loadDeityLandProtectionItemId(this.absDataDir);
        this.claimRadius = this.loadClaimRadius(this.absDataDir);
        this.maxClaimsPerPlayer = this.loadMaxClaimsPerPlayer(this.absDataDir);
        this.allowCrafting = this.loadAllowCrafting(this.absDataDir);
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
            Thread t = new Thread(r, "DeityLandProtection-ClaimsFlush");
            t.setDaemon(true);
            return t;
        });
        this.flushExecutor.scheduleAtFixedRate(() -> {
            try {
                if (this.claimStore != null) {
                    this.claimStore.flushIfDirty();
                }
            }
            catch (Exception e) {
                ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection claims flush failed");
            }
        }, 10L, 10L, TimeUnit.SECONDS);
        this.getLogger().at(Level.INFO).log("DeityLandProtection dataDir=" + String.valueOf(this.absDataDir) + ", DeityLandProtectionItemId=" + this.DeityLandProtectionItemId + ", claimRadius=" + this.claimRadius + ", maxClaimsPerPlayer=" + this.maxClaimsPerPlayer + ", allowCrafting=" + this.allowCrafting + ", claimsLoaded=" + this.claimStore.getClaims().size());
        this.getLogger().at(Level.INFO).log("DeityLandProtectionPlugin setup");
    }

    protected void start() {
        try {
            CommandManager.get().register((AbstractCommand)new DeityLandProtectionCommand(this));
            CommandManager.get().register((AbstractCommand)new DeityLandProtectionLangCommand(this, this.langPreferenceManager));
        }
        catch (Exception e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to register commands");
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
            catch (Exception exception) {
                // empty catch block
            }
            this.flushExecutor = null;
        }
        if (this.claimStore != null) {
            this.claimStore.flushIfDirty();
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
        int centerX = claim.getCenterX();
        int centerZ = claim.getCenterZ();
        int r = claim.getRadius();
        int minX = centerX - r;
        int maxX = centerX + r;
        int minZ = centerZ - r;
        int maxZ = centerZ + r;
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

    public String getDeityLandProtectionItemId() {
        return this.DeityLandProtectionItemId;
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

    public void reloadData() {
        if (this.absDataDir == null) {
            return;
        }
        this.DeityLandProtectionItemId = this.loadDeityLandProtectionItemId(this.absDataDir);
        this.claimRadius = this.loadClaimRadius(this.absDataDir);
        this.maxClaimsPerPlayer = this.loadMaxClaimsPerPlayer(this.absDataDir);
        this.allowCrafting = this.loadAllowCrafting(this.absDataDir);
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
        int q2;
        int q1;
        String json;
        block12: {
            block11: {
                int colon;
                block10: {
                    int k;
                    String key;
                    block9: {
                        Path cfg = this.resolveConfigPath(dataDir);
                        if (!Files.exists(cfg, new LinkOption[0])) {
                            try {
                                Path cfgDir = cfg.getParent();
                                if (cfgDir != null) {
                                    Files.createDirectories(cfgDir, new FileAttribute[0]);
                                }
                                String content = "{\"DeityLandProtectionItemId\":\"DeityLandProtection_Block\",\"claimRadius\":16,\"maxClaimsPerPlayer\":1,\"allowCrafting\":true,\"upkeepEssenceCostPerHour\":1}";
                                Files.writeString(cfg, (CharSequence)content, StandardCharsets.UTF_8, new OpenOption[0]);
                            }
                            catch (IOException e) {
                                ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to write config.json");
                            }
                            return DEFAULT_DeityLandProtection_ITEM_ID;
                        }
                        try {
                            json = Files.readString(cfg, StandardCharsets.UTF_8);
                            key = "\"DeityLandProtectionItemId\"";
                            k = json.indexOf(key);
                            if (k >= 0) break block9;
                            return DEFAULT_DeityLandProtection_ITEM_ID;
                        }
                        catch (IOException e) {
                            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to read config.json");
                            return DEFAULT_DeityLandProtection_ITEM_ID;
                        }
                    }
                    colon = json.indexOf(58, k + key.length());
                    if (colon >= 0) break block10;
                    return DEFAULT_DeityLandProtection_ITEM_ID;
                }
                q1 = json.indexOf(34, colon + 1);
                if (q1 >= 0) break block11;
                return DEFAULT_DeityLandProtection_ITEM_ID;
            }
            q2 = json.indexOf(34, q1 + 1);
            if (q2 >= 0) break block12;
            return DEFAULT_DeityLandProtection_ITEM_ID;
        }
        String value = json.substring(q1 + 1, q2).trim();
        return value.isEmpty() ? DEFAULT_DeityLandProtection_ITEM_ID : value;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean isOpBypass(UUID playerUuid) {
        PermissionsModule perms = PermissionsModule.get();
        if (perms == null) {
            return false;
        }
        try {
            Iterator iterator = perms.getGroupsForUser(playerUuid).iterator();
            while (true) {
                String gl;
                if (!iterator.hasNext()) {
                    return false;
                }
                String g = (String)iterator.next();
                if (g == null || (gl = g.trim().toLowerCase()).isEmpty()) continue;
                if (gl.equals("op")) return true;
                if (gl.equals("admin")) return true;
                if (gl.equals("operator")) break;
            }
            return true;
        }
        catch (Exception exception) {
            // empty catch block
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

    public boolean shouldIgnoreCenterBreak(int x, int z) {
        long key = DeityLandProtectionPlugin.centerKey(x, z);
        Long ts = this.recentClaimPlacements.get(key);
        if (ts == null) {
            return false;
        }
        long age = System.currentTimeMillis() - ts;
        if (age >= 0L && age < 2000L) {
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
        if (last != null && now - last < 1200L) {
            return;
        }
        this.lastPlayerMessageMs.put(uuid, now);
        try {
            player.sendMessage(Message.raw((String)text));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void sendPlayerMessageImmediate(PlayerRef player, String text) {
        if (player == null || text == null || text.isEmpty()) {
            return;
        }
        try {
            player.sendMessage(Message.raw((String)text));
        }
        catch (Exception exception) {
            // empty catch block
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
            String u;
            String string = u = username == null ? null : username.trim();
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
        return (long)x << 32 ^ (long)z & 0xFFFFFFFFL;
    }

    private void ensureAssetPackManifest(Path dataDir) {
        try {
            Files.createDirectories(dataDir, new FileAttribute[0]);
            Path manifest = dataDir.resolve("manifest.json");
            String desired = "{\"Group\":\"games.Austale\",\"Name\":\"DeityLandProtectionData\",\"Version\":\"1.0.0\",\"ServerVersion\":\"2026.02.19-1a311a592\"}";
            if (Files.exists(manifest, new LinkOption[0])) {
                try {
                    String existing = Files.readString(manifest, StandardCharsets.UTF_8);
                    if (existing != null && existing.equals(desired)) {
                        return;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            Files.writeString(manifest, (CharSequence)desired, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void ensureModsAssetPack() {
    }

    private void ensureDeityLandProtectionUiAssets(Path dataDir) {
        try {
            this.writeResourceIfMissingOrDifferent(dataDir.resolve("Common/UI/Custom/DeityLandProtectionCommon.ui"), "Common/UI/Custom/DeityLandProtectionCommon.ui");
            this.writeResourceIfMissingOrDifferent(dataDir.resolve("Common/UI/Custom/DeityLandProtectionSounds.ui"), "Common/UI/Custom/DeityLandProtectionSounds.ui");
            this.writeResourceIfMissingOrDifferent(dataDir.resolve("Assets/Common/UI/Custom/DeityLandProtectionCommon.ui"), "Assets/Common/UI/Custom/DeityLandProtectionCommon.ui");
            this.writeResourceIfMissingOrDifferent(dataDir.resolve("Assets/Common/UI/Custom/DeityLandProtectionSounds.ui"), "Assets/Common/UI/Custom/DeityLandProtectionSounds.ui");
            String[] stringArray = new String[]{"Common/UI/Custom/Pages/DeityLandProtectionAdminPage.ui", "Common/UI/Custom/Pages/DeityLandProtectionAdminPage_en.ui", "Common/UI/Custom/Pages/DeityLandProtectionPlayerOverviewPage.ui", "Common/UI/Custom/Pages/DeityLandProtectionPlayerOverviewPage_en.ui", "Common/UI/Custom/Pages/DeityLandProtectionUpkeepButtonElement.ui", "Common/UI/Custom/Pages/DeityLandProtectionZoneConfigPage.ui", "Common/UI/Custom/Pages/DeityLandProtectionZoneConfigPage_en.ui", "Common/UI/Custom/Pages/DeityLandProtectionContainerHintPage.ui", "Common/UI/Custom/Pages/DeityLandProtectionContainerHintPage_en.ui", "Assets/Common/UI/Custom/Pages/DeityLandProtectionAdminPage.ui", "Assets/Common/UI/Custom/Pages/DeityLandProtectionAdminPage_en.ui", "Assets/Common/UI/Custom/Pages/DeityLandProtectionPlayerOverviewPage.ui", "Assets/Common/UI/Custom/Pages/DeityLandProtectionPlayerOverviewPage_en.ui", "Assets/Common/UI/Custom/Pages/DeityLandProtectionUpkeepButtonElement.ui", "Assets/Common/UI/Custom/Pages/DeityLandProtectionZoneConfigPage.ui", "Assets/Common/UI/Custom/Pages/DeityLandProtectionZoneConfigPage_en.ui", "Assets/Common/UI/Custom/Pages/DeityLandProtectionContainerHintPage.ui", "Assets/Common/UI/Custom/Pages/DeityLandProtectionContainerHintPage_en.ui"};
            int n = stringArray.length;
            int n2 = 0;
            while (n2 < n) {
                String p = stringArray[n2];
                this.writeResourceIfMissingOrDifferent(dataDir.resolve(p), p);
                ++n2;
            }
        }
        catch (Exception e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to write UI assets");
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
            Files.writeString(dest, (CharSequence)content, StandardCharsets.UTF_8, new OpenOption[0]);
        }
    }

    private void ensureCustomDeityLandProtectionItem(Path dataDir) {
        try {
            this.writeResourceIfMissingOrDifferent(dataDir.resolve("Server/Item/Items/DeityLandProtection/DeityLandProtection_Block.json"), "Server/Item/Items/DeityLandProtection/DeityLandProtection_Block.json");
            this.writeResourceIfMissingOrDifferent(dataDir.resolve("Assets/Server/Item/Items/DeityLandProtection/DeityLandProtection_Block.json"), "Assets/Server/Item/Items/DeityLandProtection/DeityLandProtection_Block.json");
        }
        catch (Exception e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to write custom item asset");
        }
    }

    private void ensureZoneConfigPageUi(Path dataDir) {
        try {
            Path ui = dataDir.resolve("Common/UI/Custom/Pages/DeityLandProtectionZoneConfigPage.ui");
            Path uiEn = dataDir.resolve("Common/UI/Custom/Pages/DeityLandProtectionZoneConfigPage_en.ui");
            Path uiAlt = dataDir.resolve("Assets/Common/UI/Custom/Pages/DeityLandProtectionZoneConfigPage.ui");
            Path uiAltEn = dataDir.resolve("Assets/Common/UI/Custom/Pages/DeityLandProtectionZoneConfigPage_en.ui");
            String content = "$C = \"../DeityLandProtectionCommon.ui\";\n\n$C.@PageOverlay {}\n\n$C.@DecoratedContainer {\n  Anchor: (Width: 600, Height: 400);\n\n  #Title {\n    Group {\n      $C.@Title {\n        @Text = \"Configuracion de la Zona\";\n      }\n    }\n  }\n\n  #Content {\n    Group {\n      LayoutMode: Left;\n      Padding: (Right: 15, Bottom: 5);\n\n      Label {\n        FlexWeight: 1;\n        Text: %server.customUI.itemRepairPage.item;\n        Style: (RenderBold: true);\n      }\n\n      Label {\n        Text: %server.customUI.itemRepairPage.durability;\n        Style: (RenderBold: true);\n      }\n    }\n\n    Group #ElementList {\n      FlexWeight: 1;\n      LayoutMode: TopScrolling;\n      ScrollbarStyle: $C.@DefaultScrollbarStyle;\n    }\n  }\n}\n\n$C.@BackButton {}\n";
            String contentEn = "$C = \"../DeityLandProtectionCommon.ui\";\n\n$C.@PageOverlay {}\n\n$C.@DecoratedContainer {\n  Anchor: (Width: 600, Height: 400);\n\n  #Title {\n    Group {\n      $C.@Title {\n        @Text = \"Zone Configuration\";\n      }\n    }\n  }\n\n  #Content {\n    Group {\n      LayoutMode: Left;\n      Padding: (Right: 15, Bottom: 5);\n\n      Label {\n        FlexWeight: 1;\n        Text: %server.customUI.itemRepairPage.item;\n        Style: (RenderBold: true);\n      }\n\n      Label {\n        Text: %server.customUI.itemRepairPage.durability;\n        Style: (RenderBold: true);\n      }\n    }\n\n    Group #ElementList {\n      FlexWeight: 1;\n      LayoutMode: TopScrolling;\n      ScrollbarStyle: $C.@DefaultScrollbarStyle;\n    }\n  }\n}\n\n$C.@BackButton {}\n";
            boolean wrote = false;
            if (!Files.exists(ui, new LinkOption[0])) {
                Files.createDirectories(ui.getParent(), new FileAttribute[0]);
                Files.writeString(ui, (CharSequence)content, StandardCharsets.UTF_8, new OpenOption[0]);
                wrote = true;
            }
            if (!Files.exists(uiEn, new LinkOption[0])) {
                Files.createDirectories(uiEn.getParent(), new FileAttribute[0]);
                Files.writeString(uiEn, (CharSequence)contentEn, StandardCharsets.UTF_8, new OpenOption[0]);
                wrote = true;
            }
            if (!Files.exists(uiAlt, new LinkOption[0])) {
                Files.createDirectories(uiAlt.getParent(), new FileAttribute[0]);
                Files.writeString(uiAlt, (CharSequence)content, StandardCharsets.UTF_8, new OpenOption[0]);
                wrote = true;
            }
            if (!Files.exists(uiAltEn, new LinkOption[0])) {
                Files.createDirectories(uiAltEn.getParent(), new FileAttribute[0]);
                Files.writeString(uiAltEn, (CharSequence)contentEn, StandardCharsets.UTF_8, new OpenOption[0]);
                wrote = true;
            }
            if (wrote) {
                this.getLogger().at(Level.INFO).log("DeityLandProtection wrote UI page to " + String.valueOf(ui) + ", " + String.valueOf(uiEn) + ", " + String.valueOf(uiAlt) + " and/or " + String.valueOf(uiAltEn));
            }
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to write zone config UI");
        }
    }
}



