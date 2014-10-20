package de.craftlancer.clutil.modules.tracking;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.craftlancer.clutil.modules.Tracking;

public class LocationTracker
{
    private Tracking module;
    private TrackingPoint[] points;
    private int pointer = 0;
    
    public LocationTracker(Tracking module)
    {
        this.module = module;
        points = new TrackingPoint[module.getTrackingPointCount()];
    }
    
    /**
     * Add a location to the current Tracker.
     * Another, old, location will be thrown out of the list for that.
     * 
     * @param loc
     *            the location to add to the list
     * @return true if the action was successful
     */
    public boolean add(Location loc)
    {
        points[pointer] = new TrackingPoint(loc);
        increasePointer();
        return true;
    }
    
    /**
     * Get the points of this tracker, that are within the range of the given player.
     * 
     * @param p
     *            The player, who wants to query the data
     * @return a List of TrackingPoints, that are within a certain distance to the player
     */
    public Map<TrackingPoint, TrackingState> getPoints(Player p)
    {
        Map<TrackingPoint, TrackingState> list = new HashMap<>();
        
        for (int i = 0; i < module.getTrackingPointCount(); i++)
        {
            int tmp = (i + pointer) % module.getTrackingPointCount();
            TrackingState state = TrackingState.CLEAR;
            
            if (i > module.getUnclear2() && i % module.getUnclear2Gap() != 0)
                continue;
            else
                state = TrackingState.UNCLEAR2;
            
            if (i > module.getUnclear1() && i % module.getUnclear1Gap() != 0)
                continue;
            else if (state == TrackingState.CLEAR)
                state = TrackingState.UNCLEAR1;
            
            TrackingPoint point = points[tmp];
            
            if (point.distance(p) > module.getMaxDistance())
                continue;
            
            list.put(point, state);
        }
        
        return list;
    }
    
    public boolean isEmpty()
    {
        for (TrackingPoint point : points)
            if (point != null)
                return false;
        
        return true;
    }
    
    private void increasePointer()
    {
        pointer++;
        if (pointer >= module.getTrackingPointCount())
            pointer = 0;
    }
    
}