package de.craftlancer.clutil.old.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PowerManager extends BukkitRunnable
{
    private static PowerManager instance;
    private List<PowerFactor> powerMods = new ArrayList<PowerFactor>();
    
    private Map<UUID, Integer> power = new HashMap<UUID, Integer>();
    
    public static PowerManager getInstance()
    {
        if (instance == null)
            instance = new PowerManager();
        
        return instance;
    }
    
    public void registerPowerFactor(PowerFactor factor)
    {
        powerMods.add(factor);
    }
    
    public int getPower(Player player)
    {
        return getPower(player.getUniqueId());
    }
    
    public int getPower(UUID player)
    {
        return power.get(player);
    }
    
    @Override
    public void run()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            int value = 0;
            for (PowerFactor factor : powerMods)
                value += factor.calculate(player);
            
            power.put(player.getUniqueId(), value);
        }
    }
    
}
