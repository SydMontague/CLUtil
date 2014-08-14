package de.craftlancer.clutil.modules;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class HeadHunter extends Module implements Listener
{
    public HeadHunter(CLUtil plugin)
    {
        super(plugin);
        getPlugin().getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e)
    {
        if (e.getEntity().getKiller() == null)
            return;
        
        Player p = e.getEntity().getKiller();
        ItemStack item = p.getItemInHand();
        
        if (!item.containsEnchantment(Enchantment.LOOT_BONUS_MOBS) || !p.hasPermission("cl.util.headhunt"))
            return;
        
        double chance = 0;
        switch (item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS))
        {
            case 0:
                chance = 0;
                break;
            case 1:
                chance = 0.025D;
                break;
            case 2:
                chance = 0.050D;
                break;
            default:
            case 3:
                chance = 0.100D;
                break;
        }
        
        if (p.hasPermission("cl.util.headhuntDouble"))
            chance *= 2;
        
        if (chance > Math.random())
        {
            ItemStack head = new ItemStack(Material.SKULL_ITEM);
            head.setDurability((short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwner(e.getEntity().getName());
            head.setItemMeta(meta);
            e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), head).getItemStack();
        }
    }
    
    @Override
    public ModuleType getName()
    {
        return ModuleType.HEADHUNT;
    }
}
