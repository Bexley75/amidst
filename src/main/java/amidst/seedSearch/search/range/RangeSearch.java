package amidst.seedSearch.search.range;

import amidst.mojangapi.MojangApi;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.WorldSeed;
import amidst.mojangapi.world.WorldType;
import amidst.seedSearch.search.Search;

public class RangeSearch extends Search
{
    private SearchRange range;
    private boolean complete = false;

    @Override
    public World getNextWorld(MojangApi mojangApi)
    {
        if(complete)
        {
            return null;
        }

        if(range.start == null && range.finish == null)
        {
            range.start = 0L;
            range.finish = Long.MAX_VALUE;
        }

        if(range.current == null)
        {
            range.current = range.start != null ? range.start : range.finish;
        }

        boolean forward = range.start != null || (range.finish == null || range.finish > range.start);

        complete = (forward && (range.finish != null && range.current > range.finish) || range.current == Long.MAX_VALUE || !forward && range.current == Long.MIN_VALUE);

        World world = null;
        try
        {
            world = mojangApi.createWorldFromSeed(WorldSeed.fromLong(range.current), WorldType.from(worldType));
        }
        catch (MinecraftInterfaceException e)
        {
            e.printStackTrace();
        }

        if(forward)
            range.current++;
        else
            range.current--;

        return world;
    }
}
