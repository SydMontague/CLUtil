package de.craftlancer.clutil.buildings;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.Countable;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;

import de.craftlancer.clutil.CraftItYourself;

public class BuildingProcess extends BukkitRunnable
{
    private Building building;
    private Block block;
    private Inventory inventory;
    private int blocksPerTick;
    private List<BlockState> undoList = new ArrayList<BlockState>();
    private CuboidClipboard schematic;
    
    private List<ItemStack> initialCosts = new ArrayList<ItemStack>();
    
    private BuildState buildState;
    private int x = 0;
    private int y = 0;
    private int z = 0;
    
    private int xmax;
    private int ymax;
    private int zmax;
    
    private int playerFacing;
    
    public BuildingProcess(Building building, Block block)
    {
        this(building, block, null);
    }
    
    public BuildingProcess(Building building, Block block, Inventory inventory)
    {
        this(building, block, inventory, BuildingManager.getInstance().getBlocksPerTick());
    }
    
    public BuildingProcess(Building building, Player player, Inventory inventory, int blocksPerTick)
    {
        this.building = building;
        
        this.schematic = building.getClipboard();
        
        int facing = Math.abs((Math.round((player.getLocation().getYaw()) / 90)) % 4);
        
        playerFacing = facing;
        
        switch (building.getBaseFacing())
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
        }
        schematic.rotate2D(facing * 90);
        
        xmax = schematic.getWidth() - 1;
        ymax = schematic.getHeight() - 1;
        zmax = schematic.getLength() - 1;
        
        this.block = player.getLocation().getBlock().getRelative(schematic.getOffset().getBlockX(), 0, schematic.getOffset().getBlockZ());
        this.inventory = inventory;
        this.blocksPerTick = blocksPerTick;
        
        initialCosts.addAll(building.getStaticCosts());
        for (Countable<BaseBlock> items : building.getClipboard().getBlockDistributionWithData())
            initialCosts.add(new ItemStack(Material.matchMaterial(Integer.toString(items.getID().getType())), (int) (items.getAmount() * building.getInitialMod()), (short) items.getID().getData()));
        
        buildState = BuildState.BUILDING;
    }
    
    public BuildingProcess(Building building, Player player, Inventory inventory)
    {
        this(building, player, inventory, BuildingManager.getInstance().getBlocksPerTick());
    }
    
    public BuildingProcess(Building building, Block block, Inventory inventory, int blocksPerTick)
    {
        this.building = building;
        this.block = block;
        this.inventory = inventory;
        this.blocksPerTick = blocksPerTick;
        
        buildState = BuildState.BUILDING;
    }
    
    public void undo()
    {
        for (BlockState state : undoList)
            state.update(true);
        
        undoList.clear();
        buildState = BuildState.UNDOED;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void run()
    {
        if (buildState != BuildState.BUILDING)
        {
            cancel();
            return;
        }
        
        if (inventory != null && !initialCosts.isEmpty())
        {
            List<ItemStack> initialsLeft = new ArrayList<ItemStack>();
            
            for (ItemStack item : initialCosts)
                if (!CraftItYourself.removeItemFromInventory(inventory, item))
                    initialsLeft.add(item);
            
            initialCosts = initialsLeft;
            
            for (ItemStack item : initialsLeft)
                Bukkit.getLogger().info(item.getType().name());
            return;
        }
        
        LocalWorld world = null;
        for (LocalWorld w : WorldEdit.getInstance().getServer().getWorlds())
            if (w.getName().equals(block.getWorld().getName()))
            {
                world = w;
                break;
            }
        
        if (world == null)
            throw new NullPointerException("This world should never be null!");
        
        for (int i = 0; i < blocksPerTick; i++)
        {
            if (x == xmax && y == ymax && z == zmax)
            {
                buildState = BuildState.FINISHED;
                
                if (building.getFeatureBuilding() != null)
                    building.getFeatureBuilding().place(block, null, playerFacing);
                
                return;
            }
            
            BaseBlock b = schematic.getBlock(new Vector(x, y, z));
            
            if (inventory == null || b.getType() == 0 || CraftItYourself.removeItemFromInventory(inventory, new ItemStack(b.getType(), 1)))
            {
                undoList.add(block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z).getState());
                world.setBlock(new Vector(block.getX() + x, block.getY() + y, block.getZ() + z), b, false);
                
                for (Player p : Bukkit.getOnlinePlayers())
                    p.sendBlockChange(new Location(block.getWorld(), block.getX() + x, block.getY() + y, block.getZ() + z), b.getType(), (byte) b.getData());
            }
            else
            {
                Bukkit.getLogger().info(Material.getMaterial(b.getType()).name());
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
    }
    
    public boolean isProtected(Block b)
    {
        
        if (buildState != BuildState.BUILDING)
            return false;
        
        if (b.getX() >= block.getX() && b.getX() <= block.getX() + xmax)
            if (b.getY() >= block.getY() && b.getY() <= block.getY() + ymax)
                if (b.getZ() >= block.getZ() && b.getZ() <= block.getZ() + zmax)
                    return true;
        
        return false;
    }
    
}
