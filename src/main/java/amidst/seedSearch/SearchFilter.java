package amidst.seedSearch;

import amidst.mojangapi.MojangApi;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.WorldSeed;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;
import amidst.seedSearch.filter.Filter;
import amidst.seedSearch.filter.FilterSerializer;
import amidst.seedSearch.search.Search;
import amidst.seedSearch.search.SearchSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.Optional;

public class SearchFilter extends Filter
{
    private static final Gson GSON = new Gson();

    public Search search;

    public static Optional<SearchFilter> from(String queryString)
    {
        try
        {
            Gson gsonExt = null;
            {
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Filter.class, new FilterSerializer());
                builder.registerTypeAdapter(Search.class, new SearchSerializer());
                gsonExt = builder.create();
            }

            return Optional.ofNullable(gsonExt.fromJson(queryString, SearchFilter.class));
        }
        catch (JsonSyntaxException e)
        {
            return Optional.empty();
        }
    }

    public SearchResult search(MojangApi mojangApi)
    {
        SearchResult searchResult = new SearchResult();
        searchResult.match = false;

        setStartLocation();

        World world = search.getNextWorld(mojangApi);

        if(world != null)
        {
            searchResult.worldSeed = world.getWorldSeed();
            searchResult.worldType = world.getWorldType();
            SearchResult filterResult = match(world);
            searchResult.match = filterResult.match;
            filterResult.location.inherit(filterResult.location);
            mergeSearchResult(searchResult, filterResult);
        }
        return searchResult;
    }

    private void setStartLocation()
    {
        if(location== null)
        {
            location = new Location();
        }
        if(location.x == null || location.y == null)
        {
            location.x = CoordinatesInWorld.origin().getX();
            location.y = CoordinatesInWorld.origin().getY();
        }
    }

    @Override
    protected SearchResult typeMatch(World world)
    {
        SearchResult searchResult = new SearchResult();
        searchResult.location = location;
        searchResult.match = true;

        return searchResult;
    }
}
