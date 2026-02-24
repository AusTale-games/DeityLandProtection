/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.logger.HytaleLogger
 *  com.hypixel.hytale.logger.HytaleLogger$Api
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
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
import java.util.UUID;
import java.util.logging.Level;

public final class ClaimStore {
    private final Path file;
    private final HytaleLogger logger;
    private final List<Claim> claims = new ArrayList<Claim>();
    private final Map<Long, List<Claim>> claimsByChunk = new HashMap<Long, List<Claim>>();
    private final Map<Long, Claim> claimsByCenter = new HashMap<Long, Claim>();
    private int maxChunkRadius;
    private boolean dirty;

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
        int chunkX = ClaimStore.floorDiv(x, 16);
        int chunkZ = ClaimStore.floorDiv(z, 16);
        Claim found = null;
        int range = Math.max(0, this.maxChunkRadius);
        for (int dx = -range; dx <= range; ++dx) {
            for (int dz = -range; dz <= range; ++dz) {
                List<Claim> bucket = this.claimsByChunk.get(ClaimStore.chunkKey(chunkX + dx, chunkZ + dz));
                if (bucket == null) continue;
                for (Claim c : bucket) {
                    if (!c.contains(x, z)) continue;
                    found = c;
                    break;
                }
                if (found != null) break;
            }
            if (found != null) break;
        }
        return found;
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
        return unique.isEmpty() ? Collections.emptyList() : new ArrayList(unique);
    }

    public synchronized Claim findClaimByCenter(int centerX, int centerZ) {
        return this.claimsByCenter.get(ClaimStore.centerKey(centerX, centerZ));
    }

    public synchronized boolean updateClaimCenterYIfUnknown(int centerX, int centerZ, int centerY) {
        long bucketKey;
        List<Claim> bucket;
        Claim existing = this.claimsByCenter.get(ClaimStore.centerKey(centerX, centerZ));
        if (existing == null) {
            return false;
        }
        if (existing.getCenterY() != Integer.MIN_VALUE) {
            return false;
        }
        Claim updated = new Claim(existing.getOwner(), existing.getOwnerName(), existing.getCenterX(), centerY, existing.getCenterZ(), existing.getRadius(), existing.getTrusted(), existing.isPvpEnabled());
        this.claimsByCenter.put(ClaimStore.centerKey(centerX, centerZ), updated);
        for (int i = 0; i < this.claims.size(); ++i) {
            if (this.claims.get(i) != existing) continue;
            this.claims.set(i, updated);
            break;
        }
        if ((bucket = this.claimsByChunk.get(bucketKey = ClaimStore.chunkKey(ClaimStore.floorDiv(centerX, 16), ClaimStore.floorDiv(centerZ, 16)))) != null) {
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
        long bucketKey;
        List<Claim> bucket;
        Claim existing = this.claimsByCenter.get(ClaimStore.centerKey(centerX, centerZ));
        if (existing == null) {
            return false;
        }
        if (existing.isPvpEnabled() == pvpEnabled) {
            return false;
        }
        Claim updated = new Claim(existing.getOwner(), existing.getOwnerName(), existing.getCenterX(), existing.getCenterY(), existing.getCenterZ(), existing.getRadius(), existing.getTrusted(), pvpEnabled);
        this.claimsByCenter.put(ClaimStore.centerKey(centerX, centerZ), updated);
        for (int i = 0; i < this.claims.size(); ++i) {
            if (this.claims.get(i) != existing) continue;
            this.claims.set(i, updated);
            break;
        }
        if ((bucket = this.claimsByChunk.get(bucketKey = ClaimStore.chunkKey(ClaimStore.floorDiv(centerX, 16), ClaimStore.floorDiv(centerZ, 16)))) != null) {
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

    public synchronized boolean intersectsAny(int centerX, int centerZ, int radius) {
        int chunkX = ClaimStore.floorDiv(centerX, 16);
        int chunkZ = ClaimStore.floorDiv(centerZ, 16);
        int range = Math.max(0, this.maxChunkRadius + ClaimStore.chunkRadiusFor(radius));
        for (int dx = -range; dx <= range; ++dx) {
            for (int dz = -range; dz <= range; ++dz) {
                List<Claim> bucket = this.claimsByChunk.get(ClaimStore.chunkKey(chunkX + dx, chunkZ + dz));
                if (bucket == null) continue;
                for (Claim c : bucket) {
                    if (!ClaimStore.intersects(c, centerX, centerZ, radius)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean intersectsAnyExceptOwner(UUID owner, int centerX, int centerZ, int radius) {
        int chunkX = ClaimStore.floorDiv(centerX, 16);
        int chunkZ = ClaimStore.floorDiv(centerZ, 16);
        int range = Math.max(0, this.maxChunkRadius + ClaimStore.chunkRadiusFor(radius));
        for (int dx = -range; dx <= range; ++dx) {
            for (int dz = -range; dz <= range; ++dz) {
                List<Claim> bucket = this.claimsByChunk.get(ClaimStore.chunkKey(chunkX + dx, chunkZ + dz));
                if (bucket == null) continue;
                for (Claim c : bucket) {
                    if (c == null || owner != null && owner.equals(c.getOwner()) || c.getCenterX() == centerX && c.getCenterZ() == centerZ || !ClaimStore.intersects(c, centerX, centerZ, radius)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean updateClaimRadius(int centerX, int centerZ, int radius) {
        long bucketKey;
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
        Claim updated = new Claim(existing.getOwner(), existing.getOwnerName(), existing.getCenterX(), existing.getCenterY(), existing.getCenterZ(), radius, existing.getTrusted(), existing.isPvpEnabled());
        this.claimsByCenter.put(ClaimStore.centerKey(centerX, centerZ), updated);
        for (int i = 0; i < this.claims.size(); ++i) {
            if (this.claims.get(i) != existing) continue;
            this.claims.set(i, updated);
            break;
        }
        if ((bucket = this.claimsByChunk.get(bucketKey = ClaimStore.chunkKey(ClaimStore.floorDiv(existing.getCenterX(), 16), ClaimStore.floorDiv(existing.getCenterZ(), 16)))) != null) {
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
        this.claimsByChunk.computeIfAbsent(bucketKey, k -> new ArrayList()).add(claim);
        int claimChunkRadius = ClaimStore.chunkRadiusFor(claim.getRadius());
        if (claimChunkRadius > this.maxChunkRadius) {
            this.maxChunkRadius = claimChunkRadius;
        }
        this.dirty = true;
        return true;
    }

    private static boolean intersects(Claim existing, int centerX, int centerZ, int radius) {
        long dx = Math.abs((long)existing.getCenterX() - (long)centerX);
        long dz = Math.abs((long)existing.getCenterZ() - (long)centerZ);
        long sum = (long)existing.getRadius() + (long)radius;
        return dx <= sum && dz <= sum;
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
        this.dirty = true;
        return true;
    }

    public synchronized void load() {
        this.claims.clear();
        this.claimsByChunk.clear();
        this.claimsByCenter.clear();
        this.maxChunkRadius = 0;
        this.dirty = false;
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
                        this.claims.add(claim);
                        this.claimsByCenter.put(ClaimStore.centerKey(claim.getCenterX(), claim.getCenterZ()), claim);
                        long bucketKey = ClaimStore.chunkKey(ClaimStore.floorDiv(claim.getCenterX(), 16), ClaimStore.floorDiv(claim.getCenterZ(), 16));
                        this.claimsByChunk.computeIfAbsent(bucketKey, k -> new ArrayList()).add(claim);
                        int claimChunkRadius = ClaimStore.chunkRadiusFor(claim.getRadius());
                        if (claimChunkRadius > this.maxChunkRadius) {
                            this.maxChunkRadius = claimChunkRadius;
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
            if (ownerStr == null || x == null || z == null || r == null) {
                return null;
            }
            UUID owner = UUID.fromString(ownerStr);
            Map<UUID, Integer> trusted = ClaimStore.readTrusted(obj);
            int cy = y == null ? Integer.MIN_VALUE : y;
            boolean pvpEnabled = pvp != null && Boolean.TRUE.equals(pvp);
            return new Claim(owner, ownerName, x, cy, z, r, trusted, pvpEnabled);
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



