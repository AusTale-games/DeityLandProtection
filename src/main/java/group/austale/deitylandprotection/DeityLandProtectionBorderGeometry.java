package group.austale.deitylandprotection;

public final class DeityLandProtectionBorderGeometry {
    private DeityLandProtectionBorderGeometry() {
    }

    public static boolean isBorderCell(ClaimStore claimStore, int centerX, int centerZ, int x, int z) {
        return !DeityLandProtectionBorderGeometry.isSameClaim(claimStore.findClaimAt(x - 1, z), centerX, centerZ)
            || !DeityLandProtectionBorderGeometry.isSameClaim(claimStore.findClaimAt(x + 1, z), centerX, centerZ)
            || !DeityLandProtectionBorderGeometry.isSameClaim(claimStore.findClaimAt(x, z - 1), centerX, centerZ)
            || !DeityLandProtectionBorderGeometry.isSameClaim(claimStore.findClaimAt(x, z + 1), centerX, centerZ);
    }

    public static boolean isSameClaim(Claim claim, int centerX, int centerZ) {
        return claim != null && claim.getCenterX() == centerX && claim.getCenterZ() == centerZ;
    }
}
