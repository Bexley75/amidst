package amidst.seedSearch.search.random;

import amidst.mojangapi.MojangApi;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.WorldSeed;
import amidst.mojangapi.world.WorldType;
import amidst.seedSearch.search.Search;

import java.util.Iterator;
import java.util.List;

public class RandomSearch extends Search
{
    private List<Long> seeds;

    private Iterator<Long> iterator;

    @Override
    public World getNextWorld(MojangApi mojangApi)
    {
        try
        {
            return mojangApi.createWorldFromSeed(WorldSeed.random(), WorldType.from(worldType));
        }
        catch (MinecraftInterfaceException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}