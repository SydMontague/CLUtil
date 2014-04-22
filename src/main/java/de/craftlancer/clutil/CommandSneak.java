package de.craftlancer.clutil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandSneak implements CommandExecutor, Listener
{
    private CLUtil plugin;
    private Set<String> sneak = new HashSet<String>();
    
    private Set<Material> noSneakArmor = new HashSet<Material>()
    {
        private static final long serialVersionUID = -8956211739581295652L;
        {
            add(Material.GOLD_HELMET);
            add(Material.GOLD_CHESTPLATE);
            add(Material.GOLD_LEGGINGS);
            add(Material.GOLD_BOOTS);
            add(Material.IRON_HELMET);
            add(Material.IRON_CHESTPLATE);
            add(Material.IRON_LEGGINGS);
            add(Material.IRON_BOOTS);
            add(Material.DIAMOND_HELMET);
            add(Material.DIAMOND_CHESTPLATE);
            add(Material.DIAMOND_LEGGINGS);
            add(Material.DIAMOND_BOOTS);
        }
    };
    
    public CommandSneak(CLUtil plugin)
    {
        this.plugin = plugin;
        new SneakTask(sneak).runTaskTimer(plugin, 300L, 600L);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (!(sender instanceof Player) || !sender.hasPermission("cl.util.sneak"))
            return false;
        
        Player player = (Player) sender;
        
        if (checkArmor(player))
            setSneak(player, !sneak.contains(player.getName()));
        else
            player.sendMessage("Deine Rüstung ist zu schwer zum schleichen!");
        
        return true;
    }
    
    protected boolean checkArmor(Player player)
    {
        for (ItemStack i : player.getEquipment().getArmorContents())
            if (noSneakArmor.contains(i.getType()))
                return false;
        
        return true;
    }
    
    private void setSneak(Player player, boolean bool)
    {
        player.setSneaking(bool);
        
        if (bool)
        {
            player.sendMessage(ChatColor.GRAY + "Du schleichst.");
            if (!sneak.contains(player.getName()))
                sneak.add(player.getName());
        }
        else
        {
            player.sendMessage(ChatColor.GRAY + "Du schleichst nichtmehr.");
            if (sneak.contains(player.getName()))
                sneak.remove(player.getName());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (sneak.contains(event.getPlayer().getName()))
            sneak.remove(event.getPlayer().getName());
    }
    
    /*
     * @EventHandler(priority = EventPriority.MONITOR)
     * public void onPlayerRespawn(PlayerRespawnEvent event)
     * {
     * if (sneak.contains(event.getPlayer().getName()))
     * sneak.remove(event.getPlayer().getName());
     * }
     */
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event)
    {
        if (sneak.contains(event.getPlayer().getName()))
        {
            event.getPlayer().setSneaking(true);
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDoorInteract(final PlayerInteractEvent e)
    {
        if (sneak.contains(e.getPlayer().getName()))
        {
            e.getPlayer().setSneaking(false);
            
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    e.getPlayer().setSneaking(true);
                }
            }.runTaskLater(plugin, 0L);
        }
    }
    
    class SneakTask extends BukkitRunnable
    {
        private Set<String> sneakSet;
        
        protected SneakTask(Set<String> sneakSet)
        {
            this.sneakSet = sneakSet;
        }
        
        @Override
        public void run()
        {
            Iterator<String> it = sneakSet.iterator();
            while (it.hasNext())
            {
                Player p = Bukkit.getPlayerExact(it.next());
                p.setSneaking(false);
                if (checkArmor(p))
                    p.setSneaking(true);
                else
                {
                    it.remove();// sneakSet.remove(str);
                    p.sendMessage("Deine Rüstung ist zu schwer zum schleichen!");
                }
            }
        }
    }

    public boolean isSneaking(String name)
    {
        return sneak.contains(name);
    }
}
