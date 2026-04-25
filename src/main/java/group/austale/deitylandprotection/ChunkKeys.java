package group.austale.deitylandprotection;

/**
 * Bit-packing helpers for representing a 2D (x, z) coordinate pair as a single
 * {@code long}. The high 32 bits hold {@code x}, the low 32 bits hold {@code z}.
 *
 * <p>This single source of truth replaces the three identical (and identically
 * named) helpers that used to live on {@code DeityLandProtectionPlugin},
 * {@code ClaimStore}, and {@code DeityLandProtectionUpkeepStore}.</p>
 */
final class ChunkKeys {

    private ChunkKeys() {}

    /** Pack an (x, z) pair into a long, high 32 bits = x, low 32 bits = z. */
    static long pack(int x, int z) {
        return (long) x << 32 ^ (long) z & 0xFFFFFFFFL;
    }

    static int xOf(long key) {
        return (int) (key >> 32);
    }

    static int zOf(long key) {
        return (int) key;
    }
}
