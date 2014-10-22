package de.craftlancer.clutil.old;

import java.util.HashSet;
import java.util.Set;



import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;

@SuppressWarnings("unused")
public abstract class MoneyTask extends BukkitRunnable
{
    /*private CraftlancerUtil plugin;
    
    public MoneyTask(CraftlancerUtil plugin)
    {
        this.plugin = plugin;
        
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null)
        {
            RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            //if (economyProvider != null)
            //    economy = economyProvider.getProvider();
        }
        
        long time = 1728000 - (System.currentTimeMillis() / 50) % 1728000;
        
        plugin.getLogger().info(time + " Ticks until the next Money output!");
        
        //new MoneyTask(this).runTaskLater(this, time);
    }
    
    @Override
    public void run()
    {
        Set<String> ex = new HashSet<String>()
        {
            private static final long serialVersionUID = 2586577203292176738L;
            {
                add("SydMontague");
                add("Pashjn");
                add("zwilling89");
            }
        };
        
        Set<String> rewarded = new HashSet<String>();
        
        for (OfflinePlayer p : Bukkit.getServer().getOfflinePlayers().clone())
            if ((p.isOnline() || p.getLastPlayed() + 86400000 > System.currentTimeMillis()) && !ex.contains(p.getName()))
                rewarded.add(p.getName());
        
        double money = plugin.getEconomy().getBalance("Craftlancer") * plugin.getConfig().getDouble("econ.percentoutput", 0.75D);
        
        double moneyperuser = money / rewarded.size();
        
        if (moneyperuser < plugin.getConfig().getDouble("econ.minreward", 1.0))
        {
            money = plugin.getEconomy().getBalance("Craftlancer");
            moneyperuser = money / rewarded.size();
        }
        
        if (moneyperuser > plugin.getConfig().getDouble("econ.maxreward", 7.5))
            moneyperuser = plugin.getConfig().getDouble("econ.maxreward", 7.5);
        
        for (String p : rewarded)
        {
            plugin.getEconomy().depositPlayer(p, moneyperuser);
            plugin.getEconomy().withdrawPlayer("Craftlancer", moneyperuser);
        }
        
        Debug.info(moneyperuser + "$ pro Spieler verteilt, " + money + " insgesamt.");
    }
    */
}
