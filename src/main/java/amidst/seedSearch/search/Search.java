package amidst.seedSearch.search;

import amidst.mojangapi.MojangApi;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.WorldSeed;
import amidst.mojangapi.world.WorldType;
import amidst.seedSearch.SearchResult;

import java.util.List;

public abstract class Search
{
    private SearchType type;
    protected String worldType;

    public Search()
    {

    }

    public abstract World getNextWorld(MojangApi mojangApi);
}
