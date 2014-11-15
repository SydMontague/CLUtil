package de.craftlancer.clutil.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class PumpkinBandit extends Module implements Listener
{
    public PumpkinBandit(CLUtil plugin)
    {
        super(plugin);
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        
        Chat c = (Chat) Bukkit.getPluginManager().getPlugin("TownyChat");
        c.getChannelsHandler().getChannel("general").setHooked(true);
        c.getChannelsHandler().getChannel("local").setHooked(true);
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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onTownyChat(AsyncChatHookEvent event)
    {
        if (event.getChannel().getType() != channelTypes.GLOBAL)
            return;
        
        Player p = event.getPlayer();
        
        if (p.getInventory().getHelmet() == null || p.getInventory().getHelmet().getType() != Material.PUMPKIN)
            return;
        
        event.getAsyncPlayerChatEvent().setFormat(ChatColor.translateAlternateColorCodes('&', event.getChannel().getChannelTag() + "&f<Pumpkinbandit&f>" + event.getChannel().getMessageColour() + " %2$s"));
        event.setChanged(false);
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.PUMPKINBANDIT;
    }
}
