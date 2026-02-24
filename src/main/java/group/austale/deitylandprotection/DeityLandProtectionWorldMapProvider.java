/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap
 *  com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException
 *  com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionChunkWorldMap;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;

public final class DeityLandProtectionWorldMapProvider
implements IWorldMapProvider {
    public static final String ID = "DeityLandProtection";
    public static final BuilderCodec<DeityLandProtectionWorldMapProvider> CODEC = BuilderCodec.builder(DeityLandProtectionWorldMapProvider.class, DeityLandProtectionWorldMapProvider::new).build();

    public IWorldMap getGenerator(World world) throws WorldMapLoadException {
        return DeityLandProtectionChunkWorldMap.INSTANCE;
    }
}



