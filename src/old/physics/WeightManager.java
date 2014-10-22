package de.craftlancer.clutil.old.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

//TODO externalize as CLPhysics
/*
 * Features:
 * Gewichtssystem
 * Projektilphysik + Items
 * 
 * 
 */
public class WeightManager extends BukkitRunnable
{
    private static WeightManager instance;
    private List<WeigthFactor> weightMods = new ArrayList<WeigthFactor>();
    
    private Map<UUID, Integer> weight = new HashMap<UUID, Integer>();
    
    private WeightManager()
    {
        
    }
    
    public static WeightManager getInstance()
    {
        if (instance == null)
            instance = new WeightManager();
        
        return instance;
    }
    
    public void registerWeightFactor(WeigthFactor factor)
    {
        weightMods.add(factor);
    }
    
    public int getWeight(Player player)
    {
        return getWeight(player.getUniqueId());
    }
    
    public int getWeight(UUID player)
    {
        return weight.get(player);
    }
    
    @Override
    public void run()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            int value = 0;
            for (WeigthFactor factor : weightMods)
                value += factor.calculate(player);
            
            weight.put(player.getUniqueId(), value);
        }
    }
    
}
