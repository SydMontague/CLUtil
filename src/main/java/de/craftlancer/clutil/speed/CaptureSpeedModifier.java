package de.craftlancer.clutil.speed;

import org.bukkit.entity.Player;

import de.craftlancer.clutil.modules.CaptureTheToken;
import de.craftlancer.speedapi.SpeedModifier;

public class CaptureSpeedModifier extends SpeedModifier
{
    private static final float MOD = 0.5f;
    
    public CaptureSpeedModifier(int priority)
    {
        super(priority);
    }
    
    @Override
    public float getSpeedChange(Player arg0, float arg1)
    {
        if (!isApplicable(arg0))
            return 0;
        
        return arg1 * MOD;
    }
    
    @Override
    public boolean isApplicable(Player arg0)
    {
        return arg0.getInventory().containsAtLeast(CaptureTheToken.TOKENITEM, 1);
    }
    
    @Override
    public boolean isInstant(Player arg0)
    {
        return true;
    }
    
}
