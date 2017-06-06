package amidst.seedSearch.filter.biome;

import amidst.mojangapi.world.World;
import amidst.mojangapi.world.biome.Biome;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;
import amidst.mojangapi.world.coordinates.Resolution;
import amidst.seedSearch.JoinType;
import amidst.seedSearch.Location;
import amidst.seedSearch.SearchResult;
import amidst.seedSearch.filter.Filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BiomeFilter extends Filter
{
    public BiomeFilterParameters parameters;

    @Override
    protected SearchResult typeMatch(World world)
    {
        SearchResult searchResult = new SearchResult();
        searchResult.match = false;

        //this corrects the distance if its not set
        CoordinatesInWorld coordinatesInWorld = location.getCoordinatesInWorld();

        long distance = Resolution.QUARTER.convertFromWorldToThis(location.getDistance());

        short[][] region = new short[(int)distance * 2][(int)distance* 2];

        world.getBiomeDataOracle().populateArray(coordinatesInWorld, region, true);

        Set<Short> foundBiomeIndexes = new HashSet<>();
        Set<Short> validBiomeIndexes = getValidBiomeIndexes();

        for (short[] row : region)
        {
            for (short entry : row)
            {
                boolean match = (validBiomeIndexes.contains(entry));
                if (parameters.join == JoinType.all)
                {
                    if (match && !foundBiomeIndexes.contains(entry))
                    {
                        foundBiomeIndexes.add(entry);
                        if (foundBiomeIndexes.containsAll(validBiomeIndexes))
                        {
                            searchResult.match = true;
                            return searchResult;
                        }
                    }
                }
                else if(match)
                {
                    searchResult.match = true;
                    return searchResult;
                }
            }
        }
        return searchResult;
    }

    private Set<Short> getValidBiomeIndexes() {
        Set<Short> result = new HashSet<>();
        for (String name : parameters.biomes)
        {
            short index;
            Biome biome = Biome.getByName(name);
            if(biome != null)
            {
                result.add((short) biome.getIndex());
            }
        }
        return result;
    }
}
