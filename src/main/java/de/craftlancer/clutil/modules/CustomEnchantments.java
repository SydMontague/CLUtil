package de.craftlancer.clutil.modules;

import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class CustomEnchantments extends Module
{
    private final int EXPLOSION_FACTOR;
    private final int FALL_FACTOR;
    private final int FIRE_FACTOR;
    private final int PROJECTILE_FACTOR;
    private final int DEFAULT_FACTOR;
    
    private final int LEATHER_FACTOR;
    private final int GOLD_FACTOR;
    private final int CHAINMAIL_FACTOR;
    private final int IRON_FACTOR;
    private final int DIAMOND_FACTOR;
    
    private static final double MAGIC_VALUE = 0.036325;
    
    public CustomEnchantments(CLUtil plugin)
    {
        super(plugin);
        EXPLOSION_FACTOR = getConfig().getInt("protection.explosionFactor", 2);
        FALL_FACTOR = getConfig().getInt("protection.fallFactor", 3);
        FIRE_FACTOR = getConfig().getInt("protection.fireFactor", 2);
        PROJECTILE_FACTOR = getConfig().getInt("protection.projectileFactor", 2);
        DEFAULT_FACTOR = getConfig().getInt("protection.defaultFactor", 1);
        LEATHER_FACTOR = getConfig().getInt("protection.leatherFactor", 10);
        GOLD_FACTOR = getConfig().getInt("protection.goldFactor", 5);
        CHAINMAIL_FACTOR = getConfig().getInt("protection.chainmainFactor", 5);
        IRON_FACTOR = getConfig().getInt("protection.ironFactor", 3);
        DIAMOND_FACTOR = getConfig().getInt("protection.diamondFactor", 1);
        
        // TODO Auto-generated constructor stub
    }
    
    @EventHandler
    public void handleArmorEnchants(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        
        int protectionLevel = getProtectionLevel(event.getCause(), entity.getEquipment());
        int factor = getSmallestFactor(entity.getEquipment());
        
        if(factor == Integer.MAX_VALUE && protectionLevel == 0)
            return;
        
        double result = factor * MAGIC_VALUE * Math.log(1.6 * protectionLevel);
        
        event.setDamage(DamageModifier.MAGIC, event.getDamage(DamageModifier.ARMOR) * result);
    }
    
    //TODO get Sharpness damage, divide by 2 and reduce BASE damage
    
    private int getSmallestFactor(EntityEquipment equipment)
    {
        int smallest = Integer.MAX_VALUE;
        for (ItemStack item : equipment.getArmorContents())
            switch (item.getType())
            {
                case LEATHER_BOOTS:
                case LEATHER_CHESTPLATE:
                case LEATHER_HELMET:
                case LEATHER_LEGGINGS:
                    smallest = LEATHER_FACTOR;
                    break;
                
                case GOLD_BOOTS:
                case GOLD_CHESTPLATE:
                case GOLD_HELMET:
                case GOLD_LEGGINGS:
                    smallest = GOLD_FACTOR;
                    break;
                
                case CHAINMAIL_BOOTS:
                case CHAINMAIL_CHESTPLATE:
                case CHAINMAIL_HELMET:
                case CHAINMAIL_LEGGINGS:
                    smallest = CHAINMAIL_FACTOR;
                    break;
                
                case IRON_BOOTS:
                case IRON_CHESTPLATE:
                case IRON_HELMET:
                case IRON_LEGGINGS:
                    smallest = IRON_FACTOR;
                    break;
                
                case DIAMOND_BOOTS:
                case DIAMOND_CHESTPLATE:
                case DIAMOND_HELMET:
                case DIAMOND_LEGGINGS:
                    smallest = DIAMOND_FACTOR;
                    break;
                
                default:
            }
        
        return smallest;
    }
    
    private int getProtectionLevel(DamageCause cause, EntityEquipment equipment)
    {
        int level = 0;
        
        for (ItemStack equip : equipment.getArmorContents())
            for (Entry<Enchantment, Integer> enchant : equip.getEnchantments().entrySet())
                level += getProtectionValue(enchant.getKey(), cause) * enchant.getValue();
        
        return level;
    }
    
    private int getProtectionValue(Enchantment key, DamageCause cause)
    {
        switch (cause)
        {
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                if (key.equals(Enchantment.PROTECTION_EXPLOSIONS))
                    return EXPLOSION_FACTOR;
                break;
            
            case FALL:
                if (key.equals(Enchantment.PROTECTION_FALL))
                    return FALL_FACTOR;
                break;
            
            case FIRE:
            case FIRE_TICK:
            case LAVA:
            case LIGHTNING:
                if (key.equals(Enchantment.PROTECTION_FIRE))
                    return FIRE_FACTOR;
                break;
            
            case PROJECTILE:
                if (key.equals(Enchantment.PROTECTION_PROJECTILE))
                    return PROJECTILE_FACTOR;
                break;
            
            case ENTITY_ATTACK:
            case CONTACT:
            case FALLING_BLOCK:
            case THORNS:
                break;
            
            default:
            case CUSTOM:
            case DROWNING:
            case MAGIC:
            case POISON:
            case WITHER:
            case SUFFOCATION:
            case STARVATION:
            case MELTING:
            case SUICIDE:
            case VOID:
                return 0;
        }
        
        if (key.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
            return DEFAULT_FACTOR;
        
        return 0;
    }

    @Override
    public ModuleType getType()
    {
        return ModuleType.CUSTOMENCHANTS;
    }
}
