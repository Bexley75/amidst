package amidst.seedSearch.search.list;

import amidst.mojangapi.MojangApi;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.WorldSeed;
import amidst.mojangapi.world.WorldType;
import amidst.seedSearch.search.Search;

import java.util.Iterator;
import java.util.List;

public class ListSearch extends Search
{
    private List<Long> seeds;

    private Iterator<Long> iterator;

    @Override
    public World getNextWorld(MojangApi mojangApi)
    {
        if(iterator == null)
        {
            iterator = seeds.iterator();
        }
        if(iterator.hasNext())
        {
            Long seed = iterator.next();

            try
            {
                return mojangApi.createWorldFromSeed(WorldSeed.fromLong(seed), WorldType.from(worldType));
            }
            catch (MinecraftInterfaceException e)
            {
                e.printStackTrace();
            }
        }
        iterator = null;
        return null;
    }
}
