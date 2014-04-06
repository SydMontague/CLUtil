package de.craftlancer.clutil.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

//TODO externalize as CLPhysics
/*
 * Features:
 * Gewichtssystem
 * Projektilphysik + Items
 * 
 * 
 */
public class AdvPhysics
{
    private static AdvPhysics instance;
    private List<WeigthFactor> weigthMods = new ArrayList<WeigthFactor>();
    
    private Map<UUID, Integer> weight = new HashMap<UUID, Integer>();
    
    public static AdvPhysics getInstance()
    {
        if (instance == null)
            instance = new AdvPhysics();
        
        return instance;
    }
    
    public int getWeight(Player player)
    {
        return getWeight(player.getUniqueId());
    }
    
    public int getWeight(UUID player)
    {
        return weight.get(player);
    }
    
}
