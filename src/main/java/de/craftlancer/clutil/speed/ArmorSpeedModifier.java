package de.craftlancer.clutil.speed;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.speedapi.SpeedModifier;

public class ArmorSpeedModifier extends SpeedModifier
{
    private Map<Material, Double> slowArmor = new HashMap<Material, Double>();
    private Map<Material, String> slowPermission = new HashMap<Material, String>();
    
    public ArmorSpeedModifier(int priority, CLUtil plugin)
    {
        super(priority);
        
        slowArmor.put(Material.IRON_HELMET, plugin.getConfig().getDouble("ironspeed", 0.18D));
        slowArmor.put(Material.IRON_CHESTPLATE, plugin.getConfig().getDouble("ironspeed", 0.18D));
        slowArmor.put(Material.IRON_LEGGINGS, plugin.getConfig().getDouble("ironspeed", 0.18D));
        slowArmor.put(Material.IRON_BOOTS, plugin.getConfig().getDouble("ironspeed", 0.18D));
        slowArmor.put(Material.DIAMOND_HELMET, plugin.getConfig().getDouble("diaspeed", 0.16D));
        slowArmor.put(Material.DIAMOND_CHESTPLATE, plugin.getConfig().getDouble("diaspeed", 0.16D));
        slowArmor.put(Material.DIAMOND_LEGGINGS, plugin.getConfig().getDouble("diaspeed", 0.16D));
        slowArmor.put(Material.DIAMOND_BOOTS, plugin.getConfig().getDouble("diaspeed", 0.16D));
        
        slowPermission.put(Material.IRON_HELMET, "cl.util.armor.iron");
        slowPermission.put(Material.IRON_CHESTPLATE, "cl.util.armor.iron");
        slowPermission.put(Material.IRON_LEGGINGS, "cl.util.armor.iron");
        slowPermission.put(Material.IRON_BOOTS, "cl.util.armor.iron");
        slowPermission.put(Material.DIAMOND_HELMET, "cl.util.armor.dia");
        slowPermission.put(Material.DIAMOND_CHESTPLATE, "cl.util.armor.dia");
        slowPermission.put(Material.DIAMOND_LEGGINGS, "cl.util.armor.dia");
        slowPermission.put(Material.DIAMOND_BOOTS, "cl.util.armor.dia");
    }
    
    @Override
    public float getSpeedChange(Player p, float speed)
    {
        if (!isApplicable(p))
            return 0;
        
        float newSpeed = speed;
        for (ItemStack i : p.getEquipment().getArmorContents())
            if (slowArmor.containsKey(i.getType()) && slowArmor.get(i.getType()).floatValue() < newSpeed)
                newSpeed = slowArmor.get(i.getType()).floatValue();
        
        return newSpeed - speed;
    }
    
    @Override
    public boolean isApplicable(Player p)
    {
        for (ItemStack i : p.getEquipment().getArmorContents())
            if (slowArmor.containsKey(i.getType()))
                if (!p.hasPermission(slowPermission.get(i.getType())))
                    return true;
        
        return false;
    }

    @Override
    public boolean isInstant(Player p)
    {
        return false;
    }
    
}
