package de.craftlancer.clutil;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CommandStatsMe implements CommandExecutor, Listener
{
    private HashMap<String, PlayerStats> stats = new HashMap<String, PlayerStats>();
    private CLUtil plugin;
    private FileConfiguration config;
    private File file;
    
    public CommandStatsMe(CLUtil plugin)
    {
        this.plugin = plugin;
        file = new File(this.plugin.getDataFolder(), "stats.yml");
        try
        {
            if (!file.exists())
                file.createNewFile();
            
            config = YamlConfiguration.loadConfiguration(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        loadStats();
    }
    
    private void loadStats()
    {
        if (config != null)
            for (String key : config.getKeys(false))
            {
                PlayerStats pstats = new PlayerStats();
                pstats.pvedeath = config.getInt(key + ".pvedeaths", 0);
                pstats.pvpdeath = config.getInt(key + ".pvpdeaths", 0);
                pstats.pvpkills = config.getInt(key + ".pvpkills", 0);
                pstats.streak = 0;
                stats.put(key, pstats);
            }
    }
    
    public void saveStats()
    {
        if (config != null)
        {
            for (Entry<String, PlayerStats> entry : stats.entrySet())
            {
                PlayerStats pstats = entry.getValue();
                String key = entry.getKey();
                config.set(key + ".pvedeaths", pstats.pvedeath);
                config.set(key + ".pvpdeaths", pstats.pvpdeath);
                config.set(key + ".pvpkills", pstats.pvpkills);
            }
            
            try
            {
                config.save(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player) || !stats.containsKey(sender.getName()))
        {
            sender.sendMessage("You need to be a player!");
            return true;
        }
        
        PlayerStats st;
        String name;
        
        if (args.length == 1 && stats.containsKey(args[0]))
        {
            st = stats.get(args[0]);
            name = args[0];
        }
        else
        {
            st = stats.get(sender.getName());
            name = sender.getName();}
        
        DecimalFormat f = new DecimalFormat("#0.00");
        
        sender.sendMessage(name + "'s Stats");
        sender.sendMessage("PvP Kills: " + st.pvpkills);
        sender.sendMessage("PvP Deaths: " + st.pvpdeath);
        sender.sendMessage("PvE Deaths: " + st.pvedeath);
        sender.sendMessage("KDR: " + f.format(((st.pvpdeath + st.pvedeath) != 0) ? (st.pvpkills / ((double) st.pvpdeath + (double) st.pvedeath)) : 0));
        sender.sendMessage("PvP KDR: " + f.format(((st.pvpdeath != 0) ? ((double) st.pvpkills / (double) st.pvpdeath) : 0)));
        
        return true;
    }
    
    @EventHandler
    public void onLogout(PlayerQuitEvent e)
    {
        if (stats.containsKey(e.getPlayer().getName()))
            stats.get(e.getPlayer().getName()).streak = 0;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e)
    {
        PlayerStats pstats = stats.get(e.getEntity().getName());
        PlayerStats killer;
        
        if (pstats != null)
        {
            if (e.getEntity().getKiller() != null)
            {
                pstats.pvpdeath++;
                
                Player p2 = e.getEntity().getKiller();
                killer = stats.get(e.getEntity().getKiller().getName());
                if (killer != null)
                {
                    killer.pvpkills++;
                    killer.streak++;
                    
                    String name = (p2.getInventory().getHelmet() != null && p2.getInventory().getHelmet().getType() == Material.PUMPKIN) ? "PunpkinBandit" : p2.getName();
                        
                    
                    switch (killer.streak)
                    {
                        case 5:
                            plugin.getServer().broadcastMessage(ChatColor.BLUE + name + " is on a rampage!");
                            break;
                        case 10:
                            plugin.getServer().broadcastMessage(ChatColor.BLUE + name + " is unstopable!");
                            break;
                        case 15:
                            plugin.getServer().broadcastMessage(ChatColor.BLUE + name + " is godlike!");
                            break;
                        case 20:
                            plugin.getServer().broadcastMessage(ChatColor.BLUE + name + " is a PvP god!");
                            break;
                        default:
                            break;
                    }
                }
            }
            else
                pstats.pvedeath++;
            
            pstats.streak = 0;
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if (!stats.containsKey(e.getPlayer().getName()))
            stats.put(e.getPlayer().getName(), new PlayerStats());
    }
}

class PlayerStats
{
    int pvpdeath = 0;
    int pvedeath = 0;
    int pvpkills = 0;
    int streak = 0;
}
