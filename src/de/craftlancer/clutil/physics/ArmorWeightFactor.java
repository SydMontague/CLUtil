package de.craftlancer.clutil.physics;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArmorWeightFactor implements WeigthFactor
{
    private Map<Material, Integer> weightPerArmor = new HashMap<Material, Integer>();
    
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
