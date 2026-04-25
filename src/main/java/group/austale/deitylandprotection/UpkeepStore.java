package group.austale.deitylandprotection;

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

public final class UpkeepStore {
    private final Path file;
    private final HytaleLogger logger;
    private final Map<Long, UpkeepState> statesByCenter = new HashMap<Long, UpkeepState>();
    private boolean dirty;

    public UpkeepStore(Path file, HytaleLogger logger) {
        this.file = file;
        this.logger = logger;
    }

    public synchronized Map<Long, UpkeepState> getStates() {
        return Collections.unmodifiableMap(new HashMap<Long, UpkeepState>(this.statesByCenter));
    }

    public synchronized UpkeepState getOrCreate(int centerX, int centerZ) {
        long key = UpkeepStore.centerKey(centerX, centerZ);
        UpkeepState s = this.statesByCenter.get(key);
        if (s != null) {
            return s;
        }
        UpkeepState created = new UpkeepState(centerX, centerZ);
        this.statesByCenter.put(key, created);
        this.dirty = true;
        return created;
    }

    public synchronized UpkeepState get(int centerX, int centerZ) {
        return this.statesByCenter.get(UpkeepStore.centerKey(centerX, centerZ));
    }

    public synchronized boolean remove(int centerX, int centerZ) {
        UpkeepState removed = this.statesByCenter.remove(UpkeepStore.centerKey(centerX, centerZ));
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
            UpkeepState parsed = this.parseState(obj);
            if (parsed != null) {
                this.statesByCenter.put(UpkeepStore.centerKey(parsed.getCenterX(), parsed.getCenterZ()), parsed);
            }
            idx = end + 1;
        }
        this.dirty = false;
    }

    private UpkeepState parseState(String obj) {
        try {
            Integer x = JsonReader.readInt(obj, "x");
            Integer z = JsonReader.readInt(obj, "z");
            if (x == null || z == null) {
                return null;
            }
            UpkeepState st = new UpkeepState(x, z);
            st.setProtectionUntilMs(JsonReader.readLong(obj, "until"));
            // Backward compatibility: old files wrote this value as "expansionUntil".
            long feedDurationMs = JsonReader.readLong(obj, "feedDuration");
            if (feedDurationMs == 0L) {
                feedDurationMs = JsonReader.readLong(obj, "expansionUntil");
            }
            st.setTotalFeedDurationMs(feedDurationMs);
            st.setProcessEndsAtMs(JsonReader.readLong(obj, "processingUntil"));
            Integer processCarry = JsonReader.readInt(obj, "processCarry");
            if (processCarry != null) {
                st.setProcessedEssenceCarryCount(processCarry);
            }
            Integer observedOutputQty = JsonReader.readInt(obj, "observedOutputQty");
            if (observedOutputQty != null) {
                st.setObservedOutputQuantity(observedOutputQty);
            }
            Integer tier = JsonReader.readInt(obj, "tier");
            if (tier != null) {
                st.setUpgradeTier(tier);
            }
            st.setGraceUntilMs(JsonReader.readLong(obj, "graceUntil"));
            st.setGraceCountdownLastSecond(JsonReader.readLong(obj, "graceCountdown"));
            st.setPendingRemoveBlock(Boolean.TRUE.equals(JsonReader.readBoolean(obj, "removeBlock")));
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
            for (UpkeepState st : this.statesByCenter.values()) {
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
        return ChunkKeys.pack(x, z);
    }
}



