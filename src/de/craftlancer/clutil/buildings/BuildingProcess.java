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
    
    private List<ItemStack> initialCosts = new ArrayList<ItemStack>();
    
    private BuildState buildState;
    private int x = 0;
    private int y = 0;
    private int z = 0;
    
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
        
        CuboidClipboard build = building.getClipboard();
        
        this.block = player.getLocation().getBlock().getRelative(build.getOffset().getBlockX(), 0, build.getOffset().getBlockZ());
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
        
        CuboidClipboard schematic = building.getClipboard();
        
        for (int i = 0; i < blocksPerTick; i++)
        {
            if (x == building.getMaxX() && y == building.getMaxY() && z == building.getMaxZ())
            {
                buildState = BuildState.FINISHED;
                
                if (building.getFeatureBuilding() != null)
                    building.getFeatureBuilding().place(block);
                
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
            
            if (x == building.getMaxX())
            {
                x = 0;
                if (z == building.getMaxZ())
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
        
        if (b.getX() >= block.getX() && b.getX() <= block.getX() + building.getMaxX())
            if (b.getY() >= block.getY() && b.getY() <= block.getY() + building.getMaxY())
                if (b.getZ() >= block.getZ() && b.getZ() <= block.getZ() + building.getMaxZ())
                    return true;
        
        return false;
    }
    
}
