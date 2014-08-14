package de.craftlancer.clutil.modules;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

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

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

@SuppressWarnings("deprecation")
public class OreStones extends Module implements Listener
{
    public HashMap<Material, HashMap<Material, Double>> oreStones;
    private static final byte DATA = (byte) 15;
    
    public OreStones(CLUtil plugin)
    {
        super(plugin);
        loadOreStones();
        getPlugin().getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCleanstoneGen(BlockFromToEvent e)
    {
        Material from = e.getBlock().getType();
        final Block to = e.getToBlock();
        
        if ((from == Material.LAVA || from == Material.STATIONARY_LAVA) && (e.getFace() == BlockFace.DOWN && to.getType() == Material.WATER || to.getType() == Material.STATIONARY_WATER))
            getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), new Runnable()
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
        
        if (!hasTool(e.getPlayer().getItemInHand()))
            return;
        
        for (Entry<Material, Double> ores : getValues(e.getPlayer().getItemInHand()))
            if (Math.random() <= ores.getValue())
                e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(ores.getKey(), 1));
    }
    
    private boolean hasTool(ItemStack item)
    {
        return oreStones.containsKey(item.getType());
    }
    
    private Set<Entry<Material, Double>> getValues(ItemStack tool)
    {
        return oreStones.get(tool.getType()).entrySet();
    }
    
    private void loadOreStones()
    {
        oreStones = new HashMap<Material, HashMap<Material, Double>>();
        for (String tool : getConfig().getKeys(false))
        {
            Material mat = Material.getMaterial(tool);
            if (mat == null)
                continue;
            
            HashMap<Material, Double> helpmap = new HashMap<Material, Double>();
            
            for (String ore : getConfig().getConfigurationSection(tool).getKeys(false))
            {
                Material mat2 = Material.getMaterial(ore);
                if (mat2 == null)
                    continue;
                
                helpmap.put(mat2, getConfig().getDouble(tool + "." + ore, 0D));
            }
            
            oreStones.put(mat, helpmap);
        }
    }
    
    @Override
    public ModuleType getName()
    {
        return ModuleType.ORESTONES;
    }
}
