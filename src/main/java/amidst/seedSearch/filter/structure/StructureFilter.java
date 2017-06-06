package amidst.seedSearch.filter.structure;

import amidst.mojangapi.world.World;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;
import amidst.mojangapi.world.icon.WorldIcon;
import amidst.mojangapi.world.icon.producer.NameFilteredWorldIconCollector;
import amidst.mojangapi.world.icon.producer.WorldIconCollector;
import amidst.mojangapi.world.icon.producer.WorldIconProducer;
import amidst.mojangapi.world.icon.type.DefaultWorldIconTypes;
import amidst.seedSearch.Location;
import amidst.seedSearch.SearchResult;
import amidst.seedSearch.filter.Filter;

import java.util.LinkedList;
import java.util.List;

public class StructureFilter extends Filter
{
    public StructureFilterParameters parameters;

    @Override
    protected SearchResult typeMatch(World world) {

        SearchResult searchResult = new SearchResult();

        DefaultWorldIconTypes structure = DefaultWorldIconTypes.getByName(parameters.structure);
        WorldIconCollector structureCollector = getCollector(structure);

        produceAndCollect(getProducer(world, structure), structureCollector);

        searchResult.match = true;
        List<WorldIcon> structures = structureCollector.get();
        if (parameters.cluster != null && parameters.cluster > 1)
        {
            SearchResult clusterSizeResult = checkClusterSize(structures);
            searchResult.match = clusterSizeResult.match;
            if(searchResult.match)
            {
                searchResult.location.inherit(clusterSizeResult.location);
            }
            mergeSearchResult(searchResult, clusterSizeResult);
        }

        searchResult.match &= structures.size() >= (parameters.minimum == null ? 1 : parameters.minimum);

        if(searchResult.match && (parameters.cluster == null || parameters.cluster == 1))
        {
            searchResult.worldIcons.addAll(structures);
        }
        return searchResult;
    }

    private void produceAndCollect(WorldIconProducer<Void> structureProducer, WorldIconCollector structureCollector) {
        int distance = location.getDistance();

        for (long x = 0; x < 2 * distance; x += 512)
        {
            for (long y = 0; y < 2 * distance; y += 512)
            {
                CoordinatesInWorld coordinatesInWorld = CoordinatesInWorld.from(x, y).add(location.getCoordinatesInWorld());
                structureProducer.produce(coordinatesInWorld, structureCollector, null);
            }
        }
    }

    private SearchResult checkClusterSize(List<WorldIcon> structures)
    {
        SearchResult searchResult = new SearchResult();
        searchResult.match = false;

        //Todo, do we need multiple clusters?
        int distance = parameters.distance;
        if (parameters.distance == null)
        {
            distance = 180;
        }

        for (WorldIcon structure : structures)
        {
            List<WorldIcon> structuresInRange = new LinkedList<>();

            structuresInRange.add(structure);

            for (WorldIcon structureToCompare : structures)
            {
                if (structure != structureToCompare && IsWithinRange(structure, structureToCompare, distance))
                {
                    structuresInRange.add(structureToCompare);
                    int count = 1;

                    for (WorldIcon structureInRange : structuresInRange)
                    {
                        if (structureToCompare != structureInRange && IsWithinRange(structureToCompare, structureInRange, distance))
                        {
                            count++;//a structure was in range of the one we just added
                        }
                    }

                    if(count >= parameters.cluster)
                    {
                        searchResult.worldIcons.add(structure);
                        searchResult.location.x = structure.getCoordinates().getX();
                        searchResult.location.y = structure.getCoordinates().getY();
                        searchResult.match = true;
                        return searchResult;
                    }
                }
            }
        }
        return searchResult;
    }

    private boolean IsWithinRange(WorldIcon s1, WorldIcon s2, long maxDistance)
    {
        return Math.abs(s1.getCoordinates().getX() - s2.getCoordinates().getX()) < maxDistance
                && Math.abs(s1.getCoordinates().getY() - s2.getCoordinates().getY()) < maxDistance;
    }

    private WorldIconProducer<Void> getProducer(World world, DefaultWorldIconTypes structure) {
        switch (structure) {
            case JUNGLE:
            case DESERT:
            case IGLOO:
            case WITCH:
                return world.getTempleProducer();
            case STRONGHOLD:
                return world.getStrongholdProducer();
            case VILLAGE:
                return world.getVillageProducer();
            case OCEAN_MONUMENT:
                return world.getOceanMonumentProducer();
            case MINESHAFT:
                return world.getMineshaftProducer();
            default:
                throw new IllegalArgumentException("Unsupported structure type: " + structure.getName());
        }
    }

    private WorldIconCollector getCollector(DefaultWorldIconTypes structure) {
        switch (structure) {
            case JUNGLE:
            case DESERT:
            case IGLOO:
            case WITCH:
                return new NameFilteredWorldIconCollector(structure.getLabel());
            case STRONGHOLD:
            case VILLAGE:
            case OCEAN_MONUMENT:
            case MINESHAFT:
                return new WorldIconCollector();
            default:
                throw new IllegalArgumentException("Unsupported structure type: " + structure.getName());
        }
    }
}
