package de.craftlancer.clutil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R2.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_7_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class AdvancedEnchantments implements Listener
{
    private CLUtil plugin;
    private Map<Enchantment, ValueWrapper> xpMap = new HashMap<Enchantment, ValueWrapper>();
    
    {
        xpMap.put(Enchantment.ARROW_DAMAGE, new ValueWrapper(5));
        xpMap.put(Enchantment.ARROW_FIRE, new ValueWrapper(50));
        xpMap.put(Enchantment.ARROW_INFINITE, new ValueWrapper(50));
        xpMap.put(Enchantment.ARROW_KNOCKBACK, new ValueWrapper(20));
        xpMap.put(Enchantment.DAMAGE_ALL, new ValueWrapper(5));
        xpMap.put(Enchantment.DAMAGE_ARTHROPODS, new ValueWrapper(5));
        xpMap.put(Enchantment.DAMAGE_UNDEAD, new ValueWrapper(5));
        xpMap.put(Enchantment.DIG_SPEED, new ValueWrapper(5));
        xpMap.put(Enchantment.DURABILITY, new ValueWrapper(11));
        xpMap.put(Enchantment.FIRE_ASPECT, new ValueWrapper(10));
        xpMap.put(Enchantment.KNOCKBACK, new ValueWrapper(5));
        xpMap.put(Enchantment.LOOT_BONUS_BLOCKS, new ValueWrapper(11));
        xpMap.put(Enchantment.LOOT_BONUS_MOBS, new ValueWrapper(11));
        xpMap.put(Enchantment.OXYGEN, new ValueWrapper(10));
        xpMap.put(Enchantment.PROTECTION_ENVIRONMENTAL, new ValueWrapper(7));
        xpMap.put(Enchantment.PROTECTION_EXPLOSIONS, new ValueWrapper(7));
        xpMap.put(Enchantment.PROTECTION_FALL, new ValueWrapper(7));
        xpMap.put(Enchantment.PROTECTION_FIRE, new ValueWrapper(7));
        xpMap.put(Enchantment.PROTECTION_PROJECTILE, new ValueWrapper(7));
        xpMap.put(Enchantment.SILK_TOUCH, new ValueWrapper(30));
        xpMap.put(Enchantment.THORNS, new ValueWrapper(20));
        xpMap.put(Enchantment.WATER_WORKER, new ValueWrapper(10));
    }
    
    public AdvancedEnchantments(CLUtil plugin)
    {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void on(InventoryClickEvent e)
    {
        if (e.getInventory().getType() == InventoryType.ANVIL)
            new AnvilUpdateTask((CraftInventoryAnvil) e.getInventory()).runTaskLater(plugin, 1L);
        
    }
    
    @EventHandler
    public void onSignClick(PlayerInteractEvent e)
    {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        if (plugin.expsigns.contains(e.getClickedBlock().getLocation()))
            plugin.fillXPBottle(e.getPlayer(), 1);
        
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e)
    {
        if (e.getLine(1).equals("[Enchant2XP]") && !e.getPlayer().hasPermission("cl.util.admin"))
            e.setCancelled(true);
        
        if (e.getLine(1).equals("[Exp2Bottle]") && !e.getPlayer().hasPermission("cl.util.admin"))
            e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void on2(InventoryClickEvent e)
    {
        if (e.getInventory().getType() == InventoryType.ANVIL && e.getSlotType() == SlotType.RESULT && e.getCursor().getType() == Material.AIR)
        {
            if (CraftItemStack.asCraftMirror(((CraftInventoryAnvil) e.getInventory()).getResultInventory().getItem(0)).getType() == Material.ENCHANTED_BOOK)
            {
                ItemStack i1 = e.getInventory().getItem(0);
                
                if (i1.getEnchantments().size() == 0)
                    return;
                
                ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
                
                int ench = i1.getEnchantments().size() <= 1 ? 0 : new Random().nextInt(i1.getEnchantments().size());
                for (Entry<Enchantment, Integer> a : i1.getEnchantments().entrySet())
                {
                    if (ench != 0)
                    {
                        ench--;
                        continue;
                    }
                    meta.addStoredEnchant(a.getKey(), a.getValue(), true);
                    break;
                }
                result.setItemMeta(meta);
                ((CraftInventoryAnvil) e.getInventory()).getResultInventory().setItem(0, CraftItemStack.asNMSCopy(result));
                
                e.getWhoClicked().setItemOnCursor(CraftItemStack.asCraftMirror(((CraftInventoryAnvil) e.getInventory()).getResultInventory().getItem(0)));
                e.getInventory().setItem(0, null);
                ItemStack i2 = e.getInventory().getItem(1);
                i2.setAmount(i2.getAmount() - 1);
                e.getInventory().setItem(1, i2);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignInteract(PlayerInteractEvent e)
    {
        if (!e.hasBlock() || !e.hasItem() || e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        
        if (e.getClickedBlock().getType() != Material.SIGN && e.getClickedBlock().getType() != Material.SIGN_POST && e.getClickedBlock().getType() != Material.WALL_SIGN)
            return;
        
        Sign sign = (Sign) e.getClickedBlock().getState();
        
        if (!sign.getLine(1).equals("[Enchant2XP]"))
            return;
        
        ItemStack item = e.getPlayer().getItemInHand();
        int value = 0;
        
        for (Entry<Enchantment, Integer> set : item.getEnchantments().entrySet())
            value += xpMap.get(set.getKey()).getValue(set.getValue());
        
        if (item.getType() == Material.ENCHANTED_BOOK)
            for (Entry<Enchantment, Integer> set : ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().entrySet())
                value += xpMap.get(set.getKey()).getValue(set.getValue());
        
        if (value == 0)
            return;
        
        e.getPlayer().giveExp(value);
        e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
    }
}

class AnvilUpdateTask extends BukkitRunnable
{
    private CraftInventoryAnvil inventory;
    
    public AnvilUpdateTask(CraftInventoryAnvil inventory)
    {
        this.inventory = inventory;
    }
    
    @Override
    public void run()
    {
        net.minecraft.server.v1_7_R2.ItemStack nmsi1 = inventory.getIngredientsInventory().getItem(0);
        net.minecraft.server.v1_7_R2.ItemStack nmsi2 = inventory.getIngredientsInventory().getItem(1);
        
        ItemStack i1 = nmsi1 != null ? CraftItemStack.asCraftMirror(nmsi1) : null;
        ItemStack i2 = nmsi2 != null ? CraftItemStack.asCraftMirror(nmsi2) : null;
        
        if (i1 == null || i2 == null || !i1.getItemMeta().hasEnchants() || i2.getType() != Material.BOOK)
            return;
        
        ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        inventory.getResultInventory().setItem(0, CraftItemStack.asNMSCopy(result));
    }
}

class ValueWrapper
{
    private int base;
    
    public ValueWrapper(int base)
    {
        this.base = base;
    }
    
    public int getValue(int level)
    {
        return base * level * level;
    }
}
