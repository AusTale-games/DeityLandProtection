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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;

public final class DeityLandProtectionChunkWorldMap
implements IWorldMap {
    public static final DeityLandProtectionChunkWorldMap INSTANCE = new DeityLandProtectionChunkWorldMap();
    private static final int BORDER_BLOCKS = 2;

    private DeityLandProtectionChunkWorldMap() {
    }

    public WorldMapSettings getWorldMapSettings() {
        return ChunkWorldMap.INSTANCE.getWorldMapSettings();
    }

    @Nonnull
    public CompletableFuture<WorldMap> generate(World world, int imageWidth, int imageHeight, @Nonnull LongSet chunksToGenerate) {
        return ChunkWorldMap.INSTANCE.generate(world, imageWidth, imageHeight, chunksToGenerate).thenApplyAsync(worldMap -> {
            DeityLandProtectionPlugin plugin = DeityLandProtectionPlugin.getInstance();
            if (plugin == null || plugin.getClaimStore() == null) {
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
                List<Claim> candidates = plugin.getClaimStore().getClaimsNearArea(minBlockX, minBlockZ, minBlockX + 31, minBlockZ + 31);
                if (candidates.isEmpty()) continue;
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
                        Claim found = null;
                        for (Claim c : candidates) {
                            if (!c.contains(worldX, worldZ)) continue;
                            found = c;
                            break;
                        }
                        if (found == null || (owner = found.getOwner()) == null) continue;
                        int overlayColor = DeityLandProtectionChunkWorldMap.colorForOwner(owner);
                        int overlayR = overlayColor >> 16 & 0xFF;
                        int overlayG = overlayColor >> 8 & 0xFF;
                        int overlayB = overlayColor & 0xFF;
                        long dx = Math.abs((long)worldX - (long)found.getCenterX());
                        long dz = Math.abs((long)worldZ - (long)found.getCenterZ());
                        long r = found.getRadius();
                        long borderMin = Math.max(0L, r - 2L);
                        boolean border = dx >= borderMin || dz >= borderMin;
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



