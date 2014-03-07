package de.craftlancer.clutil.buildings;

import java.io.File;
import java.io.IOException;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class Building
{
    private Plugin plugin;
    private CuboidClipboard schematic;
    
    private int xmax;
    private int ymax;
    private int zmax;
    
    private Building(Plugin plugin)
    {
        this.plugin = plugin;
    }
    
    public Building(Plugin plugin, File file)
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
    
    public Building(Plugin plugin, CuboidClipboard schematic)
    {
        this(plugin);
        this.schematic = schematic;
        
        xmax = schematic.getWidth() - 1;
        ymax = schematic.getHeight() - 1;
        zmax = schematic.getLength() - 1;
    }
    
    public Building(Plugin plugin, String file)
    {
        this(plugin, new File(plugin.getDataFolder(), file));
    }
    
    public void createPreview(Player player, Block initialBlock)
    {
        createPreview(player, initialBlock, 100L);
    }
    
    @SuppressWarnings("deprecation")
    public void createPreview(Player player, Block initialBlock, long ticks)
    {
        LocalWorld world = null;
        
        for (LocalWorld w : WorldEdit.getInstance().getServer().getWorlds())
            if (w.getName().equals(initialBlock.getWorld().getName()))
            {
                world = w;
                break;
            }
        
        if (world == null)
            throw new NullPointerException("The world should never be null!");
        
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
                        
                        player.sendBlockChange(b.getLocation(), b.getType(), (byte) b.getData());
                    }
        }
    }

    public CuboidClipboard getClipboard()
    {
        return schematic;
    }
}
