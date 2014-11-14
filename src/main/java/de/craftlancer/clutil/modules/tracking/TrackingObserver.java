package de.craftlancer.clutil.modules.tracking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import de.craftlancer.clutil.modules.Tracking;

/*
 * Tasks:
 * show tracks to players
 * remove tracks to avoid inconsistent client states
 * keep track what player a player is tracking
 */
public class TrackingObserver
{
    private final UUID trackingPlayer;
    private UUID trackedPlayer = null;
    private List<Location> changedBlocks = new ArrayList<>();
    private final Tracking module;
    
    public TrackingObserver(Tracking module, UUID player)
    {
        this.module = module;
        this.trackingPlayer = player;
    }
    
    /**
     * Updates the shown tracks to the player.
     */
    @SuppressWarnings("deprecation")
    public void tick()
    {
        Player p = Bukkit.getPlayer(trackingPlayer);
        
        if(!p.isOnline())
            return;
            
        for (Location loc : changedBlocks)
        {
            Block block = loc.getBlock();
            p.sendBlockChange(loc, block.getType(), block.getData());
        }
        
        changedBlocks.clear();
        
        if (trackedPlayer == null)
            return;
        
        if (module.getLocationTracker(trackedPlayer) == null)
        {
            setTracked(null);
            return;
        }
        
        for (Entry<TrackingPoint, TrackingState> entry : module.getLocationTracker(trackedPlayer).getPoints(p).entrySet())
        {
            TrackingState state = entry.getValue();
            Location loc = entry.getKey().toLocation();
            Material mat = module.getTrackMaterial(state);
            byte data = module.getTrackData(state);
            
            p.sendBlockChange(loc, mat, data);
            changedBlocks.add(loc);
        }
    }
    
    public void setTracked(UUID uuid)
    {
        this.trackedPlayer = uuid;
    }
}
