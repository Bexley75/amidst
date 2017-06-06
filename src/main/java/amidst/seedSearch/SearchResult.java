package amidst.seedSearch;

import amidst.mojangapi.world.WorldSeed;
import amidst.mojangapi.world.WorldType;
import amidst.mojangapi.world.icon.WorldIcon;

import java.util.ArrayList;
import java.util.List;

public class SearchResult
{
    public Location location = new Location();
    public Boolean match;
    public WorldSeed worldSeed;
    public WorldType worldType;

    public final List<WorldIcon> worldIcons = new ArrayList<>();

    @Override
    public String toString()
    {
        return worldSeed != null ? Long.toString((long)worldSeed.getLong()) : "";
    }
}
