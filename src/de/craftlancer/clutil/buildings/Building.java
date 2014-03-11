package de.craftlancer.clutil.buildings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.block.Block;
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
    private CuboidClipboard schematic;
    
    private int xmax;
    private int ymax;
    private int zmax;
    
    private double initialCostMod;
    private List<ItemStack> staticCosts = new ArrayList<ItemStack>();
    
    private FeatureBuilding feature;
    
    // TODO Static Costs + initialCostMod
    // TODO config loading and saving
    // TODO FeatureBlocks
    
    private Building(Plugin plugin)
    {
        this.plugin = plugin;
    }
    
    private Building(Plugin plugin, File file)
    {
        this(plugin);
        try
        {
            schematic = SchematicFormat.MCEDIT.load(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (DataException e)
        {
            e.printStackTrace();
        }
        
        xmax = schematic.getWidth() - 1;
        ymax = schematic.getHeight() - 1;
        zmax = schematic.getLength() - 1;
    }
    
    private Building(Plugin plugin, String file)
    {
        this(plugin, new File(plugin.getDataFolder(), file));
    }
    
    public Building(CLUtil plugin, String schematic, double initialCostMod, List<ItemStack> staticCosts, FeatureBuilding feature)
    {
        this(plugin, schematic);
        
        this.initialCostMod = initialCostMod;
        this.staticCosts = staticCosts;
        this.feature = feature;
    }
    
    public void createPreview(Player player, Block initialBlock)
    {
        createPreview(player, initialBlock, 100L);
    }
    
    @SuppressWarnings("deprecation")
    public void createPreview(Player player, Block initialBlock, long ticks)
    {
        if (player == null || initialBlock == null || ticks <= 0)
            throw new IllegalArgumentException(player + " " + initialCostMod + " " + ticks);
        
        for (int x = 0; x <= xmax; x++)
            for (int y = 0; y <= ymax; y++)
                for (int z = 0; z <= zmax; z++)
                {
                    BaseBlock b = schematic.getBlock(new Vector(x, y, z));
                    player.sendBlockChange(initialBlock.getRelative(x, y, z).getLocation(), b.getType(), (byte) b.getData());
                }
        
        new RemovePreviewTask(player, initialBlock).runTaskLater(plugin, ticks);
    }
    
    public void createPreview(Player player)
    {
        createPreview(player, player.getLocation().getBlock().getRelative(schematic.getOffset().getBlockX(), 0, schematic.getOffset().getBlockZ()));
    }
    
    public void createPreview(Player player, long tick)
    {
        createPreview(player, player.getLocation().getBlock().getRelative(schematic.getOffset().getBlockX(), 0, schematic.getOffset().getBlockZ()), tick);
    }
    
    public int getMaxX()
    {
        return xmax;
    }
    
    public int getMaxY()
    {
        return ymax;
    }
    
    public int getMaxZ()
    {
        return zmax;
    }
    
    public CuboidClipboard getClipboard()
    {
        return schematic;
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
        
        public RemovePreviewTask(Player player, Block initialBlock)
        {
            this.player = player;
            this.initialBlock = initialBlock;
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
}
