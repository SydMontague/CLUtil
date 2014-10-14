package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class Tracking extends Module implements Listener
{
    private static final long DELAY = 20L; // 20 ticks = 1 second
    
    private Map<UUID, LocationTracker> locations = new HashMap<>();
    
    public Tracking(CLUtil plugin)
    {
        super(plugin);
        
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                List<UUID> removeList = new ArrayList<UUID>();
                
                for (Entry<UUID, LocationTracker> entry : locations.entrySet())
                {
                    Player p = Bukkit.getPlayer(entry.getKey());
                    
                    if (p == null)
                        entry.getValue().add(null);
                    else
                        entry.getValue().add(p.getLocation());
                    
                    if (entry.getValue().isEmpty())
                        removeList.add(entry.getKey());
                }
                
                for (UUID uuid : removeList)
                    locations.remove(uuid);
            }
        }.runTaskTimer(plugin, DELAY, DELAY);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (!locations.containsKey(event.getPlayer().getUniqueId()))
            locations.put(event.getPlayer().getUniqueId(), new LocationTracker());
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
    public List<TrackingPoint> getPoints(Player p)
    {
        List<TrackingPoint> list = new ArrayList<>();
        
        for (int i = pointer; i < size + pointer; i++)
        {
            int tmp = i % size;
            TrackingPoint point = points[tmp];
            
            if (point.distance(p) > MAX_DISTANCE)
                continue;
            
            list.add(point);
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
        if (pointer >= size)
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
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
