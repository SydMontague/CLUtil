package de.craftlancer.clutil.modules.tracking;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TrackingPoint
{
    String world;
    private int x;
    private int y;
    private int z;
    
    public TrackingPoint(String world, int x, int y, int z)
    {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public TrackingPoint(Location loc)
    {
        this(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    public double distance(Player p)
    {
        return distance(p.getLocation());
    }
    
    /**
     * Get the distance between a Location and this TrackingPoint
     * If the given points are in different worlds, it will return Integer.MAX_VALUE.
     * 
     * @param loc
     *            the location to measure the distance to.
     * @return the distance between the points, Integer.MAX_VALUE when they're in different worlds
     */
    private double distance(Location loc)
    {
        if (!loc.getWorld().getName().equalsIgnoreCase(world))
            return Integer.MAX_VALUE;
        
        double dx = loc.getX() - x;
        double dy = loc.getY() - y;
        double dz = loc.getZ() - z;
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    public Location toLocation()
    {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
