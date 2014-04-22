package de.craftlancer.clutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("deprecation")
public class GriefBlock extends BukkitRunnable implements Listener
{
    private HashMap<Block, Long> griefMap = new HashMap<Block, Long>();
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e)
    {
        if (e.getBlock().getType() != Material.GRAVEL || e.getBlock().getData() != 1)
            return;
        
        e.getBlock().setType(Material.AIR);
        e.setCancelled(true);
        e.setExpToDrop(0);
        griefMap.remove(e.getBlock());
        

        if(e.getPlayer().hasMetadata("clgroups.breakmsg"))
            e.getPlayer().removeMetadata("clgroups.breakmsg", e.getPlayer().getMetadata("clgroups.breakmsg").get(0).getOwningPlugin());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e)
    {
        if (e.getItemInHand().hasItemMeta() && e.getItemInHand().getItemMeta().hasDisplayName() && e.getItemInHand().getItemMeta().getDisplayName().equals("§4Griefblock"))
        {
            if (e.getBlock().getRelative(0, -1, 0).getType() == Material.AIR)
            {
                Location loc = e.getBlock().getLocation();
                loc.getWorld().spawnFallingBlock(loc, Material.GRAVEL, (byte) 1);
                e.setCancelled(false);
                e.getBlock().setType(Material.AIR);
                loc.getWorld().playSound(loc, Sound.DIG_GRAVEL, 1F, 1F);
            }
            else
            {
                e.setCancelled(false);
                e.getBlock().setData((byte) 1);
                griefMap.put(e.getBlock(), System.currentTimeMillis() + 300000);
            }
            
            if(e.getPlayer().hasMetadata("clgroups.placemsg"))
                e.getPlayer().removeMetadata("clgroups.placemsg", e.getPlayer().getMetadata("clgroups.placemsg").get(0).getOwningPlugin());
        }
    }
    
    @EventHandler
    public void onFallingBlockFall(EntityChangeBlockEvent e)
    {
        if (e.getEntityType() != EntityType.FALLING_BLOCK || e.getTo() != Material.GRAVEL)
            return;
        
        FallingBlock b = (FallingBlock) e.getEntity();
        if (b.getBlockData() != 1)
            return;
        
        griefMap.put(e.getBlock(), System.currentTimeMillis() + 300000);
    }
    
    @Override
    public void run()
    {
        long time = System.currentTimeMillis();
        List<Block> remove = new ArrayList<Block>();
        
        for (Entry<Block, Long> l : griefMap.entrySet())
            if (l.getValue() < time)
            {
                l.getKey().setType(Material.AIR);
                remove.add(l.getKey());
            }
        
        for (Block b : remove)
            griefMap.remove(b);
    }

    public void removeAllBlocks()
    {
        for(Block b : griefMap.keySet())
            b.setType(Material.AIR);
        
        griefMap.clear();
    }
}
