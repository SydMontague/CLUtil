package de.craftlancer.clutil.buildings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.Countable;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.groups.Town;
import de.craftlancer.groups.managers.PlotManager;

public class Building
{
    private Plugin plugin;
    
    private String name;
    private File file;
    private BlockFace baseFacing;
    
    private double initialCostMod;
    private List<ItemStack> staticCosts = new ArrayList<ItemStack>();
    
    private FeatureBuilding feature;
    private List<String> categories = new ArrayList<String>();
    
    private int numBlocks;
    private int width;
    private int height;
    private int lenght;
    private String description;
    
    private boolean isBuildable;
    
    private Building(Plugin plugin)
    {
        this.plugin = plugin;
    }
    
    private Building(Plugin plugin, File file)
    {
        this(plugin);
        
        this.file = file;
        
        try
        {
            SchematicFormat.MCEDIT.load(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not load given schematic File!");
        }
    }
    
    private Building(Plugin plugin, String file)
    {
        this(plugin, new File(plugin.getDataFolder(), "schematics" + File.separator + file));
    }
    
    @SuppressWarnings("deprecation")
    public Building(CLUtil plugin, String name, String schematic, double initialCostMod, BlockFace baseFacing, List<ItemStack> staticCosts, FeatureBuilding feature, List<String> categories, String description)
    {
        this(plugin, schematic);
        
        this.name = name;
        this.baseFacing = baseFacing;
        this.initialCostMod = initialCostMod;
        this.staticCosts = staticCosts;
        this.feature = feature;
        this.categories = categories;
        this.description = description;
        
        CuboidClipboard clip = getClipboard();
        for (Countable<Integer> i : clip.getBlockDistribution())
        {
            if (i.getID() == Material.AIR.getId())
                continue;
            
            numBlocks += i.getAmount();
        }
        
        width = clip.getWidth();
        height = clip.getHeight();
        lenght = clip.getLength();
    }
    
    @SuppressWarnings("deprecation")
    public void createPreview(Player player, Block initialBlock, long ticks)
    {
        if (player == null || initialBlock == null || ticks <= 0)
            throw new IllegalArgumentException(player + " " + initialBlock + " " + ticks);
        
        CuboidClipboard schematic = getClipboard();
        
        int facing = Math.abs((Math.round((player.getLocation().getYaw()) / 90)) % 4);
        
        switch (baseFacing)
        {
            case NORTH:
                facing += 2;
                break;
            case EAST:
                facing += 3;
                break;
            case SOUTH:
                facing += 0;
                break;
            case WEST:
                facing += 1;
                break;
            default:
        }
        schematic.rotate2D(facing * 90);
        
        initialBlock = initialBlock.getRelative(schematic.getOffset().getBlockX(), 0, schematic.getOffset().getBlockZ());
        int xmax = schematic.getWidth();
        int ymax = schematic.getHeight();
        int zmax = schematic.getLength();
        
        for (int x = 0; x < xmax; x++)
            for (int y = 0; y < ymax; y++)
                for (int z = 0; z < zmax; z++)
                {
                    BaseBlock b = schematic.getBlock(new Vector(x, y, z));
                    player.sendBlockChange(initialBlock.getRelative(x, y, z).getLocation(), b.getType(), (byte) b.getData());
                }
        
        new RemovePreviewTask(player, initialBlock, xmax, ymax, zmax).runTaskLater(plugin, ticks);
    }
    
    public void createPreview(Player player)
    {
        createPreview(player, 100L);
    }
    
    public void createPreview(Player player, long tick)
    {
        createPreview(player, player.getLocation().getBlock(), tick);
    }
    
    public CuboidClipboard getClipboard()
    {
        try
        {
            return SchematicFormat.MCEDIT.load(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (DataException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public Collection<ItemStack> getStaticCosts()
    {
        return staticCosts;
    }
    
    public double getInitialMod()
    {
        return initialCostMod;
    }
    
    public FeatureBuilding getFeatureBuilding()
    {
        return feature;
    }
    
    class RemovePreviewTask extends BukkitRunnable
    {
        private Player player;
        private Block initialBlock;
        
        private int xmax;
        private int ymax;
        private int zmax;
        
        public RemovePreviewTask(Player player, Block initialBlock, int xmax, int ymax, int zmax)
        {
            this.player = player;
            this.initialBlock = initialBlock;
            this.xmax = xmax;
            this.ymax = ymax;
            this.zmax = zmax;
        }
        
        @SuppressWarnings("deprecation")
        @Override
        public void run()
        {
            for (int x = 0; x <= xmax; x++)
                for (int y = 0; y <= ymax; y++)
                    for (int z = 0; z <= zmax; z++)
                    {
                        Block b = initialBlock.getRelative(x, y, z);
                        
                        player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
                    }
        }
    }
    
    public BlockFace getBaseFacing()
    {
        return baseFacing;
    }
    
    public boolean hasCategory(String category)
    {
        for (String s : categories)
            if (s.equalsIgnoreCase(category))
                return true;
        
        return false;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getNumBlocks()
    {
        return numBlocks;
    }
    
    public int getTotalBlocks()
    {
        return width * height * lenght;
    }
    
    public String getSizeString()
    {
        return width + "x" + height + "x" + lenght;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public boolean isInTown(Player player, Town town)
    {
        CuboidClipboard schematic = getClipboard();
        Block initialBlock = player.getLocation().getBlock();
        
        int facing = Math.abs((Math.round((player.getLocation().getYaw()) / 90)) % 4);
        
        switch (baseFacing)
        {
            case NORTH:
                facing += 2;
                break;
            case EAST:
                facing += 3;
                break;
            case SOUTH:
                facing += 0;
                break;
            case WEST:
                facing += 1;
                break;
            default:
        }
        schematic.rotate2D(facing * 90);
        
        initialBlock = initialBlock.getRelative(schematic.getOffset().getBlockX(), 0, schematic.getOffset().getBlockZ());
        
        Location loc1 = initialBlock.getLocation();
        Location loc2 = new Location(loc1.getWorld(), loc1.getBlockX() + schematic.getWidth(), 0, loc1.getBlockZ() + schematic.getLength());
        
        int chunkXmin = Math.min(loc1.getChunk().getX(), loc2.getChunk().getX());
        int chunkXmax = Math.max(loc1.getChunk().getX(), loc2.getChunk().getX());
        int chunkZmin = Math.min(loc1.getChunk().getZ(), loc2.getChunk().getZ());
        int chunkZmax = Math.max(loc1.getChunk().getZ(), loc2.getChunk().getZ());
        
        for (int x = chunkXmin; x < chunkXmax; x++)
            for (int z = chunkZmin; z < chunkZmax; z++)
                if (!town.equals(PlotManager.getPlot(x, z, loc1.getWorld().getName()).getTown()))
                    return false;
        
        return true;
    }
    
    @SuppressWarnings("deprecation")
    public void save(FileConfiguration config)
    {
        config.set(getName() + ".schematic", file.getName());
        config.set(getName() + ".initialCostMod", initialCostMod);
        config.set(getName() + ".baseFacing", baseFacing.name());
        
        List<String> stat = new ArrayList<String>();
        for (ItemStack item : staticCosts)
            stat.add(item.getType().name() + " " + item.getData().getData() + " " + item.getAmount());
        
        config.set(getName() + ".staticCosts", stat);
        config.set(getName() + ".categories", categories);
        config.set(getName() + ".description", description);
        
        config.set(getName() + ".schematic", file.getName());
        
        if (feature != null)
            feature.save(getName(), config);
    }
    
    public boolean isBuildable(boolean bool)
    {
        return isBuildable;
    }
    
    public void setBuildable(boolean bool)
    {
        this.isBuildable = bool;
    }
    
    public void setDescription(String string)
    {
        this.description = string;
    }
}
