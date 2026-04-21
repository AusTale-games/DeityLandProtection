/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 */
package group.austale.deitylandprotection;

import com.hypixel.hytale.logger.HytaleLogger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

public final class ClaimStore {
    private static final String TERRITORY_ROWS_KEY = "rows";
    private final Path file;
    private final HytaleLogger logger;
    private final List<Claim> claims = new ArrayList<Claim>();
    private final Map<Long, List<Claim>> claimsByChunk = new HashMap<Long, List<Claim>>();
    private final Map<Long, Claim> claimsByCenter = new HashMap<Long, Claim>();
    private final Map<Long, Long> claimByCell = new HashMap<Long, Long>();
    private final Map<Long, HashSet<Long>> cellsByClaim = new HashMap<Long, HashSet<Long>>();
    private int maxChunkRadius;
    private boolean dirty;
    private boolean territoryReconcilePending;

    public ClaimStore(Path file, HytaleLogger logger) {
        this.file = file;
        this.logger = logger;
    }

    public synchronized List<Claim> getClaims() {
        return Collections.unmodifiableList(new ArrayList<Claim>(this.claims));
    }

    public synchronized int countClaimsForOwner(UUID owner) {
        if (owner == null) {
            return 0;
        }
        int count = 0;
        for (Claim c : this.claims) {
            if (!owner.equals(c.getOwner())) continue;
            ++count;
        }
        return count;
    }

    public synchronized Claim findClaimAt(int x, int z) {
        Long claimKey = this.claimByCell.get(ClaimStore.cellKey(x, z));
        if (claimKey == null) {
            return null;
        }
        return this.claimsByCenter.get(claimKey);
    }

    public synchronized List<Claim> getClaimsNearArea(int minX, int minZ, int maxX, int maxZ) {
        int minChunkX = ClaimStore.floorDiv(minX, 16);
        int maxChunkX = ClaimStore.floorDiv(maxX, 16);
        int minChunkZ = ClaimStore.floorDiv(minZ, 16);
        int maxChunkZ = ClaimStore.floorDiv(maxZ, 16);
        int range = Math.max(0, this.maxChunkRadius);
        HashSet<Claim> unique = new HashSet<Claim>();
        for (int baseX = minChunkX; baseX <= maxChunkX; ++baseX) {
            for (int baseZ = minChunkZ; baseZ <= maxChunkZ; ++baseZ) {
                for (int dx = -range; dx <= range; ++dx) {
                    for (int dz = -range; dz <= range; ++dz) {
                        List<Claim> bucket = this.claimsByChunk.get(ClaimStore.chunkKey(baseX + dx, baseZ + dz));
                        if (bucket == null) continue;
                        unique.addAll(bucket);
                    }
                }
            }
        }
        return unique.isEmpty() ? Collections.emptyList() : new ArrayList<Claim>(unique);
    }

    public synchronized Claim findClaimByCenter(int centerX, int centerZ) {
        return this.claimsByCenter.get(ClaimStore.centerKey(centerX, centerZ));
    }

    public synchronized int[] getClaimCellBounds(int centerX, int centerZ) {
        long claimKey = ClaimStore.centerKey(centerX, centerZ);
        Claim claim = this.claimsByCenter.get(claimKey);
        if (claim == null) {
            return null;
        }
        HashSet<Long> owned = this.cellsByClaim.get(claimKey);
        if (owned == null || owned.isEmpty()) {
            int r = claim.getRadius();
            return new int[]{centerX - r, centerX + r, centerZ - r, centerZ + r};
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (Long cell : owned) {
            if (cell == null) {
                continue;
            }
            int x = ClaimStore.xOfCellKey(cell.longValue());
            int z = ClaimStore.zOfCellKey(cell.longValue());
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }
        if (minX == Integer.MAX_VALUE) {
            int r = claim.getRadius();
            return new int[]{centerX - r, centerX + r, centerZ - r, centerZ + r};
        }
        return new int[]{minX, maxX, minZ, maxZ};
    }

    public synchronized boolean updateClaimCenterYIfUnknown(int centerX, int centerZ, int centerY) {
        List<Claim> bucket;
        Claim existing = this.claimsByCenter.get(ClaimStore.centerKey(centerX, centerZ));
        if (existing == null) {
            return false;
        }
        if (existing.getCenterY() != Integer.MIN_VALUE) {
            return false;
        }
        Claim updated = new Claim(existing.getOwner(), existing.getOwnerName(), existing.getCenterX(), centerY, existing.getCenterZ(), existing.getRadius(), existing.getTrusted(), existing.isPvpEnabled(), existing.getDeityItemId());
        this.claimsByCenter.put(ClaimStore.centerKey(centerX, centerZ), updated);
        for (int i = 0; i < this.claims.size(); ++i) {
            if (this.claims.get(i) != existing) continue;
            this.claims.set(i, updated);
            break;
        }
        if ((bucket = this.claimsByChunk.get(ClaimStore.chunkKey(ClaimStore.floorDiv(centerX, 16), ClaimStore.floorDiv(centerZ, 16)))) != null) {
            for (int i = 0; i < bucket.size(); ++i) {
                if (bucket.get(i) != existing) continue;
                bucket.set(i, updated);
                break;
            }
        }
        this.dirty = true;
        return true;
    }

    public synchronized boolean updateClaimPvpEnabled(int centerX, int centerZ, boolean pvpEnabled) {
        List<Claim> bucket;
        Claim existing = this.claimsByCenter.get(ClaimStore.centerKey(centerX, centerZ));
        if (existing == null) {
            return false;
        }
        if (existing.isPvpEnabled() == pvpEnabled) {
            return false;
        }
        Claim updated = new Claim(existing.getOwner(), existing.getOwnerName(), existing.getCenterX(), existing.getCenterY(), existing.getCenterZ(), existing.getRadius(), existing.getTrusted(), pvpEnabled, existing.getDeityItemId());
        this.claimsByCenter.put(ClaimStore.centerKey(centerX, centerZ), updated);
        for (int i = 0; i < this.claims.size(); ++i) {
            if (this.claims.get(i) != existing) continue;
            this.claims.set(i, updated);
            break;
        }
        if ((bucket = this.claimsByChunk.get(ClaimStore.chunkKey(ClaimStore.floorDiv(centerX, 16), ClaimStore.floorDiv(centerZ, 16)))) != null) {
            for (int i = 0; i < bucket.size(); ++i) {
                if (bucket.get(i) != existing) continue;
                bucket.set(i, updated);
                break;
            }
        }
        this.dirty = true;
        return true;
    }

    public synchronized void markDirty() {
        this.dirty = true;
    }

    public synchronized boolean consumeTerritoryReconcilePending() {
        boolean pending = this.territoryReconcilePending;
        this.territoryReconcilePending = false;
        return pending;
    }

    public synchronized boolean hasUnclaimedTerritoryInRadius(int centerX, int centerZ, int radius) {
        if (radius <= 0) {
            return false;
        }
        long claimKey = ClaimStore.centerKey(centerX, centerZ);
        Claim claim = this.claimsByCenter.get(claimKey);
        if (claim == null) {
            return false;
        }
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;
        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                if (this.claimByCell.containsKey(ClaimStore.cellKey(x, z))) continue;
                return true;
            }
        }
        return false;
    }

    public synchronized boolean reconcileClaimTerritory(int centerX, int centerZ) {
        long claimKey = ClaimStore.centerKey(centerX, centerZ);
        Claim claim = this.claimsByCenter.get(claimKey);
        if (claim == null) {
            return false;
        }
        int radius = claim.getRadius();
        if (radius <= 0) {
            return false;
        }
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;
        boolean changed = false;
        HashSet<Long> owned = this.cellsByClaim.get(claimKey);
        if (owned != null && !owned.isEmpty()) {
            ArrayList<Long> toRelease = new ArrayList<Long>();
            for (Long cell : owned) {
                if (cell == null) continue;
                int x = ClaimStore.xOfCellKey(cell.longValue());
                int z = ClaimStore.zOfCellKey(cell.longValue());
                if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) continue;
                toRelease.add(cell);
            }
            for (Long cell : toRelease) {
                if (cell == null || !this.unassignCellFromClaim(claimKey, cell.longValue())) continue;
                changed = true;
            }
        }
        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                long cellKey = ClaimStore.cellKey(x, z);
                Long ownerKey = this.claimByCell.get(cellKey);
                if (ownerKey != null) continue;
                this.assignCellToClaim(claimKey, cellKey);
                changed = true;
            }
        }
        if (changed) {
            this.dirty = true;
        }
        return changed;
    }

    public synchronized boolean intersectsAny(int centerX, int centerZ, int radius) {
        if (radius <= 0) {
            return false;
        }
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;
        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                if (!this.claimByCell.containsKey(ClaimStore.cellKey(x, z))) continue;
                return true;
            }
        }
        return false;
    }

    public synchronized boolean intersectsAnyExceptOwner(UUID owner, int centerX, int centerZ, int radius) {
        if (radius <= 0) {
            return false;
        }
        long selfKey = ClaimStore.centerKey(centerX, centerZ);
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;
        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                Long claimKey = this.claimByCell.get(ClaimStore.cellKey(x, z));
                if (claimKey == null || claimKey.longValue() == selfKey) continue;
                Claim other = this.claimsByCenter.get(claimKey);
                if (other == null || owner != null && owner.equals(other.getOwner())) continue;
                return true;
            }
        }
        return false;
    }

    public synchronized boolean updateClaimRadius(int centerX, int centerZ, int radius) {
        List<Claim> bucket;
        if (radius <= 0) {
            return false;
        }
        Claim existing = this.claimsByCenter.get(ClaimStore.centerKey(centerX, centerZ));
        if (existing == null) {
            return false;
        }
        if (existing.getRadius() == radius) {
            return false;
        }
        int oldChunkRadius = ClaimStore.chunkRadiusFor(existing.getRadius());
        int newChunkRadius = ClaimStore.chunkRadiusFor(radius);
        Claim updated = new Claim(existing.getOwner(), existing.getOwnerName(), existing.getCenterX(), existing.getCenterY(), existing.getCenterZ(), radius, existing.getTrusted(), existing.isPvpEnabled(), existing.getDeityItemId());
        this.claimsByCenter.put(ClaimStore.centerKey(centerX, centerZ), updated);
        for (int i = 0; i < this.claims.size(); ++i) {
            if (this.claims.get(i) != existing) continue;
            this.claims.set(i, updated);
            break;
        }
        if ((bucket = this.claimsByChunk.get(ClaimStore.chunkKey(ClaimStore.floorDiv(existing.getCenterX(), 16), ClaimStore.floorDiv(existing.getCenterZ(), 16)))) != null) {
            for (int i = 0; i < bucket.size(); ++i) {
                if (bucket.get(i) != existing) continue;
                bucket.set(i, updated);
                break;
            }
        }
        if (newChunkRadius > this.maxChunkRadius) {
            this.maxChunkRadius = newChunkRadius;
        } else if (oldChunkRadius >= this.maxChunkRadius) {
            this.recomputeMaxChunkRadius();
        }
        this.reconcileClaimTerritory(centerX, centerZ);
        if (radius < existing.getRadius()) {
            this.territoryReconcilePending = true;
        }
        this.dirty = true;
        return true;
    }

    public synchronized boolean addClaim(Claim claim) {
        if (this.claimsByCenter.containsKey(ClaimStore.centerKey(claim.getCenterX(), claim.getCenterZ()))) {
            return false;
        }
        this.claims.add(claim);
        this.claimsByCenter.put(ClaimStore.centerKey(claim.getCenterX(), claim.getCenterZ()), claim);
        long bucketKey = ClaimStore.chunkKey(ClaimStore.floorDiv(claim.getCenterX(), 16), ClaimStore.floorDiv(claim.getCenterZ(), 16));
        this.claimsByChunk.computeIfAbsent(bucketKey, k -> new ArrayList<Claim>()).add(claim);
        int claimChunkRadius = ClaimStore.chunkRadiusFor(claim.getRadius());
        if (claimChunkRadius > this.maxChunkRadius) {
            this.maxChunkRadius = claimChunkRadius;
        }
        this.reconcileClaimTerritory(claim.getCenterX(), claim.getCenterZ());
        this.dirty = true;
        return true;
    }

    public synchronized boolean removeClaimAt(int x, int z) {
        long bucketKey;
        List<Claim> bucket;
        long cKey = ClaimStore.centerKey(x, z);
        Claim claim = this.claimsByCenter.remove(cKey);
        if (claim == null) {
            return false;
        }
        int removedChunkRadius = ClaimStore.chunkRadiusFor(claim.getRadius());
        for (int i = 0; i < this.claims.size(); ++i) {
            Claim c = this.claims.get(i);
            if (c != claim) continue;
            this.claims.remove(i);
            break;
        }
        if ((bucket = this.claimsByChunk.get(bucketKey = ClaimStore.chunkKey(ClaimStore.floorDiv(claim.getCenterX(), 16), ClaimStore.floorDiv(claim.getCenterZ(), 16)))) != null) {
            bucket.remove(claim);
            if (bucket.isEmpty()) {
                this.claimsByChunk.remove(bucketKey);
            }
        }
        if (removedChunkRadius >= this.maxChunkRadius) {
            this.recomputeMaxChunkRadius();
        }
        this.clearCellsForClaim(cKey);
        this.territoryReconcilePending = true;
        this.dirty = true;
        return true;
    }

    public synchronized void load() {
        this.claims.clear();
        this.claimsByChunk.clear();
        this.claimsByCenter.clear();
        this.claimByCell.clear();
        this.cellsByClaim.clear();
        this.maxChunkRadius = 0;
        this.dirty = false;
        this.territoryReconcilePending = false;
        if (!Files.exists(this.file, new LinkOption[0])) {
            return;
        }
        try {
            int start;
            String json = Files.readString(this.file, StandardCharsets.UTF_8).trim();
            if (json.isEmpty()) {
                return;
            }
            if (!json.startsWith("[") || !json.endsWith("]")) {
                this.logger.at(Level.WARNING).log("DeityLandProtection claims.json invalid format, ignoring");
                return;
            }
            String body = json.substring(1, json.length() - 1).trim();
            if (body.isEmpty()) {
                return;
            }
            int idx = 0;
            while (idx < body.length() && (start = body.indexOf(123, idx)) >= 0) {
                int depth = 0;
                int end = -1;
                for (int i = start; i < body.length(); ++i) {
                    char ch = body.charAt(i);
                    if (ch == '{') {
                        ++depth;
                        continue;
                    }
                    if (ch != '}' || --depth != 0) continue;
                    end = i;
                    break;
                }
                if (end >= 0) {
                    String obj = body.substring(start + 1, end);
                    Claim claim = this.parseClaim(obj);
                    if (claim != null) {
                        long claimKey = ClaimStore.centerKey(claim.getCenterX(), claim.getCenterZ());
                        this.claims.add(claim);
                        this.claimsByCenter.put(claimKey, claim);
                        long bucketKey = ClaimStore.chunkKey(ClaimStore.floorDiv(claim.getCenterX(), 16), ClaimStore.floorDiv(claim.getCenterZ(), 16));
                        this.claimsByChunk.computeIfAbsent(bucketKey, k -> new ArrayList<Claim>()).add(claim);
                        int claimChunkRadius = ClaimStore.chunkRadiusFor(claim.getRadius());
                        if (claimChunkRadius > this.maxChunkRadius) {
                            this.maxChunkRadius = claimChunkRadius;
                        }
                        String encodedRows = ClaimStore.readJsonString(obj, TERRITORY_ROWS_KEY);
                        if (encodedRows != null && !encodedRows.isEmpty()) {
                            this.decodeTerritoryRows(claimKey, claim, encodedRows);
                        } else {
                            this.claimSquareUnclaimed(claimKey, claim.getCenterX(), claim.getCenterZ(), claim.getRadius());
                        }
                    }
                    idx = end + 1;
                    continue;
                }
                break;
            }
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to load claims.json");
        }
    }

    private Claim parseClaim(String obj) {
        try {
            String ownerStr = ClaimStore.readJsonString(obj, "owner");
            String ownerName = ClaimStore.readJsonString(obj, "ownerName");
            Integer x = ClaimStore.readJsonInt(obj, "x");
            Integer y = ClaimStore.readJsonInt(obj, "y");
            Integer z = ClaimStore.readJsonInt(obj, "z");
            Integer r = ClaimStore.readJsonInt(obj, "r");
            Boolean pvp = ClaimStore.readJsonBoolean(obj, "pvp");
            String itemId = ClaimStore.readJsonString(obj, "itemId");
            if (ownerStr == null || x == null || z == null || r == null) {
                return null;
            }
            UUID owner = UUID.fromString(ownerStr);
            Map<UUID, Integer> trusted = ClaimStore.readTrusted(obj);
            int cy = y == null ? Integer.MIN_VALUE : y;
            boolean pvpEnabled = pvp != null && Boolean.TRUE.equals(pvp);
            return new Claim(owner, ownerName, x, cy, z, r, trusted, pvpEnabled, itemId);
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private static Boolean readJsonBoolean(String obj, String key) {
        int i;
        String pattern = "\"" + key + "\"";
        int k = obj.indexOf(pattern);
        if (k < 0) {
            return null;
        }
        int colon = obj.indexOf(58, k + pattern.length());
        if (colon < 0) {
            return null;
        }
        for (i = colon + 1; i < obj.length() && Character.isWhitespace(obj.charAt(i)); ++i) {
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

    private static Map<UUID, Integer> readTrusted(String obj) {
        int q2;
        int q1;
        String key = "\"trusted\"";
        int k = obj.indexOf(key);
        if (k < 0) {
            return Collections.emptyMap();
        }
        int colon = obj.indexOf(58, k + key.length());
        if (colon < 0) {
            return Collections.emptyMap();
        }
        int start = obj.indexOf(123, colon + 1);
        if (start < 0) {
            return Collections.emptyMap();
        }
        int depth = 0;
        int end = -1;
        for (int i = start; i < obj.length(); ++i) {
            char ch = obj.charAt(i);
            if (ch == '{') {
                ++depth;
                continue;
            }
            if (ch != '}' || --depth != 0) continue;
            end = i;
            break;
        }
        if (end < 0) {
            return Collections.emptyMap();
        }
        String body = obj.substring(start + 1, end).trim();
        if (body.isEmpty()) {
            return Collections.emptyMap();
        }
        HashMap<UUID, Integer> map = new HashMap<UUID, Integer>();
        int idx = 0;
        while (idx < body.length() && (q1 = body.indexOf(34, idx)) >= 0 && (q2 = body.indexOf(34, q1 + 1)) >= 0) {
            int j;
            int i;
            String uuidStr = body.substring(q1 + 1, q2);
            int c = body.indexOf(58, q2 + 1);
            if (c < 0) break;
            for (i = c + 1; i < body.length() && Character.isWhitespace(body.charAt(i)); ++i) {
            }
            for (j = i; j < body.length() && (body.charAt(j) == '-' || Character.isDigit(body.charAt(j))); ++j) {
            }
            if (j == i) break;
            try {
                UUID u = UUID.fromString(uuidStr);
                int p = Integer.parseInt(body.substring(i, j));
                if (p != 0) {
                    map.put(u, p);
                }
            }
            catch (IllegalArgumentException u) {
                // empty catch block
            }
            int comma = body.indexOf(44, j);
            if (comma < 0) break;
            idx = comma + 1;
        }
        return map.isEmpty() ? Collections.emptyMap() : map;
    }

    private static String readJsonString(String obj, String key) {
        String pattern = "\"" + key + "\"";
        int k = obj.indexOf(pattern);
        if (k < 0) {
            return null;
        }
        int colon = obj.indexOf(58, k + pattern.length());
        if (colon < 0) {
            return null;
        }
        int firstQuote = obj.indexOf(34, colon + 1);
        if (firstQuote < 0) {
            return null;
        }
        int secondQuote = obj.indexOf(34, firstQuote + 1);
        if (secondQuote < 0) {
            return null;
        }
        return obj.substring(firstQuote + 1, secondQuote);
    }

    private static Integer readJsonInt(String obj, String key) {
        int j;
        int i;
        String pattern = "\"" + key + "\"";
        int k = obj.indexOf(pattern);
        if (k < 0) {
            return null;
        }
        int colon = obj.indexOf(58, k + pattern.length());
        if (colon < 0) {
            return null;
        }
        for (i = colon + 1; i < obj.length() && Character.isWhitespace(obj.charAt(i)); ++i) {
        }
        for (j = i; j < obj.length() && (obj.charAt(j) == '-' || Character.isDigit(obj.charAt(j))); ++j) {
        }
        if (j == i) {
            return null;
        }
        int sign = 1;
        int idx = i;
        if (obj.charAt(idx) == '-') {
            sign = -1;
            if (++idx >= j) {
                return null;
            }
        }
        int value = 0;
        while (idx < j) {
            char ch = obj.charAt(idx);
            if (!Character.isDigit(ch)) {
                return null;
            }
            value = value * 10 + (ch - 48);
            ++idx;
        }
        return sign * value;
    }

    public synchronized void flushIfDirty() {
        if (!this.dirty) {
            return;
        }
        this.saveNow();
    }

    public synchronized void saveNow() {
        try {
            Files.createDirectories(this.file.getParent(), new FileAttribute[0]);
            this.logger.at(Level.INFO).log("DeityLandProtection saving claims file=" + String.valueOf(this.file.toAbsolutePath().normalize()) + " count=" + this.claims.size());
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < this.claims.size(); ++i) {
                Claim c = this.claims.get(i);
                if (i > 0) {
                    sb.append(',');
                }
                sb.append('{');
                sb.append("\"owner\":\"").append(c.getOwner()).append("\"");
                if (c.getOwnerName() != null && !c.getOwnerName().isEmpty()) {
                    sb.append(',');
                    sb.append("\"ownerName\":\"").append(c.getOwnerName()).append("\"");
                }
                sb.append(',');
                sb.append("\"x\":").append(c.getCenterX());
                if (c.getCenterY() != Integer.MIN_VALUE) {
                    sb.append(',');
                    sb.append("\"y\":").append(c.getCenterY());
                }
                sb.append(',');
                sb.append("\"z\":").append(c.getCenterZ());
                sb.append(',');
                sb.append("\"r\":").append(c.getRadius());
                sb.append(',');
                sb.append("\"pvp\":").append(c.isPvpEnabled());
                String itemId = c.getDeityItemId();
                if (itemId != null && !itemId.isEmpty()) {
                    sb.append(',');
                    sb.append("\"itemId\":\"").append(itemId).append("\"");
                }
                Map<UUID, Integer> trusted = c.getTrusted();
                if (!trusted.isEmpty()) {
                    sb.append(',');
                    sb.append("\"trusted\":{");
                    int ti = 0;
                    for (Map.Entry<UUID, Integer> e : trusted.entrySet()) {
                        int p;
                        if (e.getKey() == null || e.getValue() == null || (p = e.getValue().intValue()) == 0) continue;
                        if (ti > 0) {
                            sb.append(',');
                        }
                        sb.append('\"').append(e.getKey()).append('\"').append(':').append(p);
                        ++ti;
                    }
                    sb.append('}');
                }
                String territoryRows = this.encodeTerritoryRows(ClaimStore.centerKey(c.getCenterX(), c.getCenterZ()));
                if (!territoryRows.isEmpty()) {
                    sb.append(',');
                    sb.append('"').append(TERRITORY_ROWS_KEY).append('"').append(':').append('"').append(territoryRows).append('"');
                }
                sb.append('}');
            }
            sb.append(']');
            Files.writeString(this.file, (CharSequence)sb.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
            this.dirty = false;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause((Throwable)e)).log("DeityLandProtection failed to save claims.json");
        }
    }

    private static long centerKey(int x, int z) {
        return (long)x << 32 ^ (long)z & 0xFFFFFFFFL;
    }

    private static long cellKey(int x, int z) {
        return (long)x << 32 ^ (long)z & 0xFFFFFFFFL;
    }

    private static int xOfCellKey(long key) {
        return (int)(key >> 32);
    }

    private static int zOfCellKey(long key) {
        return (int)key;
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return (long)chunkX << 32 ^ (long)chunkZ & 0xFFFFFFFFL;
    }

    private static int floorDiv(int x, int d) {
        int r = x / d;
        if ((x ^ d) < 0 && r * d != x) {
            --r;
        }
        return r;
    }

    private static int chunkRadiusFor(int radiusBlocks) {
        if (radiusBlocks <= 0) {
            return 0;
        }
        int chunks = (radiusBlocks + 15) / 16;
        return Math.max(0, chunks);
    }

    private void assignCellToClaim(long claimKey, long cellKey) {
        this.claimByCell.put(cellKey, claimKey);
        this.cellsByClaim.computeIfAbsent(claimKey, k -> new HashSet<Long>()).add(cellKey);
    }

    private boolean unassignCellFromClaim(long claimKey, long cellKey) {
        Long owner = this.claimByCell.get(cellKey);
        if (owner == null || owner.longValue() != claimKey) {
            return false;
        }
        this.claimByCell.remove(cellKey);
        HashSet<Long> owned = this.cellsByClaim.get(claimKey);
        if (owned != null) {
            owned.remove(cellKey);
            if (owned.isEmpty()) {
                this.cellsByClaim.remove(claimKey);
            }
        }
        return true;
    }

    private void clearCellsForClaim(long claimKey) {
        HashSet<Long> owned = this.cellsByClaim.remove(claimKey);
        if (owned == null || owned.isEmpty()) {
            return;
        }
        for (Long cell : owned) {
            if (cell == null) continue;
            Long owner = this.claimByCell.get(cell.longValue());
            if (owner == null || owner.longValue() != claimKey) continue;
            this.claimByCell.remove(cell.longValue());
        }
    }

    private void claimSquareUnclaimed(long claimKey, int centerX, int centerZ, int radius) {
        if (radius <= 0) {
            return;
        }
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;
        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                long cell = ClaimStore.cellKey(x, z);
                if (this.claimByCell.containsKey(cell)) continue;
                this.assignCellToClaim(claimKey, cell);
            }
        }
    }

    private void decodeTerritoryRows(long claimKey, Claim claim, String encodedRows) {
        if (claim == null || encodedRows == null || encodedRows.isEmpty()) {
            return;
        }
        int radius = claim.getRadius();
        int minX = claim.getCenterX() - radius;
        int maxX = claim.getCenterX() + radius;
        int minZ = claim.getCenterZ() - radius;
        int maxZ = claim.getCenterZ() + radius;
        boolean parsedAnyRange = false;
        String[] rows = encodedRows.split(";");
        for (String row : rows) {
            if (row == null || row.isEmpty()) {
                continue;
            }
            int colon = row.indexOf(58);
            if (colon <= 0 || colon >= row.length() - 1) {
                continue;
            }
            int z;
            try {
                z = Integer.parseInt(row.substring(0, colon));
            }
            catch (NumberFormatException ignored) {
                continue;
            }
            String ranges = row.substring(colon + 1);
            if (ranges.isEmpty()) {
                continue;
            }
            String[] entries = ranges.split(",");
            for (String entry : entries) {
                if (entry == null || entry.isEmpty()) {
                    continue;
                }
                int separator = entry.indexOf(124);
                if (separator <= 0 || separator >= entry.length() - 1) {
                    separator = -1;
                    for (int i = 1; i < entry.length() - 1; ++i) {
                        if (entry.charAt(i) != '-' || !Character.isDigit(entry.charAt(i - 1))) continue;
                        separator = i;
                        break;
                    }
                }
                if (separator <= 0 || separator >= entry.length() - 1) {
                    continue;
                }
                int startX;
                int endX;
                try {
                    startX = Integer.parseInt(entry.substring(0, separator));
                    endX = Integer.parseInt(entry.substring(separator + 1));
                }
                catch (NumberFormatException ignored) {
                    continue;
                }
                parsedAnyRange = true;
                if (startX > endX) {
                    int t = startX;
                    startX = endX;
                    endX = t;
                }
                if (z < minZ || z > maxZ) {
                    continue;
                }
                if (endX < minX || startX > maxX) {
                    continue;
                }
                int clampedStart = Math.max(minX, startX);
                int clampedEnd = Math.min(maxX, endX);
                for (int x = clampedStart; x <= clampedEnd; ++x) {
                    long cell = ClaimStore.cellKey(x, z);
                    Long owner = this.claimByCell.get(cell);
                    if (owner != null && owner.longValue() != claimKey) continue;
                    if (owner != null) continue;
                    this.assignCellToClaim(claimKey, cell);
                }
            }
        }
        if (!parsedAnyRange) {
            this.claimSquareUnclaimed(claimKey, claim.getCenterX(), claim.getCenterZ(), claim.getRadius());
        }
    }

    private String encodeTerritoryRows(long claimKey) {
        HashSet<Long> owned = this.cellsByClaim.get(claimKey);
        if (owned == null || owned.isEmpty()) {
            return "";
        }
        TreeMap<Integer, ArrayList<Integer>> xByZ = new TreeMap<Integer, ArrayList<Integer>>();
        for (Long cell : owned) {
            if (cell == null) {
                continue;
            }
            int x = ClaimStore.xOfCellKey(cell.longValue());
            int z = ClaimStore.zOfCellKey(cell.longValue());
            xByZ.computeIfAbsent(z, k -> new ArrayList<Integer>()).add(x);
        }
        StringBuilder sb = new StringBuilder();
        int rowCount = 0;
        for (Map.Entry<Integer, ArrayList<Integer>> entry : xByZ.entrySet()) {
            ArrayList<Integer> xs = entry.getValue();
            if (xs == null || xs.isEmpty()) {
                continue;
            }
            Collections.sort(xs);
            if (rowCount > 0) {
                sb.append(';');
            }
            sb.append(entry.getKey()).append(':');
            boolean firstRange = true;
            int startX = xs.get(0);
            int prevX = startX;
            for (int i = 1; i < xs.size(); ++i) {
                int x = xs.get(i);
                if (x <= prevX + 1) {
                    if (x > prevX) {
                        prevX = x;
                    }
                    continue;
                }
                if (!firstRange) {
                    sb.append(',');
                }
                sb.append(startX).append('|').append(prevX);
                firstRange = false;
                startX = x;
                prevX = x;
            }
            if (!firstRange) {
                sb.append(',');
            }
            sb.append(startX).append('|').append(prevX);
            ++rowCount;
        }
        return sb.toString();
    }

    private void recomputeMaxChunkRadius() {
        int max = 0;
        for (Claim c : this.claims) {
            int cr = ClaimStore.chunkRadiusFor(c.getRadius());
            if (cr <= max) continue;
            max = cr;
        }
        this.maxChunkRadius = max;
    }
}



