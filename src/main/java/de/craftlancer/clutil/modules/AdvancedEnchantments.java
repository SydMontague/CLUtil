package de.craftlancer.clutil.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class AdvancedEnchantments extends Module implements Listener
{
    private static final int ITEM_SLOT = 0;
    private static final int BOOK_SLOT = 1;
    private static final int RESULT_SLOT = 2;
    
    private final Map<Enchantment, ValueWrapper> xpMap = new HashMap<Enchantment, ValueWrapper>();
    
    private boolean enableXP2Bottle;
    private boolean enableEnchant2XP;
    private boolean enableEnchant2Books;
    
    /*
     * static
     * {
     * xpMap.put(Enchantment.ARROW_DAMAGE, new ValueWrapper(5));
     * xpMap.put(Enchantment.ARROW_FIRE, new ValueWrapper(50));
     * xpMap.put(Enchantment.ARROW_INFINITE, new ValueWrapper(50));
     * xpMap.put(Enchantment.ARROW_KNOCKBACK, new ValueWrapper(20));
     * xpMap.put(Enchantment.DAMAGE_ALL, new ValueWrapper(5));
     * xpMap.put(Enchantment.DAMAGE_ARTHROPODS, new ValueWrapper(5));
     * xpMap.put(Enchantment.DAMAGE_UNDEAD, new ValueWrapper(5));
     * xpMap.put(Enchantment.DIG_SPEED, new ValueWrapper(5));
     * xpMap.put(Enchantment.DURABILITY, new ValueWrapper(11));
     * xpMap.put(Enchantment.FIRE_ASPECT, new ValueWrapper(10));
     * xpMap.put(Enchantment.KNOCKBACK, new ValueWrapper(5));
     * xpMap.put(Enchantment.LOOT_BONUS_BLOCKS, new ValueWrapper(11));
     * xpMap.put(Enchantment.LOOT_BONUS_MOBS, new ValueWrapper(11));
     * xpMap.put(Enchantment.OXYGEN, new ValueWrapper(10));
     * xpMap.put(Enchantment.PROTECTION_ENVIRONMENTAL, new ValueWrapper(7));
     * xpMap.put(Enchantment.PROTECTION_EXPLOSIONS, new ValueWrapper(7));
     * xpMap.put(Enchantment.PROTECTION_FALL, new ValueWrapper(7));
     * xpMap.put(Enchantment.PROTECTION_FIRE, new ValueWrapper(7));
     * xpMap.put(Enchantment.PROTECTION_PROJECTILE, new ValueWrapper(7));
     * xpMap.put(Enchantment.SILK_TOUCH, new ValueWrapper(30));
     * xpMap.put(Enchantment.THORNS, new ValueWrapper(20));
     * xpMap.put(Enchantment.WATER_WORKER, new ValueWrapper(10));
     * }
     */
    
    public AdvancedEnchantments(CLUtil plugin)
    {
        super(plugin);
        
        if (getConfig().isConfigurationSection("values"))
            for (String key : getConfig().getConfigurationSection("values").getKeys(false))
                if (Enchantment.getByName(key) != null)
                    xpMap.put(Enchantment.getByName(key), new ValueWrapper(getConfig().getInt("values." + key)));
        
        enableEnchant2Books = getConfig().getBoolean("enable.enchanttobooks", true);
        enableEnchant2XP = getConfig().getBoolean("enable.enchanttoxp", true);
        enableXP2Bottle = getConfig().getBoolean("enable.xptobottle", true);
        
        getPlugin().getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAnvilFill(InventoryClickEvent e)
    {
        if (!enableEnchant2Books)
            return;
        
        if (e.getInventory().getType() == InventoryType.ANVIL)
            new AnvilUpdateTask((AnvilInventory) e.getInventory()).runTaskLater(getPlugin(), 1L);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilTake(InventoryClickEvent e)
    {
        if (!enableEnchant2Books)
            return;
        
        if (e.getInventory().getType() == InventoryType.ANVIL && e.getSlotType() == SlotType.RESULT && e.getCursor().getType() == Material.AIR)
        {
            if (e.getInventory().getItem(RESULT_SLOT).getType() == Material.ENCHANTED_BOOK)
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
                e.getInventory().setItem(RESULT_SLOT, result);
                
                e.getWhoClicked().setItemOnCursor(e.getInventory().getItem(RESULT_SLOT));
                e.getInventory().setItem(0, null);
                ItemStack i2 = e.getInventory().getItem(1);
                i2.setAmount(i2.getAmount() - 1);
                e.getInventory().setItem(1, i2);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e)
    {
        if (enableEnchant2XP)
            if (e.getLine(1).equals("[Enchant2XP]") && !e.getPlayer().hasPermission("cl.util.admin"))
                e.setCancelled(true);
        
        if (enableXP2Bottle)
            if (e.getLine(1).equals("[Exp2Bottle]") && !e.getPlayer().hasPermission("cl.util.admin"))
                e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExp2Bottle(PlayerInteractEvent e)
    {
        if (!enableXP2Bottle)
            return;
        
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.hasBlock())
            return;
        
        if (!isSign(e.getClickedBlock()))
            return;
        
        Sign sign = (Sign) e.getClickedBlock().getState();
        
        if (!sign.getLine(1).equals("[Exp2Bottle]"))
            return;
        
        fillXPBottle(e.getPlayer(), 1);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchant2XP(PlayerInteractEvent e)
    {
        if (!enableEnchant2XP)
            return;
        
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
    
    @SuppressWarnings("deprecation")
    public static void fillXPBottle(Player p, int amount)
    {
        int exp = getExp((p.getLevel() + p.getExp()));
        
        if (exp >= amount * 10 && p.getLevel() > 0)
        {
            exp -= (amount * 10);
            
            int level = (int) Math.floor(getLevel(exp));
            float progress = (float) (getLevel(exp) - Math.floor(getLevel(exp)));
            
            p.setLevel(level);
            p.setExp(progress);
            
            for (ItemStack item : p.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 1)).values())
                p.getWorld().dropItem(p.getLocation(), item);
            
            p.updateInventory();
        }
        else
            p.sendMessage("You don't have enough EXP");
    }
    
    // TODO update for 1.8
    private static int getExp(double level)
    {
        int exp = 0;
        
        double nlevel = Math.ceil(level);
        
        if (nlevel - 32 > 0)
            exp += (1 - (nlevel - level)) * (65 + (nlevel - 32) * 7);
        else if (nlevel - 16 > 0)
            exp += (1 - (nlevel - level)) * (17 + (level - 16) * 3);
        else
            exp += (1 - (nlevel - level)) * 17;
        
        level = Math.ceil(level - 1);
        
        while (level > 0)
        {
            if (level - 32 > 0)
                exp += 65 + (level - 32) * 7;
            else if (level - 16 > 0)
                exp += 17 + (level - 16) * 3;
            else
                exp += 17;
            
            level--;
        }
        
        return exp;
    }
    
    // TODO update for 1.8
    private static double getLevel(int exp)
    {
        double i = 0;
        boolean stop = true;
        
        while (stop)
        {
            i++;
            
            if (i - 32 > 0)
            {
                if (exp - (65 + (i - 32) * 7) > 0)
                    exp = (int) (exp - (65 + (i - 32) * 7));
                else
                    stop = false;
            }
            else if (i - 16 > 0)
            {
                if (exp - (17 + (i - 16) * 3) > 0)
                    exp = (int) (exp - (17 + (i - 16) * 3));
                else
                    stop = false;
            }
            else if (exp > 17)
                exp -= 17;
            else
                stop = false;
        }
        
        if (exp != 0)
            if (i - 32 > 0)
                i += exp / (65 + (i - 32) * 7);
            else if (i - 16 > 0)
                i += exp / (17 + (i - 16) * 3);
            else
                i += exp / 17D;
        
        if (exp == 0)
            i--;
        
        i--;
        return i;
    }
    
    private static boolean isSign(Block block)
    {
        Material type = block.getType();
        return type == Material.WALL_SIGN || type == Material.SIGN_POST || type == Material.SIGN;
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
            ItemStack i1 = inventory.getItem(ITEM_SLOT);
            ItemStack i2 = inventory.getItem(BOOK_SLOT);
            
            if (i1 == null || i2 == null || !i1.getItemMeta().hasEnchants() || i2.getType() != Material.BOOK)
                return;
            
            ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
            inventory.setItem(RESULT_SLOT, result);
        }
    }
    
    private static class ValueWrapper
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
        
    @Override
    public ModuleType getType()
    {
        return ModuleType.ADVENCHANTMENTS;
    }
}
