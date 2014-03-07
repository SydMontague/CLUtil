package de.craftlancer.clutil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class BuildingTest extends BukkitRunnable implements Listener
{
    private static final int BLOCKS_PER_RUN = 20;
    private static final long PERIOD = 1L;
    
    private CLUtil plugin;
    private boolean started = false;
    private Block loc;
    
    private CuboidClipboard build;
    private Inventory inventory;
    
    private int x = 0;
    private int y = 0;
    private int z = 0;
    
    private int xmax;
    private int ymax;
    private int zmax;
    
    private List<BlockState> undoList = new ArrayList<BlockState>();
    
    public BuildingTest(CLUtil plugin)
    {
        this.plugin = plugin;
        try
        {
            build = SchematicFormat.MCEDIT.load(new File(plugin.getDataFolder(), "gasthaus.schematic"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (DataException e)
        {
            e.printStackTrace();
        }
        
        this.runTaskTimer(plugin, PERIOD, PERIOD);
    }
    
    class FakeTask extends BukkitRunnable
    {
        Player player;
        
        public FakeTask(Plugin plugin, Player player)
        {
            try
            {
                build = SchematicFormat.MCEDIT.load(new File(plugin.getDataFolder(), "gasthaus.schematic"));
            }
            catch (IOException ee)
            {
                ee.printStackTrace();
            }
            catch (DataException ee)
            {
                ee.printStackTrace();
            }
            
            this.player = player;
            loc = player.getLocation().getBlock().getRelative(build.getOffset().getBlockX(), 0, build.getOffset().getBlockZ());
            
            xmax = build.getWidth() - 1;
            ymax = build.getHeight() - 1;
            zmax = build.getLength() - 1;
            for (int x = 0; x <= xmax; x++)
                for (int y = 0; y <= ymax; y++)
                    for (int z = 0; z <= zmax; z++)
                    {
                        BaseBlock b = build.getBlock(new Vector(x, y, z));
                        LocalWorld world = null;
                        
                        for (LocalWorld w : WorldEdit.getInstance().getServer().getWorlds())
                            if (w.getName().equals(loc.getWorld().getName()))
                            {
                                world = w;
                                break;
                            }
                        
                        if (world == null)
                            throw new NullPointerException("This world should never be null!");
                        
                        player.sendBlockChange(loc.getRelative(x, y, z).getLocation(), b.getType(), (byte) b.getData());
                    }
            
            this.runTaskLater(plugin, 200L);
        }
        
        @Override
        public void run()
        {
            for (int x = 0; x <= xmax; x++)
                for (int y = 0; y <= ymax; y++)
                    for (int z = 0; z <= zmax; z++)
                    {
                        Block b = loc.getRelative(x, y, z);
                        
                        player.sendBlockChange(b.getLocation(), b.getType(), (byte) b.getData());
                    }
        }
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if (!e.hasItem())
            return;
        
        if (e.getItem().getType() == Material.GOLD_SWORD)
        {
            for (BlockState state : undoList)
                state.update(true);
            return;
        }
        else if (e.getItem().getType() == Material.DIAMOND_SWORD)
        {
            new FakeTask(plugin, e.getPlayer());
            return;
        }
        else if (e.getItem().getType() != Material.IRON_SWORD)
            return;
        
        try
        {
            build = SchematicFormat.MCEDIT.load(new File(plugin.getDataFolder(), "gasthaus.schematic"));
        }
        catch (IOException ee)
        {
            ee.printStackTrace();
        }
        catch (DataException ee)
        {
            ee.printStackTrace();
        }
        
        if (e.getItem() != null && e.getItem().getType() == Material.WOOD_AXE)
            return;
        
        int facing = Math.abs((Math.round((e.getPlayer().getLocation().getYaw()) / 90)) % 4);
        // TODO add base facing
        facing += 1;
        build.rotate2D(facing * 90);
        
        loc = e.getPlayer().getLocation().getBlock().getRelative(build.getOffset().getBlockX(), 0, build.getOffset().getBlockZ());
        inventory = e.getPlayer().getInventory();
        
        xmax = build.getWidth() - 1;
        ymax = build.getHeight() - 1;
        zmax = build.getLength() - 1;
        
        x = 0;
        y = 0;
        z = 0;
        
        started = true;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e)
    {
        if (isProtected(e.getBlock()))
            e.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e)
    {
        if (isProtected(e.getBlock()))
            e.setCancelled(true);
    }
    
    private boolean isProtected(Block b)
    {
        if (!started)
            return false;
        
        if (b.getX() >= loc.getX() && b.getX() <= loc.getX() + xmax)
            if (b.getY() >= loc.getY() && b.getY() <= loc.getY() + ymax)
                if (b.getZ() >= loc.getZ() && b.getZ() <= loc.getZ() + zmax)
                    return true;
        
        return false;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void run()
    {
        if (!started)
            return;
        
        for (int i = 0; i < BLOCKS_PER_RUN; i++)
        {
            if (x == xmax && y == ymax && z == zmax)
            {
                started = false;
                
                return;
            }
            
            BaseBlock b = build.getBlock(new Vector(x, y, z));
            LocalWorld world = null;
            
            for (LocalWorld w : WorldEdit.getInstance().getServer().getWorlds())
                if (w.getName().equals(loc.getWorld().getName()))
                {
                    world = w;
                    break;
                }
            
            if (world == null)
                throw new NullPointerException("This world should never be null!");
            
            // world.set
            // Block bb = loc.getRelative(x, y, z);
            // TODO support for NBT datas
            
            if (b.getType() == 0 || CraftItYourself.removeItemFromInventory(inventory, new ItemStack(b.getType(), 1)))
            {
                undoList.add(loc.getWorld().getBlockAt(loc.getX() + x, loc.getY() + y, loc.getZ() + z).getState());
                world.setBlock(new Vector(loc.getX() + x, loc.getY() + y, loc.getZ() + z), b, false);// bb.setTypeIdAndData(b.getType(),
                                                                                                     // (byte)
                                                                                                     // b.getData(),
                                                                                                     // false);
            }
            else
            {
                Bukkit.getLogger().info("" + Material.getMaterial(b.getType()));
                return;
            }
            
            if (x == xmax)
            {
                x = 0;
                if (z == zmax)
                {
                    y++;
                    z = 0;
                }
                else
                    z++;
            }
            else
                x++;
        }
        
        // TODO Auto-generated method stub
        
    }
}
