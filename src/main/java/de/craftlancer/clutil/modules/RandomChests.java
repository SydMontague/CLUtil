package de.craftlancer.clutil.modules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.minecraft.server.v1_7_R4.EntityFallingBlock;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.TileEntityChest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftFallingSand;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.clutil.ValueMap;
import de.craftlancer.core.Utils;

/*
 * key:
 *  material: material
 *  amount: int
 *  durability: int
 *  name: string
 *  lore: stringlist
 *  enchantments:
 *      ENCHANTMENT: level
 *  value: int
 *  weight: int
 * 
 * Random Zahl 0...maxValue, closest, bigger smaller, 
 */
public class RandomChests extends Module
{
    protected Random random = new Random();
    
    private int radius;
    private long removeTime;
    private double chancePerTick;
    private int minValue;
    private int randomValue;
    
    private Location center = new Location(Bukkit.getServer().getWorlds().get(0), 80, 0, 50);
    
    private ValueMap valueMap;
    protected Map<Location, Long> removeMap = new HashMap<>();
    
    protected World world;
    
    public RandomChests(CLUtil plugin)
    {
        super(plugin);
        world = getPlugin().getServer().getWorlds().get(0);
        
        radius = getConfig().getInt("radius", 350);
        removeTime = getConfig().getLong("removeTime", 1800L) * 1000L;
        chancePerTick = getConfig().getDouble("chancePerTick", 0.015);
        minValue = getConfig().getInt("minValue", 100);
        randomValue = getConfig().getInt("randomValue", 400);
        
        center = Utils.parseLocation(getConfig().getString("center", "80 0 50 world"));
        
        valueMap = new ValueMap(getConfig().getConfigurationSection("items"));
        new ChestTask().runTaskTimer(plugin, 20, 20);
    }
    
    protected void spawnChest(int value)
    {
        
        int x = random.nextInt(radius * 2) - radius;
        int z = random.nextInt(radius * 2) - radius;
        // int d = (int) Math.sqrt(localRadius * localRadius - x * x);
        // int z = d == 0 ? 0 : random.nextInt(d * 2) - d;
        
        Location target = new Location(center.getWorld(), center.getBlockX() + x, 260, center.getBlockZ() + z);
        
        switch (center.getWorld().getHighestBlockAt(target).getType())
        {
            case LAVA:
            case WATER:
            case STATIONARY_WATER:
            case STATIONARY_LAVA:
                return;
            default:
                break;
        }
        
        @SuppressWarnings("deprecation")
        FallingBlock entity = target.getWorld().spawnFallingBlock(target, Material.CHEST, (byte) 0);
        
        EntityFallingBlock nmsEntity = ((CraftFallingSand) entity).getHandle();
        
        TileEntityChest tileChest = new TileEntityChest();
        
        fillChest(value, tileChest.getContents());
        
        NBTTagCompound nbt = new NBTTagCompound();
        tileChest.b(nbt);
        nmsEntity.tileEntityData = nbt;
        
        center.getWorld().strikeLightningEffect(target.getWorld().getHighestBlockAt(target).getLocation());
    }
    
    @EventHandler
    public void onEntityBlockChange(EntityChangeBlockEvent event)
    {
        if (event.getEntityType() != EntityType.FALLING_BLOCK)
            return;
        
        if (event.getTo() != Material.CHEST)
            return;
        
        removeMap.put(event.getBlock().getLocation(), System.currentTimeMillis() + removeTime);
    }
    
    private void fillChest(int value, net.minecraft.server.v1_7_R4.ItemStack[] contents)
    {
        List<ItemStack> items = valueMap.getRandomItems(value, 27);
        
        for (int i = 0; i < items.size() && i < 27; i++)
            contents[i] = CraftItemStack.asNMSCopy(items.get(i));
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.RANDOMCHESTS;
    }
    
    @Override
    public void onDisable()
    {
        for (Entry<Location, Long> time : removeMap.entrySet())
            time.getKey().getBlock().setType(Material.AIR);
    }
    
    public double getChancePerTick()
    {
        return chancePerTick;
    }
    
    public int getMinValue()
    {
        return minValue;
    }
    
    public int getRandomValue()
    {
        return randomValue;
    }
    
    class ChestTask extends BukkitRunnable
    {
        @Override
        public void run()
        {
            Set<Location> removeSet = new HashSet<>();
            for (Entry<Location, Long> time : removeMap.entrySet())
                if (time.getValue() < System.currentTimeMillis())
                {
                    time.getKey().getBlock().setType(Material.AIR);
                    removeSet.add(time.getKey());
                }
            
            for (Location l : removeSet)
                removeMap.remove(l);
            
            if (Math.random() <= getChancePerTick())
                spawnChest(getMinValue() + random.nextInt(getRandomValue()));
        }
        
    }
}
