package de.craftlancer.clutil.modules;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.groups.CLGroups;

public class PumpkinBandit extends Module implements Listener
{
    public PumpkinBandit(CLUtil plugin)
    {
        super(plugin);
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onNameTag(AsyncPlayerReceiveNameTagEvent e)
    {
        ItemStack item = e.getNamedPlayer().getInventory().getHelmet();
        if (item != null && item.getType() == Material.PUMPKIN)
            e.setTag("PumpkinBandit");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e)
    {
        if (e.getEntity().getKiller() == null)
            return;
        
        if (e.getEntity().getKiller().getInventory().getHelmet() != null && e.getEntity().getKiller().getInventory().getHelmet().getType() == Material.PUMPKIN)
            e.setDeathMessage(e.getDeathMessage().replace(e.getEntity().getKiller().getName(), "Pumpkinbandit"));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvClose(final InventoryCloseEvent e)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                HumanEntity p = e.getPlayer();
                if (!(p instanceof Player && ((Player) p).isOnline()))
                    return;
                
                TagAPI.refreshPlayer((Player) e.getPlayer());
            }
        }.runTask(getPlugin());
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncPlayerChatEvent e)
    {
        Player p = e.getPlayer();
        
        if (e.getPlayer().getInventory().getHelmet() == null || e.getPlayer().getInventory().getHelmet().getType() != Material.PUMPKIN)
            return;
        
        e.setFormat(CLGroups.getInstance().getChatManager().getActiveChannel(p).getPlayerFormat("PumpkinBandit"));
    }

    @Override
    public ModuleType getType()
    {
        return ModuleType.PUMPKINBANDIT;
    }
}
