package de.craftlancer.clutil.buildings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.Countable;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;

import de.craftlancer.core.Utils;
import de.craftlancer.groups.Town;
import de.craftlancer.groups.managers.PlayerManager;
import de.craftlancer.groups.managers.TownManager;

public class BuildingProcess extends BukkitRunnable implements ConfigurationSerializable
{
    // TOTEST save to config when not finished
    private Town owner;
    
    private Building building;
    private Block block;
    private MassChestInventory inventory;
    private int blocksPerTick;
    private List<BlockState> undoList = new ArrayList<BlockState>();
    private CuboidClipboard schematic;
    
    private List<ItemStack> initialCosts = new ArrayList<ItemStack>();
    private Map<Material, Integer> alreadyPaid = new HashMap<Material, Integer>();
    
    private BuildState buildState;
    private int blocksSet = 0;
    private int x = 0;
    private int y = 0;
    private int z = 0;
    
    private int xmax;
    private int ymax;
    private int zmax;
    
    private int playerFacing;
    
    /*
     * public BuildingProcess(Building building, Block block)
     * {
     * this(building, block, null);
     * }
     * public BuildingProcess(Building building, Block block, Inventory inventory)
     * {
     * this(building, block, inventory, BuildingManager.getInstance().getBlocksPerTick());
     * }
     */
    
    public BuildingProcess(Building building, Player player, MassChestInventory inventory, int blocksPerTick)
    {
        this.owner = PlayerManager.getGroupPlayer(player.getName()).getTown();
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
            default:
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
    
    public BuildingProcess(Building building, Player player, MassChestInventory inventory)
    {
        this(building, player, inventory, BuildingManager.getInstance().getBlocksPerTick());
    }
    
    /*
     * public BuildingProcess(Building building, Block block, Inventory inventory, int blocksPerTick)
     * {
     * this.building = building;
     * this.block = block;
     * this.inventory = inventory;
     * this.blocksPerTick = blocksPerTick;
     * buildState = BuildState.BUILDING;
     * }
     */
    
    public void undo()
    {
        for (BlockState state : undoList)
            state.update(true);
        
        // TOTEST regrant resource usage
        for (Entry<Material, Integer> entry : alreadyPaid.entrySet())
            if(entry.getKey() != Material.AIR)
            block.getWorld().dropItem(block.getLocation(), new ItemStack(entry.getKey(), entry.getValue()));
        
        undoList.clear();
        buildState = BuildState.REMOVED;
    }
    
    public void prepareForShutdown()
    {
        this.cancel();
        for (BlockState state : undoList)
            state.update(true);
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
                
                blocksSet++;
                return;
            }
            
            BaseBlock b = schematic.getBlock(new Vector(x, y, z));
            
            if (inventory == null || b.getType() == 0 || CraftItYourself.removeItemFromInventory(inventory, new ItemStack(b.getType(), 1)))
            {
                BlockState orgiBlock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z).getState();
                
                if (b.getType() == 0 && orgiBlock.getType() == Material.AIR)
                    i--;
                else
                {
                    undoList.add(orgiBlock);
                    world.setBlock(new Vector(block.getX() + x, block.getY() + y, block.getZ() + z), b, false);
                    increasePaid(Material.matchMaterial(String.valueOf(b.getType())));
                    
                    for (Player p : Bukkit.getOnlinePlayers())
                        p.sendBlockChange(new Location(block.getWorld(), block.getX() + x, block.getY() + y, block.getZ() + z), b.getType(), (byte) b.getData());
                }
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
            
            blocksSet++;
        }
    }
    
    private void increasePaid(Material matchMaterial)
    {
        if (!alreadyPaid.containsKey(matchMaterial))
            alreadyPaid.put(matchMaterial, 0);
        
        alreadyPaid.put(matchMaterial, alreadyPaid.get(matchMaterial) + 1);
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
    
    public BuildState getState()
    {
        return buildState;
    }
    
    public Town getOwningTown()
    {
        return owner;
    }
    
    public Building getBuilding()
    {
        return building;
    }
    
    public int getBlocksSet()
    {
        return blocksSet;
    }
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put("owner", owner.getName());
        map.put("building", building.getName());
        map.put("block", Utils.getLocationString(block.getLocation()));
        
        Set<String> list = new HashSet<String>();
        for (Inventory inv : inventory.getInventories())
        {
            InventoryHolder holder = inv.getHolder();
            if (holder instanceof DoubleChest)
            {
                list.add(Utils.getLocationString(((Chest) ((DoubleChest) holder).getLeftSide()).getLocation()));
                list.add(Utils.getLocationString(((Chest) ((DoubleChest) holder).getRightSide()).getLocation()));
            }
            else
                list.add(Utils.getLocationString(((Chest) holder).getLocation()));
        }
        
        map.put("inventory", new ArrayList<String>(list));
        map.put("blocksPerTick", blocksPerTick);
        map.put("blocksSet", blocksSet);
        map.put("playerFacing", playerFacing);
        
        return map;
    }
    
    /**
     * Deserialize
     * 
     * @param map
     */
    public BuildingProcess(Map<String, Object> map)
    {
        String[] arr = { "owner", "building", "block", "inventory", "blocksPerTick", "blocksSet", "playerFacing" };
        
        for (String key : arr)
            if (!map.containsKey(key))
                throw new IllegalArgumentException("The given map is not suitable to be deserialized to a BuildingProcess");
        
        owner = TownManager.getTown(map.get("owner").toString());
        building = BuildingManager.getInstance().getBuilding(map.get("building").toString());
        schematic = building.getClipboard();
        
        block = Utils.parseLocation(map.get("block").toString()).getBlock();
        Object o = map.get("inventory");
        
        Set<Inventory> inv = new HashSet<Inventory>();
        for (Object obj : (List<?>) o)
            inv.add(((Chest) Utils.parseLocation(obj.toString()).getBlock().getState()).getInventory());
        
        inventory = new MassChestInventory(building.getName(), building.getName(), inv);
        
        blocksPerTick = (Integer) map.get("blocksPerTick");
        
        int facing = (Integer) map.get("playerFacing");
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
            default:
        }
        schematic.rotate2D(facing * 90);
        
        xmax = schematic.getWidth() - 1;
        ymax = schematic.getHeight() - 1;
        zmax = schematic.getLength() - 1;
        
        int localBlocksSet = (Integer) map.get("blocksSet");
        // x = (Integer) map.get("x");
        // y = (Integer) map.get("y");
        // z = (Integer) map.get("z");
        
        LocalWorld world = null;
        for (LocalWorld w : WorldEdit.getInstance().getServer().getWorlds())
            if (w.getName().equals(block.getWorld().getName()))
            {
                world = w;
                break;
            }
        
        if (world == null)
            throw new NullPointerException("This world should never be null!");
        
        for (int i = 0; i < localBlocksSet; i++)
        {
            if (x == xmax && y == ymax && z == zmax)
            {
                buildState = BuildState.FINISHED;
                
                if (building.getFeatureBuilding() != null)
                    building.getFeatureBuilding().place(block, null, playerFacing);
                
                this.blocksSet++;
                return;
            }
            
            BaseBlock b = schematic.getBlock(new Vector(x, y, z));
            
            BlockState orgiBlock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z).getState();
            
            //if (b.getType() == 0 && orgiBlock.getType() == Material.AIR)
            //    i--;
           // else
            {
                undoList.add(orgiBlock);
                world.setBlock(new Vector(block.getX() + x, block.getY() + y, block.getZ() + z), b, false);
                increasePaid(Material.matchMaterial(String.valueOf(b.getType())));
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
            
            this.blocksSet++;
        }
        
        this.buildState = BuildState.BUILDING;
    }
}
