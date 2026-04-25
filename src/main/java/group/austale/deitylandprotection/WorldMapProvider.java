package group.austale.deitylandprotection;

import group.austale.deitylandprotection.ClaimChunkWorldMap;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;

public final class WorldMapProvider
implements IWorldMapProvider {
    public static final String ID = "DeityLandProtection";
    public static final BuilderCodec<WorldMapProvider> CODEC = BuilderCodec.builder(WorldMapProvider.class, WorldMapProvider::new).build();

    public IWorldMap getGenerator(World world) throws WorldMapLoadException {
        return ClaimChunkWorldMap.INSTANCE;
    }
}



