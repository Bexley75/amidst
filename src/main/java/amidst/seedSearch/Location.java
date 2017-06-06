package amidst.seedSearch;

import amidst.fragment.Fragment;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;

public class Location
{
    public Long x;
    public Long y;
    public Integer distance;

    public Location()
    {
        getDistance();
    }

    public void inherit(Location location)
    {
        if (x == null && location.x != null)
        {
            x = location.x;
        }
        if (y == null && location.y != null)
        {
            y = location.y;
        }
        if (distance == null && location.distance != null)
        {
            distance = location.distance;
        }
    }

    public int getDistance()
    {
        if (distance == null || distance % Fragment.SIZE != 0)
        {
            int multiplier = 1;
            if(distance != null && distance != 0)
            {
                multiplier = Math.abs(distance / Fragment.SIZE);
            }
            distance = (multiplier > 1) ? multiplier * Fragment.SIZE : Fragment.SIZE;
        }
        return distance;
    }


    public CoordinatesInWorld getCoordinatesInWorld()
    {
        getDistance();

        if(x == null && y == null)
        {
            x = CoordinatesInWorld.origin().getX();
            y = CoordinatesInWorld.origin().getY();
        }

        return new CoordinatesInWorld(-(x + distance), -(y + distance));
    }
}
