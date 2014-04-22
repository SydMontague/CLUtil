package de.craftlancer.clutil.physics;

import org.bukkit.entity.Player;

public class ClassPowerFactor implements PowerFactor
{
    /*
     * Power per Class:
     * Waldläufer: 24
     * Schurke: 24
     * Krieger (lv 0): 24
     * Krieger (lv 1): 48
     * Krieger (lv 2): 72
     */
    
    //TODO create config loader for this stuff
    private static final String POWER1_PERM = "clutil.power.1";
    private static final String POWER2_PERM = "clutil.power.2";
    
    private static final int DEFAULT_POWER = 24;
    private static final int CLASS1_POWER = 48;
    private static final int CLASS2_POWER = 72;
    
    @Override
    public int calculate(Player player)
    {
        if (player.hasPermission(POWER1_PERM))
            return CLASS1_POWER;
        
        if (player.hasPermission(POWER2_PERM))
            return CLASS2_POWER;
                
        return DEFAULT_POWER;
    }
    
}
