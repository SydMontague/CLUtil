package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.clutil.modules.tracking.LocationTracker;
import de.craftlancer.clutil.modules.tracking.TrackingObserver;
import de.craftlancer.clutil.modules.tracking.TrackingState;

public class Tracking extends Module implements Listener, CommandExecutor
{
    private final long trackingDelay; // 20 ticks = 1 second
    private final long observerDelay; // 20 ticks = 1 second
    
    private final int maxDistance;
    
    private final int unclear1;
    private final int unclear1Gap;
    private final int unclear2;
    private final int unclear2Gap;
    
    private final int size;
    
    private final String permission;
    
    private Map<UUID, LocationTracker> locations = new HashMap<>();
    private Map<UUID, TrackingObserver> observer = new HashMap<>();
    
    public Tracking(CLUtil plugin)
    {
        super(plugin);
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        getPlugin().getCommand("find").setExecutor(this);
        
        trackingDelay = getConfig().getLong("trackingDelay", 20L);
        observerDelay = getConfig().getLong("observerDelay", 100L);
        maxDistance = getConfig().getInt("maxDistance", 100);
        unclear1 = getConfig().getInt("unclear1", 600);
        unclear1Gap = getConfig().getInt("unclear1Gap", 3);
        unclear2 = getConfig().getInt("unclear2", 1200);
        unclear2Gap = getConfig().getInt("unclear2Gap", 5);
        size = getConfig().getInt("size", 30 * 60);
        permission = getConfig().getString("permission", "cl.util.tracking");
        
        startTrackingTask();
        startObserverTask();
    }
    
    protected Map<UUID, LocationTracker> getLocationTrackers()
    {
        return locations;
    }
    
    protected Map<UUID, TrackingObserver> getObserver()
    {
        return observer;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("Only Players can run this command!");
            return true;
        }
        
        if (!sender.hasPermission(permission))
        {
            sender.sendMessage("You don't have the permission to run this command!");
            return true;
        }

        Player player = (Player) sender;
        if(args.length == 0 && observer.containsKey(player.getUniqueId()))
            observer.get(player.getUniqueId()).setTracked(null);
            
            
        
        @SuppressWarnings("deprecation")
        OfflinePlayer track = args.length >= 1 ? Bukkit.getOfflinePlayer(args[0]) : null;
        
        if (track == null || !track.hasPlayedBefore())
        {
            sender.sendMessage("You must specify a player!");
            return true;
        }
        
        if (!observer.containsKey(player.getUniqueId()))
            observer.put(player.getUniqueId(), new TrackingObserver(this, player.getUniqueId()));
        
        observer.get(player.getUniqueId()).setTracked(track.getUniqueId());
        
        return true;
    }
    
    private void startObserverTask()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (TrackingObserver ob : getObserver().values())
                    ob.tick();
            }
        }.runTaskTimer(getPlugin(), observerDelay, observerDelay);
    }
    
    private void startTrackingTask()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                List<UUID> removeList = new ArrayList<UUID>();
                
                for (Entry<UUID, LocationTracker> entry : getLocationTrackers().entrySet())
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
                    getLocationTrackers().remove(uuid);
            }
        }.runTaskTimer(getPlugin(), trackingDelay, trackingDelay);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (!locations.containsKey(event.getPlayer().getUniqueId()))
            locations.put(event.getPlayer().getUniqueId(), new LocationTracker(this));
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        Player player = e.getPlayer();

        if(observer.containsKey(player.getUniqueId()))
            observer.get(player.getUniqueId()).setTracked(null);
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.TRACKING;
    }
    
    public int getMaxDistance()
    {
        return maxDistance;
    }
    
    /**
     * After how many TrackingPoints should the LocationTracker skip nodes?
     * 
     * @return an Integer
     */
    public int getUnclear1()
    {
        return unclear1;
    }
    
    /**
     * Only mark every x TrackingPoints as viewable.
     * 
     * @return an Integer
     */
    public int getUnclear1Gap()
    {
        return unclear1Gap;
    }
    
    /**
     * After how many TrackingPoints should the LocationTracker skip nodes?
     * 
     * @return an Integer
     */
    public int getUnclear2()
    {
        return unclear2;
    }
    
    /**
     * Only mark every x TrackingPoints as viewable.
     * 
     * @return an Integer
     */
    public int getUnclear2Gap()
    {
        return unclear2Gap;
    }
    
    public int getTrackingPointCount()
    {
        return size;
    }
    
    public LocationTracker getLocationTracker(UUID player)
    {
        return locations.get(player);
    }
    
    public Material getTrackMaterial(TrackingState state)
    {
        switch (state)
        {
            default:
                return Material.CARPET;
        }
    }
    
    @SuppressWarnings("deprecation")
    public byte getTrackData(TrackingState state)
    {
        switch (state)
        {
            case CLEAR:
                return DyeColor.RED.getWoolData();
            case UNCLEAR1:
                return DyeColor.YELLOW.getWoolData();
            case UNCLEAR2:
                return DyeColor.LIME.getWoolData();
            default:
                return DyeColor.WHITE.getWoolData();
        }
    }
}
