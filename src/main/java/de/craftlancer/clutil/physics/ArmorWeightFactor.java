package de.craftlancer.clutil.physics;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ArmorWeightFactor implements WeigthFactor
{
    /*
     * Total weight:
     * Leather: 12
     * Chain: 24
     * Gold: 36
     * Iron: 48
     * Diamond: 72 
     * 
     */
    
    private Map<Material, Integer> weightPerArmor = new HashMap<Material, Integer>();
    
    public ArmorWeightFactor(Plugin plugin)
    {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "armorWeight.yml"));
        
        for (String key : config.getKeys(false))
        {
            Material mat = Material.matchMaterial(key);
            if (mat == null)
                continue;
            weightPerArmor.put(mat, config.getInt(key, 0));
        }
    }
    
    @Override
    public int calculate(Player player)
    {
        int value = 0;
        
        for (ItemStack item : player.getEquipment().getArmorContents())
            value += getValue(item);
        
        return value;
    }
    
    private int getValue(ItemStack item)
    {
        return weightPerArmor.get(item.getType());
    }
}
