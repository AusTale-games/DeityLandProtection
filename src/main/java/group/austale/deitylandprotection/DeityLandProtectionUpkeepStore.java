package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionUpkeepState;
import com.hypixel.hytale.logger.HytaleLogger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class DeityLandProtectionUpkeepStore {
    private final Path file;
    private final HytaleLogger logger;
    private final Map<Long, DeityLandProtectionUpkeepState> statesByCenter = new HashMap<Long, DeityLandProtectionUpkeepState>();
    private boolean dirty;

    public DeityLandProtectionUpkeepStore(Path file, HytaleLogger logger) {
        this.file = file;
        this.logger = logger;
    }

    public synchronized Map<Long, DeityLandProtectionUpkeepState> getStates() {
        return Collections.unmodifiableMap(new HashMap<Long, DeityLandProtectionUpkeepState>(this.statesByCenter));
    }

    public synchronized DeityLandProtectionUpkeepState getOrCreate(int centerX, int centerZ) {
        long key = DeityLandProtectionUpkeepStore.centerKey(centerX, centerZ);
        DeityLandProtectionUpkeepState s = this.statesByCenter.get(key);
        if (s != null) {
            return s;
        }
        DeityLandProtectionUpkeepState created = new DeityLandProtectionUpkeepState(centerX, centerZ);
        this.statesByCenter.put(key, created);
        this.dirty = true;
        return created;
    }

    public synchronized DeityLandProtectionUpkeepState get(int centerX, int centerZ) {
        return this.statesByCenter.get(DeityLandProtectionUpkeepStore.centerKey(centerX, centerZ));
    }

    public synchronized boolean remove(int centerX, int centerZ) {
        DeityLandProtectionUpkeepState removed = this.statesByCenter.remove(DeityLandProtectionUpkeepStore.centerKey(centerX, centerZ));
        if (removed != null) {
            this.dirty = true;
            return true;
        }
        return false;
    }

    public synchronized void markDirty() {
        this.dirty = true;
    }

    public synchronized void load() {
        int start;
        String json;
        this.statesByCenter.clear();
        if (this.file == null || !Files.exists(this.file, new LinkOption[0])) {
            this.dirty = false;
            return;
        }
        try {
            json = Files.readString(this.file, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to read upkeep.json");
            this.dirty = false;
            return;
        }
        if (json == null) {
            this.dirty = false;
            return;
        }
        String s = json.trim();
        if (s.isEmpty() || s.equals("[]")) {
            this.dirty = false;
            return;
        }
        int idx = 0;
        while (idx < s.length() && (start = s.indexOf(123, idx)) >= 0) {
            int depth = 0;
            int end = -1;
            for (int i = start; i < s.length(); ++i) {
                char ch = s.charAt(i);
                if (ch == '{') {
                    ++depth;
                    continue;
                }
                if (ch != '}' || --depth != 0) continue;
                end = i;
                break;
            }
            if (end < 0) break;
            String obj = s.substring(start, end + 1);
            DeityLandProtectionUpkeepState parsed = this.parseState(obj);
            if (parsed != null) {
                this.statesByCenter.put(DeityLandProtectionUpkeepStore.centerKey(parsed.getCenterX(), parsed.getCenterZ()), parsed);
            }
            idx = end + 1;
        }
        this.dirty = false;
    }

    private DeityLandProtectionUpkeepState parseState(String obj) {
        try {
            Integer x = DeityLandProtectionUpkeepStore.readJsonInt(obj, "x");
            Integer z = DeityLandProtectionUpkeepStore.readJsonInt(obj, "z");
            if (x == null || z == null) {
                return null;
            }
            DeityLandProtectionUpkeepState st = new DeityLandProtectionUpkeepState(x, z);
            st.setProtectionUntilMs(DeityLandProtectionUpkeepStore.readJsonLong(obj, "until"));
            // Backward compatibility: old files wrote this value as "expansionUntil".
            long feedDurationMs = DeityLandProtectionUpkeepStore.readJsonLong(obj, "feedDuration");
            if (feedDurationMs == 0L) {
                feedDurationMs = DeityLandProtectionUpkeepStore.readJsonLong(obj, "expansionUntil");
            }
            st.setTotalFeedDurationMs(feedDurationMs);
            st.setProcessEndsAtMs(DeityLandProtectionUpkeepStore.readJsonLong(obj, "processingUntil"));
            Integer processCarry = DeityLandProtectionUpkeepStore.readJsonInt(obj, "processCarry");
            if (processCarry != null) {
                st.setProcessedEssenceCarryCount(processCarry);
            }
            Integer observedOutputQty = DeityLandProtectionUpkeepStore.readJsonInt(obj, "observedOutputQty");
            if (observedOutputQty != null) {
                st.setObservedOutputQuantity(observedOutputQty);
            }
            Integer tier = DeityLandProtectionUpkeepStore.readJsonInt(obj, "tier");
            if (tier != null) {
                st.setUpgradeTier(tier);
            }
            st.setGraceUntilMs(DeityLandProtectionUpkeepStore.readJsonLong(obj, "graceUntil"));
            st.setGraceCountdownLastSecond(DeityLandProtectionUpkeepStore.readJsonLong(obj, "graceCountdown"));
            st.setPendingRemoveBlock(Boolean.TRUE.equals(DeityLandProtectionUpkeepStore.readJsonBoolean(obj, "removeBlock")));
            return st;
        }
        catch (Exception ignored) {
            return null;
        }
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
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            int i = 0;
            for (DeityLandProtectionUpkeepState st : this.statesByCenter.values()) {
                if (st == null) continue;
                if (i > 0) {
                    sb.append(',');
                }
                sb.append('{');
                sb.append("\"x\":").append(st.getCenterX()).append(',');
                sb.append("\"z\":").append(st.getCenterZ());
                if (st.getProtectionUntilMs() != 0L) {
                    sb.append(',').append("\"until\":").append(st.getProtectionUntilMs());
                }
                if (st.getTotalFeedDurationMs() != 0L) {
                    sb.append(',').append("\"feedDuration\":").append(st.getTotalFeedDurationMs());
                }
                if (st.getProcessEndsAtMs() != 0L) {
                    sb.append(',').append("\"processingUntil\":").append(st.getProcessEndsAtMs());
                }
                if (st.getProcessedEssenceCarryCount() != 0) {
                    sb.append(',').append("\"processCarry\":").append(st.getProcessedEssenceCarryCount());
                }
                if (st.getObservedOutputQuantity() >= 0) {
                    sb.append(',').append("\"observedOutputQty\":").append(st.getObservedOutputQuantity());
                }
                if (st.getUpgradeTier() > 1) {
                    sb.append(',').append("\"tier\":").append(st.getUpgradeTier());
                }
                if (st.getGraceUntilMs() != 0L) {
                    sb.append(',').append("\"graceUntil\":").append(st.getGraceUntilMs());
                }
                if (st.getGraceCountdownLastSecond() != 0L) {
                    sb.append(',').append("\"graceCountdown\":").append(st.getGraceCountdownLastSecond());
                }
                if (st.isPendingRemoveBlock()) {
                    sb.append(',').append("\"removeBlock\":true");
                }
                sb.append('}');
                ++i;
            }
            sb.append(']');
            Files.writeString(this.file, sb.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
            this.dirty = false;
        }
        catch (IOException e) {
            ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(e)).log("DeityLandProtection failed to save upkeep.json");
        }
    }

    private static long centerKey(int x, int z) {
        return (long)x << 32 ^ (long)z & 0xFFFFFFFFL;
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
        if (obj.startsWith("true", i)) {
            return Boolean.TRUE;
        }
        if (obj.startsWith("false", i)) {
            return Boolean.FALSE;
        }
        return null;
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
        try {
            return Integer.parseInt(obj, i, j, 10);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static long readJsonLong(String obj, String key) {
        int j;
        int i;
        String pattern = "\"" + key + "\"";
        int k = obj.indexOf(pattern);
        if (k < 0) {
            return 0L;
        }
        int colon = obj.indexOf(58, k + pattern.length());
        if (colon < 0) {
            return 0L;
        }
        for (i = colon + 1; i < obj.length() && Character.isWhitespace(obj.charAt(i)); ++i) {
        }
        for (j = i; j < obj.length() && (obj.charAt(j) == '-' || Character.isDigit(obj.charAt(j))); ++j) {
        }
        if (j == i) {
            return 0L;
        }
        try {
            return Long.parseLong(obj, i, j, 10);
        }
        catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}



