package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class Tracking extends Module
{
        
    private Map<UUID, LocationTracker> locations = new HashMap<>();
    
    public Tracking(CLUtil plugin)
    {
        super(plugin);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.TRACKING;
    }
    
}

class LocationTracker
{
    private static final int MAX_DISTANCE = 100;
    
    private int size = 30 * 60; // minutes in seconds
    private TrackingPoint[] points = new TrackingPoint[size];
    private int pointer = 0;
    
    public boolean add(Location loc)
    {
        points[pointer] = new TrackingPoint(loc);
        increasePointer();
        return false;
    }

    public List<TrackingPoint> getPoints(Player p)
    {
        List<TrackingPoint> list = new ArrayList<>();
        
        for(int i = pointer; i < size + pointer; i++)
        {
            int tmp = i % size;
            TrackingPoint point = points[tmp];
            
            if(point.distance(p) > MAX_DISTANCE)
                continue;
                
            list.add(point);
        }
        
        return list;
    }
    
    private void increasePointer()
    {
        pointer++;
        if(pointer >= size)
            pointer = 0;
    }
    
}

class TrackingPoint
{
    private int x;
    private int y;
    private int z;
    
    public TrackingPoint(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public TrackingPoint(Location loc)
    {
        this(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public double distance(Player p)
    {
        return distance(p.getLocation());
    }

    private double distance(Location loc)
    {
        double dx = loc.getX() - x;
        double dy = loc.getY() - y;
        double dz = loc.getZ() - z;
        
        return Math.sqrt(dx*dx+dy*dy+dz*dz);
    }
}

