/*
 * Decompiled with CFR 0.152.
 */
package group.austale.deitylandprotection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Claim {
    public static final int PERM_PLACE = 1;
    public static final int PERM_BREAK = 2;
    public static final int PERM_USE = 4;
    public static final int PERM_ALL = 7;
    public static final int UNKNOWN_Y = Integer.MIN_VALUE;
    private final UUID owner;
    private final String ownerName;
    private final int centerX;
    private final int centerY;
    private final int centerZ;
    private final int radius;
    private final boolean pvpEnabled;
    private final Map<UUID, Integer> trusted = new HashMap<UUID, Integer>();

    public Claim(UUID owner, int centerX, int centerZ, int radius) {
        this.owner = owner;
        this.ownerName = null;
        this.centerX = centerX;
        this.centerY = Integer.MIN_VALUE;
        this.centerZ = centerZ;
        this.radius = radius;
        this.pvpEnabled = false;
    }

    public Claim(UUID owner, int centerX, int centerY, int centerZ, int radius) {
        this.owner = owner;
        this.ownerName = null;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.pvpEnabled = false;
    }

    public Claim(UUID owner, String ownerName, int centerX, int centerZ, int radius) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.centerX = centerX;
        this.centerY = Integer.MIN_VALUE;
        this.centerZ = centerZ;
        this.radius = radius;
        this.pvpEnabled = false;
    }

    public Claim(UUID owner, String ownerName, int centerX, int centerY, int centerZ, int radius) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.pvpEnabled = false;
    }

    public Claim(UUID owner, int centerX, int centerZ, int radius, Map<UUID, Integer> trusted) {
        this.owner = owner;
        this.ownerName = null;
        this.centerX = centerX;
        this.centerY = Integer.MIN_VALUE;
        this.centerZ = centerZ;
        this.radius = radius;
        this.pvpEnabled = false;
        if (trusted != null && !trusted.isEmpty()) {
            this.trusted.putAll(trusted);
        }
    }

    public Claim(UUID owner, int centerX, int centerY, int centerZ, int radius, Map<UUID, Integer> trusted) {
        this.owner = owner;
        this.ownerName = null;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.pvpEnabled = false;
        if (trusted != null && !trusted.isEmpty()) {
            this.trusted.putAll(trusted);
        }
    }

    public Claim(UUID owner, String ownerName, int centerX, int centerZ, int radius, Map<UUID, Integer> trusted) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.centerX = centerX;
        this.centerY = Integer.MIN_VALUE;
        this.centerZ = centerZ;
        this.radius = radius;
        this.pvpEnabled = false;
        if (trusted != null && !trusted.isEmpty()) {
            this.trusted.putAll(trusted);
        }
    }

    public Claim(UUID owner, String ownerName, int centerX, int centerY, int centerZ, int radius, Map<UUID, Integer> trusted) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.pvpEnabled = false;
        if (trusted != null && !trusted.isEmpty()) {
            this.trusted.putAll(trusted);
        }
    }

    public Claim(UUID owner, String ownerName, int centerX, int centerY, int centerZ, int radius, Map<UUID, Integer> trusted, boolean pvpEnabled) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.pvpEnabled = pvpEnabled;
        if (trusted != null && !trusted.isEmpty()) {
            this.trusted.putAll(trusted);
        }
    }

    public UUID getOwner() {
        return this.owner;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public int getCenterY() {
        return this.centerY;
    }

    public int getCenterZ() {
        return this.centerZ;
    }

    public int getRadius() {
        return this.radius;
    }

    public boolean isPvpEnabled() {
        return this.pvpEnabled;
    }

    public synchronized Map<UUID, Integer> getTrusted() {
        return Collections.unmodifiableMap(new HashMap<UUID, Integer>(this.trusted));
    }

    public synchronized int getPermissionsFor(UUID player) {
        if (player == null) {
            return 0;
        }
        if (this.owner.equals(player)) {
            return 7;
        }
        Integer p = this.trusted.get(player);
        return p == null ? 0 : p;
    }

    public synchronized boolean hasPermission(UUID player, int perm) {
        return (this.getPermissionsFor(player) & perm) != 0;
    }

    public synchronized void setTrusted(UUID player, int perms) {
        if (player == null || this.owner.equals(player)) {
            return;
        }
        int p = perms & 7;
        if (p == 0) {
            this.trusted.remove(player);
            return;
        }
        this.trusted.put(player, p);
    }

    public synchronized void removeTrusted(UUID player) {
        if (player == null) {
            return;
        }
        this.trusted.remove(player);
    }

    public boolean contains(int x, int z) {
        long dx = Math.abs((long)x - (long)this.centerX);
        long dz = Math.abs((long)z - (long)this.centerZ);
        long r = this.radius;
        return dx <= r && dz <= r;
    }
}


