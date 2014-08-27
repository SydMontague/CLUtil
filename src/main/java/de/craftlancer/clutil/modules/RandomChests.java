package de.craftlancer.clutil.modules;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.server.v1_7_R4.EntityFallingBlock;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.TileEntityChest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftFallingSand;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

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
//TODO better chest tracking
public class RandomChests extends Module
{
    //TODO move to config
    private static final int RADIUS = 350;
    private static final int REMOVE_TIME = 15 * 1000 * 600;
    private static final double chancePerTick = 0.01;
    private static final int minValue = 100;
    private static final int randomValue = 400;
    
    protected Random random = new Random();
    private Location center = new Location(Bukkit.getServer().getWorlds().get(0), 80, 0, 50);
    
    private int maxValue = 0;
    private TreeMap<Integer, ItemStack> weightMap = new TreeMap<Integer, ItemStack>();
    private Map<ItemStack, Integer> valueMap = new HashMap<>();
    
    protected Map<Long, Point> removeTime = new HashMap<>();
    
    protected World world;
    
    public RandomChests(CLUtil plugin)
    {
        super(plugin);
        world = getPlugin().getServer().getWorlds().get(0);
        
        for (String key : getConfig().getKeys(false))
        {
            Material mat = Material.matchMaterial(getConfig().getString(key + ".material"));
            
            if (mat == null)
                continue;
            
            int amount = getConfig().getInt(key + ".amount", 1);
            short durability = (short) getConfig().getInt(key + ".durability", 0);
            String name = getConfig().getString(key + ".name", null);
            List<String> lore = getConfig().getStringList(key + ".lore");
            
            int value = getConfig().getInt(key + ".value", 1);
            int weight = getConfig().getInt(key + ".weight", 1);
            
            ItemStack item = new ItemStack(mat, amount, durability);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            if (getConfig().isConfigurationSection(key + ".enchantments"))
                for (String k : getConfig().getConfigurationSection(key + ".enchantments").getKeys(false))
                {
                    Enchantment ench = Enchantment.getByName(k);
                    if (ench == null)
                        continue;
                    
                    int level = getConfig().getInt(key + ".enchantments." + k);
                    
                    item.addUnsafeEnchantment(ench, level);
                }
            
            weightMap.put(maxValue, item);
            maxValue += weight;
            valueMap.put(item, value);
        }
        
        new ChestTask().runTaskTimer(plugin, 20, 20);
    }
    
    protected void spawnChest(int value)
    {
        
        int x = random.nextInt(RADIUS * 2) - RADIUS;
        int z = random.nextInt(RADIUS * 2) - RADIUS;
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
        
        removeTime.put(System.currentTimeMillis() + REMOVE_TIME, new Point(target.getBlockX(), target.getBlockZ()));
    }
    
    private void fillChest(int value, net.minecraft.server.v1_7_R4.ItemStack[] contents)
    {
        int failStreak = 0;
        int contentPointer = 0;
        
        while (failStreak < 5 && value > 0 && contentPointer < 27)
        {
            int rand = random.nextInt(maxValue);
            ItemStack item = weightMap.ceilingEntry(rand).getValue();
            
            int val = valueMap.get(item);
            
            if (val > value)
            {
                failStreak++;
                continue;
            }
            
            value -= val;
            contents[contentPointer] = CraftItemStack.asNMSCopy(item);
            contentPointer++;
            failStreak = 0;
        }
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.RANDOMCHESTS;
    }
    
    @Override
    public void onDisable()
    {
        for (Entry<Long, Point> time : removeTime.entrySet())
            world.getHighestBlockAt((int) time.getValue().getX(), (int) time.getValue().getY()).setType(Material.AIR);
    }
    
    class ChestTask extends BukkitRunnable
    {
        
        @Override
        public void run()
        {
            Set<Long> removeSet = new HashSet<Long>();
            for (Entry<Long, Point> time : removeTime.entrySet())
            {
                if (time.getKey() < System.currentTimeMillis())
                {
                    world.getHighestBlockAt((int) time.getValue().getX(), (int) time.getValue().getY()).setType(Material.AIR);
                    removeSet.add(time.getKey());
                }
            }
            
            for(Long l : removeSet)
                removeTime.remove(l);
            
            if (Math.random() <= chancePerTick)
                spawnChest(minValue + random.nextInt(randomValue));
        }
        
    }
}
