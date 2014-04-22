package de.craftlancer.clutil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
    
    private Map<String, Location> homes = new HashMap<String, Location>();
    private Map<String, WaitingPlayer> waitingPlayers = new HashMap<String, WaitingPlayer>();
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
        for (Entry<String, Location> set : homes.entrySet())
            config.set(set.getKey(), CLUtil.getLocationString(set.getValue()));
        
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
        for (String s : config.getKeys(false))
            homes.put(s, CLUtil.parseLocation(config.getString(s)));
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
        if (homes.containsKey(e.getPlayer().getName()))
            e.setRespawnLocation(homes.get(e.getPlayer().getName()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e)
    {
        if (e.getEntityType() == EntityType.PLAYER && (waitingPlayers.containsKey(((Player) e.getEntity()).getName())))
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
        
        if (p == null || !waitingPlayers.containsKey(p.getName()))
            return;
        
        cancelHome(p);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e)
    {
        if (waitingPlayers.containsKey(e.getPlayer().getName()))
            cancelHome(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e)
    {
        if (waitingPlayers.containsKey(e.getPlayer().getName()))
            cancelHome(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e)
    {
        if (waitingPlayers.containsKey(e.getPlayer().getName()))
            cancelHome(e.getPlayer());
    }
    
    private void cancelHome(Player p)
    {
        waitingPlayers.remove(p.getName());
        p.sendMessage(ChatColor.RED + "Teleportation abgebrochen!");
    }
    
    private void setHome(Player player, Location location)
    {
        homes.put(player.getName(), location);
        player.sendMessage(ChatColor.GOLD + "Homepunkt erfolgreich gesetzt!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern benutzt werden!");
        else if (args.length == 0 && !homes.containsKey(sender.getName()))
            sender.sendMessage(ChatColor.RED + "Du hast keinen Homepunkt gesetzt!");
        else if (args.length >= 1 && !sender.hasPermission("cl.util.admin"))
            sender.sendMessage(ChatColor.RED + "Du musst Admin sein, um diesen Befehl auszuführen!");
        else if (args.length >= 1 && !homes.containsKey(args[0]))
            sender.sendMessage(ChatColor.RED + "Dieser Spieler hat keinen Homepunkt.");
        else if (((Player) sender).hasMetadata("clutil.home.cooldown") && ((Player) sender).getMetadata("clutil.home.cooldown").get(0).asLong() > System.currentTimeMillis())
            sender.sendMessage(ChatColor.RED + "Dieser Befehl muss für weitere " + Utils.getTimeString(((Player) sender).getMetadata("clutil.home.cooldown").get(0).asLong() - System.currentTimeMillis()) + " abkühlen.");
        else
        {
            if (args.length == 0)
            {
                waitingPlayers.put(sender.getName(), new WaitingPlayer(System.currentTimeMillis() + getTeleportTime(), ((Player) sender).getLocation()));
                sender.sendMessage(ChatColor.GOLD + "Du wirst in " + teleportTime + " Sekunden teleportiert.");
            }
            else if (args.length >= 1)
                ((Player) sender).teleport(homes.get(args[0]));
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
        
        for (Entry<String, WaitingPlayer> e : waitingPlayers.entrySet())
        {
            Player p = plugin.getServer().getPlayerExact(e.getKey());
            
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
            p.teleport(homes.get(p.getName()));
            p.setMetadata("clutil.home.cooldown", new FixedMetadataValue(plugin, System.currentTimeMillis() + getHomeCooldown()));
            waitingPlayers.remove(p.getName());
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
