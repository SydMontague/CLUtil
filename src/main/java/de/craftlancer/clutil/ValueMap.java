package de.craftlancer.clutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ValueMap
{
    private Random random = new Random();
    private int maxValue = 0;
    private TreeMap<Integer, ItemStack> weightMap = new TreeMap<Integer, ItemStack>();
    private Map<ItemStack, Integer> valueMap = new HashMap<>();
    
    public ValueMap(ConfigurationSection config)
    {
        if (config == null)
            return;
        
        for (String key : config.getKeys(false))
            if (config.isConfigurationSection(key))
                addItem(config.getConfigurationSection(key));
    }
    
    /**
     * add an item to the given ValueMap
     * 
     * @param config
     *            a configuration section, where the item definition is saved
     * @return true when the item was added, false when not (e.g. when an invalid material was given)
     */
    public boolean addItem(ConfigurationSection config)
    {
        Material mat = Material.matchMaterial(config.getString("material"));
        
        if (mat == null)
            return false;
        
        int amount = config.getInt("amount", 1);
        short durability = (short) config.getInt("durability", 0);
        String name = config.getString("name", null);
        List<String> lore = config.getStringList("lore");
        
        int value = config.getInt("value", 1);
        int weight = config.getInt("weight", 1);
        
        ItemStack item = new ItemStack(mat, amount, durability);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        if (config.isConfigurationSection("enchantments"))
            for (String k : config.getConfigurationSection("enchantments").getKeys(false))
            {
                Enchantment ench = Enchantment.getByName(k);
                if (ench == null)
                    continue;
                
                int level = config.getInt("enchantments." + k);
                
                if (mat == Material.ENCHANTED_BOOK)
                {
                    ((EnchantmentStorageMeta) meta).addStoredEnchant(ench, level, true);
                    item.setItemMeta(meta);
                }
                else
                    item.addUnsafeEnchantment(ench, level);
            }
        
        addItem(item, value, weight);
        
        return true;
    }
    
    public boolean addItem(ItemStack item, int value, int weight)
    {
        maxValue += weight;
        weightMap.put(maxValue, item);
        valueMap.put(item, value);
        
        return true;
    }
    
    public List<ItemStack> getRandomItems(int value, int maxItems)
    {
        List<ItemStack> list = new ArrayList<>();
        
        int failStreak = 0;
        
        while (failStreak < 5 && value > 0 && list.size() < maxItems)
        {
            ItemStack item = getEntry(random.nextInt(getMaxValue()));
            int val = getValue(item);
            
            if (val > value)
            {
                failStreak++;
                continue;
            }
            
            value -= val;
            list.add(item);
            failStreak = 0;
        }
        
        return list;
    }
    
    public int getMaxValue()
    {
        return maxValue;
    }
    
    public ItemStack getEntry(int rand)
    {
        return weightMap.ceilingEntry(rand).getValue();
    }
    
    public int getValue(ItemStack item)
    {
        return valueMap.get(item);
    }
}
