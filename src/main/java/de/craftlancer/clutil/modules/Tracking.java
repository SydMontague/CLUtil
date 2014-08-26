package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class Tracking extends Module
{
    
    private TrackingHandler handler = new TrackingHandler();
    
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
    private long time;
    private int x;
    private int y;
    private int z;
    
    public TrackingPoint(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.time = System.currentTimeMillis();
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

class TrackingHandler extends BukkitRunnable
{
    private TrackingBuffer buffer1;
    private TrackingBuffer buffer2;
    private boolean useBuffer1;
    
    @Override
    public void run()
    {
        swapBuffers();
    }
    
    private synchronized void swapBuffers()
    {
        if (useBuffer1)
            buffer1.clear();
        else
            buffer2.clear();
        
        useBuffer1 = !useBuffer1;
    }
    
    private TrackingBuffer getReadBuffer()
    {
        return useBuffer1 ? buffer1 : buffer2;
    }
    
    private TrackingBuffer getWriteBuffer()
    {
        return useBuffer1 ? buffer2 : buffer1;
    }
}

class TrackingBuffer
{
    private Map<UUID, List<BlockChange>> values = new HashMap<>();
    
    public void clear()
    {
    }
}

class BlockChange
{
    private Location location;
    private Material material;
    private byte data;
    private boolean reset;
    
    public BlockChange(Location location, Material material, byte data, boolean reset)
    {
        this.location = location;
        this.material = material;
        this.data = data;
        this.reset = reset;
    }
    
    @SuppressWarnings("deprecation")
    public void sendBlockChange(Player p)
    {
        if (reset)
            p.sendBlockChange(location, location.getBlock().getType(), location.getBlock().getData());
        else
            p.sendBlockChange(location, material, data);
    }
}
