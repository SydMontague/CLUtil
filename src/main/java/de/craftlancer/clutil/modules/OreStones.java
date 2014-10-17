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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.core.ValueWrapper;
import de.craftlancer.skilllevels.SkillLevels;

@SuppressWarnings("deprecation")
public class OreStones extends Module implements Listener
{
    private String levelSystem;
    public HashMap<Material, HashMap<Material, ValueWrapper>> oreStones;
    private static final byte DATA = (byte) 15;
    private SkillLevels slevel;
    
    public OreStones(CLUtil plugin)
    {
        super(plugin);
        
        PluginManager pm = plugin.getServer().getPluginManager();
        if (pm.getPlugin("SkillLevels") != null && pm.getPlugin("SkillLevels").isEnabled())
            slevel = (SkillLevels) pm.getPlugin("SkillLevels");
        else
            throw new UnknownDependencyException("Dependency 'SkillLevels' not found, but mandatory!");
        
        this.levelSystem = getConfig().getString("levelSystem");
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
        
        if (!e.getPlayer().hasPermission(getPermission()))
            return;
        
        int level = slevel.getLevelSystem(levelSystem).getUser(e.getPlayer()).getLevel();
        
        for (Entry<Material, ValueWrapper> ores : getValues(e.getPlayer().getItemInHand()))
            if (Math.random() <= ores.getValue().getValue(level))
                e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(ores.getKey(), 1));
    }
    
    public String getPermission()
    {
        return "cl.util.ore2stone";
    }
    
    private boolean hasTool(ItemStack item)
    {
        return oreStones.containsKey(item.getType());
    }
    
    private Set<Entry<Material, ValueWrapper>> getValues(ItemStack tool)
    {
        return oreStones.get(tool.getType()).entrySet();
    }
    
    private void loadOreStones()
    {
        oreStones = new HashMap<Material, HashMap<Material, ValueWrapper>>();
        for (String tool : getConfig().getKeys(false))
        {
            if (tool.equalsIgnoreCase("levelSystem"))
                continue;
            
            Material mat = Material.getMaterial(tool);
            if (mat == null)
                continue;
            
            HashMap<Material, ValueWrapper> helpmap = new HashMap<Material, ValueWrapper>();
            
            for (String ore : getConfig().getConfigurationSection(tool).getKeys(false))
            {
                Material mat2 = Material.getMaterial(ore);
                if (mat2 == null)
                    continue;
                
                helpmap.put(mat2, new ValueWrapper(getConfig().getString(tool + "." + ore, "0")));
            }
            
            oreStones.put(mat, helpmap);
        }
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.ORESTONES;
    }
}
