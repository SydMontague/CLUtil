package de.craftlancer.clutil.buildings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.clutil.CLUtil;

/*
 * Building Definition - YAML
 * 
 * key:                                 #name of the building
 *   schematic: <file path>             #path of the schematic file
 *   initialCostMod: <double>           #same named variable
 *   facing: <facing>                   #the direction the player looked when taking the schematic, used for rotation (default: SOUTH)
 *   staticCosts:                       #a list of static start costs, format: <material> <data> <amount>
 *     - <Material> <Data> <Amount>
 *   feature:                           #defines the feature that will be automaticly attached to the building
 *     name: <name>                     #the name of the feature, defined in another file
 *     <block>: <x> <y> <z> <southData> <westData> <northData> <eastData>            #coordinates relative to the schematic
 */
public class BuildingManager implements Listener
{
    private static BuildingManager instance;
    
    private long defaultPeriod = 2L;
    private int blocksPerPeriod = 50;
    
    private CLUtil plugin;
    private Map<String, Building> buildings = new HashMap<String, Building>();
    private List<BuildingProcess> processes = new ArrayList<BuildingProcess>();
    
    private File configFile;
    private FileConfiguration config;
    
    private BuildingManager(CLUtil plugin)
    {
        configFile = new File(plugin.getDataFolder(), "buildings.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        this.plugin = plugin;
        
        loadBuildings();
        
    }
    
    private void loadBuildings()
    {
        for (String key : config.getKeys(false))
        {
            String schematic = config.getString(key + ".schematic");
            double initialCostMod = config.getDouble(key + ".initialCostMod", 0D);
            BlockFace baseFacing = BlockFace.valueOf(config.getString(key + ".facing", "SOUTH"));
            
            List<ItemStack> staticCosts = new ArrayList<ItemStack>();
            for (Object o : config.getList(key + ".staticCosts"))
                staticCosts.add(getItemStack(o));
            
            String type = null;
            Map<String, RelativeLocation> blockLoc = new HashMap<String, RelativeLocation>();
            
            for (String s : config.getConfigurationSection(key + ".feature").getKeys(false))
            {
                if (s.equalsIgnoreCase("type"))
                    type = config.getString(key + ".feature." + s);
                else
                    blockLoc.put(s, RelativeLocation.parseString(config.getString(key + ".feature." + s)));
            }
            
            FeatureBuilding feature = FeatureFactory.loadFeature(type, blockLoc);
            
            buildings.put(key, new Building(plugin, schematic, initialCostMod, baseFacing, staticCosts, feature));
        }
        
    }
    
    @Deprecated
    private ItemStack getItemStack(Object o)
    {
        String s = o.toString();
        
        String[] arr = s.split(" ");
        
        if (arr.length != 3)
            throw new IllegalArgumentException();
        
        Material mat = Material.getMaterial(arr[0]);
        short data = Short.parseShort(arr[1]);
        int amount = Integer.parseInt(arr[2]);
        
        return new ItemStack(mat, amount, data);
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
    
    protected FileConfiguration getConfig()
    {
        return config;
    }
    
    protected void save()
    {
        try
        {
            config.save(configFile);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
   
}
