package de.craftlancer.clutil.old;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;

public class BerserkSkill implements Listener
{
    protected static float defaultSpeed = 0.2f;
    private double maxDmg;
    private float maxSpeed;
    private int maxCombo;
    private float speedStep;
    private double dmgStep;
    protected HashMap<String, BerserkValue> berserkPlayers = new HashMap<String, BerserkValue>();
    
    public BerserkSkill(CLUtil plugin)
    {
        maxSpeed = (float) plugin.getConfig().getDouble("berserk_speed", 0.3);
        maxDmg = plugin.getConfig().getDouble("berserk_dmg", 2);
        maxCombo = plugin.getConfig().getInt("berserk_combo", 7);
        
        speedStep = (maxSpeed - defaultSpeed) / maxCombo;
        dmgStep = (maxDmg - 1) / maxCombo;
        
        new BerserkTask().runTaskTimer(plugin, 200L, 200L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e)
    {
        if (!e.getDamager().getType().equals(EntityType.PLAYER))
            return;
        
        Player p = (Player) e.getDamager();
        
        if (!p.hasPermission("cl.util.krieger"))
            return;
        
        BerserkValue bv = berserkPlayers.containsKey(p.getName()) ? berserkPlayers.get(p.getName()) : new BerserkValue();
        bv.incCombo();
        
        float speed = defaultSpeed + speedStep * bv.getCombo();
        speed = speed > maxSpeed ? maxSpeed : speed;
        p.setWalkSpeed(speed);
        
        berserkPlayers.put(p.getName(), bv);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamage2(EntityDamageByEntityEvent e)
    {
        if (!e.getDamager().getType().equals(EntityType.PLAYER))
            return;
        
        Player p = (Player) e.getDamager();
        
        if (!p.hasPermission("cl.util.krieger") || !berserkPlayers.containsKey(p.getName()))
            return;
        
        double dmgMod = 1 + berserkPlayers.get(p.getName()).getCombo() * dmgStep;
        dmgMod = dmgMod > maxDmg ? maxDmg : dmgMod;
        
        e.setDamage(e.getDamage() * dmgMod);
    }
    
    public boolean hasBerserker(String player)
    {
        return berserkPlayers.containsKey(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        if (berserkPlayers.containsKey(e.getPlayer().getName()))
            berserkPlayers.remove(e.getPlayer().getName());
    }
    
    class BerserkValue
    {
        private long time = 0;
        private int count = 0;
        
        public int getCombo()
        {
            return count;
        }
        
        public void incCombo()
        {
            time = System.currentTimeMillis();
            count++;
        }
        
        public long getLastHit()
        {
            return time;
        }
    }
    
    class BerserkTask extends BukkitRunnable
    {
        @Override
        public void run()
        {
            for (Entry<String, BerserkValue> pl : new HashSet<Entry<String, BerserkValue>>(berserkPlayers.entrySet()))
            {
                Player p = Bukkit.getPlayerExact(pl.getKey());
                
                if (pl.getValue().getLastHit() + 10000L < System.currentTimeMillis())
                {
                    berserkPlayers.remove(pl.getKey());
                    p.setWalkSpeed(defaultSpeed);
                }
            }
        }
        
    }
}
