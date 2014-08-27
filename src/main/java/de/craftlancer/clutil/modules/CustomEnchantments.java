package de.craftlancer.clutil.modules;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.clutil.modules.token.EnchantmentToken;
import de.craftlancer.clutil.modules.token.Token;
import de.craftlancer.clutil.modules.token.TokenFactory;

/*
 * To handle enchantments:
 * 
 * Enchantment              status effect   max level   status level handling
 * Protection               done            10
 * Fire Protection          done            10
 * Feather Falling          done            10
 * Blast Protection         done            10
 * Projectile Protection    done            10
 * 
 * Respiration              no need?        3           vanilla
 * Aqua Affinity            not scalable    1           vanilla
 * Thorns                   done            10
 * (Depth Strider)          no need?        3           vanilla
 * 
 * Sharpness                done            10
 * Smite                    done            10
 * Bane of Arthropots       done            10
 * 
 * Knockback                no need         2           vanilla
 * Fire Aspect              no need         2           vanilla
 * Looting                  no need         3           vanilla 
 * 
 * Efficiency               no need         10
 * Silk Touch               not scalable    1           vanilla
 * Unbreaking               no need         10
 * Fortune                                  10
 * 
 * Power                    done            10
 * Punch                    no need         2           vanilla
 * Flame                    not scalable    1           vanilla
 * Infinity                 not scalable    1           vanilla
 * 
 * Luck of the Sea          no need         3           vanilla
 * Lure                     no need         3           vanilla
 * 
 * 
 */
public class CustomEnchantments extends Module implements Listener
{
    private static final int WEAPON_SLOT = 0;
    private static final int TOKEN_SLOT = 1;
    private static final int RESULT_SLOT = 2;
    // magic values
    private static final double MAGIC_VALUE = 0.036325;
    private static final double BANE_DAMAGE = 2.5;
    private static final double SMITE_DAMAGE = 2.5;
    private static final double SHARP_DAMAGE = 1.25;
    
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
    
    private final List<String> enchants;
    
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
        CHAINMAIL_FACTOR = getConfig().getInt("protection.chainmailFactor", 5);
        IRON_FACTOR = getConfig().getInt("protection.ironFactor", 3);
        DIAMOND_FACTOR = getConfig().getInt("protection.diamondFactor", 1);
        
        enchants = getConfig().getStringList("extract.enchants");
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }
    
    // handle crafting
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryInteract(InventoryClickEvent event)
    {
        if (event.getInventory().getType() == InventoryType.ANVIL)
            new AnvilUpdateTask((AnvilInventory) event.getInventory()).runTaskLater(getPlugin(), 1L);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClickHighest(InventoryClickEvent e)
    {
        if (!(e.getInventory().getType() == InventoryType.ANVIL && e.getSlotType() == SlotType.RESULT && e.getCursor().getType() == Material.AIR))
            return;
        
        if (!isToken(e.getInventory().getItem(TOKEN_SLOT)))
            return;
        
        e.getWhoClicked().setItemOnCursor(e.getInventory().getItem(RESULT_SLOT));
        e.getInventory().setItem(WEAPON_SLOT, null);
        e.getInventory().setItem(TOKEN_SLOT, null);
        e.getInventory().setItem(RESULT_SLOT, null);
    }
    
    private class AnvilUpdateTask extends BukkitRunnable
    {
        private AnvilInventory inventory;
        
        public AnvilUpdateTask(AnvilInventory inventory)
        {
            this.inventory = inventory;
        }
        
        @Override
        public void run()
        {
            ItemStack i1 = inventory.getItem(WEAPON_SLOT);
            ItemStack i2 = inventory.getItem(TOKEN_SLOT);
            
            if (i1 == null || i2 == null)
                return;
            
            if (!isToken(i2))
                return;
            
            Token token = TokenFactory.getToken(i2);
            
            if (!isApplicable(i1, token))
                return;
            
            inventory.setItem(RESULT_SLOT, getResultItem(i1, token));
        }
        
        private ItemStack getResultItem(ItemStack i1, Token token)
        {
            switch (token.getType())
            {
                case ENCHANTMENT:
                {
                    Enchantment ench = ((EnchantmentToken) token).getEnchantment();
                    ItemStack item = i1.clone();
                    int level = item.getEnchantmentLevel(ench);
                    item.addUnsafeEnchantment(ench, level + 1);
                    return item;
                }
                case UNDEFINED:
                {
                    for (Enchantment entry : i1.getEnchantments().keySet())
                        if (canExtract(entry))
                            return TokenFactory.craftEnchantmentTokenItem(entry);
                    return null;
                }
                default:
                    return null;
            }
        }
        
        private boolean isApplicable(ItemStack i1, Token token)
        {
            switch (token.getType())
            {
                case ENCHANTMENT:
                    return i1.getEnchantmentLevel(((EnchantmentToken) token).getEnchantment()) < 10 && ((EnchantmentToken) token).getEnchantment().canEnchantItem(i1);
                case UNDEFINED:
                    for (Enchantment entry : i1.getEnchantments().keySet())
                        if (canExtract(entry))
                            return true;
                    return false;
                default:
                    return false;
            }
        }
        
    }
    
    protected boolean isToken(ItemStack i2)
    {
        return TokenFactory.isToken(i2);
    }
    
    public boolean canExtract(Enchantment entry)
    {
        return enchants.contains(entry.getName());
    }
    
    @EventHandler
    public void handleArmorEnchants(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        
        int protectionLevel = getProtectionLevel(event.getCause(), entity.getEquipment());
        int factor = getSmallestFactor(entity.getEquipment());
        
        if (factor == Integer.MAX_VALUE || protectionLevel == 0)
            return;
        
        double result = factor * MAGIC_VALUE * Math.log(1.6 * protectionLevel);
        
        event.setDamage(DamageModifier.MAGIC, event.getDamage(DamageModifier.ARMOR) * result);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof LivingEntity))
            return;
        
        LivingEntity damager = (LivingEntity) event.getDamager();
        ItemStack item = damager.getEquipment().getItemInHand();
        
        if (item == null)
            return;
        
        int sharp = item.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
        int bane = item.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
        int smite = item.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD);
        
        double extraDamage = 0;
        switch (event.getEntityType())
        {
            case CAVE_SPIDER:
            case SPIDER:
            case SILVERFISH:
                extraDamage += bane * BANE_DAMAGE;
                break;
            case SKELETON:
            case ZOMBIE:
            case WITHER:
            case PIG_ZOMBIE:
                extraDamage += smite * SMITE_DAMAGE;
                break;
            default:
                break;
        }
        
        extraDamage += sharp * SHARP_DAMAGE;
        
        event.setDamage(DamageModifier.BASE, event.getDamage(DamageModifier.BASE) - extraDamage / 2);
    }
    
    @EventHandler
    public void onBowShoot(EntityShootBowEvent event)
    {
        event.getProjectile().setMetadata("bow", new FixedMetadataValue(getPlugin(), event.getBow()));
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onArrow(EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity().hasMetadata("bow")))
            return;
        
        if (!(event.getDamager() instanceof Arrow))
            return;
        
        ItemStack bow = (ItemStack) event.getEntity().getMetadata("bow").get(0).value();
        
        if (!bow.containsEnchantment(Enchantment.ARROW_DAMAGE))
            return;
        
        int level = bow.getEnchantmentLevel(Enchantment.ARROW_DAMAGE);
        
        double base = event.getDamage(DamageModifier.BASE) / (1 + 0.25 * (level + 1));
        event.setDamage(DamageModifier.BASE, base * (1 + 0.15 * (level + 1)));
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onThornsDamage(EntityDamageByEntityEvent event)
    {
        if (event.getCause() != DamageCause.THORNS)
            return;
        
        int item = getHighestThorns(((LivingEntity) event.getDamager()).getEquipment());
        
        if (Math.random() > getThornsCancelChance(item))
            event.setCancelled(true);
    }
    
    private static double getThornsCancelChance(int item)
    {
        switch (item)
        {
            case 0:
                return 1;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return 0.5;
            case 7:
                return 0.475;
            case 8:
                return 0.4;
            case 9:
                return 0.325;
            case 10:
                return 0.25;
            default:
                return 0;
        }
    }
    
    private static int getHighestThorns(EntityEquipment equipment)
    {
        int level = 0;
        for (ItemStack i : equipment.getArmorContents())
            if (i.containsEnchantment(Enchantment.THORNS) && i.getEnchantmentLevel(Enchantment.THORNS) > level)
                level = i.getEnchantmentLevel(Enchantment.THORNS);
        
        return level;
    }
    
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
