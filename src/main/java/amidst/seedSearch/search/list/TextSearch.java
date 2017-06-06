package amidst.seedSearch.search.list;

import amidst.mojangapi.MojangApi;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.WorldSeed;
import amidst.mojangapi.world.WorldType;
import amidst.seedSearch.search.Search;

import java.util.Iterator;
import java.util.List;

public class TextSearch extends Search
{
    private List<String> text;
    private Iterator<String> iterator;

    @Override
    public World getNextWorld(MojangApi mojangApi)
    {
        if(iterator == null)
        {
            iterator = text.iterator();
        }
        if(iterator.hasNext())
        {
            String seed = iterator.next();

            try
            {
                return mojangApi.createWorldFromSeed(WorldSeed.fromUserInput(seed), WorldType.from(worldType));
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
