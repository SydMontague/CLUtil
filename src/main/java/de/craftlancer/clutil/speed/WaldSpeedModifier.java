package de.craftlancer.clutil.speed;

import org.bukkit.entity.Player;

import de.craftlancer.speedapi.SpeedModifier;

public class WaldSpeedModifier extends SpeedModifier
{
    private float mod;
    
    public WaldSpeedModifier(int priority, float mod)
    {
        super(priority);
        this.mod = mod;
    }

    @Override
    public float getSpeedChange(Player p, float speed)
    {
        if(!isApplicable(p))
            return 0;
        
        return speed * mod;
    }

    @Override
    public boolean isApplicable(Player p)
    {
        if(p.hasPermission("cl.util.wald.speed"))
            return true;
        
        return false;
    }

    @Override
    public boolean isInstant(Player p)
    {
        return false;
    }
    
}
