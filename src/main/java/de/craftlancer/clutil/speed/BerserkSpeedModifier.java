package de.craftlancer.clutil.speed;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import de.craftlancer.speedapi.SpeedModifier;

public class BerserkSpeedModifier extends SpeedModifier implements Listener
{
    private final double maxDmg;
    private final float maxSpeed;
    private final float speedStep;
    private final double dmgStep;
    private final long duration;
    private final HashMap<UUID, BerserkValue> berserkPlayers = new HashMap<UUID, BerserkValue>();
    
    public BerserkSpeedModifier(int priority, float maxSpeed, double maxDmg, int maxCombo, long duration)
    {
        super(priority);
        
        this.maxSpeed = maxSpeed;
        this.maxDmg = maxDmg;
        this.duration = duration;
        
        speedStep = (maxSpeed) / maxCombo;
        dmgStep = (maxDmg - 1) / maxCombo;
    }
    
    @Override
    public float getSpeedChange(Player p, float speed)
    {
        if (!isApplicable(p))
            return 0;
        
        BerserkValue bv = getBerserkValue(p);
        
        float addSpeed = speedStep * bv.getCombo();
        addSpeed = addSpeed > maxSpeed ? maxSpeed : addSpeed;
        
        return speed * addSpeed;
    }
    
    @Override
    public boolean isApplicable(Player p)
    {
        if (!p.hasPermission("cl.util.berserk.speed"))
            return false;
        
        return true;
    }
    
    public BerserkValue getBerserkValue(Player p)
    {
        return getBerserkValue(p.getUniqueId());
    }
    
    public BerserkValue getBerserkValue(UUID p)
    {
        if (!berserkPlayers.containsKey(p))
            berserkPlayers.put(p, new BerserkValue());
        
        return berserkPlayers.get(p);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e)
    {
        if (!e.getDamager().getType().equals(EntityType.PLAYER))
            return;
        
        Player p = (Player) e.getDamager();
        
        if (!p.hasPermission("cl.util.berserk.speed") && !p.hasPermission("cl.util.berserk.damage"))
            return;
        
        BerserkValue bv = getBerserkValue(p);
        bv.incCombo();
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamage2(EntityDamageByEntityEvent e)
    {
        if (!e.getDamager().getType().equals(EntityType.PLAYER))
            return;
        
        Player p = (Player) e.getDamager();
        
        if (!p.hasPermission("cl.util.berserk.damage"))
            return;
        
        double dmgMod = 1 + getBerserkValue(p).getCombo() * dmgStep;
        dmgMod = dmgMod > maxDmg ? maxDmg : dmgMod;
        
        e.setDamage(e.getDamage() * dmgMod);
    }
    
    public long getDuration()
    {
        return duration;
    }
    
    class BerserkValue
    {
        private long time = 0;
        private int count = 0;
        
        public int getCombo()
        {
            if (time + getDuration() < System.currentTimeMillis())
                count = 0;
            
            return count;
        }
        
        public void incCombo()
        {
            if (time + getDuration() < System.currentTimeMillis())
                count = 0;
            
            time = System.currentTimeMillis();
            count++;
        }
        
        public long getLastHit()
        {
            return time;
        }
    }
    
    @Override
    public boolean isInstant(Player p)
    {
        return false;
    }
}
