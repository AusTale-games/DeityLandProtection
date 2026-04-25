package group.austale.deitylandprotection;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;

public final class BorderSurfaceUtil {
    private static final int SURFACE_SCAN_RANGE = 10;
    private static final int SURFACE_SCAN_FALLBACK_RANGE = 64;
    private static final String AIR_TYPE_ID = "Air";

    private BorderSurfaceUtil() {
    }

    public static int resolveSurfaceY(World world, int x, int baseY, int z) {
        int surfaceY = baseY;
        BlockType baseType = BorderSurfaceUtil.safeGetBlockType(world, x, baseY, z);
        if (baseType == null) {
            return surfaceY;
        }
        if (BorderSurfaceUtil.isAirType(baseType)) {
            for (int dy = 1; dy <= SURFACE_SCAN_FALLBACK_RANGE; dy++) {
                int y = baseY - dy;
                BlockType type = BorderSurfaceUtil.safeGetBlockType(world, x, y, z);
                if (BorderSurfaceUtil.isAirType(type)) {
                    continue;
                }
                if (type != null) {
                    surfaceY = y;
                    break;
                }
            }
            return surfaceY;
        }
        int lastSolidY = baseY;
        for (int dy = 1; dy <= SURFACE_SCAN_RANGE; dy++) {
            int y = baseY + dy;
            BlockType type = BorderSurfaceUtil.safeGetBlockType(world, x, y, z);
            if (type == null || BorderSurfaceUtil.isAirType(type)) {
                return lastSolidY;
            }
            lastSolidY = y;
        }
        for (int dy = SURFACE_SCAN_RANGE + 1; dy <= SURFACE_SCAN_FALLBACK_RANGE; dy++) {
            int y = baseY + dy;
            BlockType type = BorderSurfaceUtil.safeGetBlockType(world, x, y, z);
            if (type == null || BorderSurfaceUtil.isAirType(type)) {
                return lastSolidY;
            }
            lastSolidY = y;
        }
        return lastSolidY;
    }

    public static BlockType safeGetBlockType(World world, int x, int y, int z) {
        try {
            return world.getBlockType(x, y, z);
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean isAirType(BlockType type) {
        if (type == null) {
            return true;
        }
        if (type == BlockType.EMPTY) {
            return true;
        }
        String id = BorderSurfaceUtil.blockTypeId(type);
        if (id == null || id.isEmpty()) {
            return false;
        }
        if (AIR_TYPE_ID == null || AIR_TYPE_ID.isEmpty()) {
            return false;
        }
        return id.equalsIgnoreCase(AIR_TYPE_ID);
    }

    public static String blockTypeId(BlockType type) {
        if (type == null) {
            return null;
        }
        if (type == BlockType.EMPTY) {
            return "EMPTY";
        }
        String text = String.valueOf(type);
        int idIndex = text.indexOf("id=");
        if (idIndex < 0) {
            return text;
        }
        int start = idIndex + 3;
        int end = text.indexOf(',', start);
        if (end <= start) {
            return text.substring(start).trim();
        }
        return text.substring(start, end).trim();
    }
}
