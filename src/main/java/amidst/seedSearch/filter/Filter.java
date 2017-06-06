package amidst.seedSearch.filter;

import amidst.mojangapi.world.World;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;
import amidst.seedSearch.JoinType;
import amidst.seedSearch.Location;
import amidst.seedSearch.SearchResult;

import java.util.List;

public abstract class Filter
{
    public JoinType join;
    public FilterType type;
    public Location location;

    private List<Filter> filters;

    protected abstract SearchResult typeMatch(World world);

    public SearchResult match(World world)
    {
        SearchResult searchResult = typeMatch(world);

        if(searchResult.match && filters != null)
        {
            for (Filter filter : filters)
            {
                SearchResult filterResult = null;
                filter.location.inherit(searchResult.location);

                if (filter.join == null || filter.join == JoinType.and && searchResult.match)
                {
                    filterResult = filter.match(world);
                    searchResult.match &= filterResult.match;
                }
                if (filter.join == JoinType.or && !searchResult.match)
                {
                    filterResult = filter.match(world);
                    searchResult.match |= filterResult.match;
                }
                mergeSearchResult(searchResult, filterResult);
            }
        }
        return searchResult;
    }

    protected void mergeSearchResult(SearchResult searchResult, SearchResult filterResult)
    {
        if(filterResult != null)
        {
            if (filterResult.match && searchResult.match)
            {
                searchResult.worldIcons.addAll(filterResult.worldIcons);
            }
        }
    }
}
