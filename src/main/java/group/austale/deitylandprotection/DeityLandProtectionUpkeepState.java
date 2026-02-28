/*
 * Decompiled with CFR 0.152.
 */
package group.austale.deitylandprotection;

public final class DeityLandProtectionUpkeepState {
    private static final int MIN_TIER = 1;
    private static final int MAX_TIER = 4;
    private final int centerX;
    private final int centerZ;
    private int upgradeTier = 1;
    private long protectionUntilMs;
    // Stored as a duration amount (not a wall-clock timestamp).
    private long totalFeedDurationMs;
    private long processEndsAtMs;
    private int processedEssenceCarryCount;
    private int observedOutputQuantity = -1;
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

    public int getUpgradeTier() {
        return DeityLandProtectionUpkeepState.clampTier(this.upgradeTier);
    }

    public void setUpgradeTier(int upgradeTier) {
        this.upgradeTier = DeityLandProtectionUpkeepState.clampTier(upgradeTier);
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

    public long getProcessEndsAtMs() {
        return this.processEndsAtMs;
    }

    public void setProcessEndsAtMs(long processEndsAtMs) {
        this.processEndsAtMs = Math.max(0L, processEndsAtMs);
    }

    public int getProcessedEssenceCarryCount() {
        return this.processedEssenceCarryCount;
    }

    public void setProcessedEssenceCarryCount(int processedEssenceCarryCount) {
        this.processedEssenceCarryCount = Math.max(0, processedEssenceCarryCount);
    }

    public int getObservedOutputQuantity() {
        return this.observedOutputQuantity;
    }

    public void setObservedOutputQuantity(int observedOutputQuantity) {
        this.observedOutputQuantity = Math.max(-1, observedOutputQuantity);
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

    private static int clampTier(int tier) {
        if (tier < MIN_TIER) {
            return MIN_TIER;
        }
        return Math.min(tier, MAX_TIER);
    }
}



