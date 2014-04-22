package de.craftlancer.clutil;

import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class OreStones implements Listener
{
    private CLUtil plugin;
    protected static byte DATA = (byte) 15;

    public OreStones(CLUtil plugin)
    {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCleanstoneGen(BlockFromToEvent e)
    {
        Material from = e.getBlock().getType();
        final Block to = e.getToBlock();
        
        if ((from == Material.LAVA || from == Material.STATIONARY_LAVA) && (e.getFace() == BlockFace.DOWN && to.getType() == Material.WATER || to.getType() == Material.STATIONARY_WATER))
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
            {
                @Override
                public void run()
                {
                    to.setData(OreStones.DATA);
                }
            }, 0L);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCleanstonePlace(BlockPlaceEvent e)
    {
        if (e.getBlockPlaced().getType() == Material.STONE)
            e.getBlockPlaced().setData(DATA);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCleanstoneBreak(BlockBreakEvent e)
    {
        
        if (!e.getBlock().getType().equals(Material.STONE) || e.getBlock().getData() == DATA)
            return;
        
        if (!plugin.oreStones.containsKey(e.getPlayer().getItemInHand().getType()))
            return;
        
        for (Entry<Material, Double> ores : plugin.oreStones.get(e.getPlayer().getItemInHand().getType()).entrySet())
            if (Math.random() <= ores.getValue())
                e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(ores.getKey(), 1));
    }
}
