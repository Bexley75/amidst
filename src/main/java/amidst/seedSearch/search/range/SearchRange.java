package amidst.seedSearch.search.range;

import amidst.mojangapi.MojangApi;
import amidst.mojangapi.world.World;
import amidst.seedSearch.search.Search;

public class SearchRange extends Search
{
    public Long start;
    public Long finish;
    public Long current;

    @Override
    public World getNextWorld(MojangApi mojangApi)
    {
        return null;
    }
}
