/*
 * Decompiled with CFR 0.152.
 */
package group.austale.deitylandprotection;

public final class DeityLandProtectionUpkeepState {
    private final int centerX;
    private final int centerZ;
    private long protectionUntilMs;
    // Stored as a duration amount (not a wall-clock timestamp).
    private long totalFeedDurationMs;
    private long graceUntilMs;
    private long graceCountdownLastSecond;
    private boolean pendingRemoveBlock;

    public DeityLandProtectionUpkeepState(int centerX, int centerZ) {
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public int getCenterZ() {
        return this.centerZ;
    }

    public long getProtectionUntilMs() {
        return this.protectionUntilMs;
    }

    public void setProtectionUntilMs(long protectionUntilMs) {
        this.protectionUntilMs = protectionUntilMs;
    }

    public long getTotalFeedDurationMs() {
        return this.totalFeedDurationMs;
    }

    public void setTotalFeedDurationMs(long totalFeedDurationMs) {
        this.totalFeedDurationMs = totalFeedDurationMs;
    }

    public long getGraceUntilMs() {
        return this.graceUntilMs;
    }

    public void setGraceUntilMs(long graceUntilMs) {
        this.graceUntilMs = graceUntilMs;
    }

    public long getGraceCountdownLastSecond() {
        return this.graceCountdownLastSecond;
    }

    public void setGraceCountdownLastSecond(long graceCountdownLastSecond) {
        this.graceCountdownLastSecond = graceCountdownLastSecond;
    }

    public boolean isPendingRemoveBlock() {
        return this.pendingRemoveBlock;
    }

    public void setPendingRemoveBlock(boolean pendingRemoveBlock) {
        this.pendingRemoveBlock = pendingRemoveBlock;
    }
}



