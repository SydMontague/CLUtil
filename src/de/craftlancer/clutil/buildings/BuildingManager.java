package de.craftlancer.clutil.buildings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;

import de.craftlancer.clutil.CLUtil;

public class BuildingManager implements Listener
{
    private static BuildingManager instance;
    
    private long defaultPeriod = 20L;
    private int blocksPerPeriod = 5;
    
    private CLUtil plugin;
    private Map<String, Building> buildings = new HashMap<String, Building>();
    private List<BuildingProcess> processes = new ArrayList<BuildingProcess>();
    
    private BuildingManager(CLUtil plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        this.plugin = plugin;
        buildings.put("gasthaus", new Building(plugin, new File(plugin.getDataFolder(), "gasthaus.schematic")));
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e)
    {
        handleBlockEvent(e);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockPlaceEvent e)
    {
        handleBlockEvent(e);
    }
    
    public <T extends BlockEvent & Cancellable> void handleBlockEvent(T event)
    {
        for (BuildingProcess pro : processes)
            if (pro.isProtected(event.getBlock()))
                event.setCancelled(true);
    }
    
    public void previewBuilding(Player player, String building)
    {
        getBuilding(building).createPreview(player);
    }
    
    public static synchronized BuildingManager getInstance()
    {
        if (instance == null)
            instance = new BuildingManager(CLUtil.getInstance());
        
        return instance;
    }
    
    public void startBuilding(Player player, String building, Inventory inventory)
    {
        startBuilding(player, building, inventory, getDefaultPeriod());
    }
    
    public void startBuilding(Player player, String building, Inventory inventory, long period)
    {
        BuildingProcess proc = new BuildingProcess(getBuilding(building), player, inventory);
        
        processes.add(proc);
        proc.runTaskTimer(plugin, period, period);
    }
    
    public void startBuilding(Player player, String building)
    {
        startBuilding(player, building, player.getInventory());
    }
    
    public Building getBuilding(String name)
    {
        return buildings.get(name);
    }
    
    public void undoLastBuilding()
    {
        if (processes.size() == 0)
            return;
        
        undoBuilding(processes.size() - 1);
    }
    
    public void undoBuilding(int index)
    {
        if (index < 0)
            throw new IllegalArgumentException("Index can't be < 0!");
        
        processes.get(index).undo();
        processes.remove(index);
    }
    
    public int getBlocksPerTick()
    {
        return blocksPerPeriod;
    }
    
    public long getDefaultPeriod()
    {
        return defaultPeriod;
    }
}
