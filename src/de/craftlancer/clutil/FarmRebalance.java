package de.craftlancer.clutil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class FarmRebalance implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreed(CreatureSpawnEvent e)
    {
        if (e.getSpawnReason() != SpawnReason.EGG && e.getSpawnReason() != SpawnReason.BREEDING)
            return;
        
        if (e.getEntity().getLocation().getY() <= e.getEntity().getWorld().getHighestBlockYAt(e.getEntity().getLocation()) -1)
            e.setCancelled(true);
    }
}
