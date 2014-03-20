package de.craftlancer.clutil.buildings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import de.craftlancer.clutil.CLUtil;

public class Building
{
    private Plugin plugin;
    
    private File file;
    private BlockFace baseFacing;
    
    private double initialCostMod;
    private List<ItemStack> staticCosts = new ArrayList<ItemStack>();
    
    private FeatureBuilding feature;
    
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
        this(plugin, new File(plugin.getDataFolder(), file));
    }
    
    public Building(CLUtil plugin, String schematic, double initialCostMod, BlockFace baseFacing, List<ItemStack> staticCosts, FeatureBuilding feature)
    {
        this(plugin, schematic);
        
        this.baseFacing = baseFacing;
        this.initialCostMod = initialCostMod;
        this.staticCosts = staticCosts;
        this.feature = feature;
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
}
