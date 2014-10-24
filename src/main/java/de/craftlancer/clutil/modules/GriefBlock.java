package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

@SuppressWarnings("deprecation")
public class GriefBlock extends Module implements Listener
{
    private static final byte DATA = 1;
    private final long checkPerTicks;
    private final long delay;
    private HashMap<Block, Long> griefMap = new HashMap<Block, Long>();
    
    public GriefBlock(CLUtil plugin)
    {
        super(plugin);
        this.delay = getConfig().getLong("delay", 300000);
        this.checkPerTicks = getConfig().getLong("checkPerTicks", 20);
        runRunnable();
        
        ItemStack gravel = new ItemStack(Material.GRAVEL, 3);
        ItemMeta gmeta = gravel.getItemMeta();
        gmeta.setDisplayName("§4Griefblock");
        gravel.setItemMeta(gmeta);
        getPlugin().getServer().addRecipe(new ShapelessRecipe(gravel).addIngredient(3, Material.GRAVEL).addIngredient(Material.INK_SACK, 4));
        getPlugin().getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e)
    {
        if (e.getBlock().getType() != Material.GRAVEL || e.getBlock().getData() != DATA)
            return;
        
        e.getBlock().setType(Material.AIR);
        e.setCancelled(true);
        e.setExpToDrop(0);
        griefMap.remove(e.getBlock());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e)
    {
        if (e.getItemInHand().hasItemMeta() && e.getItemInHand().getItemMeta().hasDisplayName() && e.getItemInHand().getItemMeta().getDisplayName().equals("§4Griefblock"))
        {
            if (e.getBlock().getRelative(0, -1, 0).getType() == Material.AIR)
            {
                Location loc = e.getBlock().getLocation();
                loc.getWorld().spawnFallingBlock(loc, Material.GRAVEL, DATA);
                e.setCancelled(false);
                e.getBlock().setType(Material.AIR);
                loc.getWorld().playSound(loc, Sound.DIG_GRAVEL, 1F, 1F);
            }
            else
            {
                e.setCancelled(false);
                e.getBlock().setData(DATA);
                griefMap.put(e.getBlock(), System.currentTimeMillis() + delay);
            }
        }
    }
    
    @EventHandler
    public void onFallingBlockFall(EntityChangeBlockEvent e)
    {
        if (e.getEntityType() != EntityType.FALLING_BLOCK || e.getTo() != Material.GRAVEL)
            return;
        
        FallingBlock b = (FallingBlock) e.getEntity();
        if (b.getBlockData() != DATA)
            return;
        
        griefMap.put(e.getBlock(), System.currentTimeMillis() + delay);
    }
    
    private void runRunnable()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                long time = System.currentTimeMillis();
                List<Block> remove = new ArrayList<Block>();
                
                for (Entry<Block, Long> l : getGriefMap().entrySet())
                    if (l.getValue() < time)
                    {
                        if (l.getKey().getType() == Material.GRAVEL)
                            l.getKey().setType(Material.AIR);
                        remove.add(l.getKey());
                    }
                
                for (Block b : remove)
                    getGriefMap().remove(b);
            }
        }.runTaskTimer(getPlugin(), getDelay(), getDelay());
    }
    
    protected Map<Block, Long> getGriefMap()
    {
        return griefMap;
    }
    
    public void removeAllBlocks()
    {
        for (Block b : griefMap.keySet())
            b.setType(Material.AIR);
        
        griefMap.clear();
    }
    
    public long getDelay()
    {
        return checkPerTicks;
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.GRIEFBLOCK;
    }
}
