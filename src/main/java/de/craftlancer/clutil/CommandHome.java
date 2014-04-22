package de.craftlancer.clutil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.core.Utils;
import de.craftlancer.groups.Plot;
import de.craftlancer.groups.managers.PlotManager;

public class CommandHome extends BukkitRunnable implements CommandExecutor, Listener
{
    private final CLUtil plugin;
    
    private Map<UUID, Location> homes = new HashMap<UUID, Location>();
    private Map<UUID, WaitingPlayer> waitingPlayers = new HashMap<UUID, WaitingPlayer>();
    private long runTime = 0;
    
    private final long homeCooldown;
    private final long teleportTime;
    private FileConfiguration config;
    
    public CommandHome(CLUtil plugin)
    {
        this.plugin = plugin;
        this.homeCooldown = plugin.getConfig().getLong("homeCooldown", 10800L);
        this.teleportTime = plugin.getConfig().getLong("teleportTime", 60);
        this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "homes.yml"));
        load();
    }
    
    public void save()
    {
        for (Entry<UUID, Location> set : homes.entrySet())
            config.set(set.getKey().toString(), CLUtil.getLocationString(set.getValue()));
        
        try
        {
            config.save(new File(plugin.getDataFolder(), "homes.yml"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void load()
    {
        boolean updated = false;
        for (String s : config.getKeys(false))
            try
            {
                homes.put(UUID.fromString(s), CLUtil.parseLocation(config.getString(s)));
            }
            catch (IllegalArgumentException e)
            {
                OfflinePlayer player = Bukkit.getOfflinePlayer(s);
                if (player.hasPlayedBefore())
                {
                    homes.put(player.getUniqueId(), CLUtil.parseLocation(config.getString(s)));
                    updated = true;
                }
            }
        
        if (updated)
        {
            for (String key : config.getKeys(false))
                config.set(key, false);
            
            save();
        }
        
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedInteract(PlayerInteractEvent e)
    {
        if (!e.hasBlock() || e.getClickedBlock().getType() != Material.BED_BLOCK)
            return;
        
        Plot p = PlotManager.getPlot(e.getClickedBlock().getLocation());
        
        if (!p.canBuild(e.getPlayer()))
            return;
        
        setHome(e.getPlayer(), e.getPlayer().getLocation());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
        if (homes.containsKey(e.getPlayer().getUniqueId()))
            e.setRespawnLocation(homes.get(e.getPlayer().getUniqueId()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e)
    {
        if (e.getEntityType() == EntityType.PLAYER && (waitingPlayers.containsKey(((Player) e.getEntity()).getUniqueId())))
            cancelHome((Player) e.getEntity());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent e)
    {
        Player p = null;
        
        if (e.getDamager().getType() == EntityType.PLAYER)
            p = (Player) e.getDamager();
        else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player)
            p = (Player) ((Projectile) e.getDamager()).getShooter();
        
        if (p == null || !waitingPlayers.containsKey(p.getUniqueId()))
            return;
        
        cancelHome(p);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e)
    {
        if (waitingPlayers.containsKey(e.getPlayer().getUniqueId()))
            cancelHome(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e)
    {
        if (waitingPlayers.containsKey(e.getPlayer().getUniqueId()))
            cancelHome(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e)
    {
        if (waitingPlayers.containsKey(e.getPlayer().getUniqueId()))
            cancelHome(e.getPlayer());
    }
    
    private void cancelHome(Player p)
    {
        waitingPlayers.remove(p.getUniqueId());
        p.sendMessage(ChatColor.RED + "Teleportation abgebrochen!");
    }
    
    private void setHome(Player player, Location location)
    {
        homes.put(player.getUniqueId(), location);
        player.sendMessage(ChatColor.GOLD + "Homepunkt erfolgreich gesetzt!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern benutzt werden!");
        else if (args.length == 0 && !homes.containsKey(((Player) sender).getUniqueId()))
            sender.sendMessage(ChatColor.RED + "Du hast keinen Homepunkt gesetzt!");
        else if (args.length >= 1 && !sender.hasPermission("cl.util.admin"))
            sender.sendMessage(ChatColor.RED + "Du musst Admin sein, um diesen Befehl auszuführen!");
        else if (args.length >= 1 && !homes.containsKey(Bukkit.getOfflinePlayer(args[0]).getUniqueId()))
            sender.sendMessage(ChatColor.RED + "Dieser Spieler hat keinen Homepunkt.");
        else if (((Player) sender).hasMetadata("clutil.home.cooldown") && ((Player) sender).getMetadata("clutil.home.cooldown").get(0).asLong() > System.currentTimeMillis())
            sender.sendMessage(ChatColor.RED + "Dieser Befehl muss für weitere " + Utils.getTimeString(((Player) sender).getMetadata("clutil.home.cooldown").get(0).asLong() - System.currentTimeMillis()) + " abkühlen.");
        else
        {
            if (args.length == 0)
            {
                waitingPlayers.put(((Player) sender).getUniqueId(), new WaitingPlayer(System.currentTimeMillis() + getTeleportTime(), ((Player) sender).getLocation()));
                sender.sendMessage(ChatColor.GOLD + "Du wirst in " + teleportTime + " Sekunden teleportiert.");
            }
            else if (args.length >= 1)
                ((Player) sender).teleport(homes.get(Bukkit.getOfflinePlayer(args[0]).getUniqueId()));
        }
        
        return true;
    }
    
    private long getTeleportTime()
    {
        return teleportTime * 1000;
    }
    
    @Override
    public void run()
    {
        List<Player> cancelList = new LinkedList<Player>();
        List<Player> teleportList = new LinkedList<Player>();
        
        for (Entry<UUID, WaitingPlayer> e : waitingPlayers.entrySet())
        {
            Player p = plugin.getServer().getPlayer(e.getKey());
            
            if (e.getValue().getLocation().distance(p.getLocation()) > 1)
                cancelList.add(p);
            else if (e.getValue().getTime() <= System.currentTimeMillis())
                teleportList.add(p);
            else if (runTime % 20 == 0)
            {
                long time = e.getValue().getTime() - System.currentTimeMillis();
                p.sendMessage(ChatColor.GOLD + "Noch " + (time / 1000) + "s bis zum Teleport!");
            }
            
        }
        
        for (Player p : cancelList)
            cancelHome(p);
        
        for (Player p : teleportList)
        {
            p.teleport(homes.get(p.getUniqueId()));
            p.setMetadata("clutil.home.cooldown", new FixedMetadataValue(plugin, System.currentTimeMillis() + getHomeCooldown()));
            waitingPlayers.remove(p.getUniqueId());
        }
        
        if (runTime % 3600 == 0)
            save();
        
        runTime++;
    }
    
    private long getHomeCooldown()
    {
        return homeCooldown * 1000;
    }
    
    class WaitingPlayer
    {
        private long time;
        private Location loc;
        
        public WaitingPlayer(long time, Location loc)
        {
            this.time = time;
            this.loc = loc;
        }
        
        public long getTime()
        {
            return time;
        }
        
        public Location getLocation()
        {
            return loc;
        }
    }
    
}
