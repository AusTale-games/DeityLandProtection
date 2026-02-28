/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.math.util.ChunkUtil
 *  com.hypixel.hytale.protocol.packets.worldmap.MapImage
 *  com.hypixel.hytale.protocol.packets.worldmap.MapMarker
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.map.WorldMap
 *  com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap
 *  com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapSettings
 *  com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk.ChunkWorldMap
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  javax.annotation.Nonnull
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.map.WorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapSettings;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk.ChunkWorldMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;

public final class DeityLandProtectionChunkWorldMap
implements IWorldMap {
    public static final DeityLandProtectionChunkWorldMap INSTANCE = new DeityLandProtectionChunkWorldMap();
    private static final int BORDER_BLOCKS = 2;
    private static final int OUTLANDER_PURPLE = 0xA020F0;

    private DeityLandProtectionChunkWorldMap() {
    }

    public WorldMapSettings getWorldMapSettings() {
        return ChunkWorldMap.INSTANCE.getWorldMapSettings();
    }

    @Nonnull
    public CompletableFuture<WorldMap> generate(World world, int imageWidth, int imageHeight, @Nonnull LongSet chunksToGenerate) {
        return ChunkWorldMap.INSTANCE.generate(world, imageWidth, imageHeight, chunksToGenerate).thenApplyAsync(worldMap -> {
            DeityLandProtectionPlugin plugin = DeityLandProtectionPlugin.getInstance();
            ClaimStore claimStore = plugin == null ? null : plugin.getClaimStore();
            if (plugin == null || claimStore == null || !plugin.isMapClaimVisualEnabled()) {
                return worldMap;
            }
            LongIterator iter = chunksToGenerate.iterator();
            while (iter.hasNext()) {
                long index = iter.nextLong();
                MapImage image = (MapImage)worldMap.getChunks().get(index);
                if (image == null || image.data == null) continue;
                int chunkX = ChunkUtil.xOfChunkIndex((long)index);
                int chunkZ = ChunkUtil.zOfChunkIndex((long)index);
                int minBlockX = ChunkUtil.minBlock((int)chunkX);
                int minBlockZ = ChunkUtil.minBlock((int)chunkZ);
                int w = image.width;
                int h = image.height;
                int sampleWidth = Math.min(32, w);
                int sampleHeight = Math.min(32, h);
                int blockStepX = Math.max(1, 32 / w);
                int blockStepZ = Math.max(1, 32 / h);
                float imageToSampleRatioWidth = (float)sampleWidth / (float)w;
                float imageToSampleRatioHeight = (float)sampleHeight / (float)h;
                for (int y = 0; y < h; ++y) {
                    int sampleZ = Math.min((int)((float)y * imageToSampleRatioHeight), sampleHeight - 1);
                    int zInChunk = sampleZ * blockStepZ;
                    int worldZ = minBlockZ + zInChunk;
                    for (int x = 0; x < w; ++x) {
                        UUID owner;
                        int sampleX = Math.min((int)((float)x * imageToSampleRatioWidth), sampleWidth - 1);
                        int xInChunk = sampleX * blockStepX;
                        int worldX = minBlockX + xInChunk;
                        Claim found = claimStore.findClaimAt(worldX, worldZ);
                        if (found == null || (owner = found.getOwner()) == null) continue;
                        int overlayColor = DeityLandProtectionChunkWorldMap.colorForClaim(plugin, found, owner);
                        int overlayR = overlayColor >> 16 & 0xFF;
                        int overlayG = overlayColor >> 8 & 0xFF;
                        int overlayB = overlayColor & 0xFF;
                        boolean border = DeityLandProtectionChunkWorldMap.isBorderCell(claimStore, found, worldX, worldZ);
                        float alpha = border ? 0.75f : 0.4f;
                        int px = y * w + x;
                        int packed = image.data[px];
                        int baseR = packed >> 24 & 0xFF;
                        int baseG = packed >> 16 & 0xFF;
                        int baseB = packed >> 8 & 0xFF;
                        int outR = (int)((float)baseR * (1.0f - alpha) + (float)overlayR * alpha);
                        int outG = (int)((float)baseG * (1.0f - alpha) + (float)overlayG * alpha);
                        int outB = (int)((float)baseB * (1.0f - alpha) + (float)overlayB * alpha);
                        image.data[px] = (outR & 0xFF) << 24 | (outG & 0xFF) << 16 | (outB & 0xFF) << 8 | 0xFF;
                    }
                }
            }
            return worldMap;
        }, (Executor)world);
    }

    @Nonnull
    public CompletableFuture<Map<String, MapMarker>> generatePointsOfInterest(World world) {
        return CompletableFuture.completedFuture(Collections.emptyMap());
    }

    private static boolean isBorderCell(ClaimStore claimStore, Claim claim, int worldX, int worldZ) {
        if (claimStore == null || claim == null) {
            return false;
        }
        return !DeityLandProtectionChunkWorldMap.isSameClaim(claimStore.findClaimAt(worldX - 1, worldZ), claim) || !DeityLandProtectionChunkWorldMap.isSameClaim(claimStore.findClaimAt(worldX + 1, worldZ), claim) || !DeityLandProtectionChunkWorldMap.isSameClaim(claimStore.findClaimAt(worldX, worldZ - 1), claim) || !DeityLandProtectionChunkWorldMap.isSameClaim(claimStore.findClaimAt(worldX, worldZ + 1), claim);
    }

    private static boolean isSameClaim(Claim a, Claim b) {
        return a != null && b != null && a.getCenterX() == b.getCenterX() && a.getCenterZ() == b.getCenterZ();
    }

    private static int colorForClaim(DeityLandProtectionPlugin plugin, Claim claim, UUID owner) {
        if (plugin != null && claim != null && plugin.isOutlanderClaimItemId(claim.getDeityItemId())) {
            return OUTLANDER_PURPLE;
        }
        return DeityLandProtectionChunkWorldMap.colorForOwner(owner);
    }

    private static int colorForOwner(UUID owner) {
        int h = owner.hashCode();
        float hue = (float)(h & 0xFFFF) / 65535.0f;
        float saturation = 0.7f;
        float value = 0.95f;
        return DeityLandProtectionChunkWorldMap.hsvToRgb(hue, saturation, value);
    }

    private static int hsvToRgb(float h, float s, float v) {
        float g;
        float r;
        float hh = h % 1.0f * 6.0f;
        int i = (int)Math.floor(hh);
        float f = hh - (float)i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * f);
        float t = v * (1.0f - s * (1.0f - f));
        float b = switch (i) {
            case 0 -> {
                r = v;
                g = t;
                yield p;
            }
            case 1 -> {
                r = q;
                g = v;
                yield p;
            }
            case 2 -> {
                r = p;
                g = v;
                yield t;
            }
            case 3 -> {
                r = p;
                g = q;
                yield v;
            }
            case 4 -> {
                r = t;
                g = p;
                yield v;
            }
            default -> {
                r = v;
                g = p;
                yield q;
            }
        };
        int ir = Math.round(r * 255.0f);
        int ig = Math.round(g * 255.0f);
        int ib = Math.round(b * 255.0f);
        return (ir & 0xFF) << 16 | (ig & 0xFF) << 8 | ib & 0xFF;
    }
}



