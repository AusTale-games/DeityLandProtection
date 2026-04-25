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
import java.util.function.UnaryOperator;
import java.util.logging.Level;

public final class ClaimStore {
    private static final int CHUNK_SIZE = 16;
    private static final String TERRITORY_ROWS_KEY = "rows";

    private final Path file;
    private final HytaleLogger logger;
    private final List<Claim> claims = new ArrayList<>();
    private final Map<Long, List<Claim>> claimsByChunk = new HashMap<>();
    private final Map<Long, Claim> claimsByCenter = new HashMap<>();
    private final Map<Long, Long> claimByCell = new HashMap<>();
    private final Map<Long, HashSet<Long>> cellsByClaim = new HashMap<>();
    private int maxChunkRadius;
    private boolean dirty;
    private boolean territoryReconcilePending;

    public ClaimStore(Path file, HytaleLogger logger) {
        this.file = file;
        this.logger = logger;
    }

    public synchronized List<Claim> getClaims() {
        return Collections.unmodifiableList(new ArrayList<>(this.claims));
    }

    public synchronized int countClaimsForOwner(UUID owner) {
        if (owner == null) {
            return 0;
        }
        int count = 0;
        for (Claim c : this.claims) {
            if (owner.equals(c.getOwner())) {
                count++;
            }
        }
        return count;
    }

    public synchronized Claim findClaimAt(int x, int z) {
        Long claimKey = this.claimByCell.get(cellKey(x, z));
        return claimKey == null ? null : this.claimsByCenter.get(claimKey);
    }

    public synchronized List<Claim> getClaimsNearArea(int minX, int minZ, int maxX, int maxZ) {
        int minChunkX = floorDiv(minX, CHUNK_SIZE);
        int maxChunkX = floorDiv(maxX, CHUNK_SIZE);
        int minChunkZ = floorDiv(minZ, CHUNK_SIZE);
        int maxChunkZ = floorDiv(maxZ, CHUNK_SIZE);
        int range = Math.max(0, this.maxChunkRadius);
        HashSet<Claim> unique = new HashSet<>();
        for (int baseX = minChunkX; baseX <= maxChunkX; baseX++) {
            for (int baseZ = minChunkZ; baseZ <= maxChunkZ; baseZ++) {
                for (int dx = -range; dx <= range; dx++) {
                    for (int dz = -range; dz <= range; dz++) {
                        List<Claim> bucket = this.claimsByChunk.get(chunkKey(baseX + dx, baseZ + dz));
                        if (bucket != null) {
                            unique.addAll(bucket);
                        }
                    }
                }
            }
        }
        return unique.isEmpty() ? Collections.emptyList() : new ArrayList<>(unique);
    }

    public synchronized Claim findClaimByCenter(int centerX, int centerZ) {
        return this.claimsByCenter.get(centerKey(centerX, centerZ));
    }

    public synchronized int[] getClaimCellBounds(int centerX, int centerZ) {
        long claimKey = centerKey(centerX, centerZ);
        Claim claim = this.claimsByCenter.get(claimKey);
        if (claim == null) {
            return null;
        }
        HashSet<Long> owned = this.cellsByClaim.get(claimKey);
        if (owned == null || owned.isEmpty()) {
            return radiusBounds(centerX, centerZ, claim.getRadius());
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (Long cell : owned) {
            if (cell == null) continue;
            int x = xOfCellKey(cell);
            int z = zOfCellKey(cell);
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }
        if (minX == Integer.MAX_VALUE) {
            return radiusBounds(centerX, centerZ, claim.getRadius());
        }
        return new int[]{minX, maxX, minZ, maxZ};
    }

    private static int[] radiusBounds(int centerX, int centerZ, int r) {
        return new int[]{centerX - r, centerX + r, centerZ - r, centerZ + r};
    }

    public synchronized boolean updateClaimCenterYIfUnknown(int centerX, int centerZ, int centerY) {
        return replaceClaim(centerX, centerZ, existing -> {
            if (existing.getCenterY() != Integer.MIN_VALUE) {
                return null;
            }
            return new Claim(existing.getOwner(), existing.getOwnerName(),
                    existing.getCenterX(), centerY, existing.getCenterZ(),
                    existing.getRadius(), existing.getTrusted(),
                    existing.isPvpEnabled(), existing.getDeityItemId());
        });
    }

    public synchronized boolean updateClaimPvpEnabled(int centerX, int centerZ, boolean pvpEnabled) {
        return replaceClaim(centerX, centerZ, existing -> {
            if (existing.isPvpEnabled() == pvpEnabled) {
                return null;
            }
            return new Claim(existing.getOwner(), existing.getOwnerName(),
                    existing.getCenterX(), existing.getCenterY(), existing.getCenterZ(),
                    existing.getRadius(), existing.getTrusted(),
                    pvpEnabled, existing.getDeityItemId());
        });
    }

    public synchronized boolean updateClaimRadius(int centerX, int centerZ, int radius) {
        if (radius <= 0) {
            return false;
        }
        Claim existing = this.claimsByCenter.get(centerKey(centerX, centerZ));
        if (existing == null || existing.getRadius() == radius) {
            return false;
        }
        int oldChunkRadius = chunkRadiusFor(existing.getRadius());
        int newChunkRadius = chunkRadiusFor(radius);
        int oldRadius = existing.getRadius();
        Claim updated = new Claim(existing.getOwner(), existing.getOwnerName(),
                existing.getCenterX(), existing.getCenterY(), existing.getCenterZ(),
                radius, existing.getTrusted(),
                existing.isPvpEnabled(), existing.getDeityItemId());
        swapClaim(existing, updated);
        if (newChunkRadius > this.maxChunkRadius) {
            this.maxChunkRadius = newChunkRadius;
        } else if (oldChunkRadius >= this.maxChunkRadius) {
            this.recomputeMaxChunkRadius();
        }
        this.reconcileClaimTerritory(centerX, centerZ);
        if (radius < oldRadius) {
            this.territoryReconcilePending = true;
        }
        this.dirty = true;
        return true;
    }

    private boolean replaceClaim(int centerX, int centerZ, UnaryOperator<Claim> mutator) {
        Claim existing = this.claimsByCenter.get(centerKey(centerX, centerZ));
        if (existing == null) {
            return false;
        }
        Claim updated = mutator.apply(existing);
        if (updated == null) {
            return false;
        }
        swapClaim(existing, updated);
        this.dirty = true;
        return true;
    }

    private void swapClaim(Claim existing, Claim updated) {
        this.claimsByCenter.put(centerKey(updated.getCenterX(), updated.getCenterZ()), updated);
        replaceInList(this.claims, existing, updated);
        List<Claim> bucket = this.claimsByChunk.get(bucketKey(existing));
        if (bucket != null) {
            replaceInList(bucket, existing, updated);
        }
    }

    private static void replaceInList(List<Claim> list, Claim oldClaim, Claim newClaim) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == oldClaim) {
                list.set(i, newClaim);
                return;
            }
        }
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
        if (radius <= 0 || !this.claimsByCenter.containsKey(centerKey(centerX, centerZ))) {
            return false;
        }
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                if (!this.claimByCell.containsKey(cellKey(x, z))) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean reconcileClaimTerritory(int centerX, int centerZ) {
        long claimKey = centerKey(centerX, centerZ);
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
        boolean changed = releaseCellsOutside(claimKey, minX, maxX, minZ, maxZ);
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                long cellKey = cellKey(x, z);
                if (!this.claimByCell.containsKey(cellKey)) {
                    this.assignCellToClaim(claimKey, cellKey);
                    changed = true;
                }
            }
        }
        if (changed) {
            this.dirty = true;
        }
        return changed;
    }

    private boolean releaseCellsOutside(long claimKey, int minX, int maxX, int minZ, int maxZ) {
        HashSet<Long> owned = this.cellsByClaim.get(claimKey);
        if (owned == null || owned.isEmpty()) {
            return false;
        }
        ArrayList<Long> toRelease = new ArrayList<>();
        for (Long cell : owned) {
            if (cell == null) continue;
            int x = xOfCellKey(cell);
            int z = zOfCellKey(cell);
            if (x < minX || x > maxX || z < minZ || z > maxZ) {
                toRelease.add(cell);
            }
        }
        boolean changed = false;
        for (Long cell : toRelease) {
            if (cell != null && this.unassignCellFromClaim(claimKey, cell)) {
                changed = true;
            }
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
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                if (this.claimByCell.containsKey(cellKey(x, z))) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean intersectsAnyExceptOwner(UUID owner, int centerX, int centerZ, int radius) {
        if (radius <= 0) {
            return false;
        }
        long selfKey = centerKey(centerX, centerZ);
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                Long claimKey = this.claimByCell.get(cellKey(x, z));
                if (claimKey == null || claimKey == selfKey) continue;
                Claim other = this.claimsByCenter.get(claimKey);
                if (other == null) continue;
                if (owner != null && owner.equals(other.getOwner())) continue;
                return true;
            }
        }
        return false;
    }

    public synchronized boolean addClaim(Claim claim) {
        long cKey = centerKey(claim.getCenterX(), claim.getCenterZ());
        if (this.claimsByCenter.containsKey(cKey)) {
            return false;
        }
        this.claims.add(claim);
        this.claimsByCenter.put(cKey, claim);
        this.claimsByChunk.computeIfAbsent(bucketKey(claim), k -> new ArrayList<>()).add(claim);
        int claimChunkRadius = chunkRadiusFor(claim.getRadius());
        if (claimChunkRadius > this.maxChunkRadius) {
            this.maxChunkRadius = claimChunkRadius;
        }
        this.reconcileClaimTerritory(claim.getCenterX(), claim.getCenterZ());
        this.dirty = true;
        return true;
    }

    public synchronized boolean removeClaimAt(int x, int z) {
        long cKey = centerKey(x, z);
        Claim claim = this.claimsByCenter.remove(cKey);
        if (claim == null) {
            return false;
        }
        int removedChunkRadius = chunkRadiusFor(claim.getRadius());
        this.claims.remove(claim);
        long bucketKey = bucketKey(claim);
        List<Claim> bucket = this.claimsByChunk.get(bucketKey);
        if (bucket != null) {
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
            loadClaimsFromBody(body);
        } catch (IOException e) {
            ((HytaleLogger.Api) this.logger.at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to load claims.json");
        }
    }

    private void loadClaimsFromBody(String body) {
        int idx = 0;
        while (idx < body.length()) {
            int start = body.indexOf('{', idx);
            if (start < 0) break;
            int end = findMatchingBrace(body, start);
            if (end < 0) break;
            String obj = body.substring(start + 1, end);
            Claim claim = this.parseClaim(obj);
            if (claim != null) {
                registerLoadedClaim(claim, obj);
            }
            idx = end + 1;
        }
    }

    private void registerLoadedClaim(Claim claim, String rawObject) {
        long claimKey = centerKey(claim.getCenterX(), claim.getCenterZ());
        this.claims.add(claim);
        this.claimsByCenter.put(claimKey, claim);
        this.claimsByChunk.computeIfAbsent(bucketKey(claim), k -> new ArrayList<>()).add(claim);
        int claimChunkRadius = chunkRadiusFor(claim.getRadius());
        if (claimChunkRadius > this.maxChunkRadius) {
            this.maxChunkRadius = claimChunkRadius;
        }
        String encodedRows = readJsonString(rawObject, TERRITORY_ROWS_KEY);
        if (encodedRows != null && !encodedRows.isEmpty()) {
            this.decodeTerritoryRows(claimKey, claim, encodedRows);
        } else {
            this.claimSquareUnclaimed(claimKey, claim.getCenterX(), claim.getCenterZ(), claim.getRadius());
        }
    }

    private static int findMatchingBrace(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '{') {
                depth++;
            } else if (ch == '}' && --depth == 0) {
                return i;
            }
        }
        return -1;
    }

    private Claim parseClaim(String obj) {
        try {
            String ownerStr = readJsonString(obj, "owner");
            Integer x = readJsonInt(obj, "x");
            Integer z = readJsonInt(obj, "z");
            Integer r = readJsonInt(obj, "r");
            if (ownerStr == null || x == null || z == null || r == null) {
                return null;
            }
            String ownerName = readJsonString(obj, "ownerName");
            Integer y = readJsonInt(obj, "y");
            Boolean pvp = readJsonBoolean(obj, "pvp");
            String itemId = readJsonString(obj, "itemId");
            UUID owner = UUID.fromString(ownerStr);
            Map<UUID, Integer> trusted = readTrusted(obj);
            int cy = y == null ? Integer.MIN_VALUE : y;
            boolean pvpEnabled = Boolean.TRUE.equals(pvp);
            return new Claim(owner, ownerName, x, cy, z, r, trusted, pvpEnabled, itemId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int findColonAfter(String obj, String key) {
        String pattern = "\"" + key + "\"";
        int k = obj.indexOf(pattern);
        if (k < 0) {
            return -1;
        }
        return obj.indexOf(':', k + pattern.length());
    }

    private static int skipWhitespace(String s, int from) {
        int i = from;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static Boolean readJsonBoolean(String obj, String key) {
        int colon = findColonAfter(obj, key);
        if (colon < 0) return null;
        int i = skipWhitespace(obj, colon + 1);
        if (i >= obj.length()) return null;
        if (obj.startsWith("true", i)) return Boolean.TRUE;
        if (obj.startsWith("false", i)) return Boolean.FALSE;
        return null;
    }

    private static String readJsonString(String obj, String key) {
        int colon = findColonAfter(obj, key);
        if (colon < 0) return null;
        int firstQuote = obj.indexOf('"', colon + 1);
        if (firstQuote < 0) return null;
        int secondQuote = obj.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) return null;
        return obj.substring(firstQuote + 1, secondQuote);
    }

    private static Integer readJsonInt(String obj, String key) {
        int colon = findColonAfter(obj, key);
        if (colon < 0) return null;
        int i = skipWhitespace(obj, colon + 1);
        int j = i;
        while (j < obj.length() && (obj.charAt(j) == '-' || Character.isDigit(obj.charAt(j)))) {
            j++;
        }
        if (j == i) return null;
        try {
            return Integer.parseInt(obj.substring(i, j));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Map<UUID, Integer> readTrusted(String obj) {
        String key = "\"trusted\"";
        int k = obj.indexOf(key);
        if (k < 0) return Collections.emptyMap();
        int colon = obj.indexOf(':', k + key.length());
        if (colon < 0) return Collections.emptyMap();
        int start = obj.indexOf('{', colon + 1);
        if (start < 0) return Collections.emptyMap();
        int end = findMatchingBrace(obj, start);
        if (end < 0) return Collections.emptyMap();
        String body = obj.substring(start + 1, end).trim();
        if (body.isEmpty()) return Collections.emptyMap();
        HashMap<UUID, Integer> map = new HashMap<>();
        int idx = 0;
        while (idx < body.length()) {
            int q1 = body.indexOf('"', idx);
            if (q1 < 0) break;
            int q2 = body.indexOf('"', q1 + 1);
            if (q2 < 0) break;
            String uuidStr = body.substring(q1 + 1, q2);
            int c = body.indexOf(':', q2 + 1);
            if (c < 0) break;
            int i = skipWhitespace(body, c + 1);
            int j = i;
            while (j < body.length() && (body.charAt(j) == '-' || Character.isDigit(body.charAt(j)))) {
                j++;
            }
            if (j == i) break;
            try {
                UUID u = UUID.fromString(uuidStr);
                int p = Integer.parseInt(body.substring(i, j));
                if (p != 0) {
                    map.put(u, p);
                }
            } catch (IllegalArgumentException ignored) {
            }
            int comma = body.indexOf(',', j);
            if (comma < 0) break;
            idx = comma + 1;
        }
        return map.isEmpty() ? Collections.emptyMap() : map;
    }

    public synchronized void flushIfDirty() {
        if (this.dirty) {
            this.saveNow();
        }
    }

    public synchronized void saveNow() {
        try {
            Files.createDirectories(this.file.getParent(), new FileAttribute[0]);
            this.logger.at(Level.INFO).log("DeityLandProtection saving claims file="
                    + this.file.toAbsolutePath().normalize() + " count=" + this.claims.size());
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < this.claims.size(); i++) {
                if (i > 0) sb.append(',');
                writeClaim(sb, this.claims.get(i));
            }
            sb.append(']');
            Files.writeString(this.file, sb, StandardCharsets.UTF_8, new OpenOption[0]);
            this.dirty = false;
        } catch (IOException e) {
            ((HytaleLogger.Api) this.logger.at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to save claims.json");
        }
    }

    private void writeClaim(StringBuilder sb, Claim c) {
        sb.append('{');
        sb.append("\"owner\":\"").append(c.getOwner()).append('"');
        if (c.getOwnerName() != null && !c.getOwnerName().isEmpty()) {
            sb.append(",\"ownerName\":\"").append(c.getOwnerName()).append('"');
        }
        sb.append(",\"x\":").append(c.getCenterX());
        if (c.getCenterY() != Integer.MIN_VALUE) {
            sb.append(",\"y\":").append(c.getCenterY());
        }
        sb.append(",\"z\":").append(c.getCenterZ());
        sb.append(",\"r\":").append(c.getRadius());
        sb.append(",\"pvp\":").append(c.isPvpEnabled());
        String itemId = c.getDeityItemId();
        if (itemId != null && !itemId.isEmpty()) {
            sb.append(",\"itemId\":\"").append(itemId).append('"');
        }
        Map<UUID, Integer> trusted = c.getTrusted();
        if (!trusted.isEmpty()) {
            sb.append(",\"trusted\":{");
            int ti = 0;
            for (Map.Entry<UUID, Integer> e : trusted.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                int p = e.getValue();
                if (p == 0) continue;
                if (ti > 0) sb.append(',');
                sb.append('"').append(e.getKey()).append("\":").append(p);
                ti++;
            }
            sb.append('}');
        }
        String territoryRows = this.encodeTerritoryRows(centerKey(c.getCenterX(), c.getCenterZ()));
        if (!territoryRows.isEmpty()) {
            sb.append(",\"").append(TERRITORY_ROWS_KEY).append("\":\"").append(territoryRows).append('"');
        }
        sb.append('}');
    }

    private static long centerKey(int x, int z) {
        return (long) x << 32 ^ (long) z & 0xFFFFFFFFL;
    }

    private static long cellKey(int x, int z) {
        return (long) x << 32 ^ (long) z & 0xFFFFFFFFL;
    }

    private static int xOfCellKey(long key) {
        return (int) (key >> 32);
    }

    private static int zOfCellKey(long key) {
        return (int) key;
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return (long) chunkX << 32 ^ (long) chunkZ & 0xFFFFFFFFL;
    }

    private static long bucketKey(Claim claim) {
        return chunkKey(floorDiv(claim.getCenterX(), CHUNK_SIZE), floorDiv(claim.getCenterZ(), CHUNK_SIZE));
    }

    private static int floorDiv(int x, int d) {
        int r = x / d;
        if ((x ^ d) < 0 && r * d != x) {
            r--;
        }
        return r;
    }

    private static int chunkRadiusFor(int radiusBlocks) {
        if (radiusBlocks <= 0) {
            return 0;
        }
        return Math.max(0, (radiusBlocks + CHUNK_SIZE - 1) / CHUNK_SIZE);
    }

    private void assignCellToClaim(long claimKey, long cellKey) {
        this.claimByCell.put(cellKey, claimKey);
        this.cellsByClaim.computeIfAbsent(claimKey, k -> new HashSet<>()).add(cellKey);
    }

    private boolean unassignCellFromClaim(long claimKey, long cellKey) {
        Long owner = this.claimByCell.get(cellKey);
        if (owner == null || owner != claimKey) {
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
            Long owner = this.claimByCell.get(cell);
            if (owner != null && owner == claimKey) {
                this.claimByCell.remove(cell);
            }
        }
    }

    private void claimSquareUnclaimed(long claimKey, int centerX, int centerZ, int radius) {
        if (radius <= 0) return;
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                long cell = cellKey(x, z);
                if (!this.claimByCell.containsKey(cell)) {
                    this.assignCellToClaim(claimKey, cell);
                }
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
        for (String row : encodedRows.split(";")) {
            if (row == null || row.isEmpty()) continue;
            int colon = row.indexOf(':');
            if (colon <= 0 || colon >= row.length() - 1) continue;
            Integer zParsed = tryParseInt(row.substring(0, colon));
            if (zParsed == null) continue;
            int z = zParsed;
            String ranges = row.substring(colon + 1);
            if (ranges.isEmpty()) continue;
            for (String entry : ranges.split(",")) {
                if (entry == null || entry.isEmpty()) continue;
                int[] range = parseRange(entry);
                if (range == null) continue;
                parsedAnyRange = true;
                int startX = Math.min(range[0], range[1]);
                int endX = Math.max(range[0], range[1]);
                if (z < minZ || z > maxZ) continue;
                if (endX < minX || startX > maxX) continue;
                int clampedStart = Math.max(minX, startX);
                int clampedEnd = Math.min(maxX, endX);
                for (int x = clampedStart; x <= clampedEnd; x++) {
                    long cell = cellKey(x, z);
                    if (!this.claimByCell.containsKey(cell)) {
                        this.assignCellToClaim(claimKey, cell);
                    }
                }
            }
        }
        if (!parsedAnyRange) {
            this.claimSquareUnclaimed(claimKey, claim.getCenterX(), claim.getCenterZ(), claim.getRadius());
        }
    }

    private static Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static int[] parseRange(String entry) {
        int separator = entry.indexOf('|');
        if (separator <= 0 || separator >= entry.length() - 1) {
            separator = -1;
            for (int i = 1; i < entry.length() - 1; i++) {
                if (entry.charAt(i) == '-' && Character.isDigit(entry.charAt(i - 1))) {
                    separator = i;
                    break;
                }
            }
        }
        if (separator <= 0 || separator >= entry.length() - 1) {
            return null;
        }
        Integer a = tryParseInt(entry.substring(0, separator));
        Integer b = tryParseInt(entry.substring(separator + 1));
        if (a == null || b == null) return null;
        return new int[]{a, b};
    }

    private String encodeTerritoryRows(long claimKey) {
        HashSet<Long> owned = this.cellsByClaim.get(claimKey);
        if (owned == null || owned.isEmpty()) {
            return "";
        }
        TreeMap<Integer, ArrayList<Integer>> xByZ = new TreeMap<>();
        for (Long cell : owned) {
            if (cell == null) continue;
            xByZ.computeIfAbsent(zOfCellKey(cell), k -> new ArrayList<>()).add(xOfCellKey(cell));
        }
        StringBuilder sb = new StringBuilder();
        int rowCount = 0;
        for (Map.Entry<Integer, ArrayList<Integer>> entry : xByZ.entrySet()) {
            ArrayList<Integer> xs = entry.getValue();
            if (xs == null || xs.isEmpty()) continue;
            Collections.sort(xs);
            if (rowCount > 0) sb.append(';');
            sb.append(entry.getKey()).append(':');
            appendXRanges(sb, xs);
            rowCount++;
        }
        return sb.toString();
    }

    private static void appendXRanges(StringBuilder sb, List<Integer> sortedXs) {
        int startX = sortedXs.get(0);
        int prevX = startX;
        boolean firstRange = true;
        for (int i = 1; i < sortedXs.size(); i++) {
            int x = sortedXs.get(i);
            if (x <= prevX + 1) {
                if (x > prevX) prevX = x;
                continue;
            }
            if (!firstRange) sb.append(',');
            sb.append(startX).append('|').append(prevX);
            firstRange = false;
            startX = x;
            prevX = x;
        }
        if (!firstRange) sb.append(',');
        sb.append(startX).append('|').append(prevX);
    }

    private void recomputeMaxChunkRadius() {
        int max = 0;
        for (Claim c : this.claims) {
            int cr = chunkRadiusFor(c.getRadius());
            if (cr > max) max = cr;
        }
        this.maxChunkRadius = max;
    }
}
