package de.craftlancer.clutil;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadHunter implements Listener
{
    
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
        switch(item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS))
        {
            case 1: chance = 0.025D; break;
            case 2: chance = 0.050D; break;
            case 3: chance = 0.100D; break;
            default: chance = 0; break;
        }

        if(chance > Math.random())
        {
            ItemStack head = new ItemStack(Material.SKULL_ITEM);
            head.setDurability((short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwner(e.getEntity().getName());
            head.setItemMeta(meta);
            e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), head).getItemStack();
        }
        
    }
}
