package de.craftlancer.clutil.speed;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import de.craftlancer.speedapi.SpeedModifier;

public class SneakSpeedModifier extends SpeedModifier
{
    private final float change;
    private final Map<UUID, Boolean> map = new HashMap<>();
    
    public SneakSpeedModifier(int priority, float change)
    {
        super(priority);
        this.change = change;
    }
    
    @Override
    public float getSpeedChange(Player p, float speed)
    {
        if (!isApplicable(p))
            return 0;
        
        boolean prev = false;
        if (map.containsKey(p.getUniqueId()))
            prev = map.get(p.getUniqueId());
        
        boolean current = p.isSneaking();
        
        if (!prev && !current)
            return 0;
        
        if (prev && current)
            return change;
        
        if (prev && !current)
        {
            p.setWalkSpeed(p.getWalkSpeed() - change);
            return 0;
        }
        
        if (!prev && current)
            return change;
        
        return 0;
    }
    
    @Override
    public boolean isApplicable(Player p)
    {
        return p.hasPermission("cl.util.speed.sneak") && !p.hasMetadata("cl.util.sneak");
    }
    
    @Override
    public boolean isInstant(Player p)
    {
        boolean prev = false;
        if (map.containsKey(p.getUniqueId()))
            prev = map.get(p.getUniqueId());
        boolean current = p.isSneaking();
        
        map.put(p.getUniqueId(), p.isSneaking());
        
        return !prev && current;
    }
    
}
