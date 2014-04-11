package de.craftlancer.clutil.speed;

import org.bukkit.entity.Player;

import de.craftlancer.clutil.physics.PowerManager;
import de.craftlancer.clutil.physics.WeightManager;
import de.craftlancer.speedapi.SpeedModifier;

public class WeightSpeedModifier extends SpeedModifier
{
    public WeightSpeedModifier(int priority)
    {
        super(priority);
    }
    
    @Override
    public float getSpeedChange(Player p, float speed)
    {
        int weight = WeightManager.getInstance().getWeight(p);
        int power = PowerManager.getInstance().getPower(p);
        
        if (weight <= power)
            return 0;
        
        float speedChange = 0.02f / 24 * (weight - power);
        return speed - speedChange;
    }
    
    @Override
    public boolean isApplicable(Player p)
    {
        return true;
    }
    
}
