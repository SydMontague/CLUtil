package de.craftlancer.clutil.modules;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.core.Utils;

public class Home extends Module implements CommandExecutor, Listener
{
    private Map<UUID, Location> homes = new HashMap<UUID, Location>();
    private Map<UUID, WaitingPlayer> waitingPlayers = new HashMap<UUID, WaitingPlayer>();
    
    private final long homeCooldown;
    private final long teleportTime;
    
    private HomeTask homeTask = new HomeTask();
    
    public Home(CLUtil plugin)
    {
        super(plugin);
        this.homeCooldown = getConfig().getLong("config.homeCooldown", 10800L);
        this.teleportTime = getConfig().getLong("config.teleportTime", 60);
        load();
        
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        getPlugin().getCommand("home").setExecutor(this);
        homeTask.runTaskTimer(getPlugin(), 10L, 10L);
    }
    
    @Override
    public void onDisable()
    {
        save(true);
    }
    
    public void save(boolean shutdown)
    {
        for (Entry<UUID, Location> set : homes.entrySet())
            getConfig().set("homes." + set.getKey().toString(), Utils.getLocationString(set.getValue()));
        
        if (!shutdown)
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    
                    saveConfig();
                }
            }.runTaskAsynchronously(getPlugin());
        else
            saveConfig();
    }
    
    public void load()
    {
        if (!getConfig().isConfigurationSection("homes"))
            return;
        
        ConfigurationSection config = getConfig().getConfigurationSection("homes");
        
        for (String s : config.getKeys(false))
            homes.put(UUID.fromString(s), Utils.parseLocation(config.getString(s)));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedInteract(PlayerInteractEvent e)
    {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.hasBlock() || e.getClickedBlock().getType() != Material.BED_BLOCK)
            return;
        
        Town town1 = null;
        Town town2 = null;
        
        try
        {
            town1 = TownyUniverse.getDataSource().getResident(e.getPlayer().getName()).getTown();
            town2 = TownyUniverse.getTownBlock(e.getClickedBlock().getLocation()).getTown();
        }
        catch (TownyException ex)
        {
        }
        
        if(town2 != null && !town2.equals(town1))
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
    
    protected void cancelHome(Player p)
    {
        waitingPlayers.remove(p.getUniqueId());
        p.sendMessage(ChatColor.RED + "Teleportation abgebrochen!");
    }
    
    private void setHome(Player player, Location location)
    {
        homes.put(player.getUniqueId(), location);
        player.sendMessage(ChatColor.GOLD + "Hompunkt erfolgreich gesetzt!");
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern ausgeführt werden!");
        else if (args.length == 0 && !homes.containsKey(((Player) sender).getUniqueId()))
            sender.sendMessage(ChatColor.RED + "Du hast keine Homepunkt!");
        else if (args.length >= 1 && !sender.hasPermission("cl.util.admin"))
            sender.sendMessage(ChatColor.RED + "Du musst admin sein um diesen Befehl zu nutzen!");
        else if (args.length >= 1 && !homes.containsKey(Bukkit.getOfflinePlayer(args[0]).getUniqueId()))
            sender.sendMessage(ChatColor.RED + "Dieser Spieler hat kein Homepunkt.");
        else if (((Player) sender).hasMetadata("clutil.home.cooldown") && ((Player) sender).getMetadata("clutil.home.cooldown").get(0).asLong() > System.currentTimeMillis())
            sender.sendMessage(ChatColor.RED + "Du kannst diesen Befehl erst in " + Utils.getTimeString(((Player) sender).getMetadata("clutil.home.cooldown").get(0).asLong() - System.currentTimeMillis()) + " wieder benutzen.");
        else
        {
            if (args.length == 0)
            {
                waitingPlayers.put(((Player) sender).getUniqueId(), new WaitingPlayer(System.currentTimeMillis() + getTeleportTime(), ((Player) sender).getLocation()));
                sender.sendMessage(ChatColor.GOLD + "Du wirst in " + teleportTime + " teleportiert.");
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
    
    protected Map<UUID, WaitingPlayer> getWaitingPlayers()
    {
        return waitingPlayers;
    }
    
    public Location getHome(UUID uuid)
    {
        return homes.get(uuid);
    }
    
    protected long getHomeCooldown()
    {
        return homeCooldown * 1000;
    }
    
    class HomeTask extends BukkitRunnable
    {
        private long runTime = 0;
        
        @Override
        public void run()
        {
            List<Player> cancelList = new LinkedList<Player>();
            List<Player> teleportList = new LinkedList<Player>();
            
            for (Entry<UUID, WaitingPlayer> e : getWaitingPlayers().entrySet())
            {
                Player p = getPlugin().getServer().getPlayer(e.getKey());
                
                if (e.getValue().getLocation().distance(p.getLocation()) > 1)
                    cancelList.add(p);
                else if (e.getValue().getTime() <= System.currentTimeMillis())
                    teleportList.add(p);
                else if (runTime % 20 == 0)
                {
                    long time = e.getValue().getTime() - System.currentTimeMillis();
                    p.sendMessage(ChatColor.GOLD + "" + (time / 1000) + "s bis zur teleportation!");
                }
                
            }
            
            for (Player p : cancelList)
                cancelHome(p);
            
            for (Player p : teleportList)
            {
                if (p.isInsideVehicle())
                {
                    if (p.getVehicle().getType() == EntityType.HORSE)
                        p.getVehicle().teleport(getHome(p.getUniqueId()));
                    else
                        p.eject();
                }
                
                p.teleport(getHome(p.getUniqueId()));
                p.setMetadata("clutil.home.cooldown", new FixedMetadataValue(getPlugin(), System.currentTimeMillis() + getHomeCooldown()));
                getWaitingPlayers().remove(p.getUniqueId());
            }
            
            if (runTime % 3600 == 0)
                save(false);
            
            runTime++;
        }
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
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.HOME;
    }
}
